package cse416.districting.manager;

import cse416.districting.Enums;
import cse416.districting.Enums.Demographic;
import cse416.districting.Enums.States;
import cse416.districting.model.Precinct;
import cse416.districting.model.StateObject;
import cse416.districting.repository.PrecinctRepository;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapManager {

    private Map<States,StateObject> StateList;

    @Autowired
    private PrecinctRepository precinctRepository;

    public MapManager(){
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
            StateObject stateObject = new StateObject(state, new ArrayList<>());
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

    public Map<String,Map<Demographic,Integer>> getStateHeatMap(States state){
        state = States.VIRGINIA;
        Map<String,Map<Demographic,Integer>> ret = new HashMap<String,Map<Demographic,Integer>>();
        for (Precinct p : precinctRepository.findByState(Enums.getStateShortcut(state))){
            ret.put(p.getGeoid(), p.getPopulationData());
        }
        return ret;
    }

    public Map<String,Map<Demographic,Integer>> getStateHeatMapVap(States state){
        state = States.VIRGINIA;
        Map<String,Map<Demographic,Integer>> ret = new HashMap<String,Map<Demographic,Integer>>();
        for (Precinct p : precinctRepository.findByState(Enums.getStateShortcut(state))){
            ret.put(p.getGeoid(), p.getPopulationDataVAP());
        }
        return ret;
    }

    public JSONObject getDistrictingFile(String filename){
        Resource resource = new ClassPathResource("json/generatedDistrictings/" + filename + ".geojson");
        Object obj;
        JSONParser parser = new JSONParser();
        try {
            obj = parser.parse(new InputStreamReader(resource.getInputStream()));
            JSONObject jsonObject = (JSONObject) obj;
            //If successful return object
            return jsonObject;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        //If failed, return error
        HashMap<String,String> hm = new HashMap<>();
        hm.put("error", "getDistrictingFile() from StateManager failed");
        return new JSONObject(hm);
    }
}
