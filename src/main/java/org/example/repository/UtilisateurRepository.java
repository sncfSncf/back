package org.example.repository;

import org.example.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Objects;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {


    Utilisateur findByLogin(@Param("login")String login);

    default boolean exists(String email) {
        List<Utilisateur> utilisateurList = findAll();
        for (Utilisateur utilisateur : utilisateurList) {
            if (utilisateur.getLogin().equals(email)) {
                return true;
            }
        }
        return false;
    }

    //en excluant l'objet correspondant à id
    default boolean exists(Utilisateur utilisateur, Long id){
        List<Utilisateur> utilisateurList = findAll();
        for (Utilisateur utilisateur1 : utilisateurList){
            if (!Objects.equals(utilisateur1.getId(), id) && utilisateur1.equals(utilisateur)) return true;
        }
        return false;
    }
}
