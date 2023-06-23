package org.example.repository;

import org.example.model.Mr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MrRepository extends JpaRepository<Mr,Long> {


    Mr findByNumTrain(String numTrain);

    List<Mr> findAllByNumTrain(String numTrain);






    List<Mr> findByMr(String typeMr);



List<Mr> findAll();
}
