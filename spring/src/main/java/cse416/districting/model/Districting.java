package cse416.districting.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "districtings")
public class Districting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "districting_generator")
    @SequenceGenerator(name="districting_generator", sequenceName = "districting_seq", allocationSize = 1)
    private int id;

    @OneToMany(mappedBy = "districting", fetch = FetchType.LAZY)
    private Set<District> districts = new HashSet<District>();
}
