package cse416.districting.controller;

import org.springframework.web.bind.annotation.RestController;

import cse416.districting.Enums.JobStatus;
import cse416.districting.Enums.States;
import cse416.districting.dto.*;
import cse416.districting.manager.*;

import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

import javax.print.attribute.standard.JobState;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class RestServerController {
	
	@Autowired
	private JobManager jobManager;

	@Autowired
	private StateManager stateManager;

	private States currentState;

	@PostMapping(value="/getStateData", consumes = "application/json")
	public JSONObject getStateData(@RequestBody GenericRequest req) {
		currentState = req.getState();
		return stateManager.getDefaultStateInfo(currentState);
	}

	//@RequestMapping("/getHeatMap")
	//public GenericResponse getHeatMap(){
	//	GenericResponse res = new GenericResponse();
	//	res.setJsonObject(stateManager.getStateHeatMap(currentState));
	//	return res;
	//}

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
		String filename = jobManager.getDistrictingFilename(req.getID());
		Map<String,JSONObject> map = new HashMap<>();
		map.put("random", stateManager.getDistrictingFile(filename));
		map.put("random2", stateManager.getDistrictingFile(filename+"-2"));
		return map;
	}
}