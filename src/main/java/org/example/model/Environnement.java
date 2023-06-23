package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;


@JsonIgnoreProperties(ignoreUnknown = true, value = {"VitesseSortie_km/h","VitesseEntree_km/h","TempsMesure_sec","Voie_2 Active?","CompteurDeMarche","AcquisitionAmbiante","DetecteurEntree","DetecteurSortie","CheminCorrectionCapteur"})

@Data
@Embeddable
public class Environnement {




    @Transient
    private Double VitesseSortie;

    @Transient
    private Double VitesseEntree;



    @JsonProperty("CompteurEssieuxSortie")
    private Integer CompteurEssieuxSortie;

    @JsonProperty("CompteurEssieuxEntree")
    private Integer CompteurEssieuxEntree;




    @JsonProperty("Sens")
    private String Sens;








    @JsonProperty("Meteo")
    @Embedded
    private Meteo meteo;

    @Transient
    private Double TempsMesure_sec;

    @Transient
    private String Voie_2;


    @Transient
    private Integer CompteurDeMarche;

    @Transient
    private String AcquisitionAmbiante;

    @Transient
    private String DetecteurEntree;

    @Transient
    private String DetecteurSortie;

    @Transient
    private String CheminCorrectionCapteur;


    @Column(name = "ville_Depart")
    private String villeDepart;

    @Column(name = "ville_Arrivee")
    private String villeArrivee;

    public String getVilleDepart() {
        return villeDepart;
    }

    public void setVilleDepart(String villeDepart) {
        this.villeDepart = villeDepart;
    }

    public String getVilleArrivee() {
        return villeArrivee;
    }

    public void setVilleArrivee(String villeArrivee) {
        this.villeArrivee = villeArrivee;
    }

    public String getSens() {
        return Sens;
    }

    public void setSens(String sens) {
        Sens = sens;
    }

    public Environnement (){}
    public Environnement(String Sens) {

        extraireVilles(Sens);
    }

    public String[] extraireVilles(String Sens) {
        if (this.Sens != null) {
            String[] villes = this.Sens.split("-");
            String villeDepart = villes[1].trim().split("vers")[0].trim();
            String villeArrivee = villes[2].trim().split("vers")[0].trim();
            return new String[] { villeDepart, villeArrivee };
        }
        return null;
    }






    public Integer getCompteurEssieuxSortie() {
        return CompteurEssieuxSortie;
    }

    public void setCompteurEssieuxSortie(Integer compteurEssieuxSortie) {
        CompteurEssieuxSortie = compteurEssieuxSortie;
    }

    public Integer getCompteurEssieuxEntree() {
        return CompteurEssieuxEntree;
    }

    public void setCompteurEssieuxEntree(Integer compteurEssieuxEntree) {
        CompteurEssieuxEntree = compteurEssieuxEntree;
    }








}
