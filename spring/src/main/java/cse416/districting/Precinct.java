package cse416.districting;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import lombok.Getter;

@Getter
public class Precinct {
    String id;
    JSONArray coordinates;
    JSONObject properties;

    public Precinct(String i, JSONArray c, JSONObject p){
        id = i;
        coordinates = c;
        properties = p;
    }
}
