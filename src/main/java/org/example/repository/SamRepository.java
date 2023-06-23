package org.example.repository;

import org.example.model.Sam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.util.Date;
import java.util.List;

@Repository
public interface SamRepository extends JpaRepository<Sam, Long> {
//    List<Sam> findByDateBetween(Date startDate, Date endDate);

    List<Sam> findBySiteAndDateFichier(String site, Date dateFichier);


    List<Sam> findBySiteAndDateFichierAndHeureFichier(String site, Date dateFichier ,Time heure);

    List<Sam> findBySiteAndDateFichierBetween(String site, Date dateFichier , Date dateF);

    List<Sam> findBySiteAndDateFichierBetweenAndStatutSAM(String site,Date dateFichier ,Date dateF ,String statut );



    boolean existsByfileName(String nomFichier);


}
