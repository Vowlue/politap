package cse416.districting.repository;

import org.springframework.data.repository.CrudRepository;

import cse416.districting.model.District;

public interface DistrictRepository extends CrudRepository<District, Integer> {

}