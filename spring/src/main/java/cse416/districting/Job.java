package cse416.districting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Job {
    private int jobID;
    private JobInfo jobInfo;

    public Job(JobInfo info, int ID){
        jobInfo = info;
        jobID = ID;
    }

    public boolean cancel(){
        return true;
    }
}
