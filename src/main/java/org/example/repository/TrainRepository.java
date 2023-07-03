package org.example.repository;


import org.example.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Time;
import java.util.Date;
import java.util.List;

public interface TrainRepository extends JpaRepository<Train, Long> {

  List<Train> findByDateFichierAndSite(Date dateFichier, String site);
//
    List<Train> findBySiteAndDateFichier(String site, Date dateFichier);
 List<Train> findBySiteAndDateFichierBetween(String site, Date dateFichier , Date dateF);

    int countBySiteAndDateFichierBetween( String site,Date start, Date end);

    List<Train> findBySiteAndDateFichierAndHeureFichier(String site , Date dateFichier , Time heure);


}
