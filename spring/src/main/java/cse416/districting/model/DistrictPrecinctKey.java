package cse416.districting.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
@Embeddable
public class DistrictPrecinctKey implements Serializable{
    private String precinct_id;
    private int district_id;
}