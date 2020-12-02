package cse416.districting.manager;

import cse416.districting.Enums.JobStatus;
import cse416.districting.dto.GenericResponse;
import cse416.districting.dto.JobInfo;
import cse416.districting.model.Job;
import cse416.districting.model.JobInfoModel;
import cse416.districting.repository.JobInfoRepository;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;

@Setter
@Getter
public class JobManager {

    private Map<Integer, Job> jobs;
    private int idCounter = 1;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private JobInfoRepository jobInfoRepository;

    @PostConstruct
    public void init(){
        jobInfoRepository.deleteAll();
    }

    @Async("threadPoolTaskExecutor")
    public void createJob(JobInfo jobInfo) {
        System.out.println(jobInfo.toString());
        Job job = new Job(jobInfo, idCounter);
        jobs.put(idCounter, job);
        jobInfoRepository.save(new JobInfoModel(idCounter,jobInfo));
        idCounter++;
        if (jobInfo.isLocal()) {
            runLocalProcess(job);
        } else {
            runSeawulfProcess(job);
        }
    }

    private void runLocalProcess(Job job) {
        String stateName = job.getJobInfo().getState().toString();
        int jobID = job.getJobID();
        ProcessBuilder processBuilder = new ProcessBuilder("py", "spring/src/main/resources/script/testscript.py",
                stateName, Integer.toString(jobID));
        processBuilder.redirectErrorStream(true);
        try {
            Thread.sleep(3000);
            Process process = processBuilder.start();
            sendMessage(JobStatus.RUNNING, jobID);


            job.setProcess(process);
            System.out.println(process);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())); 
            String output = reader.readLine();
            System.out.println("Script output:");
            System.out.println(output);

            if (output == null) return;
            job.setFilename(output);
            sendMessage(JobStatus.DONE, jobID);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void runSeawulfProcess(Job job){
        //implement later
    }

    private void sendMessage(JobStatus jobStatus, int jobID){
        GenericResponse res = new GenericResponse();
        res.setJobStatus(jobStatus);
        res.setID(jobID);
        simpMessagingTemplate.convertAndSend("/jobStatus", res);
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

    public String getDistrictingFilename(int ID){
        return jobs.get(ID).getFilename();
    }
}
