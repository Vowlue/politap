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

    public DistrictPrecinct(String precinct_id, int district_id){
        this.id = new DistrictPrecinctKey(precinct_id, district_id);
    }
}