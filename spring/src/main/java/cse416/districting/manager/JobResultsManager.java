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
import cse416.districting.model.JobResults;
import cse416.districting.repository.JobInfoRepository;
import cse416.districting.repository.JobResultsRepository;


@Getter
@Setter
public class JobResultsManager {

    @Autowired
    JobInfoRepository jobInfoRepository;

    @Autowired
    JobResultsRepository jobResultsRepository;
    
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
            Object obj;
            JSONParser parser = new JSONParser();
            try {
                while (!resource.exists()){
                    Thread.sleep(100);
                    resource = new ClassPathResource("json/generatedDistrictings/" + Integer.toString(jobID) + type + ".geojson");
                }
                obj = parser.parse(new InputStreamReader(resource.getInputStream()));
                JSONObject jsonObject = (JSONObject) obj;
                maps.put(type, jsonObject);
            } catch (IOException | ParseException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return maps;
    }

    public List<ArrayList<Float>> getPlotData(int jobID){
        JobResults jobResults = jobResultsRepository.findOneByJobID(jobID);
        return jobResults.getPlot();
    }
}
