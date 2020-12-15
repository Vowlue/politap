package cse416.districting.manager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import cse416.districting.model.JobInfoModel;
import cse416.districting.repository.JobInfoRepository;


@Getter
@Setter
public class JobResultsManager {

    @Autowired
    JobInfoRepository jobInfoRepository;
    
    public List<JobInfoModel> getHistory(){
        List<JobInfoModel> ret = new ArrayList<JobInfoModel>();
        jobInfoRepository.findAll().forEach(ret::add);;
        return ret;
    }

    public Map<String,JSONObject> getDistrictingFiles(int jobID){
        String[] types = {"average","extreme","random1","random2"};
        Map<String,JSONObject> maps = new HashMap<String,JSONObject>();
        for (String type : types){
            Resource resource = new ClassPathResource("json/generatedDistrictings/" + Integer.toString(jobID) + type + ".geojson");
            while (!resource.exists() || !resource.isReadable()){
                resource = new ClassPathResource("json/generatedDistrictings/" + Integer.toString(jobID) + type + ".geojson");
            }
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
