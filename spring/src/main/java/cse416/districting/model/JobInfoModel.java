package cse416.districting.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import cse416.districting.Enums;
import cse416.districting.Enums.Demographic;
import cse416.districting.dto.JobInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "job_info")
public class JobInfoModel {

    @Id
    private int id;

    private int plans;

    private String state;

    private String compactness;

    private float populationVariance; 

    @Transient
    private Demographic[] demographicsList;

    private String demographics;

    public JobInfoModel(int id, JobInfo jobInfo){
        this.id = id;
        this.plans = jobInfo.getPlans();
        this.state = Enums.getStateShortcut(jobInfo.getState());
        this.compactness = jobInfo.getCompactness().toString();
        this.populationVariance = jobInfo.getPopulationVariance();
        this.demographicsList = jobInfo.getDemographic();
        String str = "";
        for (int i = 0; i < demographicsList.length; i++){
            str += demographicsList[i].toString();
            if (i != demographicsList.length-1) str += ",";
        }
        this.demographics = str;
    }
}