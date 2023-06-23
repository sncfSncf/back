package org.example.repository;

import org.example.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Long> {

    List<Result> findAll();


    List<Result> findByTrainIdAndEngine(Long trainId, String engine);
}
