package cse416.districting;

import org.springframework.web.bind.annotation.RestController;

import cse416.districting.Enums.States;

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

	@RequestMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	private States currentState;

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
	public int initiateJob(@RequestBody JobInfo jobInfo) {
		return jobManager.createJob(jobInfo);
	}

	@PostMapping(value="/cancelJob", consumes = "application/json")
	public boolean cancelJob(@RequestBody GenericRequest req) {
		return jobManager.cancelJob(req.getID());
	}

	@PostMapping(value="/deleteJob", consumes = "application/json")
	public boolean deleteJob(@RequestBody GenericRequest req) {
		return jobManager.deleteJob(req.getID());
	}
}