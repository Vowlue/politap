package cse416.districting.controller;

import org.springframework.web.bind.annotation.RestController;

import cse416.districting.Enums.JobStatus;
import cse416.districting.Enums.States;
import cse416.districting.dto.*;
import cse416.districting.manager.*;

import org.springframework.web.bind.annotation.RequestMapping;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
public class RestServerController {
	
	@Autowired
	private JobManager jobManager;

	@Autowired
	private JobResultsManager jobResultsManager;

	@Autowired
	private StateManager stateManager;

	private States currentState;
	private int IDCounter = 1;

	@PostMapping(value="/getStateData", consumes = "application/json")
	public JSONObject getStateData(@RequestBody GenericRequest req) {
		currentState = req.getState();
		return stateManager.getDefaultStateInfo(req.getState());
	}

	@RequestMapping("/getHeatMap")
	public JSONObject getHeatMap(){
		return stateManager.getStateHeatMap(currentState);
	}

	@PostMapping(value="/initiateJob", consumes = "application/json")
	public GenericResponse initiateJob(@RequestBody JobInfo jobInfo) {
		jobManager.createJob(jobInfo, IDCounter);
		GenericResponse res = new GenericResponse();
		res.setID(IDCounter);
		IDCounter++;
		return res;
	}

	@PostMapping(value="/cancelJob", consumes = "application/json")
	public GenericResponse cancelJob(@RequestBody GenericRequest req) {
		GenericResponse res = new GenericResponse();
		if (!jobManager.cancelJob(req.getID())){
			res.setError(true);
			res.setErrorMessage("ID not found");
			return res;
		}
		res.setError(false);
		return res;
	}

	@PostMapping(value="/deleteJob", consumes = "application/json")
	public GenericResponse deleteJob(@RequestBody GenericRequest req) {
		GenericResponse res = new GenericResponse();
		if (!jobManager.deleteJob(req.getID())){
			res.setError(true);
			res.setErrorMessage("ID not found");
			return res;
		}
		res.setError(false);
		return res;
	}

	@PostMapping(value="/checkJob", consumes = "application/json")
	public GenericResponse checkJob(@RequestBody GenericRequest req) {
		GenericResponse res = new GenericResponse();
		JobStatus status = jobManager.jobStatus(req.getID());
		if (status == JobStatus.ERROR){
			res.setError(true);
			res.setErrorMessage("ID not found");
			return res;
		}
		res.setJobStatus(status);
		return res;
	}
}