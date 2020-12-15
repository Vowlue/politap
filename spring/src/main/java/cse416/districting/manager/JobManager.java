package cse416.districting.manager;

import cse416.districting.Enums.Compactness;
import cse416.districting.Enums.Demographic;
import cse416.districting.Enums.JobStatus;
import cse416.districting.dto.GenericResponse;
import cse416.districting.dto.JobInfo;
import cse416.districting.model.District;
import cse416.districting.model.DistrictPrecinct;
import cse416.districting.model.Districting;
import cse416.districting.model.Job;
import cse416.districting.model.JobInfoModel;
import cse416.districting.model.JobResults;
import cse416.districting.model.Precinct;
import cse416.districting.repository.DistrictPrecinctRepository;
import cse416.districting.repository.DistrictRepository;
import cse416.districting.repository.DistrictingRepository;
import cse416.districting.repository.JobInfoRepository;
import cse416.districting.repository.JobResultsRepository;
import cse416.districting.repository.PrecinctRepository;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Setter
@Getter
public class JobManager {

    private Map<Integer, Job> jobs;
    private int id = -1;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private JobInfoRepository jobInfoRepository;

    @Autowired
    private PrecinctRepository precinctRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private DistrictingRepository districtingRepository;

    @Autowired
    private DistrictPrecinctRepository districtPrecinctRepository;

    @Autowired
    private JobResultsRepository jobResultsRepository;

    @PostConstruct
    public void init() {
        //districtPrecinctRepository.deleteAll();
        //jobInfoRepository.deleteAll();
        //districtRepository.deleteAll();
        //districtingRepository.deleteAll();
        //jobResultsRepository.deleteAll();
    }

    @Async("threadPoolTaskExecutor")
    public void createJob(JobInfo jobInfo) {
        JobInfoModel jobInfoModel = new JobInfoModel(jobInfo);
        int jobID = jobInfoRepository.save(jobInfoModel).getId();
        this.id = jobID;
        System.out.println(jobInfo.toString());
        Job job = new Job(jobInfo, jobID);
        jobs.put(jobID, job);
        if (jobInfo.isLocal()) {
            runLocalProcess(job);
        } else {
            runSeawulfProcess(job);
        }
    }

