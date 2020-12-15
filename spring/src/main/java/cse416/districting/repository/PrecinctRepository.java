package cse416.districting.repository;

import org.springframework.data.repository.CrudRepository;

import cse416.districting.model.Precinct;

public interface PrecinctRepository extends CrudRepository<Precinct, Integer> {

	Iterable<Precinct> findByState(String state);
	Precinct findOneByGeoid(String geoid);
}