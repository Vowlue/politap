package cse416.districting;

import java.util.HashMap;

public class JobManager {
    
    private HashMap<Integer,Job> jobs;
    private int IDCounter;

    public JobManager(){
        jobs = new HashMap<Integer,Job>();
        IDCounter = 1;
    }

    public int createJob(JobInfo jobInfo){
        Job job = new Job(jobInfo, IDCounter);
        jobs.put(IDCounter,job);
        return IDCounter++;
    }

    public boolean cancelJob(int ID){
        if (jobs.get(ID).cancel()){
            jobs.remove(ID);
            return true;
        }
        return false;
    }

    public boolean deleteJob(int ID){
        jobs.remove(ID);
        return true;
    }
}
