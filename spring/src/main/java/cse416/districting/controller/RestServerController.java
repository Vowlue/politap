package cse416.districting.controller;

import org.springframework.web.bind.annotation.RestController;

import cse416.districting.Enums.JobStatus;
import cse416.districting.Enums.States;
import cse416.districting.dto.*;
import cse416.districting.manager.*;

import org.springframework.web.bind.annotation.RequestMapping;
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
	private StateManager stateManager;

	private States currentState;
	private int IDCounter = 1;

	@PostMapping(value="/getStateData", consumes = "application/json")
	public GenericResponse getStateData(@RequestBody GenericRequest req) {
		currentState = req.getState();
		GenericResponse res = new GenericResponse();
		res.setJsonObject(stateManager.getDefaultStateInfo(req.getState()));
		return res;
	}

	@RequestMapping("/getHeatMap")
	public GenericResponse getHeatMap(){
		GenericResponse res = new GenericResponse();
		res.setJsonObject(stateManager.getStateHeatMap(currentState));
		return res;
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

	@PostMapping(value="/getDistrictingByJobID", consumes = "application/json")
	public GenericResponse getDistrictingByJobID(@RequestBody GenericRequest req) {
		GenericResponse res = new GenericResponse();
		String filename = jobManager.getDistrictingFile(req.getID());
		if (filename == null){
			res.setError(true);
			res.setErrorMessage("ID not found");
			return res;
		}
		res.setJsonObject(stateManager.getDistrictingFile(filename));
		return res;
	}
}