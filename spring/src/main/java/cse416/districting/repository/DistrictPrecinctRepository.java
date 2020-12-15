package cse416.districting.repository;

import org.springframework.data.repository.CrudRepository;

import cse416.districting.model.DistrictPrecinct;

public interface DistrictPrecinctRepository extends CrudRepository<DistrictPrecinct, Integer> {

}