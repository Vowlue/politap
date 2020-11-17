package cse416.districting;

import java.io.IOException;
import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Component
public class Job {
    private int jobID;
    private JobInfo jobInfo;
    private Process process;

    public Job(JobInfo info, int ID) {
        jobInfo = info;
        jobID = ID;
    }

    public Future<Boolean> start() {
        
        return null;
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
