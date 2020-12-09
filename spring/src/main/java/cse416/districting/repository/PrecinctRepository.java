package cse416.districting.repository;

import org.springframework.data.repository.CrudRepository;

import cse416.districting.model.Precinct;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface PrecinctRepository extends CrudRepository<Precinct, Integer> {

	Iterable<Precinct> findByState(String state);

}