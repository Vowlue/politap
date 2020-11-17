package cse416.districting;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;

import cse416.districting.Enums.JobStatus;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class JobManager {

    private HashMap<Integer, Job> jobs;
    private Socket socket;

    public JobManager() {
        jobs = new HashMap<Integer, Job>();
        try {
            System.out.println("connecting to socket");
            this.socket = IO.socket("http://localhost:3000");
            socket.connect();
        } catch (URISyntaxException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

    }

    @Async("threadPoolTaskExecutor")
    public void createJob(JobInfo jobInfo, int IDCounter) {
        Job job = new Job(jobInfo, IDCounter);
        jobs.put(IDCounter, job);
        ProcessBuilder processBuilder = new ProcessBuilder("py", "spring/src/main/java/cse416/districting/script/testscript.py");
        processBuilder.redirectErrorStream(true);
        try {
            //call script locally
            Process process = processBuilder.start();
            job.setProcess(process);
            process.waitFor();

            //do something with result
            //------------------------

            //tell react that job is done
            JSONObject obj = new JSONObject();
            obj.put("jobID", IDCounter);
            socket.emit("jobFinished", obj);
        } catch (Exception e) {
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
