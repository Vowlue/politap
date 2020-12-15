package cse416.districting.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "job_results")
public class JobResults {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    private int jobID = -1;
    private int extreme = -1;
    private int average = -1;
    private int random1 = -1;
    private int random2 = -1;

    @Transient
    private int extremeIndex;
    @Transient
    private int averageIndex;
    @Transient
    private int random1Index;
    @Transient
    private int random2Index;

    @Transient
    private List<ArrayList<Float>> plot;
}
