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

    public List<ArrayList<Double>> getBoxPlot(int jobID) {
        double[][] arr = {{0.13926792,0.14449945,0.15733826,0.16049133,0.17465732},{0.20542927,0.18136427,0.19075516,0.16801538,0.18189588},{0.2256838,0.2452773,0.23182505,0.2656849,0.22966945},{0.2774849,0.2813917,0.26852936,0.26782882,0.28987065},{0.31758156,0.28438917,0.30510363,0.2917112,0.29532367},{0.3216292,0.33497465,0.33846822,0.31257874,0.2999543},{0.37569448,0.3972359,0.38130325,0.39152887,0.39263552}};
        double[][] arr2 = {{0.13652785,0.122192845,0.16945335,0.1440406,0.12940986,0.1641327,0.14981052,0.1564407,0.13756612,0.1529187,0.16150926,0.13747619,0.15681672,0.11844777,0.13978693,0.13511744,0.14135791,0.14788423,0.14797004,0.15918148,0.1602973,0.17296551,0.13368846,0.15420993,0.1282574,0.14440395,0.13643111,0.15249501,0.16835184,0.13144924,0.17057618,0.13645783,0.16809,0.17456941,0.15167159,0.1722322,0.1784231,0.15390864,0.14287283,0.16141187,0.15607029,0.18750292,0.11740515,0.15523598,0.17252076,0.12664472,0.14701508,0.16676463,0.17991439,0.14761408},{0.20524359,0.23056582,0.23854186,0.20599179,0.2075178,0.18071204,0.22171777,0.18231185,0.19711472,0.17113203,0.23248024,0.2064856,0.21809652,0.23620738,0.19952033,0.21198232,0.20494919,0.21175183,0.21717596,0.18487215,0.19204478,0.17684947,0.21916327,0.19948533,0.22633602,0.16720395,0.19545785,0.20115857,0.20551045,0.21170418,0.18761075,0.21236172,0.1838503,0.18582171,0.18653837,0.18884711,0.18837306,0.18886222,0.2037599,0.17186241,0.1942307,0.18854554,0.22655328,0.16967429,0.18940042,0.2107765,0.19219074,0.20309855,0.21225257,0.20814021},{0.22892918,0.2648875,0.2751481,0.23589297,0.23517247,0.20179833,0.27459767,0.23782736,0.24343921,0.22890271,0.2580029,0.24208654,0.24404073,0.27021122,0.25610808,0.24793065,0.20919928,0.23224056,0.26145118,0.27365598,0.22845459,0.22647965,0.24870528,0.24334167,0.2319599,0.2748425,0.21911825,0.24256293,0.21681266,0.25019315,0.26730737,0.25570726,0.24507985,0.24386306,0.22295183,0.19754918,0.20526429,0.21513061,0.21937934,0.22876954,0.281665,0.19933869,0.24848889,0.25492465,0.21464856,0.22455707,0.21963109,0.24930201,0.23372819,0.26671094},{0.26484367,0.28319782,0.279152,0.23628643,0.24834082,0.27938786,0.27705434,0.3161886,0.2807812,0.25931397,0.27174264,0.25363284,0.25668636,0.27348658,0.25736153,0.29169396,0.24353895,0.2565834,0.27453148,0.27867374,0.27818108,0.23314454,0.2956513,0.2543584,0.23750658,0.2902956,0.29986814,0.2479714,0.2654427,0.28007442,0.28687987,0.27184716,0.2891287,0.26179942,0.26469478,0.23122233,0.21572982,0.23824485,0.27762753,0.23204742,0.2922899,0.22415878,0.28880823,0.26911017,0.25755474,0.25248954,0.27294135,0.25137216,0.24315678,0.2711763},{0.31568146,0.29390258,0.28593832,0.27677396,0.3064567,0.30201873,0.27772394,0.31786188,0.28433535,0.26996034,0.28411108,0.25860998,0.28741664,0.28521016,0.30514887,0.30460364,0.29244307,0.2940199,0.27530324,0.29980028,0.28709716,0.25849196,0.30597445,0.26663992,0.28070277,0.29200143,0.30771643,0.29964966,0.30998424,0.28342634,0.29073715,0.3311343,0.30063084,0.2752141,0.33380795,0.33021426,0.3114178,0.26625985,0.3170861,0.301972,0.29359385,0.32917652,0.31172347,0.2910796,0.2719901,0.286623,0.3181361,0.2978893,0.27735463,0.27232698},{0.31649196,0.32098335,0.29535267,0.3425204,0.31466988,0.32909584,0.3308606,0.3236612,0.30158937,0.37240815,0.3291256,0.34671184,0.29239982,0.32411087,0.3470565,0.34830436,0.35749093,0.3092734,0.3146464,0.30542865,0.33090776,0.37763372,0.3204061,0.35789156,0.3581937,0.3132187,0.33384237,0.31880996,0.341044,0.32480857,0.33092776,0.33525488,0.33439514,0.31072348,0.33759144,0.3585804,0.37480778,0.37625915,0.33854157,0.3456955,0.3102485,0.33198428,0.336484,0.3078203,0.31032297,0.35843676,0.3447203,0.3372084,0.32524708,0.30275324},{0.37197033,0.36341387,0.33837703,0.42128462,0.4262504,0.4155096,0.34542382,0.33462128,0.3953655,0.4120409,0.33414868,0.44665623,0.41321006,0.35823843,0.36818832,0.34887195,0.40882596,0.40400267,0.38362494,0.38001913,0.3965876,0.42563012,0.3470155,0.3911088,0.40778792,0.3870919,0.3949294,0.44059774,0.3461253,0.38132238,0.3352917,0.33901227,0.34267333,0.4066864,0.3541973,0.39035335,0.38918227,0.43796825,0.38730666,0.40227702,0.34748667,0.4091115,0.3379697,0.41487366,0.44256052,0.39365178,0.3726393,0.37304616,0.396421,0.40361664}};
        
        double[][] pick = arr;
        if (jobID == 8) pick = arr;
        if (jobID == 9) pick = arr2;
        List<ArrayList<Double>> bigd = new ArrayList<ArrayList<Double>>();
        for (double[] d : pick){
            ArrayList<Double> list= new ArrayList<Double>();
            for (double dd : d){
                list.add(dd);
            }
            bigd.add(list);
        }
        return bigd;
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
