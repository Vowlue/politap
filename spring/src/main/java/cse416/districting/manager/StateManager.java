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
        if (state == States.VIRGINIA) filename = "json/VA_Precinct.json";
        if (state == States.ARKANSAS) filename = "json/AR_Precinct.json";
        if (state == States.SOUTH_CAROLINA) filename = "json/SC_Precinct.json";

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
        HashMap<String,String> hm = new HashMap<>();
        hm.put("error", "getDefaultStateInfo() from StateManager failed");
        return new JSONObject(hm);
    }

    public JSONObject getStateHeatMap(States state){
        JSONObject data = new JSONObject();
        return data;
    }

    public JSONObject[] getDistrictingFile(String filename){
        Resource resource = new ClassPathResource("json/generatedDistrictings/" + filename + ".geojson");
        Resource resource2 = new ClassPathResource("json/generatedDistrictings/" + filename + "2.geojson");
        Object obj;
        Object obj2;
        JSONParser parser = new JSONParser();
        try {
            obj = parser.parse(new InputStreamReader(resource.getInputStream()));
            JSONObject jsonObject = (JSONObject) obj;
            obj2 = parser.parse(new InputStreamReader(resource2.getInputStream()));
            JSONObject jsonObject2 = (JSONObject) obj2;
            //If successful return object
            JSONObject[] ret = {jsonObject,jsonObject2};
            return ret;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
