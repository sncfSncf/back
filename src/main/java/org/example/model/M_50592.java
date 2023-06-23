package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.example.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"FFT_R1","FFT_R2","ParametresBE","ParametresBL"})
@Entity
@Data
@Table(name = "T_50592")
public class M_50592 {
    private static final Logger logger = LoggerFactory.getLogger(M_50592.class);
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;



    @Column(columnDefinition = "Varchar",name = "file_Name")
    private String fileName;
    @Column(name = "date_Fichier")
    @Temporal(TemporalType.DATE)
    private java.util.Date dateFichier;

    @Column(name = "heure_Fichier")
    @Temporal(TemporalType.TIME)
    private java.util.Date heureFichier;
    @Column(name = "ville_Depart", insertable = false, updatable = false)
    private String villeDepart;

    @Column(name = "ville_Arrivee", insertable = false, updatable = false)
    private String villeArrivee;

    @Column(name = "site")
    private String site;

    @Column(name = "url50592")
    private String url50592;

    @Column(name = "statut50592")
    private String statut50592;

    @JsonProperty("Environnement")
    @Embedded
    private Environnement environnement;


    @JsonProperty("BE_R1")
    @Embedded
    private BE_R1 beR1 ;

    @JsonProperty("BE_R2")
    @Embedded
    private BE_R2 beR2;

    @JsonProperty("BL_R1")
    @Embedded
    private BL_R1 blR1;

    @JsonProperty("BL_R2")
    @Embedded
    private BL_R2 blR2;



    public BE_R2 getBeR2() {
        return beR2;
    }

    public void setBeR2(BE_R2 beR2) {
        this.beR2 = beR2;
    }

    public BL_R1 getBlR1() {
        return blR1;
    }

    public void setBlR1(BL_R1 blR1) {
        this.blR1 = blR1;
    }

    public BL_R2 getBlR2() {
        return blR2;
    }

    public void setBlR2(BL_R2 blR2) {
        this.blR2 = blR2;
    }





    public String getUrl50592() {
        return url50592;
    }

    public void setUrl50592(String url50592) {
        this.url50592 = url50592;
    }

    public void loadSite(String fileName) {
        String[] tokens = fileName.split("_");
        if (tokens.length >= 2) {
            String name = tokens[0];
            String [] nom = name.split("-");
            String site = nom[1];

            this.setSite(site);
        }
    }


    public String getStatut50592() {
        return statut50592;
    }

    public void setStatut50592(String statut50592) {
        this.statut50592 = statut50592;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDateFichier(Date dateFichier) {
        this.dateFichier = dateFichier;
    }

    public void setHeureFichier(Date heureFichier) {
        this.heureFichier = heureFichier;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }








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

    public M_50592() {
        loadFilenamesStartingWith50592();


    }


    public BE_R1 getBeR1() {
        return beR1;
    }

    public void setBeR1(BE_R1 beR1) {
        this.beR1 = beR1;
    }



    public void loadFilenamesStartingWith50592() {

        Properties prop = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
        try {
            prop.load(input);
        } catch (IOException e) {
            // Traitement de l'exception
            e.printStackTrace();
        }

        String path = prop.getProperty("input.folder.path");
        File directory = new File(path);
        List<File> files = List.of(directory.listFiles())
                .stream()
                .filter(f -> f.getName().startsWith("50592") && f.getName().endsWith(".json"))
                .collect(Collectors.toList());

        List<String> filenames = new ArrayList<>();
        for (File file : files) {
            String filename = file.getName();
            filenames.add(filename);
        }

        for (String filename : filenames) {
            this.setFileName(filename);
        }
    }

    public void loadStartingWith50592(String fileName) {
        int index = fileName.indexOf("_");
        if (index > 0) { // Vérifier si le nom de fichier contient au moins un "_"
            // Trouver l'index du 2ème "_" en partant de la droite
            int lastIndex = fileName.lastIndexOf("_");
            if (lastIndex > index) {
                String dateTimePart = fileName.substring(index+1, lastIndex); // Extraire la partie qui contient la date et l'heure en excluant l'extension du fichier (.json)



                String[] dateTimeParts = dateTimePart.split("[_ .hms]+");


                if (dateTimeParts.length == 6) { // Vérifier si la partie date-heure a été correctement divisée
                    String datePart = dateTimeParts[0] + "." + dateTimeParts[1] + "." + dateTimeParts[2]; // Concaténer les parties pour former la date
                    String heurePart = dateTimeParts[3] + "h" + dateTimeParts[4] + "m" + dateTimeParts[5]+ "s"; // Concaténer les parties pour former l'heure


                    // Convertir la date et l'heure en objets Date et Time
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
                        java.util.Date parsedDate = dateFormat.parse(datePart);
                        java.sql.Date date = new java.sql.Date(parsedDate.getTime());

                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh'h'mm'm'ss's'");
                        java.util.Date parsedTime = timeFormat.parse(heurePart);
                        java.sql.Time time = new java.sql.Time(parsedTime.getTime());

                        // Mettre à jour les champs dateFichier et heureFichier de l'objet M_50592
                        this.setDateFichier(date);
                        this.setHeureFichier(time);
                        this.setFileName(fileName);
                    } catch (ParseException e) {
                        // Gérer l'exception si la date ou l'heure ne peut pas être analysée
                        e.printStackTrace();
                    }

                }
            }
        }
    }


    public M_50592(String fileName, Environnement environnement) {
        this.fileName = fileName;
        this.environnement = environnement;

    }
















    public Environnement getEnvironnement() {
        return environnement;
    }

    public void setEnvironnement(Environnement environnement) {
        this.environnement = environnement;

    }
}
