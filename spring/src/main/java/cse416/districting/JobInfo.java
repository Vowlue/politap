package cse416.districting;

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
    private boolean isLocal;
    private Compactness compactness;
    private float populationVariance; 
    private JobStatus jobStatus;
    private Demographic[] demographic;
}
