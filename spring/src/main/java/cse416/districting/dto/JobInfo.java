package cse416.districting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import cse416.districting.Enums.Compactness;
import cse416.districting.Enums.Demographic;
import cse416.districting.Enums.JobStatus;
import cse416.districting.Enums.States;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JobInfo {
    private int plans;
    private States state;
    @JsonProperty
    private boolean isLocal;
    private Compactness compactness;
    private float populationVariance; 
    private JobStatus jobStatus;
    private Demographic[] demographic;

    @Override
    public String toString(){
        String ret = "plans: " + String.valueOf(plans) + "\n" +
                     "state: " + state.toString() + "\n"  +
                     "isLocal: " + String.valueOf(isLocal) + "\n" + 
                     "compactness: " + String.valueOf(compactness) + "\n" + 
                     "populationVariance: " + String.valueOf(populationVariance) + "\n" + 
                     "demographic: ";
        for(Demographic d : demographic){
            ret += d.toString() + ", ";
        }
        return ret + "\n";
    }
}
