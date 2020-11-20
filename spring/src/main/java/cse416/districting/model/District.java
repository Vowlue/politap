package cse416.districting.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class District {
    private int id;
    private List<Precinct> precincts;
    private int countyCount;

    public void countCounties(){
        Set<String> set = new HashSet<>();
        for (Precinct precinct : precincts){
            set.add(precinct.getCounty());
        }
        countyCount = set.size();
    }
}