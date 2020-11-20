package cse416.districting.model;

import java.util.List;
import java.util.Map;

import cse416.districting.Enums.Demographic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Precinct {
    private String id;
    private String name;
    private List<Precinct> neighbors;
    private String county;
    private Map<Demographic,Long> demo;
    private Map<Demographic,Long> demoVAP;
    private long population;
    private long populationVAP;
}
