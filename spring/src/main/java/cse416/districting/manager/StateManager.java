package cse416.districting.manager;

import cse416.districting.Enums.States;
import cse416.districting.model.StateObject;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class StateManager {

    private Map<States,StateObject> StateList;

    public StateManager(){
        StateList = new HashMap<>();
    }

    public JSONObject getDefaultStateInfo(States state) {
        JSONParser parser = new JSONParser();
        String filename = "";
        if (state == States.VIRGINIA) filename = "static/VA_Precinct.json";
        if (state == States.ARKANSAS) filename = "static/AR_Precinct.json";
        if (state == States.SOUTH_CAROLINA) filename = "static/SC_Precinct.json";

        Resource resource = new ClassPathResource(filename);
        Object obj;
        try {
            obj = parser.parse(new InputStreamReader(resource.getInputStream()));
            JSONObject jsonObject = (JSONObject) obj;
            StateObject stateObject = new StateObject(state);
            stateObject.processData(jsonObject);
            StateList.put(state,stateObject);
            //If successful return object
            return jsonObject;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //If failed, return error
        JSONObject error = new JSONObject();
        error.put("error", "getDefaultStateInfo() from StateManager failed");
        return error;
    }

    public JSONObject getStateHeatMap(States state){
        JSONObject data = new JSONObject();
        data.put("1","100");
        return data;
    }
}
