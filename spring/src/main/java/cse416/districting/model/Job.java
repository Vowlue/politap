package cse416.districting.model;

import cse416.districting.dto.JobInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Job {
    private int jobID;
    private JobInfo jobInfo;
    private Process process;
    private String filename;

    public Job(JobInfo info, int ID) {
        jobInfo = info;
        jobID = ID;
    }
    
    public void cancel(){
        if(process.isAlive()){
            System.out.println(Integer.toString(jobID));
            System.out.println("deleted");
            process.destroyForcibly();
        }
    }

    public boolean checkAlive(){
        return process.isAlive();
    }
}
