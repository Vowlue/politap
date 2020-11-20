package cse416.districting.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JobResults {
    private String filename;
    private List<Districting> districtings;
    private Districting extreme;
    private Districting average;
    private Districting random1;
    private Districting random2;
    private List<BoxPlot> plot;

    public JobResults(List<Districting> districtings){
        this.districtings = districtings;
    }

    public void countCounties(){
        for (Districting districting : districtings){
            for (District district : districting.getDistricts()){
                district.countCounties();
            }
        }
    }
    public void generateSummaryFile(){}
    public void generateOrderedCollection(){}
    public void generateBoxAndWhisker(){}
    public void determineAverage(){}
    public void determineExtreme(){}
    public void pickRandom(){}
    public void calculateAssociation(){}
}
