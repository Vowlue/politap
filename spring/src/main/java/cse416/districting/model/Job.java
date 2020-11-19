package cse416.districting.model;

import cse416.districting.dto.JobInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Job {
    private int jobID;
    private JobInfo jobInfo;
    private Process process;
    private JobResults jobResults;

    public Job(JobInfo info, int ID) {
        jobInfo = info;
        jobID = ID;
    }
    
    public void cancel(){
        if(process.isAlive()){
            process.destroy();
        }
    }

    public boolean checkAlive(){
        return process.isAlive();
    }
}
