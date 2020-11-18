package cse416.districting.model;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cse416.districting.Enums.States;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StateObject {
    private States state;
    private Map<String,Precinct> precincts;

    public StateObject(States state){
        this.state = state;
    }

    public void processData(JSONObject obj){
        precincts = new HashMap<>();
        JSONArray arr = (JSONArray) obj.get("features");
        for (int i = 0; i < arr.size(); i++){
            JSONObject geometry = ((JSONObject)((JSONObject)arr.get(i)).get("geometry"));
            JSONArray coordinates = null;
            if (geometry != null) coordinates = (JSONArray)geometry.get("coordinates");
            JSONObject properties = ((JSONObject)((JSONObject)arr.get(i)).get("properties"));
            String id = ((JSONObject)((JSONObject)arr.get(i)).get("properties")).get("GEOID").toString();
            Precinct precinct = new Precinct(id, coordinates, properties);
            precincts.put(id,precinct);
        }
    }
}