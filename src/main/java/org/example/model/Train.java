package org.example.model;

import lombok.Data;

import javax.persistence.*;
import java.util.*;


@Entity
@Data
@Table(name = "T_TRAIN")
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL)
    private List<Result> results= new ArrayList<>();

    @Column(name = "site")
    private String site;



    @Column(name = "date_Fichier")
    @Temporal(TemporalType.DATE)
    private java.util.Date dateFichier;

    @Column(name = "heure_Fichier")
    @Temporal(TemporalType.TIME)
    private java.util.Date heureFichier;




    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }


    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public Date getDateFichier() {
        return dateFichier;
    }

    public void setDateFichier(Date dateFichier) {
        this.dateFichier = dateFichier;
    }

    public Date getHeureFichier() {
        return heureFichier;
    }

    public void setHeureFichier(Date heureFichier) {
        this.heureFichier = heureFichier;
    }

    public Train(){}

































//    @Column(name = "url")
//    private String url;
//
//    @Column(name = "Statut")
//    private String Statut;
//
//    @JsonProperty("Num_train")
//    @Column(columnDefinition = "Varchar",name = "num_train")
//    private  String numTrain;
//
//    @Column(name = "site")
//    private String site;
//    @Column(columnDefinition = "Varchar",name = "file_Name")
//    private String fileName;
//
//    @Column(name = "date_Fichier")
//    @Temporal(TemporalType.DATE)
//    private java.util.Date dateFichier;
//
//    @Column(name = "heure_Fichier")
//    @Temporal(TemporalType.TIME)
//    private java.util.Date heureFichier;
//
//    public Train() {
//        loadFilenamesStartingWithTRAIN();
//    }
//
//    public Date getDateFichier() {
//        return dateFichier;
//    }
//
//    public void setDateFichier(Date dateFichier) {
//        this.dateFichier = dateFichier;
//    }
//
//    public Date getHeureFichier() {
//        return heureFichier;
//    }
//
//    public void setHeureFichier(Date heureFichier) {
//        this.heureFichier = heureFichier;
//    }
//
//    public String getSite() {
//        return site;
//    }
//
//    public void setSite(String site) {
//        this.site = site;
//    }
//
//    public String getFileName() {
//        return fileName;
//    }
//
//    public void setFileName(String fileName) {
//        this.fileName = fileName;
//    }
//
//    public String getUrl() {
//        return url;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }
//
//    public String getStatut() {
//        return Statut;
//    }
//
//    public void setStatut(String statut) {
//        Statut = statut;
//    }
//
//    public String getNumTrain() {
//        return numTrain;
//    }
//
//    public void setNumTrain(String numTrain) {
//        this.numTrain = numTrain;
//    }
//
//
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public Train(Long id, M_50592 m_50592) {
//        this.id = id;
//
//
//    }
//
//    public Train(Long id, String url, String statut, String numTrain, String site, String fileName, Date dateFichier, Time heureFichier) {
//        this.id = id;
//        this.url = url;
//        Statut = statut;
//        this.numTrain = numTrain;
//        this.site = site;
//        this.fileName = fileName;
//        this.dateFichier = dateFichier;
//        this.heureFichier = heureFichier;
//    }
//
//
//    public void loadFilenamesStartingWithTRAIN() {
//
//        Properties prop = new Properties();
//        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
//        try {
//            prop.load(input);
//        } catch (IOException e) {
//            // Traitement de l'exception
//            e.printStackTrace();
//        }
//
//        String path = prop.getProperty("input.folder.path");
//
//        File directory = new File(path);
//        List<File> files = List.of(directory.listFiles())
//                .stream()
//                .filter(f -> f.getName().startsWith("TRAIN") && f.getName().endsWith(".json"))
//                .collect(Collectors.toList());
//
//        List<String> filenames = new ArrayList<>();
//
//        for (File file : files) {
//            String filename = file.getName();
//            filenames.add(filename);
//
//        }
//
//        for (String filename : filenames) {
//            this.setFileName(filename);
//        }
//
//    }
//
//
//    public void loadSite(String fileName) {
//        String[] tokens = fileName.split("_");
//        if (tokens.length >= 2) {
//            String name = tokens[0];
//            String [] nom = name.split("-");
//            String site = nom[1];
//
//            this.setSite(site);
//        }
//    }
//
//
//
//    public void loadStartingWithTRAIN(String fileName) {
//        int index = fileName.indexOf("_");
//        if (index > 0) { // Vérifier si le nom de fichier contient au moins un "_"
//            // Trouver l'index du 2ème "_" en partant de la droite
//            int lastIndex = fileName.lastIndexOf("_");
//            if (lastIndex > index) {
//                String dateTimePart = fileName.substring(index+1, fileName.length()-5); // Extraire la partie qui contient la date et l'heure en excluant l'extension du fichier (.json)
//
//
//
//                String[] dateTimeParts = dateTimePart.split("[_ .hms]+");
//
//
//                if (dateTimeParts.length == 6) { // Vérifier si la partie date-heure a été correctement divisée
//                    String datePart = dateTimeParts[0] + "." + dateTimeParts[1] + "." + dateTimeParts[2]; // Concaténer les parties pour former la date
//                    String heurePart = dateTimeParts[3] + "h" + dateTimeParts[4] + "m" + dateTimeParts[5]+ "s"; // Concaténer les parties pour former l'heure
//
//                    // Convertir la date et l'heure en objets Date et Time
//                    try {
//                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
//                        java.util.Date parsedDate = dateFormat.parse(datePart);
//                        java.sql.Date date = new java.sql.Date(parsedDate.getTime());
//
//                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh'h'mm'm'ss's'");
//                        java.util.Date parsedTime = timeFormat.parse(heurePart);
//                        java.sql.Time time = new java.sql.Time(parsedTime.getTime());
//
//                        // Mettre à jour les champs dateFichier et heureFichier de l'objet M_50592
//                        this.setDateFichier(date);
//                        this.setHeureFichier(time);
//                        this.setFileName(fileName);
//                    } catch (ParseException e) {
//                        // Gérer l'exception si la date ou l'heure ne peut pas être analysée
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//        }
//    }
}
