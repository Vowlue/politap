package cse416.districting;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.scheduling.annotation.Async;

import cse416.districting.Enums.JobStatus;

public class JobManager {
    
    private HashMap<Integer,Job> jobs;

    public JobManager(){
        jobs = new HashMap<Integer,Job>();
    }

    @Async("threadPoolTaskExecutor")
    public void createJob(JobInfo jobInfo, int IDCounter){
        Job job = new Job(jobInfo, IDCounter);
        jobs.put(IDCounter,job);
        ProcessBuilder processBuilder = new ProcessBuilder("py", "spring/src/main/java/cse416/districting/script/testscript.py");
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            job.setProcess(process);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
