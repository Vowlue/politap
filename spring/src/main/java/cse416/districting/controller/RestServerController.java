package cse416.districting.controller;

import org.springframework.web.bind.annotation.RestController;

import cse416.districting.Enums.Demographic;
import cse416.districting.Enums.JobStatus;
import cse416.districting.Enums.States;
import cse416.districting.dto.*;
import cse416.districting.manager.*;

import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class RestServerController {
	
	@Autowired
	private JobManager jobManager;

	@Autowired
	private JobResultsManager jobResultsManager;

	@Autowired
	private MapManager mapManager;

	private States currentState;

	@PostMapping(value="/getStateData", consumes = "application/json")
	public JSONObject getStateData(@RequestBody GenericRequest req) {
		currentState = req.getState();
		return mapManager.getDefaultStateInfo(currentState);
	}

	@PostMapping(value="/setState", consumes = "application/json")
	public void setState(@RequestBody GenericRequest req) {
		currentState = req.getState();
	}

	@RequestMapping("/getHeatMap")
	public Map<String,Map<Demographic,Integer>> getHeatMap(){
		return mapManager.getStateHeatMap(currentState);
	}

	@RequestMapping("/getHeatMapVap")
	public Map<String,Map<Demographic,Integer>> getHeatMapVap(){
		return mapManager.getStateHeatMapVap(currentState);
	}

	@PostMapping(value="/initiateJob", consumes = "application/json")
	public GenericResponse initiateJob(@RequestBody JobInfo jobInfo) {
		GenericResponse res = new GenericResponse();
		int id = jobManager.getIdCounter();
		jobManager.createJob(jobInfo);
		res.setID(id);
		return res;
	}

	@PostMapping(value="/cancelJob", consumes = "application/json")
	public GenericResponse cancelJob(@RequestBody GenericRequest req) {
		GenericResponse res = new GenericResponse();
		res.setSuccess(jobManager.cancelJob(req.getID()));	
		return res;
	}

	@PostMapping(value="/deleteJob", consumes = "application/json")
	public GenericResponse deleteJob(@RequestBody GenericRequest req) {
		GenericResponse res = new GenericResponse();
		res.setSuccess(jobManager.deleteJob(req.getID()));
		return res;
	}

	@PostMapping(value="/checkJob", consumes = "application/json")
	public GenericResponse checkJob(@RequestBody GenericRequest req) {
		GenericResponse res = new GenericResponse();
		JobStatus status = jobManager.jobStatus(req.getID());
		res.setJobStatus(status);
		return res;
	}

	@PostMapping(value="/getDistrictings", consumes = "application/json")
	public Map<String,JSONObject> getDistrictings(@RequestBody GenericRequest req) {
		return jobResultsManager.getDistrictingFiles(req.getID());
	}
}