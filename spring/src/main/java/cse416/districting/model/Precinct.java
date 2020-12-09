package cse416.districting.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

import cse416.districting.Enums.Demographic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "precinctdemo")
@SecondaryTable(name = "precinct", pkJoinColumns = @PrimaryKeyJoinColumn(name = "geoid"))
public class Precinct {
    @Id
    @Column(name = "`GEOID`")
    private String geoid;

    private int total;

    @Column(name = "`Hispanic or Latino`")
    private int hispanic_or_latino;

    private int white;

    @Column(name = "`Black or African American`")
    private int black_or_african_american;

    @Column(name = "`American Indian and Alaska Native`")
    private int american_indian_and_alaska_native;

    private int asian;

    @Column(name = "`Native Hawaiian and Other Pacific Islander`")
    private int native_hawaiian_and_other_pacific_islander;

    @Column(name = "`Some Other Race`")
    private int some_other_race;

    @Column(name = "`Total VAP`")
    private int total_vap;

    @Column(name = "`Hispanic or Latino VAP`")
    private int hispanic_or_latino_vap;

    @Column(name = "`White VAP`")
    private int white_vap;

    @Column(name = "`Black or African American VAP`")
    private int black_or_african_american_vap;

    @Column(name = "`American Indian and Alaska Native VAP`")
    private int american_indian_and_alaska_native_vap;

    @Column(name = "`Asian VAP`")
    private int asian_vap;

    @Column(name = "`Native Hawaiian and Other Pacific Islander VAP`")
    private int native_hawaiian_and_other_pacific_islander_vap;

    @Column(name = "`Some Other Race VAP`")
    private int some_other_race_vap;

    @Column(name = "state", table = "precinct")
    private String state;

    @Column(name = "name", table = "precinct")
    private String name;

    @Column(name = "county", table = "precinct")
    private String county;

    public Map<Demographic,Integer> getPopulationData(){
        Map<Demographic,Integer> map = new HashMap<Demographic,Integer>();
        map.put(Demographic.WHITE,white);
        map.put(Demographic.HISPANIC_OR_LATINO, hispanic_or_latino);
        map.put(Demographic.BLACK_OR_AFRICAN_AMERICAN,black_or_african_american);
        map.put(Demographic.AMERICAN_INDIAN_OR_ALASKA_NATIVE, american_indian_and_alaska_native);
        map.put(Demographic.ASIAN, asian);
        map.put(Demographic.NATIVE_HAWAIIAN_OR_OTHER_PACIFIC_ISLANDER,native_hawaiian_and_other_pacific_islander);
        map.put(Demographic.OTHER, some_other_race);
        map.put(Demographic.TOTAL, total);
        return map;
    }

    public Map<Demographic,Integer> getPopulationDataVAP(){
        Map<Demographic,Integer> map = new HashMap<Demographic,Integer>();
        map.put(Demographic.WHITE,white_vap);
        map.put(Demographic.HISPANIC_OR_LATINO, hispanic_or_latino_vap);
        map.put(Demographic.BLACK_OR_AFRICAN_AMERICAN,black_or_african_american_vap);
        map.put(Demographic.AMERICAN_INDIAN_OR_ALASKA_NATIVE, american_indian_and_alaska_native_vap);
        map.put(Demographic.ASIAN, asian_vap);
        map.put(Demographic.NATIVE_HAWAIIAN_OR_OTHER_PACIFIC_ISLANDER,native_hawaiian_and_other_pacific_islander_vap);
        map.put(Demographic.OTHER, some_other_race_vap);
        map.put(Demographic.TOTAL, total_vap);
        return map;
    }
}
