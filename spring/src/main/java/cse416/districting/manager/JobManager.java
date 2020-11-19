package cse416.districting.manager;

import cse416.districting.Enums.JobStatus;
import cse416.districting.dto.GenericResponse;
import cse416.districting.dto.JobInfo;
import cse416.districting.model.Job;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;

public class JobManager {

    private Map<Integer, Job> jobs;

    private SimpMessagingTemplate simpMessagingTemplate;

    public JobManager(SimpMessagingTemplate simpMessagingTemplate) {
        jobs = new HashMap<Integer, Job>();
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Async("threadPoolTaskExecutor")
    public void createJob(JobInfo jobInfo, int IDCounter) {
        System.out.println(jobInfo.toString());
        Job job = new Job(jobInfo, IDCounter);
        jobs.put(IDCounter, job);
        if (jobInfo.isLocal()) runLocalProcess(job);
        else runSeawulfProcess(job);
    }

    private void runLocalProcess(Job job){
        String stateName = job.getJobInfo().getState().toString();
        ProcessBuilder processBuilder = new ProcessBuilder("py", "spring/src/main/resources/script/testscript.py", stateName);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            
            GenericResponse res = new GenericResponse();
            res.setJobStatus(JobStatus.RUNNING);
            res.setID(job.getJobID());
            simpMessagingTemplate.convertAndSend("/jobStatus", res);

            job.setProcess(process);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())); 
            String s = reader.readLine();
            System.out.println("Script output:");
            System.out.println(s);
            //do something with result
            //------------------------
            res.setJobStatus(JobStatus.DONE);
            simpMessagingTemplate.convertAndSend("/jobStatus", res);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void runSeawulfProcess(Job job){
        //implement later
    }

    public boolean cancelJob(int ID){
        if (!jobs.containsKey(ID)) return false;
        jobs.get(ID).cancel();
        return true;
    }

    public boolean deleteJob(int ID){
        if (!jobs.containsKey(ID)) return false;
        jobs.remove(ID);
        return true;
    }

    public JobStatus jobStatus(int ID){
        if (!jobs.containsKey(ID)) return JobStatus.ERROR;
        if (jobs.get(ID).checkAlive()) return JobStatus.RUNNING;
        return JobStatus.DONE;
    }
}
