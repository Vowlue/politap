package cse416.districting.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "district_precinct")
public class DistrictPrecinct {

    @EmbeddedId
    private DistrictPrecinctKey id;

    private long minority = 0;
    private long minorityvap = 0;

    public DistrictPrecinct(String precinctid, int districtid){
        this.id = new DistrictPrecinctKey(precinctid, districtid);
    }
}