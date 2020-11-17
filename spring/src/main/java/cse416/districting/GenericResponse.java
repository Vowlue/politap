package cse416.districting;

import cse416.districting.Enums.JobStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GenericResponse {
    private String errormessage;
    private boolean error;
    private int ID;
    private JobStatus jobStatus;
}
