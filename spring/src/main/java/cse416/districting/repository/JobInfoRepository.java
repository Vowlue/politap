package cse416.districting.repository;

import org.springframework.data.repository.CrudRepository;

import cse416.districting.model.JobInfoModel;

public interface JobInfoRepository extends CrudRepository<JobInfoModel, Integer> {

}