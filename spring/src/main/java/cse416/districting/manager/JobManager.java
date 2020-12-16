package cse416.districting.manager;

import cse416.districting.Enums.Compactness;
import cse416.districting.Enums.Demographic;
import cse416.districting.Enums.JobStatus;
import cse416.districting.Enums.States;
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
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Setter
@Getter
public class JobManager {

    private Map<Integer, Job> jobs;
    private int id = -1;

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
        // districtPrecinctRepository.deleteAll();
        // jobInfoRepository.deleteAll();
        // districtRepository.deleteAll();
        // districtingRepository.deleteAll();
        // jobResultsRepository.deleteAll();
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
        String stateName = job.getJobInfo().getState().name();
        int jobID = job.getJobID();
        int plans = job.getJobInfo().getPlans();
        float variance = job.getJobInfo().getPopulationVariance();
        String compactness = job.getJobInfo().getCompactness().toString();
        ProcessBuilder processBuilder = new ProcessBuilder("py", "algorithm/main.py", Integer.toString(plans),
                stateName, Float.toString(variance), compactness, Integer.toString(jobID));
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            updateStatus(JobStatus.RUNNING, jobID);

            job.setProcess(process);
            System.out.println(process);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            while (output != null){
                System.out.println(output);
                output = reader.readLine();
            }
            if (output == null)
                return;
            System.out.println(output);
            Resource resource = new ClassPathResource(output);
            while (!resource.exists()) {
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
            createSummaryFile(jobID);
            updateStatus(JobStatus.DONE, jobID);
        } catch (InterruptedException | IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void runSeawulfProcess(Job job) {
        String stateName = job.getJobInfo().getState().name();
        int jobID = job.getJobID();
        int plans = job.getJobInfo().getPlans();
        float variance = job.getJobInfo().getPopulationVariance();
        String compactness = job.getJobInfo().getCompactness().toString();
        ProcessBuilder processBuilder = new ProcessBuilder().command("cmd", "sh", "seawulf/jobsubmit.sh",
                Integer.toString(jobID), stateName, Float.toString(variance), compactness, Integer.toString(plans));
        processBuilder.redirectErrorStream(true);
        try {
            processBuilder.start();
            updateStatus(JobStatus.RUNNING, jobID);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private JobResults serverProcessing(JSONArray districtingObj, Job job) {
        Demographic[] demo = job.getJobInfo().getDemographic();
        ArrayList<ArrayList<Float>> orderedDistrictsList = new ArrayList<ArrayList<Float>>();
        int numDistricts = ((JSONArray) districtingObj.get(0)).size();
        int numDistrictings = districtingObj.size();
        ArrayList<Float> average = new ArrayList<Float>(Collections.nCopies(numDistricts, Float.valueOf(0)));

        for (Object districting : districtingObj) {
            ArrayList<Float> populations = new ArrayList<Float>();
            for (Object district : (JSONArray) districting) {
                float districtTotal = 0;
                float totalVAP = 0;
                for (Object precinctGeoid : (JSONArray) district) {
                    Precinct precinct = precinctRepository.findOneByGeoid((String) precinctGeoid);
                    HashMap<Demographic, Integer> map = new HashMap<Demographic, Integer>(
                            precinct.getPopulationDataVAP());
                    int precinctTotal = 0;
                    for (Demographic d : demo)
                        precinctTotal += map.get(d);
                    totalVAP += map.get(Demographic.TOTAL);
                    districtTotal += precinctTotal;
                }
                populations.add(districtTotal / totalVAP);
                System.out.println(populations);
            }
            Collections.sort(populations);
            for (int i = 0; i < populations.size(); i++) {
                average.set(i, average.get(i) + populations.get(i));
            }
            orderedDistrictsList.add(populations);
        }

        for (int i = 0; i < average.size(); i++) {
            average.set(i, average.get(i) / numDistrictings);
        }
        ArrayList<Float> absoluteDifferenceList = new ArrayList<Float>();
        ArrayList<ArrayList<Float>> boxPlotData = new ArrayList<ArrayList<Float>>();
        for (int i = 0; i < numDistricts; i++)
            boxPlotData.add(new ArrayList<Float>());
        for (ArrayList<Float> a : orderedDistrictsList) {
            Float absoluteDifference = Float.valueOf(0);
            for (int i = 0; i < average.size(); i++) {
                absoluteDifference += Math.abs(average.get(i) - a.get(i));
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
        while (random1 == minPos || random1 == maxPos)
            random1 = random.nextInt(absoluteDifferenceList.size());
        int random2 = random.nextInt(absoluteDifferenceList.size());
        while (random2 == minPos || random2 == maxPos || random2 == random1)
            random2 = random.nextInt(absoluteDifferenceList.size());
        JobResults jobResults = new JobResults();
        System.out.println(random1);
        System.out.println(random2);
        jobResults.setAverageIndex(minPos);
        jobResults.setExtremeIndex(maxPos);
        jobResults.setRandom1Index(random1);
        jobResults.setRandom2Index(random2);
        job.setPlot(boxPlotData);
        return jobResults;
    }

    private void updateToDatabase(JSONArray districtings, JobResults jobResults, Job job) {
        jobResultsRepository.save(jobResults);
        updateDistrictings((JSONArray) districtings.get(jobResults.getAverageIndex()), jobResults, 1, job);
        updateDistrictings((JSONArray) districtings.get(jobResults.getExtremeIndex()), jobResults, 2, job);
        updateDistrictings((JSONArray) districtings.get(jobResults.getRandom1Index()), jobResults, 3, job);
        updateDistrictings((JSONArray) districtings.get(jobResults.getRandom2Index()), jobResults, 4, job);
        jobResultsRepository.save(jobResults);
    }

    private void updateDistrictings(JSONArray districting, JobResults jobResults, int type, Job job) {
        Districting districtingObj = new Districting();
        if (type == 1)
            jobResults.setAverage(districtingRepository.save(districtingObj).getId());
        if (type == 2)
            jobResults.setExtreme(districtingRepository.save(districtingObj).getId());
        if (type == 3)
            jobResults.setRandom1(districtingRepository.save(districtingObj).getId());
        if (type == 4)
            jobResults.setRandom2(districtingRepository.save(districtingObj).getId());
        for (int j = 0; j < districting.size(); j++) {
            updateDistricts((JSONArray) districting.get(j), districtingObj, job);
        }
    }

    private void updateDistricts(JSONArray district, Districting districtingObj, Job job) {
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
        for (int k = 0; k < district.size(); k++) {
            Precinct precinct = precinctRepository.findOneByGeoid((String) district.get(k));
            population += precinct.getTotal();
            vap += precinct.getTotal_vap();
            long precinctminority = 0;
            long precinctminorityvap = 0;
            for (Demographic d : demo) {
                precinctminority += precinct.getPopulationData().get(d);
                minority += precinct.getPopulationData().get(d);
                precinctminorityvap += precinct.getPopulationDataVAP().get(d);
                minorityvap += precinct.getPopulationDataVAP().get(d);
            }
            counties.add(precinct.getCounty());
            DistrictPrecinct districtPrecinct = new DistrictPrecinct(precinct.getGeoid(), districtID);
            districtPrecinct.setMinority(precinctminority);
            districtPrecinct.setMinorityvap(precinctminorityvap);
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

    private void makeMaps(int jobID) {
        ProcessBuilder processBuilder = new ProcessBuilder("py", "spring/src/main/resources/script/merge.py",
                Integer.toString(jobID));
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void createSummaryFile(int jobID) {
        ProcessBuilder processBuilder = new ProcessBuilder("py", "spring/src/main/resources/script/generatesummary.py",
                Integer.toString(jobID));
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void updateStatus(JobStatus jobStatus, int jobID) {
        JobInfoModel jobInfo = jobInfoRepository.findById(jobID).get();
        jobInfo.setStatus(jobStatus.toString());
        jobInfoRepository.save(jobInfo);
    }

    public boolean cancelJob(int ID) {
        if (jobs.containsKey(ID)) {
            if (!jobs.get(ID).getJobInfo().isLocal())
                cancelJobSeawulf(ID);
            else
                jobs.get(ID).cancel();
        }
        JobInfoModel jobinfoModel = jobInfoRepository.findById(ID).get();
        jobInfoRepository.delete(jobinfoModel);
        return true;
    }

    public void cancelJobSeawulf(int jobID) {
        ProcessBuilder processBuilder = new ProcessBuilder().command("cmd", "sh", "seawulf/jobcancel.sh",
                Integer.toString(jobID));
        processBuilder.redirectErrorStream(true);
        try {
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteJob(int ID) {
        return cancelJob(ID);
        // JobInfoModel jobInfoModel = jobInfoRepository.findById(ID).get();
        // if (!jobInfoModel.getStatus().equals("DONE")){
        // jobInfoRepository.delete(jobInfoModel);
        // return true;
        // }
        // JobResults jobResult = jobResultsRepository.findOneByJobID(ID);
        // List<Integer> arr = new ArrayList<>();
        // arr.add(jobResult.getAverage());
        // arr.add(jobResult.getExtreme());
        // arr.add(jobResult.getRandom1());
        // arr.add(jobResult.getRandom2());
        // for (Integer a : arr){
        // Districting districting = districtingRepository.findById(a).get();
        // for (District d : districting.getDistricts()){
        // districtPrecinctRepository.deleteByIdDistrictid(d.getId());
        // districtRepository.delete(d);
        // }
        // districtingRepository.delete(districting);
        // }
        // jobResultsRepository.delete(jobResult);
        // jobInfoRepository.delete(jobInfoModel);
        // jobs.remove(ID);
        // return true;
    }

    public JobStatus jobStatus(int ID) {
        if (!jobs.containsKey(ID))
            return JobStatus.ERROR;
        if (jobs.get(ID).checkAlive())
            return JobStatus.RUNNING;
        return JobStatus.DONE;
    }

    public List<ArrayList<Float>> getBoxPlot(int jobID) {
        return jobs.get(jobID).getPlot();
    }

    @Async("threadPoolTaskExecutor")
    public void loadPlans() {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setCompactness(Compactness.VERY);
        jobInfo.setPopulationVariance(Float.valueOf("0.1"));
        jobInfo.setLocal(true);
        jobInfo.setPlans(10);
        jobInfo.setState(States.ARKANSAS);
        Demographic[] demo = new Demographic[1];
        demo[0] = Demographic.BLACK_OR_AFRICAN_AMERICAN;
        jobInfo.setDemographic(demo);
        JobInfoModel jobInfoModel = new JobInfoModel(jobInfo);
        int jobID = jobInfoRepository.save(jobInfoModel).getId();
        Job job = new Job(jobInfo, jobID);
        jobs.put(jobID, job);
        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("seawulf\\testdata\\plan2\\plan0.json");
            FileReader reader2 = new FileReader("seawulf\\testdata\\plan2\\plan1.json");
            JSONObject object = (JSONObject)jsonParser.parse(reader);
            JSONObject object2 = (JSONObject)jsonParser.parse(reader2);
            JSONArray districting1 = (JSONArray) object.get("plans");
            JSONArray districting2 = (JSONArray) object2.get("plans");
            districting1.addAll(districting2);
            JobResults jobResults = serverProcessing(districting1, job);
            jobResults.setJobID(jobID);
            updateToDatabase(districting1, jobResults, job);
            makeMaps(jobID);
            createSummaryFile(jobID);
            updateStatus(JobStatus.DONE, jobID);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Async("threadPoolTaskExecutor")
    public void loadPlans2() {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setCompactness(Compactness.VERY);
        jobInfo.setPopulationVariance(Float.valueOf("0.1"));
        jobInfo.setLocal(false);
        jobInfo.setPlans(50);
        jobInfo.setState(States.SOUTH_CAROLINA);
        Demographic[] demo = new Demographic[1];
        demo[0] = Demographic.BLACK_OR_AFRICAN_AMERICAN;
        jobInfo.setDemographic(demo);
        JobInfoModel jobInfoModel = new JobInfoModel(jobInfo);
        int jobID = jobInfoRepository.save(jobInfoModel).getId();
        Job job = new Job(jobInfo, jobID);
        jobs.put(jobID, job);
        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("seawulf\\testdata\\plan5\\plan0.json");
            FileReader reader2 = new FileReader("seawulf\\testdata\\plan5\\plan1.json");
            JSONObject object = (JSONObject)jsonParser.parse(reader);
            JSONObject object2 = (JSONObject)jsonParser.parse(reader2);
            JSONArray districting1 = (JSONArray) object.get("plans");
            JSONArray districting2 = (JSONArray) object2.get("plans");
            districting1.addAll(districting2);
            JobResults jobResults = serverProcessing(districting1, job);
            jobResults.setJobID(jobID);
            updateToDatabase(districting1, jobResults, job);
            makeMaps(jobID);
            createSummaryFile(jobID);
            updateStatus(JobStatus.DONE, jobID);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Async("threadPoolTaskExecutor")
    public void loadPlans3() {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setCompactness(Compactness.VERY);
        jobInfo.setPopulationVariance(Float.valueOf("0.1"));
        jobInfo.setLocal(false);
        jobInfo.setPlans(50);
        jobInfo.setState(States.VIRGINIA);
        Demographic[] demo = new Demographic[1];
        demo[0] = Demographic.BLACK_OR_AFRICAN_AMERICAN;
        jobInfo.setDemographic(demo);
        JobInfoModel jobInfoModel = new JobInfoModel(jobInfo);
        int jobID = jobInfoRepository.save(jobInfoModel).getId();
        Job job = new Job(jobInfo, jobID);
        jobs.put(jobID, job);
        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("seawulf\\testdata\\plan6\\plan0.json");
            FileReader reader2 = new FileReader("seawulf\\testdata\\plan6\\plan1.json");
            JSONObject object = (JSONObject)jsonParser.parse(reader);
            JSONObject object2 = (JSONObject)jsonParser.parse(reader2);
            JSONArray districting1 = (JSONArray) object.get("plans");
            JSONArray districting2 = (JSONArray) object2.get("plans");
            districting1.addAll(districting2);
            JobResults jobResults = serverProcessing(districting1, job);
            jobResults.setJobID(jobID);
            updateToDatabase(districting1, jobResults, job);
            makeMaps(jobID);
            createSummaryFile(jobID);
            updateStatus(JobStatus.DONE, jobID);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
