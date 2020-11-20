package cse416.districting.manager;

import java.util.Map;

import cse416.districting.model.JobResults;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobResultsManager {
    private Map<Integer,JobResults> results;
}
