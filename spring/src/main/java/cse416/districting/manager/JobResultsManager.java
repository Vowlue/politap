package cse416.districting.manager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import lombok.Getter;
import lombok.Setter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


@Getter
@Setter
public class JobResultsManager {

    public Map<String,JSONObject> getDistrictingFiles(int jobID){
        String[] types = {"average","extreme","random1","random2"};
        Map<String,JSONObject> maps = new HashMap<String,JSONObject>();
        for (String type : types){
            Resource resource = new ClassPathResource("json/generatedDistrictings/" + Integer.toString(jobID) + type + ".geojson");
            Object obj;
            JSONParser parser = new JSONParser();
            try {
                obj = parser.parse(new InputStreamReader(resource.getInputStream()));
                JSONObject jsonObject = (JSONObject) obj;
                maps.put(type, jsonObject);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
        return maps;
    }
}