    private void runLocalProcess(Job job) {
        String stateName = job.getJobInfo().getState().toString();
        int jobID = job.getJobID();
        int plans = job.getJobInfo().getPlans();
        ProcessBuilder processBuilder = new ProcessBuilder("py", "spring/src/main/resources/script/testscript.py",
                stateName, Integer.toString(plans), Integer.toString(jobID));
        processBuilder.redirectErrorStream(true);
        try {
            Resource scriptResource = new ClassPathResource("script\\testscript.py");
            while (!scriptResource.exists()){
                Thread.sleep(1000);
                System.out.println("where's the script?");
                scriptResource = new ClassPathResource("script\\testscript.py");
            }
            Process process = processBuilder.start();
            sendMessage(JobStatus.RUNNING, jobID);

            job.setProcess(process);
            System.out.println(process);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            if (output == null)
                return;
            System.out.println(output);
            Resource resource = new ClassPathResource(output);
            while (!resource.exists()){
                Thread.sleep(1000);
                resource = new ClassPathResource(output);
            }
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(new InputStreamReader(resource.getInputStream()));
            JSONArray districtings = (JSONArray) json.get("plans");
            JobResults jobResults = serverProcessing(districtings, job);
            jobResults.setJobID(jobID);
            updateToDatabase(districtings, jobResults, job);
            makeMaps(jobID);
            //createSummaryFile(jobID);
            sendMessage(JobStatus.DONE, jobID);
        } catch (InterruptedException | IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private JobResults serverProcessing(JSONArray districtingObj, Job job){
        Demographic[] demo = job.getJobInfo().getDemographic();
        ArrayList<ArrayList<Float>> orderedDistrictsList = new ArrayList<ArrayList<Float>>();
        int numDistricts = ((JSONArray)districtingObj.get(0)).size();
        int numDistrictings = districtingObj.size();
        ArrayList<Float> average = new ArrayList<Float>(Collections.nCopies(numDistricts,Float.valueOf(0)));

        for (Object districting : districtingObj){
            ArrayList<Float> populations = new ArrayList<Float>();
            for (Object district : (JSONArray)districting){
                float districtTotal = 0;
                float totalVAP = 0;
                for (Object precinctGeoid : (JSONArray)district){
                    Precinct precinct = precinctRepository.findOneByGeoid((String)precinctGeoid);
                    HashMap<Demographic,Integer> map = new HashMap<Demographic,Integer>(precinct.getPopulationDataVAP());
                    int precinctTotal = 0;
                    for (Demographic d : demo) precinctTotal += map.get(d);
                    totalVAP += map.get(Demographic.TOTAL);
                    districtTotal += precinctTotal;
                }
                populations.add(districtTotal / totalVAP);
                System.out.println(populations);
            }
            Collections.sort(populations);
            for (int i = 0; i < populations.size(); i++){
                average.set(i, average.get(i) + populations.get(i));
            }
            orderedDistrictsList.add(populations);
        }

        for (int i = 0; i < average.size(); i++){
            average.set(i, average.get(i) / numDistrictings);
        }
        ArrayList<Float> absoluteDifferenceList = new ArrayList<Float>();
        ArrayList<ArrayList<Float>> boxPlotData = new ArrayList<ArrayList<Float>>();
        for (int i = 0; i < numDistricts; i++) boxPlotData.add(new ArrayList<Float>());
        for (ArrayList<Float> a : orderedDistrictsList){
            Float absoluteDifference = Float.valueOf(0);
            for (int i = 0; i < average.size(); i++){
                absoluteDifference += Math.abs(average.get(i)-a.get(i));
                boxPlotData.get(i).add(a.get(i));
            }
            absoluteDifferenceList.add(absoluteDifference);
        }
        Float min = Collections.min(absoluteDifferenceList);
        Float max = Collections.max(absoluteDifferenceList);
        int minPos = absoluteDifferenceList.indexOf(min);
        int maxPos = absoluteDifferenceList.indexOf(max);
        System.out.println(minPos);
        System.out.println(maxPos);
        Random random = new Random();
        int random1 = random.nextInt(absoluteDifferenceList.size());
        while (random1 == minPos || random1 == maxPos) random1 = random.nextInt(absoluteDifferenceList.size());
        int random2 = random.nextInt(absoluteDifferenceList.size());
        while (random2 == minPos || random2 == maxPos || random2 == random1) random2 = random.nextInt(absoluteDifferenceList.size());
        JobResults jobResults = new JobResults();
        System.out.println(random1);
        System.out.println(random2);
        jobResults.setAverageIndex(minPos);
        jobResults.setExtremeIndex(maxPos);
        jobResults.setRandom1Index(random1);
        jobResults.setRandom2Index(random2);
        jobResults.setPlot(boxPlotData);
        return jobResults;
    }

    private void updateToDatabase(JSONArray districtings, JobResults jobResults, Job job){
        jobResultsRepository.save(jobResults);
        updateDistrictings((JSONArray)districtings.get(jobResults.getAverageIndex()),jobResults,1,job);
        updateDistrictings((JSONArray)districtings.get(jobResults.getExtremeIndex()),jobResults,2,job);
        updateDistrictings((JSONArray)districtings.get(jobResults.getRandom1Index()),jobResults,3,job);
        updateDistrictings((JSONArray)districtings.get(jobResults.getRandom2Index()),jobResults,4,job);
        jobResultsRepository.save(jobResults);
    }

    private void updateDistrictings(JSONArray districting, JobResults jobResults, int type, Job job){
        Districting districtingObj = new Districting();
        if (type == 1) jobResults.setAverage(districtingRepository.save(districtingObj).getId());
        if (type == 2) jobResults.setExtreme(districtingRepository.save(districtingObj).getId());
        if (type == 3) jobResults.setRandom1(districtingRepository.save(districtingObj).getId());
        if (type == 4) jobResults.setRandom2(districtingRepository.save(districtingObj).getId());
        for (int j = 0; j < districting.size(); j++){
            updateDistricts((JSONArray)districting.get(j),districtingObj, job);
        }
    }

    private void updateDistricts(JSONArray district, Districting districtingObj, Job job){
        District districtObj = new District();
        districtObj.setDistricting(districtingObj);
        int districtID = districtRepository.save(districtObj).getId();
        List<DistrictPrecinct> arr = new ArrayList<DistrictPrecinct>();
        Set<String> counties = new HashSet<String>();
        long population = 0;
        long vap = 0;
        long minority = 0;
        long minorityvap = 0;
        Demographic[] demo = job.getJobInfo().getDemographic();
        for (int k = 0; k < district.size(); k++){
            Precinct precinct = precinctRepository.findOneByGeoid((String)district.get(k));
            population += precinct.getTotal();
            vap += precinct.getTotal_vap();
            for(Demographic d : demo){
                minority += precinct.getPopulationData().get(d);
                minorityvap += precinct.getPopulationDataVAP().get(d);
            }
            counties.add(precinct.getCounty());
            DistrictPrecinct districtPrecinct = new DistrictPrecinct(precinct.getGeoid(),districtID);
            arr.add(districtPrecinct);
        }
        districtObj.setCounties(counties.size());
        districtObj.setMinority(minority);
        districtObj.setMinorityvap(minorityvap);
        districtObj.setPopulation(population);
        districtObj.setVap(vap);
        districtRepository.save(districtObj);
        districtPrecinctRepository.saveAll(arr);
    }

    private void makeMaps(int jobID){
        ProcessBuilder processBuilder = new ProcessBuilder("py", "spring/src/main/resources/script/merge.py", Integer.toString(jobID));
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void createSummaryFile(int jobID) {
        ProcessBuilder processBuilder = new ProcessBuilder("py", "spring/src/main/resources/script/generatesummary.py", Integer.toString(jobID));
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void runSeawulfProcess(Job job){
        //implement later
    }

    private void sendMessage(JobStatus jobStatus, int jobID){
        JobInfoModel jobInfo = jobInfoRepository.findById(jobID).get();
        GenericResponse res = new GenericResponse();
        res.setJobStatus(jobStatus);
        res.setID(jobID);
        simpMessagingTemplate.convertAndSend("/jobStatus", res);
        jobInfo.setStatus(jobStatus.toString());
        jobInfoRepository.save(jobInfo);
    }

    public boolean cancelJob(int ID){
        jobs.get(ID).cancel();
        return true;
    }

    public boolean deleteJob(int ID){
        jobs.remove(ID);
        return true;
    }

    public JobStatus jobStatus(int ID){
        if (!jobs.containsKey(ID)) return JobStatus.ERROR;
        if (jobs.get(ID).checkAlive()) return JobStatus.RUNNING;
        return JobStatus.DONE;
    }

    @Async("threadPoolTaskExecutor")
    public void loadPlans() {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setCompactness(Compactness.VERY);
        //jobInfo.setPopulationVariance(0.1);
    }
}
