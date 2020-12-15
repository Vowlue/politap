package cse416.districting.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "districts")
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "district_generator")
    @SequenceGenerator(name="district_generator", sequenceName = "district_seq", allocationSize = 1)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Districting districting;

    private long population = 0;

    private long minority = 0;

    private long vap = 0;

    private long minorityvap = 0;

    private int counties = -1;
}