package org.example.repository;

import org.example.model.M_50592;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Time;
import java.util.Date;
import java.util.List;

@Repository
public interface M_50592Repository extends JpaRepository<M_50592, Long> {

    List<M_50592> findBySiteAndDateFichier(String site, Date dateFichier);

    List<M_50592> findBySiteAndDateFichierBetween(String site, Date dateFichier , Date dateF);

    List<M_50592> findBySiteAndDateFichierBetweenAndStatut50592(String site, Date dateFichier , Date dateF , String statut50592);
    List<M_50592> findBySiteAndDateFichierAndHeureFichier(String site, Date dateFichier, Time heure);
    boolean existsByfileName(String nomFichier);





}
