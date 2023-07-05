package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.component.Utils;

import org.apache.commons.io.FileUtils;
import org.example.model.*;
import org.example.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
public class SamTrainController {


    @Autowired
    private Utils utils;


    private final SamRepository samRepository;


    private final TrainRepository trainRepository;

    private final MrRepository mrRepository;

    private final M_50592Repository m50592Repository;

    private  final ResultRepository resultRepository;


    public SamTrainController(SamRepository samRepository, TrainRepository trainRepository, MrRepository mrRepository, M_50592Repository m50592Repository,ResultRepository resultRepository) {
        this.samRepository = samRepository;
        this.trainRepository = trainRepository;
        this.mrRepository = mrRepository;
        this.m50592Repository = m50592Repository;
        this.resultRepository =resultRepository;

    }


    private static final Logger logger = LoggerFactory.getLogger(SamTrainController.class);





    /**
     * Récupère le fichier correspondant à un site, une date et une heure donnés.
     * @param site Le nom du site.
     * @param dateFichier La date du fichier.
     * @param heure L'heure du fichier.
     * @return Le fichier correspondant, s'il existe ; sinon, null.
     * @throws IOException En cas d'erreur lors de la manipulation des fichiers ou des propriétés.
     */
    private File getFileBySiteAndDateFichier(String site, Date dateFichier, Time heure) throws IOException {
        // Charger les propriétés à partir du fichier "application.properties"
        Properties prop = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
        prop.load(input);

        // Récupérer le chemin du dossier de sortie à partir des propriétés
        String outputFolderPath = prop.getProperty("output.folder.path");

        // Formater la date et l'heure en chaînes de caractères
        String year = new SimpleDateFormat("yyyy").format(dateFichier);
        String month = new SimpleDateFormat("MM").format(dateFichier);
        String dateFichierStr = new SimpleDateFormat("yyyy.MM.dd").format(dateFichier);
        String heureStr = new SimpleDateFormat("HH'h'mm'm'ss's'").format(new Date(heure.getTime()));

        // Construire le chemin complet du dossier correspondant à l'année et au mois
        String folderPath = outputFolderPath + File.separator + year + "-" + month;

        // Créer un objet File représentant le dossier correspondant à l'année et au mois
        File targetFolder = new File(folderPath);

        // Rechercher les fichiers correspondants dans le dossier cible
        File[] samFiles = targetFolder.listFiles((dir, name) ->
                name.startsWith("SAM005-" + site + "_" + dateFichierStr + "_" + heureStr) && name.endsWith(".json"));

        // Afficher un message indiquant si le tableau de fichiers est vide ou non
        logger.info(samFiles.length == 0 ? "Le tableau est vide" : "Le tableau contient des éléments");

        // Vérifier si des fichiers ont été trouvés
        if (samFiles != null && samFiles.length > 0) {
            // Utiliser un logger pour enregistrer le fichier trouvé
            logger.info("Fichier trouvé : " + samFiles[0]);
            return samFiles[0];
        } else {
            // Utiliser un logger pour enregistrer l'absence de fichier trouvé
            logger.info("Aucun fichier trouvé");
            return null;
        }
    }




    /**
     * Récupère les temps en millisecondes pour un site donné, une heure et une date spécifiées.
     * @param site Le nom du site.
     * @param heure L'heure spécifiée (au format ISO TIME, ex : 10:30:00).
     * @param date La date spécifiée (au format ISO DATE, ex : 2023-06-06).
     * @return Une liste de maps contenant les temps en millisecondes pour chaque t1, t2 et t3.
     * @throws IOException En cas d'erreur lors de la manipulation des fichiers ou des propriétés.
     */
    @GetMapping("/temps")
    public List<Map<String, JsonNode>> getTempsMs(@RequestParam("site") String site,
                                                  @RequestParam("heure") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heure,
                                                  @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        // Convertir les types de date et d'heure
        Time heureTime = Time.valueOf(heure);
        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Récupérer le fichier correspondant
        File file = getFileBySiteAndDateFichier(site, dateFichier, heureTime);

        // Liste des noeuds tempsMs
        List<Map<String, JsonNode>> tempsMsNodesList = new ArrayList<>();

        // Vérifier si un fichier a été trouvé
        if (file != null) {
            // Lire le contenu du fichier JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(file);

            // Récupérer les noeuds tempsMs, t1, t2 et t3
            JsonNode tempsMsNodes = rootNode.get("Temps_ms");
            JsonNode t1Nodes = tempsMsNodes.get("t1");
            JsonNode t2Nodes = tempsMsNodes.get("t2");
            JsonNode t3Nodes = tempsMsNodes.get("t3");

            // Vérifier que les trois tableaux ont la même longueur
            if (t1Nodes.size() == t2Nodes.size() && t2Nodes.size() == t3Nodes.size()) {
                for (int i = 0; i < t1Nodes.size(); i++) {
                    // Créer une map pour stocker les tempsMs pour chaque t1, t2 et t3
                    Map<String, JsonNode> tempsMsMap = new HashMap<>();
                    tempsMsMap.put("t1", t1Nodes.get(i));
                    tempsMsMap.put("t2", t2Nodes.get(i));
                    tempsMsMap.put("t3", t3Nodes.get(i));
                    tempsMsNodesList.add(tempsMsMap);
                }
            }
        }

        // Utiliser un logger pour enregistrer la taille de la liste des tempsMs
        logger.info("Nombre de tempsMs récupérés : " + tempsMsNodesList.size());

        return tempsMsNodesList;
    }

    /**
     * Lit les fichiers JSON à partir d'un dossier spécifié et retourne une liste de maps contenant les noms des fichiers et leur contenu.
     * @param dossier Le chemin du dossier contenant les fichiers JSON.
     * @param startIndex L'index de départ pour les clés "capteur".
     * @return Une liste de maps contenant les noms des fichiers et leur contenu.
     * @throws IOException En cas d'erreur lors de la lecture des fichiers.
     */
    private List<Map<String, JsonNode>> lireFichiersJson(String dossier, int startIndex) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, JsonNode>> result = new ArrayList<>();
        File[] fichiers = new File(dossier).listFiles();
        if (fichiers != null) {
            for (File fichier : fichiers) {
                if (fichier.isFile() && fichier.getName().endsWith(".json")) {
                    Map<String, JsonNode> map = new HashMap<>();
                    String nomFichier = fichier.getName();
                    map.put("nomFichier", mapper.valueToTree(nomFichier)); // Ajout de la clé "nomFichier"
                    byte[] contenuFichier = Files.readAllBytes(fichier.toPath());
                    JsonNode jsonContenuFichier = mapper.readTree(contenuFichier);
                    map.put("contenuFichier", jsonContenuFichier); // Ajout du contenu du fichier
                    result.add(map);
                }
            }
            for (int i = 0; i < result.size(); i++) {
                Map<String, JsonNode> map = result.get(i);
                map.put("capteur", mapper.valueToTree("capteur" + (startIndex + i))); // Ajout de la clé "capteur" avec index incrémenté
            }
        }

        // Utiliser un logger pour enregistrer le nombre de fichiers lus
        logger.info("Nombre de fichiers lus : " + result.size());

        return result;
    }


    /**
     * Récupère les enveloppes pour un site donné, une heure et une date spécifiées.
     * @param site Le nom du site.
     * @param heure L'heure spécifiée (au format ISO TIME, ex : 10:30:00).
     * @param date La date spécifiée (au format ISO DATE, ex : 2023-06-06).
     * @return Une liste de maps contenant les enveloppes pour chaque capteur.
     * @throws IOException En cas d'erreur lors de la manipulation des fichiers ou des propriétés.
     */
    @GetMapping("/echantillonage")
    public List<Map<String, JsonNode>> getEnveloppes(@RequestParam("site") String site,
                                                     @RequestParam("heure") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heure,
                                                     @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        // Convertir les types de date et d'heure
        Time heureTime = Time.valueOf(heure);
        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Récupérer les Sams correspondants à la requête
        List<Sam> sams = samRepository.findBySiteAndDateFichierAndHeureFichier(site, dateFichier, heureTime);

        // Liste des capteurs
        List<Map<String, JsonNode>> capteurs = new ArrayList<>();
        int index = 0; // Initialisation de l'index
        for (Sam sam : sams) {
            String urlsamList = sam.getUrlSam();

            // Lire les fichiers JSON du dossier et ajouter les résultats à la liste des capteurs
            List<Map<String, JsonNode>> fichiers = lireFichiersJson(urlsamList, index);
            capteurs.addAll(fichiers);
            index += fichiers.size(); // Mise à jour de l'index
        }

        // Utiliser un logger pour enregistrer le nombre de capteurs récupérés
        logger.info("Nombre de capteurs récupérés : " + capteurs.size());

        return capteurs;
    }


    @GetMapping("/allcapteurs")
    public List<String> getAllCapteurs() throws IOException {
        List<String> capteurs = new ArrayList<>();
        Set<String> entetesDejaAjoutes = new HashSet<>(); // ensemble temporaire pour stocker les entêtes déjà ajoutées
        List<M_50592> m50592s = m50592Repository.findAll();
        Properties prop = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
        prop.load(input);
        String outputFolderPath = prop.getProperty("output.folder.path");

        for (M_50592 m50592 : m50592s) {
            String fileName = m50592.getFileName();
            // Extraire l'année et le mois du nom de fichier
            String[] parts = fileName.split("_");
            String[] dateParts = parts[1].split("\\.");
            String year = dateParts[0];
            String month = dateParts[1];

            // Construire le chemin complet du dossier correspondant à l'année et au mois
            String folderPath = outputFolderPath + File.separator + year + "-" + month;

            // Créer un objet File représentant le dossier correspondant à l'année et au mois
            File inputFile = new File(folderPath,m50592.getFileName());

            // Rechercher le fichier 50592 dans le dossier cible
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class); // lire depuis le fichier d'entrée
            JsonNode parametreBENode = rootNode.get("ParametresBE");
            for (int i = 0; i < parametreBENode.size(); i++) {
                JsonNode entete = parametreBENode.get(i).get(0);
                String enteteText = entete.asText();
                if (!entetesDejaAjoutes.contains(enteteText)) { // vérifier si l'entête n'a pas déjà été ajoutée
                    capteurs.add(enteteText);
                    entetesDejaAjoutes.add(enteteText); // ajouter l'entête à l'ensemble temporaire
                }
            }
        }

        // Utiliser un logger pour enregistrer le nombre de capteurs récupérés
        logger.info("Nombre de capteurs récupérés : " + capteurs.size());

        return capteurs;
    }




    /**
     * Récupère la liste des capteurs.
     * @return Une liste de chaînes de caractères représentant les capteurs.
     * @throws IOException En cas d'erreur lors de la manipulation des fichiers ou des propriétés.
     */
    @GetMapping("/capteurs")
    public List<String> getCapteurs() throws IOException {
        List<String> capteurs = new ArrayList<>();
        Set<String> entetesDejaAjoutes = new HashSet<>(); // ensemble temporaire pour stocker les entêtes déjà ajoutées
        List<M_50592> m50592s = m50592Repository.findAll();
        Properties prop = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
        prop.load(input);
        String outputFolderPath = prop.getProperty("output.folder.path");

        for (M_50592 m50592 : m50592s) {
            String fileName = m50592.getFileName();
            // Extraire l'année et le mois du nom de fichier
            String[] parts = fileName.split("_");
            String[] dateParts = parts[1].split("\\.");
            String year = dateParts[0];
            String month = dateParts[1];

            // Construire le chemin complet du dossier correspondant à l'année et au mois
            String folderPath = outputFolderPath + File.separator + year + "-" + month;

            // Créer un objet File représentant le dossier correspondant à l'année et au mois
            File inputFile = new File(folderPath,m50592.getFileName());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class); // read from input file
            JsonNode parametreBENode = rootNode.get("ParametresBE");
            for (int i = 0; i < parametreBENode.size(); i++) {
                JsonNode entete = parametreBENode.get(i).get(0);
                String enteteText = entete.asText();
                if (!entetesDejaAjoutes.contains(enteteText)) { // vérifier si l'entête n'a pas déjà été ajoutée
                    if (!(enteteText.startsWith("D39") || enteteText.startsWith("D50"))) { // vérifier si l'entête ne commence pas par D39 ou D50
                        capteurs.add(enteteText);
                        entetesDejaAjoutes.add(enteteText); // ajouter l'entête à l'ensemble temporaire
                    }
                }
            }
        }

        // Utiliser un logger pour enregistrer le nombre de capteurs récupérés
        logger.info("Nombre de capteurs récupérés : " + capteurs.size());

        return capteurs;
    }




    /**
     * Récupère les URLs des images pour un site donné, une heure et une date spécifiées.
     * @param site Le nom du site.
     * @param heure L'heure spécifiée (au format ISO TIME, ex : 10:30:00).
     * @param date La date spécifiée (au format ISO DATE, ex : 2023-06-06).
     * @return Une réponse contenant une liste de maps représentant les URLs des images.
     * @throws IOException En cas d'erreur lors de la manipulation des fichiers ou des propriétés.
     */
    @GetMapping("/urls")
    public ResponseEntity<List<Map<String, Object>>> geturl(@RequestParam("site") String site,
                                                            @RequestParam("heure") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heure,
                                                            @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        Time heureTime = Time.valueOf(heure);
        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierAndHeureFichier(site, dateFichier, heureTime);
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichierAndHeureFichier(site, dateFichier,heureTime);
        Map<String, Object> trainMap = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Train train : trains) {
            for (Result results : train.getResults()) {
                if (train.getHeureFichier().equals(heureTime) &&
                        train.getDateFichier().equals(dateFichier)) {

                    trainMap.put("dateFichier", train.getDateFichier());
                    trainMap.put("heureFichier", train.getHeureFichier());

                    trainMap.put("image", results.getImage());
                    trainMap.put("imagemini", results.getThumbnail());

                }
                else{
                    trainMap.put("image", null);
                    trainMap.put("imagemini", null);
                }
            }
        }
                for (M_50592 m50592 : m50592s) {
                    if (m50592.getHeureFichier().equals(heureTime) &&
                            m50592.getDateFichier().equals(dateFichier)) {

                        // Créer une liste de noms d'images PNG à partir de l'URL
                        List<Map<String, Object>> images50592 = new ArrayList<>();
                        String url50592 = m50592.getUrl50592() + '/';
                        int index50592 = url50592.lastIndexOf('/');
                        String directory50592 = url50592.substring(0, index50592 + 1);
                        File folder50592 = new File(directory50592);
                        File[] files50592 = folder50592.listFiles();
                        if (files50592 != null) {
                            for (File file : files50592) {
                                if (file.isFile() && file.getName().toLowerCase().endsWith(".png")) {
                                    Map<String, Object> image = new HashMap<>();
                                    image.put("name", file.getName());

                                    try {
                                        byte[] fileContent = FileUtils.readFileToByteArray(file);
                                        String base64 = Base64.getEncoder().encodeToString(fileContent);
                                        image.put("content", base64);
                                        images50592.add(image);
                                    } catch (IOException e) {
                                        // handle exception
                                    }
                                }
                            }
                        }

                        trainMap.put("images50592", images50592);
                        trainMap.put("url50592", m50592.getUrl50592());


                    }else{
                        trainMap.put("images50592", null);
                        trainMap.put("url50592", null);
                    }

                }



                result.add(trainMap);


        // Utiliser un logger pour enregistrer le nombre de résultats récupérés
        logger.info("Nombre de résultats récupérés : " + result.size());

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }














    //Api pour la partie Jour J
    /**
     * Récupère les numéro de train et leur type mr pour un site donné et une date spécifiées.
     * @param site Le nom du site.
     * @param date La date spécifiée (au format ISO DATE, ex : 2023-06-06).
     * @return Une réponse contenant une liste de maps représentant les infos de train & SAM005 & 50592.
     * @throws IOException En cas d'erreur lors de la manipulation des fichiers ou des propriétés.
     */
    @GetMapping("/trainmr")
    public ResponseEntity<List<Map<String, Object>>> getByStrainmr(
            @RequestParam("site") String site,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws Exception{


        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);


        List<Map<String, Object>> result = new ArrayList<>();


// le cas où train & SAM005 & 50592 sont présents
        for (Train train : trains) {

            Map<String, Object> trainMap = new HashMap<>();




            for (Result results : train.getResults()) {
                trainMap.put("numTrain", results.getEngine());

                Mr mr = mrRepository.findByNumTrain(results.getEngine());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                } else {
                    trainMap.put("mr", null); // Ou une valeur par défaut appropriée si nécessaire
                }
            }

// Vérification si numTrain ou mr est manquant
            if (!trainMap.containsKey("numTrain") || !trainMap.containsKey("mr")) {
                trainMap.put("numTrain", null); // Ou une valeur par défaut appropriée si nécessaire
                trainMap.put("mr", null); // Ou une valeur par défaut appropriée si nécessaire
            }

            result.add(trainMap);
        }

                if (result.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }

                return ResponseEntity.ok(result);
            }

    /**
     * Vérifie si deux objets Date représentent le même moment dans le temps (heure et minute identiques).
     * @param time1 Le premier objet Date.
     * @param time2 Le deuxième objet Date.
     * @return true si les objets Date représentent le même moment dans le temps, sinon false.
     */
    boolean isBetweenTime(Date targetTime, Date startTime, Date endTime) {
        Calendar calendarTarget = Calendar.getInstance();
        calendarTarget.setTime(targetTime);
        int hourTarget = calendarTarget.get(Calendar.HOUR_OF_DAY);
        int minuteTarget = calendarTarget.get(Calendar.MINUTE);

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(startTime);
        calendarStart.add(Calendar.MINUTE, -1); // Soustraire 1 minute à la date de début
        int hourStart = calendarStart.get(Calendar.HOUR_OF_DAY);
        int minuteStart = calendarStart.get(Calendar.MINUTE);

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(endTime);
        calendarEnd.add(Calendar.MINUTE, 1); // Ajouter 1 minute à la date de fin
        int hourEnd = calendarEnd.get(Calendar.HOUR_OF_DAY);
        int minuteEnd = calendarEnd.get(Calendar.MINUTE);

        // Comparaison des minutes
        boolean isBetween = false;

        if (hourTarget == hourStart && hourTarget == hourEnd) {
            isBetween = minuteTarget >= minuteStart && minuteTarget <= minuteEnd;
        }

        // Affichage du résultat de la comparaison pour débogage
        logger.debug("La date cible est entre les minutes de début et de fin : {}", isBetween);

        return isBetween;
    }



    //Api pour la partie Jour J
    /**
     * Récupère les informations des trains & SAM005 & 50592 pour un site donné et une date spécifiées.
     * @param site Le nom du site.
     * @param date La date spécifiée (au format ISO DATE, ex : 2023-06-06).
     * @return Une réponse contenant une liste de maps représentant les infos de train & SAM005 & 50592.
     * @throws IOException En cas d'erreur lors de la manipulation des fichiers ou des propriétés.
     */
    @GetMapping("/data")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichier(
            @RequestParam("site") String site,
            @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Sam> sams = samRepository.findBySiteAndDateFichier(site, dateFichier);
        List<Train> trains = trainRepository.findBySiteAndDateFichier(site, dateFichier);
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichier(site, dateFichier);


        List<Map<String, Object>> result = new ArrayList<>();
        Set<Date> processedDates = new HashSet<>(); // Stocke les dates déjà traitées
        Set<String> processedTimes = new HashSet<>(); // Stocke les heures et minutes déjà traitées

// le cas où train & SAM005 & 50592 sont présents
        for (Train train : trains) {

            Map<String, Object> trainMap = new HashMap<>();

            Date dateKey = train.getDateFichier();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(train.getHeureFichier());

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            String timeKey = hour + ":" + minute; // Crée une clé de temps au format "heure:minute"

            if (processedDates.contains(dateKey) && processedTimes.contains(timeKey)) {
                continue; // Ignorer si la même heure et minute ont déjà été traitées
            }


            for (Result results : train.getResults()) {
                trainMap.put("numTrain", results.getEngine());
                trainMap.put("dateFichier", train.getDateFichier());
                trainMap.put("heureFichier", train.getHeureFichier());
                trainMap.put("imagemini", results.getThumbnail());
                trainMap.put("site", site);
                Mr mr = mrRepository.findByNumTrain(results.getEngine());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                }

                boolean foundSam = false;
                for (Sam sam : sams) {
                    if (isBetweenTime(train.getHeureFichier(), sam.getHeureFichier(), sam.getHeureFichier())
                            && train.getDateFichier().equals(sam.getDateFichier())) {
                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
                        trainMap.put("heuresam", sam.getHeureFichier());
                        trainMap.put("datesam", sam.getDateFichier());
                        trainMap.put("NbEssieux", sam.getNbEssieux());
                        trainMap.put("urlSam", sam.getUrlSam());
                        trainMap.put("statutSAM", sam.getStatutSAM());
                        trainMap.put("NbOccultations", sam.getNbOccultations());

                        foundSam = true;
                        break;
                    }
                }

                boolean found50592 = false;
                for (M_50592 m50592 : m50592s) {
                    if (isBetweenTime(train.getHeureFichier(), m50592.getHeureFichier(),m50592.getHeureFichier()) &&
                            train.getDateFichier().equals(m50592.getDateFichier())) {
                        trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                        trainMap.put("heure50592", m50592.getHeureFichier());
                        trainMap.put("date50592", m50592.getDateFichier());
                        trainMap.put("statut50592", m50592.getStatut50592());
                        trainMap.put("url50592", m50592.getUrl50592());
                        trainMap.put("ber1", m50592.getBeR1());
                        trainMap.put("ber2", m50592.getBeR2());
                        trainMap.put("blr1", m50592.getBlR1());
                        trainMap.put("blr2", m50592.getBlR2());

                        // Code commun pour les deux objets
                        Properties prop = new Properties();
                        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                        prop.load(input);

                        String outputFolderPath = prop.getProperty("output.folder.path");
                        String fileName = m50592.getFileName();
                        // Extraire l'année et le mois du nom de fichier
                        String[] parts = fileName.split("_");
                        String[] dateParts = parts[1].split("\\.");
                        String year = dateParts[0];
                        String month = dateParts[1];

                        // Construire le chemin complet du dossier correspondant à l'année et au mois
                        String folderPath = outputFolderPath + File.separator + year + "-" + month;

                        // Créer un objet File représentant le dossier correspondant à l'année et au mois
                        File inputFile = new File(folderPath,m50592.getFileName());

                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                        JsonNode parametreBENode = rootNode.get("ParametresBE");
                        JsonNode parametreBLNode = rootNode.get("ParametresBL");
                        JsonNode outofband = rootNode.get("OutOfBand");
                        JsonNode fondoutofband = rootNode.get("OutOfBand_Fond");


                        List<Object> entetesbl = new ArrayList<>();
                        List<Object> frequencesbl = new ArrayList<>();
                        List<Object> entetesbe = new ArrayList<>();
                        List<Object> frequencesbe = new ArrayList<>();

                        for (int i = 0; i < parametreBLNode.size(); i++) {
                            JsonNode entete = parametreBLNode.get(i).get(0);
                            JsonNode frequence = parametreBLNode.get(i).get(1);
                            entetesbl.add(entete);
                            frequencesbl.add(frequence);
                        }

                        for (int i = 0; i < parametreBENode.size(); i++) {
                            JsonNode entete = parametreBENode.get(i).get(0);
                            JsonNode frequence = parametreBENode.get(i).get(1);
                            entetesbe.add(entete);
                            frequencesbe.add(frequence);
                        }



                        trainMap.put("entetesbl", entetesbl);
                        trainMap.put("frequencebl", frequencesbl);
                        trainMap.put("entetesbe", entetesbe);
                        trainMap.put("frequencebe", frequencesbe);

                        trainMap.put("outofband", outofband);
                        trainMap.put("fondhorsbande", fondoutofband);
                        found50592 = true;
                        break;
                    }
                }

                if (!foundSam) {
                    trainMap.put("vitesse_moy", null);
                    trainMap.put("NbEssieux", null);
                    trainMap.put("urlSam", null);
                    trainMap.put("statutSAM", null);
                    trainMap.put("NbOccultations", null);
                    trainMap.put("tempsMs", null);
                    trainMap.put("heuresam", null);
                    trainMap.put("datesam", null);
                }

                if (!found50592) {
                    trainMap.put("meteo", null);
                    trainMap.put("statut50592", null);
                    trainMap.put("url50592", null);
                    trainMap.put("BE_R1", null);
                    trainMap.put("BE_R2", null);
                    trainMap.put("BL_R1", null);
                    trainMap.put("BL_R2", null);
                    trainMap.put("heure50592", null);
                    trainMap.put("date50592", null);
                }
            }
            processedDates.add(dateKey);
            processedTimes.add(timeKey);
            result.add(trainMap);

        }




// Traiter les cas où sam n'est pas égal à train
        for (Sam sam : sams) {

            Date dateKey = sam.getDateFichier();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sam.getHeureFichier());

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            String timeKey = hour + ":" + minute; // Crée une clé de temps au format "heure:minute"

            if (processedDates.contains(dateKey) && processedTimes.contains(timeKey)) {
                continue; // Ignorer si la même heure et minute ont déjà été traitées
            }
            Map<String, Object> samTrainMap = new HashMap<>();
            samTrainMap.put("vitesse_moy", sam.getVitesse_moy());
            samTrainMap.put("heuresam", sam.getHeureFichier());
            samTrainMap.put("NbEssieux", sam.getNbEssieux());
            samTrainMap.put("urlSam", sam.getUrlSam());
            samTrainMap.put("statutSAM", sam.getStatutSAM());
            samTrainMap.put("NbOccultations", sam.getNbOccultations());
            samTrainMap.put("datesam", sam.getDateFichier());
            boolean foundTrain = false;
            boolean found50592 = false;
            for (Train train : trains) {
                if (isBetweenTime(train.getHeureFichier(), sam.getHeureFichier(),train.getHeureFichier()) &&
                        train.getDateFichier().equals(sam.getDateFichier())) {
                    foundTrain = true;

                }else{
                    samTrainMap.put("numTrain", null);
                    samTrainMap.put("dateFichier", null);
                    samTrainMap.put("heureFichier", null);
                    samTrainMap.put("imagemini", null);
                    samTrainMap.put("site", site);

                }
            }


            for (M_50592 m50592 : m50592s) {
                if (sam.getHeureFichier().equals(m50592.getHeureFichier()) && sam.getDateFichier().equals(m50592.getDateFichier())) {
                    samTrainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                    samTrainMap.put("heure50592", m50592.getHeureFichier());
                    samTrainMap.put("date50592", m50592.getDateFichier());
                    samTrainMap.put("statut50592", m50592.getStatut50592());
                    samTrainMap.put("url50592", m50592.getUrl50592());
                    samTrainMap.put("ber1", m50592.getBeR1());
                    samTrainMap.put("ber2", m50592.getBeR2());
                    samTrainMap.put("blr1", m50592.getBlR1());
                    samTrainMap.put("blr2", m50592.getBlR2());

                    // Code commun pour les deux objets
                    Properties prop = new Properties();
                    InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                    prop.load(input);

                    String outputFolderPath = prop.getProperty("output.folder.path");
                    String fileName = m50592.getFileName();
                    // Extraire l'année et le mois du nom de fichier
                    String[] parts = fileName.split("_");
                    String[] dateParts = parts[1].split("\\.");
                    String year = dateParts[0];
                    String month = dateParts[1];

                    // Construire le chemin complet du dossier correspondant à l'année et au mois
                    String folderPath = outputFolderPath + File.separator + year + "-" + month;

                    // Créer un objet File représentant le dossier correspondant à l'année et au mois
                    File inputFile = new File(folderPath,m50592.getFileName());

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                    JsonNode parametreBENode = rootNode.get("ParametresBE");
                    JsonNode parametreBLNode = rootNode.get("ParametresBL");
                    JsonNode outofband = rootNode.get("OutOfBand");

                    JsonNode fondoutofband = rootNode.get("OutOfBand_Fond");


                    List<Object> entetesbl = new ArrayList<>();
                    List<Object> frequencesbl = new ArrayList<>();
                    List<Object> entetesbe = new ArrayList<>();
                    List<Object> frequencesbe = new ArrayList<>();

                    for (int i = 0; i < parametreBLNode.size(); i++) {
                        JsonNode entete = parametreBLNode.get(i).get(0);
                        JsonNode frequence = parametreBLNode.get(i).get(1);
                        entetesbl.add(entete);
                        frequencesbl.add(frequence);
                    }

                    for (int i = 0; i < parametreBENode.size(); i++) {
                        JsonNode entete = parametreBENode.get(i).get(0);
                        JsonNode frequence = parametreBENode.get(i).get(1);
                        entetesbe.add(entete);
                        frequencesbe.add(frequence);
                    }



                    samTrainMap.put("entetesbl", entetesbl);
                    samTrainMap.put("frequencebl", frequencesbl);
                    samTrainMap.put("entetesbe", entetesbe);
                    samTrainMap.put("frequencebe", frequencesbe);
                    samTrainMap.put("outofband", outofband);
                    samTrainMap.put("fondhorsbande", fondoutofband);


                    found50592 = true;

                }
            }



            if (!foundTrain) {

                samTrainMap.put("numTrain", null);
                samTrainMap.put("dateFichier", null);
                samTrainMap.put("heureFichier", null);
                samTrainMap.put("imagemini", null);
                samTrainMap.put("site", site);


            }


            if (!found50592) {

                samTrainMap.put("meteo", null);
                samTrainMap.put("statut50592", null);
                samTrainMap.put("url50592", null);
                samTrainMap.put("BE_R1", null);
                samTrainMap.put("BE_R2", null);
                samTrainMap.put("BL_R1", null);
                samTrainMap.put("BL_R2", null);
                samTrainMap.put("heure50592", null);
                samTrainMap.put("date50592", null);


            }

            if (foundTrain && found50592) {
                continue; // Ignorer si sam, train et 50592 sont égaux
            }
            processedDates.add(dateKey);
            processedTimes.add(timeKey);
            result.add(samTrainMap);

        }




        // Traiter les cas où 50592 n'est pas égal à train
        for (M_50592 m50592 : m50592s) {
            Map<String, Object> samTrainMap = new HashMap<>();
            Date dateKey = m50592.getDateFichier();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(m50592.getHeureFichier());

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            String timeKey = hour + ":" + minute; // Crée une clé de temps au format "heure:minute"

            if (processedDates.contains(dateKey) && processedTimes.contains(timeKey)) {
                continue; // Ignorer si la même heure et minute ont déjà été traitées
            }


            samTrainMap.put("meteo", m50592.getEnvironnement().getMeteo());
            samTrainMap.put("heure50592", m50592.getHeureFichier());
            samTrainMap.put("date50592", m50592.getDateFichier());
            samTrainMap.put("statut50592", m50592.getStatut50592());
            samTrainMap.put("url50592", m50592.getUrl50592());
            samTrainMap.put("ber1", m50592.getBeR1());
            samTrainMap.put("ber2", m50592.getBeR2());
            samTrainMap.put("blr1", m50592.getBlR1());
            samTrainMap.put("blr2", m50592.getBlR2());

            // Code commun pour les deux objets
            Properties prop = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
            prop.load(input);

            String outputFolderPath = prop.getProperty("output.folder.path");
            String fileName = m50592.getFileName();
            // Extraire l'année et le mois du nom de fichier
            String[] parts = fileName.split("_");
            String[] dateParts = parts[1].split("\\.");
            String year = dateParts[0];
            String month = dateParts[1];

            // Construire le chemin complet du dossier correspondant à l'année et au mois
            String folderPath = outputFolderPath + File.separator + year + "-" + month;

            // Créer un objet File représentant le dossier correspondant à l'année et au mois
            File inputFile = new File(folderPath,m50592.getFileName());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
            JsonNode parametreBENode = rootNode.get("ParametresBE");
            JsonNode parametreBLNode = rootNode.get("ParametresBL");
            JsonNode outofband = rootNode.get("OutOfBand");
            JsonNode fondoutofband = rootNode.get("OutOfBand_Fond");


            List<Object> entetesbl = new ArrayList<>();
            List<Object> frequencesbl = new ArrayList<>();
            List<Object> entetesbe = new ArrayList<>();
            List<Object> frequencesbe = new ArrayList<>();

            for (int i = 0; i < parametreBLNode.size(); i++) {
                JsonNode entete = parametreBLNode.get(i).get(0);
                JsonNode frequence = parametreBLNode.get(i).get(1);
                entetesbl.add(entete);
                frequencesbl.add(frequence);
            }

            for (int i = 0; i < parametreBENode.size(); i++) {
                JsonNode entete = parametreBENode.get(i).get(0);
                JsonNode frequence = parametreBENode.get(i).get(1);
                entetesbe.add(entete);
                frequencesbe.add(frequence);
            }



            samTrainMap.put("entetesbl", entetesbl);
            samTrainMap.put("frequencebl", frequencesbl);
            samTrainMap.put("entetesbe", entetesbe);
            samTrainMap.put("frequencebe", frequencesbe);
            samTrainMap.put("outofband", outofband);
            samTrainMap.put("fondhorsbande", fondoutofband);

            boolean foundTrain = false;
            boolean foundsam =false;
            for (Train train : trains) {
                if (isBetweenTime(train.getHeureFichier(), m50592.getHeureFichier(),m50592.getHeureFichier()) &&
                        train.getDateFichier().equals(m50592.getDateFichier())) {
                    foundTrain =true;

                }else{
                    samTrainMap.put("numTrain", null);
                    samTrainMap.put("dateFichier", null);
                    samTrainMap.put("heureFichier", null);
                    samTrainMap.put("imagemini", null);
                    samTrainMap.put("site", site);
                }
            }

            for (Sam sam : sams) {
                if (m50592.getHeureFichier().equals(sam.getHeureFichier() )&& m50592.getDateFichier().equals(sam.getDateFichier())) {
                    foundsam = true;


                }else{
                    samTrainMap.put("vitesse_moy", null);
                    samTrainMap.put("NbEssieux", null);
                    samTrainMap.put("urlSam", null);
                    samTrainMap.put("statutSAM", null);
                    samTrainMap.put("NbOccultations", null);
                    samTrainMap.put("tempsMs", null);
                    samTrainMap.put("heuresam", null);
                    samTrainMap.put("datesam", null);
                }
            }
            if (foundTrain && foundsam) {
                continue; // Ignorer si sam, train et 50592 sont égaux
            }
            if(foundTrain && !foundTrain){
                continue;
            }

            if (!foundsam) {
                samTrainMap.put("vitesse_moy", null);
                samTrainMap.put("NbEssieux", null);
                samTrainMap.put("urlSam", null);
                samTrainMap.put("statutSAM", null);
                samTrainMap.put("NbOccultations", null);
                samTrainMap.put("tempsMs", null);
                samTrainMap.put("heuresam", null);
                samTrainMap.put("datesam", null);


            }

            if (!foundTrain) {

                samTrainMap.put("numTrain", null);
                samTrainMap.put("dateFichier", null);
                samTrainMap.put("heureFichier", null);
                samTrainMap.put("imagemini", null);
                samTrainMap.put("site", site);


            }






            processedDates.add(dateKey);
            processedDates.add(dateKey);
            processedTimes.add(timeKey);
            result.add(samTrainMap);

        }




        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }








    //Api pour la partie historique
    /**
     * Récupère les informations des trains & SAM005 & 50592 pour un site donné et une période spécifiées.
     * @param "site" Le nom du site.
     * @param "startDateFichier" La date spécifiée (au format ISO DATE, ex : 2023-06-06).
     * @param "FinDateFichier" La date spécifiée (au format ISO DATE, ex : 2023-07-06).
     * @return Une réponse contenant une liste de maps représentant les infos de train & SAM005 & 50592.
     * @throws IOException En cas d'erreur lors de la manipulation des fichiers ou des propriétés.
     */
    @GetMapping("/dataBetween")
public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierBetween(
        @RequestParam("site") String site,
        @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
) throws Exception{


    Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichierBetween(site, start, end);
        List<Sam> sams = samRepository.findBySiteAndDateFichierBetween(site, start, end);

        List<Map<String, Object>> result = new ArrayList<>();
        Set<Date> processedDates = new HashSet<>(); // Stocke les dates déjà traitées
        Set<String> processedTimes = new HashSet<>(); // Stocke les heures et minutes déjà traitées

// le cas où train & SAM005 & 50592 sont présents
        for (Train train : trains) {

            Map<String, Object> trainMap = new HashMap<>();

            Date dateKey = train.getDateFichier();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(train.getHeureFichier());

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            String timeKey = hour + ":" + minute; // Crée une clé de temps au format "heure:minute"

            if (processedDates.contains(dateKey) && processedTimes.contains(timeKey)) {
                continue; // Ignorer si la même heure et minute ont déjà été traitées
            }


            for (Result results : train.getResults()) {
                trainMap.put("numTrain", results.getEngine());
                trainMap.put("dateFichier", train.getDateFichier());
                trainMap.put("heureFichier", train.getHeureFichier());
                trainMap.put("imagemini", results.getThumbnail());
                trainMap.put("site", site);
                Mr mr = mrRepository.findByNumTrain(results.getEngine());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                }

                boolean foundSam = false;
                for (Sam sam : sams) {
                    if (isBetweenTime(train.getHeureFichier(), sam.getHeureFichier(),sam.getHeureFichier()) &&
                            train.getDateFichier().equals(sam.getDateFichier())) {
                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
                        trainMap.put("heuresam", sam.getHeureFichier());
                        trainMap.put("datesam", sam.getDateFichier());
                        trainMap.put("NbEssieux", sam.getNbEssieux());
                        trainMap.put("urlSam", sam.getUrlSam());
                        trainMap.put("statutSAM", sam.getStatutSAM());
                        trainMap.put("NbOccultations",sam.getNbOccultations());


                        foundSam = true;
                        break;
                    }
                }

                boolean found50592 = false;
                for (M_50592 m50592 : m50592s) {
                    if (isBetweenTime(train.getHeureFichier(), m50592.getHeureFichier(),m50592.getHeureFichier()) &&
                            train.getDateFichier().equals(m50592.getDateFichier())) {
                        trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                        trainMap.put("heure50592", m50592.getHeureFichier());
                        trainMap.put("date50592", m50592.getDateFichier());
                        trainMap.put("statut50592", m50592.getStatut50592());
                        trainMap.put("url50592", m50592.getUrl50592());
                        trainMap.put("ber1", m50592.getBeR1());
                        trainMap.put("ber2", m50592.getBeR2());
                        trainMap.put("blr1", m50592.getBlR1());
                        trainMap.put("blr2", m50592.getBlR2());

                        // Code commun pour les deux objets
                        Properties prop = new Properties();
                        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                        prop.load(input);

                        String outputFolderPath = prop.getProperty("output.folder.path");
                        String fileName = m50592.getFileName();
                        // Extraire l'année et le mois du nom de fichier
                        String[] parts = fileName.split("_");
                        String[] dateParts = parts[1].split("\\.");
                        String year = dateParts[0];
                        String month = dateParts[1];

                        // Construire le chemin complet du dossier correspondant à l'année et au mois
                        String folderPath = outputFolderPath + File.separator + year + "-" + month;

                        // Créer un objet File représentant le dossier correspondant à l'année et au mois
                        File inputFile = new File(folderPath,m50592.getFileName());

                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                        JsonNode parametreBENode = rootNode.get("ParametresBE");
                        JsonNode parametreBLNode = rootNode.get("ParametresBL");
                        JsonNode outofband = rootNode.get("OutOfBand");
                        JsonNode fondoutofband = rootNode.get("OutOfBand_Fond");

                        List<Object> enteteshb = new ArrayList<>();
                        List<Object> entetesbl = new ArrayList<>();
                        List<Object> frequencesbl = new ArrayList<>();
                        List<Object> entetesbe = new ArrayList<>();
                        List<Object> frequencesbe = new ArrayList<>();

                        for (int i = 0; i < parametreBLNode.size(); i++) {
                            JsonNode entete = parametreBLNode.get(i).get(0);
                            JsonNode frequence = parametreBLNode.get(i).get(1);
                            entetesbl.add(entete);
                            frequencesbl.add(frequence);
                        }

                        for (int i = 0; i < parametreBENode.size(); i++) {
                            JsonNode entete = parametreBENode.get(i).get(0);
                            JsonNode frequence = parametreBENode.get(i).get(1);
                            entetesbe.add(entete);
                            frequencesbe.add(frequence);
                        }



                        trainMap.put("entetesbl", entetesbl);
                        trainMap.put("frequencebl", frequencesbl);
                        trainMap.put("entetesbe", entetesbe);
                        trainMap.put("frequencebe", frequencesbe);
                        trainMap.put("outofband", outofband);
                        trainMap.put("fondhorsbande", fondoutofband);
                        found50592 = true;
                        break;
                    }
                }

                if (!foundSam) {
                    trainMap.put("vitesse_moy", null);
                    trainMap.put("NbEssieux", null);
                    trainMap.put("urlSam", null);
                    trainMap.put("statutSAM", null);
                    trainMap.put("NbOccultations", null);
                    trainMap.put("tempsMs", null);
                    trainMap.put("heuresam", null);
                    trainMap.put("datesam", null);
                }

                if (!found50592) {
                    trainMap.put("meteo", null);
                    trainMap.put("statut50592", null);
                    trainMap.put("url50592", null);
                    trainMap.put("BE_R1", null);
                    trainMap.put("BE_R2", null);
                    trainMap.put("BL_R1", null);
                    trainMap.put("BL_R2", null);
                    trainMap.put("heure50592", null);
                    trainMap.put("date50592", null);
                }
            }
            processedDates.add(dateKey);
            processedTimes.add(timeKey);
                result.add(trainMap);

        }



// Traiter les cas où sam n'est pas égal à train
        for (Sam sam : sams) {

            Date dateKey = sam.getDateFichier();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sam.getHeureFichier());

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            String timeKey = hour + ":" + minute; // Crée une clé de temps au format "heure:minute"

            if (processedDates.contains(dateKey) && processedTimes.contains(timeKey)) {
                continue; // Ignorer si la même heure et minute ont déjà été traitées
            }
            Map<String, Object> samTrainMap = new HashMap<>();
            samTrainMap.put("vitesse_moy", sam.getVitesse_moy());
            samTrainMap.put("heuresam", sam.getHeureFichier());
            samTrainMap.put("NbEssieux", sam.getNbEssieux());
            samTrainMap.put("urlSam", sam.getUrlSam());
            samTrainMap.put("statutSAM", sam.getStatutSAM());
            samTrainMap.put("NbOccultations", sam.getNbOccultations());
            samTrainMap.put("datesam", sam.getDateFichier());
            boolean foundTrain = false;
            boolean found50592 = false;
            for (Train train : trains) {
                if (isBetweenTime(train.getHeureFichier(), sam.getHeureFichier(),train.getHeureFichier()) &&
                        train.getDateFichier().equals(sam.getDateFichier())) {
                    foundTrain = true;

                }else{
                    samTrainMap.put("numTrain", null);
                    samTrainMap.put("dateFichier", null);
                    samTrainMap.put("heureFichier", null);
                    samTrainMap.put("imagemini", null);
                    samTrainMap.put("site", site);

                }
            }


            for (M_50592 m50592 : m50592s) {
                if (sam.getHeureFichier().equals(m50592.getHeureFichier()) && sam.getDateFichier().equals(m50592.getDateFichier())) {
                    samTrainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                    samTrainMap.put("heure50592", m50592.getHeureFichier());
                    samTrainMap.put("date50592", m50592.getDateFichier());
                    samTrainMap.put("statut50592", m50592.getStatut50592());
                    samTrainMap.put("url50592", m50592.getUrl50592());
                    samTrainMap.put("ber1", m50592.getBeR1());
                    samTrainMap.put("ber2", m50592.getBeR2());
                    samTrainMap.put("blr1", m50592.getBlR1());
                    samTrainMap.put("blr2", m50592.getBlR2());

                    // Code commun pour les deux objets
                    Properties prop = new Properties();
                    InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                    prop.load(input);

                    String outputFolderPath = prop.getProperty("output.folder.path");
                    String fileName = m50592.getFileName();
                    // Extraire l'année et le mois du nom de fichier
                    String[] parts = fileName.split("_");
                    String[] dateParts = parts[1].split("\\.");
                    String year = dateParts[0];
                    String month = dateParts[1];

                    // Construire le chemin complet du dossier correspondant à l'année et au mois
                    String folderPath = outputFolderPath + File.separator + year + "-" + month;

                    // Créer un objet File représentant le dossier correspondant à l'année et au mois
                    File inputFile = new File(folderPath,m50592.getFileName());

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                    JsonNode parametreBENode = rootNode.get("ParametresBE");
                    JsonNode parametreBLNode = rootNode.get("ParametresBL");
                    JsonNode outofband = rootNode.get("OutOfBand");

                    JsonNode fondoutofband = rootNode.get("OutOfBand_Fond");


                    List<Object> entetesbl = new ArrayList<>();
                    List<Object> frequencesbl = new ArrayList<>();
                    List<Object> entetesbe = new ArrayList<>();
                    List<Object> frequencesbe = new ArrayList<>();

                    for (int i = 0; i < parametreBLNode.size(); i++) {
                        JsonNode entete = parametreBLNode.get(i).get(0);
                        JsonNode frequence = parametreBLNode.get(i).get(1);
                        entetesbl.add(entete);
                        frequencesbl.add(frequence);
                    }

                    for (int i = 0; i < parametreBENode.size(); i++) {
                        JsonNode entete = parametreBENode.get(i).get(0);
                        JsonNode frequence = parametreBENode.get(i).get(1);
                        entetesbe.add(entete);
                        frequencesbe.add(frequence);
                    }



                    samTrainMap.put("entetesbl", entetesbl);
                    samTrainMap.put("frequencebl", frequencesbl);
                    samTrainMap.put("entetesbe", entetesbe);
                    samTrainMap.put("frequencebe", frequencesbe);
                    samTrainMap.put("outofband", outofband);
                    samTrainMap.put("fondhorsbande", fondoutofband);


                    found50592 = true;

                }
            }



            if (!foundTrain) {

                samTrainMap.put("numTrain", null);
                samTrainMap.put("dateFichier", null);
                samTrainMap.put("heureFichier", null);
                samTrainMap.put("imagemini", null);
                samTrainMap.put("site", site);


            }


            if (!found50592) {

                samTrainMap.put("meteo", null);
                samTrainMap.put("statut50592", null);
                samTrainMap.put("url50592", null);
                samTrainMap.put("BE_R1", null);
                samTrainMap.put("BE_R2", null);
                samTrainMap.put("BL_R1", null);
                samTrainMap.put("BL_R2", null);
                samTrainMap.put("heure50592", null);
                samTrainMap.put("date50592", null);


            }

            if (foundTrain && found50592) {
                continue; // Ignorer si sam, train et 50592 sont égaux
            }
            processedDates.add(dateKey);
            processedTimes.add(timeKey);
            result.add(samTrainMap);

        }




        // Traiter les cas où 50592 n'est pas égal à train
            for (M_50592 m50592 : m50592s) {
            Map<String, Object> samTrainMap = new HashMap<>();
                Date dateKey = m50592.getDateFichier();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(m50592.getHeureFichier());

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                String timeKey = hour + ":" + minute; // Crée une clé de temps au format "heure:minute"

                if (processedDates.contains(dateKey) && processedTimes.contains(timeKey)) {
                    continue; // Ignorer si la même heure et minute ont déjà été traitées
                }


                samTrainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                samTrainMap.put("heure50592", m50592.getHeureFichier());
                samTrainMap.put("date50592", m50592.getDateFichier());
                samTrainMap.put("statut50592", m50592.getStatut50592());
                samTrainMap.put("url50592", m50592.getUrl50592());
                samTrainMap.put("ber1", m50592.getBeR1());
                samTrainMap.put("ber2", m50592.getBeR2());
                samTrainMap.put("blr1", m50592.getBlR1());
                samTrainMap.put("blr2", m50592.getBlR2());

                // Code commun pour les deux objets
                Properties prop = new Properties();
                InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                prop.load(input);

                String outputFolderPath = prop.getProperty("output.folder.path");
                String fileName = m50592.getFileName();
                // Extraire l'année et le mois du nom de fichier
                String[] parts = fileName.split("_");
                String[] dateParts = parts[1].split("\\.");
                String year = dateParts[0];
                String month = dateParts[1];

                // Construire le chemin complet du dossier correspondant à l'année et au mois
                String folderPath = outputFolderPath + File.separator + year + "-" + month;

                // Créer un objet File représentant le dossier correspondant à l'année et au mois
                File inputFile = new File(folderPath,m50592.getFileName());

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                JsonNode parametreBENode = rootNode.get("ParametresBE");
                JsonNode parametreBLNode = rootNode.get("ParametresBL");
                JsonNode outofband = rootNode.get("OutOfBand");
                JsonNode fondoutofband = rootNode.get("OutOfBand_Fond");


                List<Object> entetesbl = new ArrayList<>();
                List<Object> frequencesbl = new ArrayList<>();
                List<Object> entetesbe = new ArrayList<>();
                List<Object> frequencesbe = new ArrayList<>();

                for (int i = 0; i < parametreBLNode.size(); i++) {
                    JsonNode entete = parametreBLNode.get(i).get(0);
                    JsonNode frequence = parametreBLNode.get(i).get(1);
                    entetesbl.add(entete);
                    frequencesbl.add(frequence);
                }

                for (int i = 0; i < parametreBENode.size(); i++) {
                    JsonNode entete = parametreBENode.get(i).get(0);
                    JsonNode frequence = parametreBENode.get(i).get(1);
                    entetesbe.add(entete);
                    frequencesbe.add(frequence);
                }



                samTrainMap.put("entetesbl", entetesbl);
                samTrainMap.put("frequencebl", frequencesbl);
                samTrainMap.put("entetesbe", entetesbe);
                samTrainMap.put("frequencebe", frequencesbe);
                samTrainMap.put("outofband", outofband);
                samTrainMap.put("fondhorsbande", fondoutofband);

                boolean foundTrain = false;
                boolean foundsam =false;
                for (Train train : trains) {
                    if (isBetweenTime(train.getHeureFichier(), m50592.getHeureFichier(),m50592.getHeureFichier()) &&
                            train.getDateFichier().equals(m50592.getDateFichier())) {
foundTrain =true;

                    }else{
                        samTrainMap.put("numTrain", null);
                        samTrainMap.put("dateFichier", null);
                        samTrainMap.put("heureFichier", null);
                        samTrainMap.put("imagemini", null);
                        samTrainMap.put("site", site);
                    }
                }

                for (Sam sam : sams) {
                    if (m50592.getHeureFichier().equals(sam.getHeureFichier() )&& m50592.getDateFichier().equals(sam.getDateFichier())) {
                        foundsam = true;


                    }else{
                        samTrainMap.put("vitesse_moy", null);
                        samTrainMap.put("NbEssieux", null);
                        samTrainMap.put("urlSam", null);
                        samTrainMap.put("statutSAM", null);
                        samTrainMap.put("NbOccultations", null);
                        samTrainMap.put("tempsMs", null);
                        samTrainMap.put("heuresam", null);
                        samTrainMap.put("datesam", null);
                    }
                }
                if (foundTrain && foundsam) {
                    continue; // Ignorer si sam, train et 50592 sont égaux
                }
                if(foundTrain && !foundTrain){
                    continue;
                }

                    if (!foundsam) {
                    samTrainMap.put("vitesse_moy", null);
                    samTrainMap.put("NbEssieux", null);
                    samTrainMap.put("urlSam", null);
                    samTrainMap.put("statutSAM", null);
                    samTrainMap.put("NbOccultations", null);
                    samTrainMap.put("tempsMs", null);
                    samTrainMap.put("heuresam", null);
                    samTrainMap.put("datesam", null);


            }

                if (!foundTrain) {

                    samTrainMap.put("numTrain", null);
                    samTrainMap.put("dateFichier", null);
                    samTrainMap.put("heureFichier", null);
                    samTrainMap.put("imagemini", null);
                    samTrainMap.put("site", site);


                }






                processedDates.add(dateKey);
                processedDates.add(dateKey);
                processedTimes.add(timeKey);
            result.add(samTrainMap);

        }




        if (result.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(result);
}

    // Méthode pour calculer le dépassement en fonction des limites
    private double getDepassement(List<Double> limites) {
        double depassement = 0.0;

        for (Double limite : limites) {
            if (limite >= 0 && limite < 1.5) {
                depassement = 0.0;
            } else if (limite >= 1.5 && limite < 3) {
                depassement = 1.5;
            } else if (limite >= 3 && limite < 6) {
                depassement = 3.0;
            } else if (limite >= 6 && limite < 9) {
                depassement = 6.0;
            } else if (limite >= 9 && limite < 25) {
                depassement = 9.0;
            } else if (limite >= 25) {
                depassement = 25.0;
            }
        }

        return depassement;
    }


//Api pour la partie rapoort automatique
    @GetMapping("/dataBetweenRapport")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierBetweenRapport(
            @RequestParam("site") String site,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws IOException {

        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichierBetweenAndStatut50592(site, start, end, "NOK");

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);




        List<Map<String, Object>> result = new ArrayList<>();






        JsonNode parametreBENode =null;
        List<String> Trains50592nok = new ArrayList<>();
        Map<String, Object> trainMap = new HashMap<>();
        for (Train train : trains) {




            for (Result result1 : train.getResults()) {

                Trains50592nok.add(result1.getEngine());

                Map<String, Map<String, Double>> entetesLimitesMap = new HashMap<>();


            for (M_50592 m50592 : m50592s) {
                if (isBetweenTime(train.getHeureFichier(), m50592.getHeureFichier(),m50592.getHeureFichier())
                        && train.getDateFichier().equals(m50592.getDateFichier())) {

                    Properties prop = new Properties();
                    InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                    prop.load(input);

                    String outputFolderPath = prop.getProperty("output.folder.path");

                    String fileName = m50592.getFileName();
                    // Extraire l'année et le mois du nom de fichier
                    String[] parts = fileName.split("_");
                    String[] dateParts = parts[1].split("\\.");
                    String year = dateParts[0];
                    String month = dateParts[1];

                    // Construire le chemin complet du dossier correspondant à l'année et au mois
                    String folderPath = outputFolderPath + File.separator + year + "-" + month;

                    // Créer un objet File représentant le dossier correspondant à l'année et au mois
                    File inputFile = new File(folderPath,m50592.getFileName());
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                    // read from input file
                     parametreBENode = rootNode.get("ParametresBE");

                    if (!m50592.getStatut50592().equals("OK")) {


                        // Parcourir les paramètres BE
                        // Avant la boucle for qui parcourt les paramètres BE

                        String enteteValue = "";

                        for (int i = 0; i < parametreBENode.size(); i++) {
                            JsonNode entete = parametreBENode.get(i).get(0);
                            enteteValue = entete.asText();




                            //la Limite X
                            JsonNode X = parametreBENode.get(i).get(4);
                            Double XValue = (X != null) ? X.asDouble() : null;

                            //la Limite Y
                            JsonNode Y = parametreBENode.get(i).get(5);
                            Double YValue = (Y != null) ? Y.asDouble() : null;

                            //la Limite Z
                            JsonNode Z = parametreBENode.get(i).get(6);
                            Double ZValue = (Z != null) ? Z.asDouble() : null;

                            List<Double> limitesX = new ArrayList<>();
                            List<Double> limitesY = new ArrayList<>();
                            List<Double> limitesZ = new ArrayList<>();

                            // Add the null check for XValue here
                            if (XValue != null && YValue != null && ZValue != null) {
                                Map<String, Integer> depassementMap = new HashMap<>();
                                for (int j = 0; j < m50592.getBeR1().getX().size(); j++) {

                                    Double valeurR1X = m50592.getBeR1().getX().get(j);
                                    Double valeurR1Y = m50592.getBeR1().getY().get(j);
                                    Double valeurR1Z = m50592.getBeR1().getZ().get(j);

                                    Double valeurR2X = m50592.getBeR2().getX1().get(j);
                                    Double valeurR2Y = m50592.getBeR2().getY1().get(j);
                                    Double valeurR2Z = m50592.getBeR2().getZ1().get(j);

                                    Double resultatR1X = valeurR1X - XValue;
                                    Double resultatR1Y = valeurR1Y - YValue;
                                    Double resultatR1Z = valeurR1Z - ZValue;

                                    Double resultatR2X = valeurR2X - XValue;
                                    Double resultatR2Y = valeurR2Y - YValue;
                                    Double resultatR2Z = valeurR2Z - ZValue;

                                    // Vérifier si le résultat est positif pour chaque axe
                                    if (resultatR1X > 0) {
                                        limitesX.add(resultatR1X);
                                    }
                                    if (resultatR1Y > 0) {
                                        limitesY.add(resultatR1Y);
                                    }
                                    if (resultatR1Z > 0) {
                                        limitesZ.add(resultatR1Z);
                                    }

                                    if (resultatR2X > 0) {
                                        limitesX.add(resultatR2X);
                                    }
                                    if (resultatR2Y > 0) {
                                        limitesY.add(resultatR2Y);
                                    }
                                    if (resultatR2Z > 0) {
                                        limitesZ.add(resultatR2Z);
                                    }



                                }


                                // Vérifier le dépassement pour chaque axe
                                double depassementX = getDepassement(limitesX);

                                double depassementY = getDepassement(limitesY);

                                double depassementZ = getDepassement(limitesZ);


                                // Vérifier si les dépassements sont vides pour chaque axe et définir une valeur par défaut
                                if (limitesX.isEmpty()) {
                                    depassementX = -0.0; // Ou la valeur que vous souhaitez afficher
                                }
                                if (limitesY.isEmpty()) {
                                    depassementY = -0.0; // Ou la valeur que vous souhaitez afficher
                                }
                                if (limitesZ.isEmpty()) {
                                    depassementZ = -0.0; // Ou la valeur que vous souhaitez afficher
                                }


                                // Ajouter les limites pour chaque axe à l'entête correspondante dans la map
                                Map<String, Double> axesDepassementMap = new HashMap<>();
                                axesDepassementMap.put("X", depassementX);
                                axesDepassementMap.put("Y", depassementY);
                                axesDepassementMap.put("Z", depassementZ);

                                entetesLimitesMap.put(enteteValue, axesDepassementMap);

                            }




                        }

                    }

                }

            }

// quand le depassement == 6.0
                Map<String, Object> occurrencesMap = new HashMap<>();
                Map<String, Object> occurrencesMap00 = new HashMap<>();
                Map<String, Object> occurrencesMap0 = new HashMap<>();
                Map<String, Object> occurrencesMap3 = new HashMap<>();
                Map<String, Object> occurrencesMap9 = new HashMap<>();
                Map<String, Object> occurrencesMap1 = new HashMap<>();
                Map<String, Object> occurrencesMap25 = new HashMap<>();
                if (!entetesLimitesMap.isEmpty()) {


                    for (Map.Entry<String, Map<String, Double>> entry : entetesLimitesMap.entrySet()) {

                        Map<String, Double> valeurs = entry.getValue();




                        if (valeurs.containsKey("X") && valeurs.get("X").equals(6.0)) {
                            String entete = entry.getKey();
                            int index = (int) occurrencesMap.getOrDefault(entete, 0);
                            index++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index", index);
                            occurrencesMap.put(entete, enteteObject);
                        }
                        if (valeurs.containsKey("X") && valeurs.get("X").equals(0.0)) {
                            String entete = entry.getKey();
                            int index0 = (int) occurrencesMap0.getOrDefault(entete, 0);
                            index0++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 0.0", index0);
                            occurrencesMap0.put(entete, enteteObject);
                        }
                        if (valeurs.containsKey("X") && valeurs.get("X").equals(1.5)) {
                            String entete = entry.getKey();
                            int index1 = (int) occurrencesMap1.getOrDefault(entete, 0);
                            index1++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 1.5", index1);
                            occurrencesMap1.put(entete, enteteObject);
                        }
                        if (valeurs.containsKey("X") && valeurs.get("X").equals(3.0)) {
                            String entete = entry.getKey();
                            int index3 = (int) occurrencesMap3.getOrDefault(entete, 0);
                            index3++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 3.0", index3);
                            occurrencesMap3.put(entete, enteteObject);
                        }


                        if (valeurs.containsKey("X") && valeurs.get("X").equals(9.0)) {
                            String entete = entry.getKey();
                            int index9 = (int) occurrencesMap9.getOrDefault(entete, 0);
                            index9++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 9.0", index9);
                            occurrencesMap9.put(entete, enteteObject);
                        }
                        if (valeurs.containsKey("X") && valeurs.get("X").equals(25.0)) {
                            String entete = entry.getKey();
                            int index25 = (int) occurrencesMap25.getOrDefault(entete, 0);
                            index25++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 25.0", index25);
                            occurrencesMap25.put(entete, enteteObject);
                        }
// les dépassement dans Y


                        if (valeurs.containsKey("Y") && valeurs.get("Y").equals(6.0)) {
                            String entete = entry.getKey();
                            int index = (int) occurrencesMap.getOrDefault(entete, 0);
                            index++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index", index);
                            occurrencesMap.put(entete, enteteObject);
                        }

                        if (valeurs.containsKey("Y") && valeurs.get("Y").equals(0.0)) {
                            String entete = entry.getKey();
                            int index0 = (int) occurrencesMap0.getOrDefault(entete, 0);
                            index0++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 0.0", index0);
                            occurrencesMap0.put(entete, enteteObject);
                        }

                        if (valeurs.containsKey("Y") && valeurs.get("Y").equals(1.5)) {
                            String entete = entry.getKey();
                            int index1 = (int) occurrencesMap1.getOrDefault(entete, 0);
                            index1++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 1.5", index1);
                            occurrencesMap1.put(entete, enteteObject);
                        }
                        if (valeurs.containsKey("Y") && valeurs.get("Y").equals(3.0)) {
                            String entete = entry.getKey();
                            int index3 = (int) occurrencesMap3.getOrDefault(entete, 0);
                            index3++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 3.0", index3);
                            occurrencesMap3.put(entete, enteteObject);
                        }
//
//
                        if (valeurs.containsKey("Y") && valeurs.get("Y").equals(9.0)) {
                            String entete = entry.getKey();
                            int index9 = (int) occurrencesMap9.getOrDefault(entete, 0);
                            index9++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 9.0", index9);
                            occurrencesMap9.put(entete, enteteObject);
                        }
                        if (valeurs.containsKey("Y") && valeurs.get("Y").equals(25.0)) {
                            String entete = entry.getKey();
                            int index25 = (int) occurrencesMap25.getOrDefault(entete, 0);
                            index25++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 25.0", index25);
                            occurrencesMap25.put(entete, enteteObject);
                        }


                        if (valeurs.containsKey("Z") && valeurs.get("Z").equals(6.0)) {
                            String entete = entry.getKey();
                            int index = (int) occurrencesMap.getOrDefault(entete, 0);
                            index++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index", index);
                            occurrencesMap.put(entete, enteteObject);
                        }

                        if (valeurs.containsKey("Z") && valeurs.get("Z").equals(0.0)) {
                            String entete = entry.getKey();
                            int index0 = (int) occurrencesMap0.getOrDefault(entete, 0);
                            index0++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 0.0", index0);
                            occurrencesMap0.put(entete, enteteObject);
                        }
                        if (valeurs.containsKey("Z") && valeurs.get("Z").equals(1.5)) {
                            String entete = entry.getKey();
                            int index1 = (int) occurrencesMap1.getOrDefault(entete, 0);
                            index1++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 1.5", index1);
                            occurrencesMap1.put(entete, enteteObject);
                        }
                        if (valeurs.containsKey("Z") && valeurs.get("Z").equals(3.0)) {
                            String entete = entry.getKey();
                            int index3 = (int) occurrencesMap3.getOrDefault(entete, 0);
                            index3++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 3.0", index3);
                            occurrencesMap3.put(entete, enteteObject);
                        }


                        if (valeurs.containsKey("Z") && valeurs.get("Z").equals(9.0)) {
                            String entete = entry.getKey();
                            int index9 = (int) occurrencesMap9.getOrDefault(entete, 0);
                            index9++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 9.0", index9);
                            enteteObject.put("depassement 9.0", 9.0);
                            occurrencesMap9.put(entete, enteteObject);
                        }
                        if (valeurs.containsKey("Z") && valeurs.get("Z").equals(25.0)) {
                            String entete = entry.getKey();
                            int index25 = (int) occurrencesMap25.getOrDefault(entete, 0);
                            index25++;
                            Map<String, Object> enteteObject = new HashMap<>();
                            enteteObject.put("index 25.0", index25);
                            occurrencesMap25.put(entete, enteteObject);
                        }
                    }





                }

                if (!occurrencesMap00.isEmpty()) {
                    Map<String, Object> occurrencesEntetesMap = new HashMap<>();
                    occurrencesEntetesMap.put("occurrences_entetes -0.0", occurrencesMap00);
                    trainMap.put("entetes_depassement -0.0", occurrencesEntetesMap);

                }

                if (!occurrencesMap.isEmpty()) {
                    Map<String, Object> occurrencesEntetesMap = new HashMap<>();
                    occurrencesEntetesMap.put("occurrences_entetes 6.0", occurrencesMap);
                    trainMap.put("entetes_depassement 6.0", occurrencesEntetesMap);

                }
                if (!occurrencesMap0.isEmpty()) {
                    Map<String, Object> occurrencesEntetesMap = new HashMap<>();
                    occurrencesEntetesMap.put("occurrences_entetes 0.0", occurrencesMap0);
                    trainMap.put("entetes_depassement 0.0", occurrencesEntetesMap);

                }
                if (!occurrencesMap.isEmpty()) {
                    Map<String, Object> occurrencesEntetesMap = new HashMap<>();
                    occurrencesEntetesMap.put("occurrences_entetes 1.5", occurrencesMap1);
                    trainMap.put("entetes_depassement 1.5", occurrencesEntetesMap);

                }
                if (!occurrencesMap.isEmpty()) {
                    Map<String, Object> occurrencesEntetesMap = new HashMap<>();
                    occurrencesEntetesMap.put("occurrences_entetes 3.0", occurrencesMap3);
                    trainMap.put("entetes_depassement 3.0", occurrencesEntetesMap);

                }
                if (!occurrencesMap.isEmpty()) {
                    Map<String, Object> occurrencesEntetesMap = new HashMap<>();
                    occurrencesEntetesMap.put("occurrences_entetes 9.0", occurrencesMap9);
                    trainMap.put("entetes_depassement 9.0", occurrencesEntetesMap);

                }
                if (!occurrencesMap.isEmpty()) {
                    Map<String, Object> occurrencesEntetesMap = new HashMap<>();
                    occurrencesEntetesMap.put("occurrences_entetes 25.0", occurrencesMap25);
                    trainMap.put("entetes_depassement 25.0", occurrencesEntetesMap);

                }


        }

    }

        int count50592nok = Trains50592nok.size();
        trainMap.put("nombre de train passé est ", count50592nok);
        if (!trainMap.isEmpty()) {
            result.add(trainMap);
        }



        int totalNombreTrain = 0;
        Map<String, Integer> occurrencesEntetesTotales = new HashMap<>();
        Map<String, Integer> occurrencesEntetesTotales0 = new HashMap<>();
        Map<String, Integer> occurrencesEntetesTotales00 = new HashMap<>();
        Map<String, Integer> occurrencesEntetesTotales3 = new HashMap<>();
        Map<String, Integer> occurrencesEntetesTotales1 = new HashMap<>();
        Map<String, Integer> occurrencesEntetesTotales9 = new HashMap<>();
        Map<String, Integer> occurrencesEntetesTotales25 = new HashMap<>();
        Map<String, Object> trainMapp = new HashMap<>();

        for (Map<String, Object> t : result) {


            Map<String, Object> entetesDepassementMap00 = (Map<String, Object>) t.get("entetes_depassement -0.0");

            if (entetesDepassementMap00 != null) {
                Map<String, Object> occurrencesEntetesMap = (Map<String, Object>) entetesDepassementMap00.get("occurrences_entetes -0.0");

                if (occurrencesEntetesMap != null) {
                    for (Map.Entry<String, Object> entry : occurrencesEntetesMap.entrySet()) {
                        String entete = entry.getKey();
                        Object value = entry.getValue();

                        if (value instanceof Map) {
                            Map<String, Object> enteteObject = (Map<String, Object>) value;
                            occurrencesEntetesTotales00.put(entete, occurrencesEntetesTotales00.getOrDefault(entete, 0));
                        }
                    }

                }
            }



            Map<String, Object> entetesDepassementMap = (Map<String, Object>) t.get("entetes_depassement 6.0");

            if (entetesDepassementMap != null) {
                Map<String, Object> occurrencesEntetesMap = (Map<String, Object>) entetesDepassementMap.get("occurrences_entetes 6.0");

                if (occurrencesEntetesMap != null) {
                    for (Map.Entry<String, Object> entry : occurrencesEntetesMap.entrySet()) {
                        String entete = entry.getKey();
                        Object value = entry.getValue();

                        if (value instanceof Map) {
                            Map<String, Object> enteteObject = (Map<String, Object>) value;
                            int occurrences = (int) enteteObject.getOrDefault("index", 0);
                            occurrencesEntetesTotales.put(entete, occurrencesEntetesTotales.getOrDefault(entete, 0) + occurrences);
                        }
                    }

                }
            }


            // LA LIMITE EST 0.0
            Map<String, Object> entetesDepassementMap0 = (Map<String, Object>) t.get("entetes_depassement 0.0");

            if (entetesDepassementMap0 != null) {
                Map<String, Object> occurrencesEntetesMap = (Map<String, Object>) entetesDepassementMap0.get("occurrences_entetes 0.0");

                if (occurrencesEntetesMap != null) {
                    for (Map.Entry<String, Object> entry : occurrencesEntetesMap.entrySet()) {
                        String entete = entry.getKey();
                        Object value = entry.getValue();

                        if (value instanceof Map) {
                            Map<String, Object> enteteObject = (Map<String, Object>) value;
                            int occurrences = (int) enteteObject.getOrDefault("index 0.0", 0);
                            occurrencesEntetesTotales0.put(entete, occurrencesEntetesTotales0.getOrDefault(entete, 0) + occurrences);
                        }
                    }

                }
            }

            // LA LIMITE EST 3.0
            Map<String, Object> entetesDepassementMap3 = (Map<String, Object>) t.get("entetes_depassement 3.0");

            if (entetesDepassementMap3 != null) {
                Map<String, Object> occurrencesEntetesMap = (Map<String, Object>) entetesDepassementMap3.get("occurrences_entetes 3.0");

                if (occurrencesEntetesMap != null) {
                    for (Map.Entry<String, Object> entry : occurrencesEntetesMap.entrySet()) {
                        String entete = entry.getKey();
                        Object value = entry.getValue();
                        if (value instanceof Map) {
                            Map<String, Object> enteteObject = (Map<String, Object>) value;
                            int occurrences = (int) enteteObject.getOrDefault("index 3.0", 0);
                            occurrencesEntetesTotales3.put(entete, occurrencesEntetesTotales3.getOrDefault(entete, 0) + occurrences);
                        }
                    }

                }
            }


            // LA LIMITE EST 9.0
            Map<String, Object> entetesDepassementMap9 = (Map<String, Object>) t.get("entetes_depassement 9.0");

            if (entetesDepassementMap9 != null) {
                Map<String, Object> occurrencesEntetesMap = (Map<String, Object>) entetesDepassementMap9.get("occurrences_entetes 9.0");

                if (occurrencesEntetesMap != null) {
                    for (Map.Entry<String, Object> entry : occurrencesEntetesMap.entrySet()) {
                        String entete = entry.getKey();
                        Object value = entry.getValue();

                        if (value instanceof Map) {
                            Map<String, Object> enteteObject = (Map<String, Object>) value;
                            int occurrences = (int) enteteObject.getOrDefault("index 9.0", 0);
                            occurrencesEntetesTotales9.put(entete, occurrencesEntetesTotales9.getOrDefault(entete, 0) + occurrences);
                        }
                    }

                }
            }

            // LA LIMITE EST 1.5
            Map<String, Object> entetesDepassementMap1 = (Map<String, Object>) t.get("entetes_depassement 1.5");

            if (entetesDepassementMap1 != null) {
                Map<String, Object> occurrencesEntetesMap = (Map<String, Object>) entetesDepassementMap1.get("occurrences_entetes 1.5");

                if (occurrencesEntetesMap != null) {
                    for (Map.Entry<String, Object> entry : occurrencesEntetesMap.entrySet()) {
                        String entete = entry.getKey();
                        Object value = entry.getValue();

                        if (value instanceof Map) {
                            Map<String, Object> enteteObject = (Map<String, Object>) value;
                            int occurrences = (int) enteteObject.getOrDefault("index 1.5", 0);
                            occurrencesEntetesTotales1.put(entete, occurrencesEntetesTotales1.getOrDefault(entete, 0) + occurrences);
                        }
                    }

                }
            }


            // LA LIMITE EST 25.0
            Map<String, Object> entetesDepassementMap25 = (Map<String, Object>) t.get("entetes_depassement 25.0");

            if (entetesDepassementMap25 != null) {
                Map<String, Object> occurrencesEntetesMap = (Map<String, Object>) entetesDepassementMap25.get("occurrences_entetes 25.0");

                if (occurrencesEntetesMap != null) {
                    for (Map.Entry<String, Object> entry : occurrencesEntetesMap.entrySet()) {
                        String entete = entry.getKey();
                        Object value = entry.getValue();

                        if (value instanceof Map) {
                            Map<String, Object> enteteObject = (Map<String, Object>) value;
                            int occurrences = (int) enteteObject.getOrDefault("index 25.0", 0);
                            occurrencesEntetesTotales25.put(entete, occurrencesEntetesTotales25.getOrDefault(entete, 0) + occurrences);
                        }
                    }

                }
            }

            int nombreTrain = (int) t.getOrDefault("nombre de train passé est ", 0);
            totalNombreTrain += nombreTrain;


        }


        if (!occurrencesEntetesTotales.isEmpty()) {
            Map<String, Object> occurrencesEntetesTotalesMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : occurrencesEntetesTotales.entrySet()) {
                String entete = entry.getKey();
                int index = entry.getValue();
                Map<String, Object> enteteObject = new HashMap<>();
                enteteObject.put("index", index);
                occurrencesEntetesTotalesMap.put(entete, enteteObject);
            }
            trainMapp.put("occurrences_entetes_totales 6.0", occurrencesEntetesTotalesMap);
        }

        if (!occurrencesEntetesTotales0.isEmpty()) {
            Map<String, Object> occurrencesEntetesTotalesMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : occurrencesEntetesTotales0.entrySet()) {
                String entete = entry.getKey();
                int index = entry.getValue();
                Map<String, Object> enteteObject = new HashMap<>();
                enteteObject.put("index 0.0", index);
                occurrencesEntetesTotalesMap.put(entete, enteteObject);
            }
            trainMapp.put("occurrences_entetes_totales 0.0", occurrencesEntetesTotalesMap);
        }

        if (!occurrencesEntetesTotales1.isEmpty()) {
            Map<String, Object> occurrencesEntetesTotalesMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : occurrencesEntetesTotales1.entrySet()) {
                String entete = entry.getKey();
                int index = entry.getValue();
                Map<String, Object> enteteObject = new HashMap<>();
                enteteObject.put("index 1.5", index);
                occurrencesEntetesTotalesMap.put(entete, enteteObject);
            }
            trainMapp.put("occurrences_entetes_totales 1.5", occurrencesEntetesTotalesMap);
        }

        if (!occurrencesEntetesTotales3.isEmpty()) {
            Map<String, Object> occurrencesEntetesTotalesMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : occurrencesEntetesTotales3.entrySet()) {
                String entete = entry.getKey();
                int index = entry.getValue();
                Map<String, Object> enteteObject = new HashMap<>();
                enteteObject.put("index 3.0", index);
                occurrencesEntetesTotalesMap.put(entete, enteteObject);
            }
            trainMapp.put("occurrences_entetes_totales 3.0", occurrencesEntetesTotalesMap);
        }

        if (!occurrencesEntetesTotales9.isEmpty()) {
            Map<String, Object> occurrencesEntetesTotalesMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : occurrencesEntetesTotales9.entrySet()) {
                String entete = entry.getKey();
                int index = entry.getValue();
                Map<String, Object> enteteObject = new HashMap<>();
                enteteObject.put("index 9.0", index);
                occurrencesEntetesTotalesMap.put(entete, enteteObject);
            }
            trainMapp.put("occurrences_entetes_totales 9.0", occurrencesEntetesTotalesMap);
        }

        if (!occurrencesEntetesTotales25.isEmpty()) {
            Map<String, Object> occurrencesEntetesTotalesMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : occurrencesEntetesTotales25.entrySet()) {
                String entete = entry.getKey();
                int index = entry.getValue();
                Map<String, Object> enteteObject = new HashMap<>();
                enteteObject.put("index 25.0", index);
                occurrencesEntetesTotalesMap.put(entete, enteteObject);
            }
            trainMapp.put("occurrences_entetes_totales 25.0", occurrencesEntetesTotalesMap);
        }





// Calcul des pourcentages
        DecimalFormat decimalFormat = new DecimalFormat("0.00"); // Définit le format avec deux chiffres après la virgule
        Map<String, Object> pourcentageEntetesTotales = new HashMap<>();
        for (Map.Entry<String, Integer> entry : occurrencesEntetesTotales.entrySet()) {
            String entete = entry.getKey();
            int occurrences = entry.getValue();

            double pourcentage = (occurrences / (double) totalNombreTrain) * 100;

            String pourcentageFormatted = decimalFormat.format(pourcentage); // Formate le pourcentage avec deux chiffres après la virgule



            pourcentageEntetesTotales.put(entete, pourcentageFormatted);
        }

        trainMapp.put("pourcentage_entetes_totales 6.0", pourcentageEntetesTotales);



        Map<String, Object> pourcentageEntetesTotales0 = new HashMap<>();

        for (Map.Entry<String, Integer> entry : occurrencesEntetesTotales0.entrySet()) {
            String entete = entry.getKey();
            int occurrences = entry.getValue();

            double pourcentage = (occurrences / (double) totalNombreTrain) * 100;

            String pourcentageFormatted = decimalFormat.format(pourcentage); // Formate le pourcentage avec deux chiffres après la virgule

            pourcentageEntetesTotales0.put(entete, pourcentageFormatted);
        }

        trainMapp.put("pourcentage_entetes_totales 0.0", pourcentageEntetesTotales0);



        Map<String, Object> pourcentageEntetesTotales3 = new HashMap<>();

        for (Map.Entry<String, Integer> entry : occurrencesEntetesTotales3.entrySet()) {
            String entete = entry.getKey();
            int occurrences = entry.getValue();

            double pourcentage = (occurrences / (double) totalNombreTrain) * 100;

            String pourcentageFormatted = decimalFormat.format(pourcentage); // Formate le pourcentage avec deux chiffres après la virgule

            pourcentageEntetesTotales3.put(entete, pourcentageFormatted);
        }

        trainMapp.put("pourcentage_entetes_totales 3.0", pourcentageEntetesTotales3);



        Map<String, Object> pourcentageEntetesTotales1 = new HashMap<>();

        for (Map.Entry<String, Integer> entry : occurrencesEntetesTotales1.entrySet()) {
            String entete = entry.getKey();
            int occurrences = entry.getValue();

            double pourcentage = (occurrences / (double) totalNombreTrain) * 100;

            String pourcentageFormatted = decimalFormat.format(pourcentage); // Formate le pourcentage avec deux chiffres après la virgule

            pourcentageEntetesTotales1.put(entete, pourcentageFormatted);
        }

        trainMapp.put("pourcentage_entetes_totales 1.5", pourcentageEntetesTotales1);


        Map<String, Object> pourcentageEntetesTotales9 = new HashMap<>();

        for (Map.Entry<String, Integer> entry : occurrencesEntetesTotales9.entrySet()) {
            String entete = entry.getKey();
            int occurrences = entry.getValue();

            double pourcentage = (occurrences / (double) totalNombreTrain) * 100;

            String pourcentageFormatted = decimalFormat.format(pourcentage); // Formate le pourcentage avec deux chiffres après la virgule

            pourcentageEntetesTotales9.put(entete, pourcentageFormatted);
        }

        trainMapp.put("pourcentage_entetes_totales 9.0", pourcentageEntetesTotales9);


        Map<String, Object> pourcentageEntetesTotales25 = new HashMap<>();

        for (Map.Entry<String, Integer> entry : occurrencesEntetesTotales25.entrySet()) {
            String entete = entry.getKey();
            int occurrences = entry.getValue();

            double pourcentage = (occurrences / (double) totalNombreTrain) * 100;

            String pourcentageFormatted = decimalFormat.format(pourcentage); // Formate le pourcentage avec deux chiffres après la virgule

            pourcentageEntetesTotales25.put(entete, pourcentageFormatted);
        }

        trainMapp.put("pourcentage_entetes_totales 25.0", pourcentageEntetesTotales25);


        result.add(trainMapp);





        if (result.isEmpty()) {
            // Le résultat est vide, vous pouvez renvoyer une réponse spécifique
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);



    }




    //Api pour statistique
    /**
     * Récupère le pourcentage des trains passés, des trains avec SAM005(OK & NOK ) et les trains avec 50592(OK & NOK) pour un site donné ,un type Mr donné , statut de sam (&/ou) statut de 50592 donné durant une périide spécifiée.
     * @param site Le nom du site.
     * @param typemr Le nom du type Mr.
     * @param statutsam Le statut de sam.
     * @param statut50592 Le statut de 50592.
     * @param startDateFichier La date spécifiée (au format ISO DATE, ex : 2023-06-06).
     * @param FinDateFichier La date spécifiée (au format ISO DATE, ex : 2023-07-06)
     * @return Une réponse contenant une liste de maps représentant les pourcentage des trains selon le chois de paramètres specifiés.
     * @throws IOException En cas d'erreur lors de la manipulation des fichiers ou des propriétés.
     */
    @GetMapping("/dataBetweenstatistique")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierBetweenstatistique(
            @RequestParam("site") String site,
            @RequestParam(name ="typemr" , required = false) List<String> typemrList,
            @RequestParam(name = "statutsam", required = false) String statutSam,
            @RequestParam(name = "statut50592", required = false) String statut50592 ,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws IOException {

        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichierBetweenAndStatut50592(site, start, end ,statut50592);
        List<M_50592> m50592s1 = m50592Repository.findBySiteAndDateFichierBetween(site, start, end);
        List<Sam> sams = samRepository.findBySiteAndDateFichierBetweenAndStatutSAM(site, start, end,statutSam);
        List<Sam> samuniquement = samRepository.findBySiteAndDateFichierBetween(site, start, end);
        Map<String, Integer> m505952nokIndexValueMap = new HashMap<>();
        Map<String, Integer> samnokIndexValueMap = new HashMap<>();
        Map<String, Integer> redHeadersCountMap = new HashMap<>();
        Map<Integer, Integer> redHeadersCountSamMap = new HashMap<>();
        List<String> numTrains = new ArrayList<>();
        List<String> Trainssamnok = new ArrayList<>();
        List<String> Trainssamok = new ArrayList<>();
        List<String> Trains50592ok = new ArrayList<>();
        List<String> Trains50592nok = new ArrayList<>();
        boolean foundmr = false ;
        List<Map<String, Object>> result = new ArrayList<>();
        if(typemrList != null && !typemrList.isEmpty() ) {

            for (String typemr : typemrList) {
                foundmr = true;
                List<Mr> mrs = mrRepository.findByMr(typemr);

                Map<String, Object> traintypemr = new HashMap<>();

// Déclarer un compteur pour le nombre d'occurrences de resultatR1 > 0
                for (Mr mr : mrs) {
if(typemr.equals(mr.getMr())){
                    List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
                    for (Train train : trains) {
                        Long trainId = train.getId(); // Récupérer l'id du train
                        List<Result> resultss = resultRepository.findByTrainIdAndEngine(trainId, mr.getNumTrain());

                        for (Result results : resultss) {

                            numTrains.add(results.getEngine());


                            boolean samFound = false; // Variable pour indiquer si un sam correspondant a été trouvé

                            boolean allSamsOk = true;
                            for (Sam sam : sams) {
                                if (isBetweenTime(train.getHeureFichier(), sam.getHeureFichier(), sam.getHeureFichier())
                                        && train.getDateFichier().equals(sam.getDateFichier())) {

                                    samFound = true; // Un sam correspondant a été trouvé
                                    if (!sam.getStatutSAM().equals("OK") && statutSam.equals("NOK")) {
                                        Trainssamnok.add(train.getResults().get(0).getEngine());

                                        if (sam.getNbOccultations() != null && sam.getNbOccultations().size() > 0) {
                                            for (int i = 0; i < sam.getNbOccultations().size(); i++) {
                                                int index = i;
                                                if (!sam.getNbOccultations().get(i).equals(sam.getNbEssieux())) {

                                                    int occurrenceCount = redHeadersCountSamMap.getOrDefault(index, 0) + 1;
                                                    redHeadersCountSamMap.put(index, occurrenceCount);
                                                } else {
                                                    redHeadersCountSamMap.put(index, 0);
                                                }
                                            }
                                        }


                                    }
                                    if (sam.getStatutSAM().equals("OK") && statutSam.equals("OK")) {
                                        Trainssamok.add(sam.getFileName());

                                    }
                                }
                            }
                            if (statutSam != null && !statutSam.isEmpty() && statutSam.equals("uniquement sam") && !statutSam.equals("OK") && !statutSam.equals("NOK")) {
                                for (Sam sam1 : samuniquement) {
                                    if (isBetweenTime(train.getHeureFichier(), sam1.getHeureFichier(), sam1.getHeureFichier())
                                            && train.getDateFichier().equals(sam1.getDateFichier())) {

                                        if (!sam1.getStatutSAM().equals("OK")) {
                                            Trainssamnok.add(train.getResults().get(0).getEngine());

                                            if (sam1.getNbOccultations() != null && sam1.getNbOccultations().size() > 0) {
                                                for (int i = 0; i < sam1.getNbOccultations().size(); i++) {
                                                    int index = i;
                                                    if (!sam1.getNbOccultations().get(i).equals(sam1.getNbEssieux())) {

                                                        int occurrenceCount = redHeadersCountSamMap.getOrDefault(index, 0) + 1;
                                                        redHeadersCountSamMap.put(index, occurrenceCount);
                                                    } else {
                                                        redHeadersCountSamMap.put(index, 0);
                                                    }
                                                }
                                            }

                                            allSamsOk = false;
                                            break;
                                        } else {
                                            Trainssamok.add(results.getEngine());

                                        }


                                    }
                                }


                            }
                            boolean all50592Ok = true;


                            for (M_50592 m50592 : m50592s) {
                                if (isBetweenTime(train.getHeureFichier(), m50592.getHeureFichier(), m50592.getHeureFichier())
                                        && train.getDateFichier().equals(m50592.getDateFichier())) {

                                    Properties prop = new Properties();
                                    InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                                    prop.load(input);

                                    String outputFolderPath = prop.getProperty("output.folder.path");

                                    String fileName = m50592.getFileName();
                                    // Extraire l'année et le mois du nom de fichier
                                    String[] parts = fileName.split("_");
                                    String[] dateParts = parts[1].split("\\.");
                                    String year = dateParts[0];
                                    String month = dateParts[1];

                                    // Construire le chemin complet du dossier correspondant à l'année et au mois
                                    String folderPath = outputFolderPath + File.separator + year + "-" + month;

                                    // Créer un objet File représentant le dossier correspondant à l'année et au mois
                                    File inputFile = new File(folderPath, m50592.getFileName());
                                    ObjectMapper mapper = new ObjectMapper();
                                    JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class); // read from input file
                                    JsonNode parametreBENode = rootNode.get("ParametresBE");
                                    if (!m50592.getStatut50592().equals("OK")) {
                                        Trains50592nok.add(results.getEngine());

                                        for (int i = 0; i < parametreBENode.size(); i++) {
                                            JsonNode entete = parametreBENode.get(i).get(0);


                                            boolean isRedHeader = false;


                                            if (m50592.getBeR1().getxFond().get(i).equals("FF382A") || m50592.getBeR1().getyFond().get(i).equals("FF382A") || m50592.getBeR1().getzFond().get(i).equals("FF382A") || m50592.getBeR2().getxFond1().get(i).equals("FF382A") || m50592.getBeR2().getyFond1().get(i).equals("FF382A") || m50592.getBeR2().getzFond1().get(i).equals("FF382A")) {
                                                isRedHeader = true;
                                                String enteteValue = entete.asText();

                                                // Mise à jour du compteur pour l'en-tête rouge
                                                redHeadersCountMap.put(enteteValue, redHeadersCountMap.getOrDefault(enteteValue, 0) + 1);


                                            }
                                            if (!isRedHeader) {
                                                String enteteValue = entete.asText();
                                                redHeadersCountMap.putIfAbsent(enteteValue, 0);
                                            }


                                        }


                                        all50592Ok = false;
                                        break;
                                    }
                                    if (all50592Ok) {
                                        Trains50592ok.add(results.getEngine());

                                    }

                                }

                            }

                            if (statut50592 != null && !statut50592.isEmpty() && statut50592.equals("uniquement 50592")) {
                                for (M_50592 m50592 : m50592s1) {

                                    if (isBetweenTime(train.getHeureFichier(), m50592.getHeureFichier(), m50592.getHeureFichier())
                                            && train.getDateFichier().equals(m50592.getDateFichier())) {


                                        Properties prop = new Properties();
                                        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                                        prop.load(input);

                                        String outputFolderPath = prop.getProperty("output.folder.path");

                                        String fileName = m50592.getFileName();
                                        // Extraire l'année et le mois du nom de fichier
                                        String[] parts = fileName.split("_");
                                        String[] dateParts = parts[1].split("\\.");
                                        String year = dateParts[0];
                                        String month = dateParts[1];

                                        // Construire le chemin complet du dossier correspondant à l'année et au mois
                                        String folderPath = outputFolderPath + File.separator + year + "-" + month;

                                        // Créer un objet File représentant le dossier correspondant à l'année et au mois
                                        File inputFile = new File(folderPath, m50592.getFileName());
                                        ObjectMapper mapper = new ObjectMapper();
                                        JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class); // read from input file
                                        JsonNode parametreBENode = rootNode.get("ParametresBE");
                                        if (!m50592.getStatut50592().equals("OK")) {
                                            Trains50592nok.add(results.getEngine());

                                            for (int i = 0; i < parametreBENode.size(); i++) {
                                                JsonNode entete = parametreBENode.get(i).get(0);


                                                boolean isRedHeader = false;


                                                if (m50592.getBeR1().getxFond().get(i).equals("FF382A") || m50592.getBeR1().getyFond().get(i).equals("FF382A") || m50592.getBeR1().getzFond().get(i).equals("FF382A") || m50592.getBeR2().getxFond1().get(i).equals("FF382A") || m50592.getBeR2().getyFond1().get(i).equals("FF382A") || m50592.getBeR2().getzFond1().get(i).equals("FF382A")) {
                                                    isRedHeader = true;
                                                    String enteteValue = entete.asText();

                                                    // Mise à jour du compteur pour l'en-tête rouge
                                                    redHeadersCountMap.put(enteteValue, redHeadersCountMap.getOrDefault(enteteValue, 0) + 1);
                                                }
                                                if (!isRedHeader) {
                                                    String enteteValue = entete.asText();
                                                    redHeadersCountMap.putIfAbsent(enteteValue, 0);
                                                }


                                            }


                                            all50592Ok = false;
                                            break;
                                        }
                                        Trains50592ok.add(results.getEngine());


                                    }

                                }

                            }


                        }

                    }


                    int trainCount = trainRepository.countBySiteAndDateFichierBetween(site, start, end);
                    double pourcentagemr = ((double) (numTrains.size()) / trainCount) * 100;
                    Double pourcentageFormattedmr = Math.round(pourcentagemr * 100.0) / 100.0;

                    traintypemr.put("Type MR", typemr);
                    traintypemr.put("Nombre de trains avec ce type MR", numTrains.size());
                    traintypemr.put("Nombre de trains passé", trainCount);
                    traintypemr.put("Pourcentage", pourcentageFormattedmr);
                }

            }
                Map<String, Object> trainMapSam = new HashMap<>();
                Map<String, Object> trainMap50592 = new HashMap<>();



                int countsamok = Trainssamok.size();
                int countsamnok = Trainssamnok.size();
                int count50592ok = Trains50592ok.size();
                int count50592nok = Trains50592nok.size();



                double pourcentagesamok = ((double) countsamok / (numTrains.size())) * 100;

                double percentageok = ((double) count50592ok / (numTrains.size())) * 100;

                // Affichage des en-têtes rouges et leur nombre de fois 50592 not ok
                Map<String, Double> percentageMap = new HashMap<>();
                for (Map.Entry<String, Integer> entry : redHeadersCountMap.entrySet()) {
                    String entete = entry.getKey();
                    Integer countbe = entry.getValue();

                    m505952nokIndexValueMap.put(entete, countbe);

                    if (!Trains50592nok.isEmpty()) {


                        // Calcul du pourcentage
                        double percentagenok = ((double) countbe / numTrains.size()) * 100;

                        percentageMap.put(entete, percentagenok);
                    }

                }


// sam ok
                if (!Trainssamok.isEmpty()) {

                    trainMapSam.put("nombre de train passé (sam ok)", numTrains.size());
                    trainMapSam.put("mr(sam ok)", typemr);
                    trainMapSam.put("nombre de train passé avec sam ok", countsamok);
                    Double pourcentageFormatted = Math.round(pourcentagesamok * 100.0) / 100.0;
                    trainMapSam.put("pourcentage de chaque type mr sam ok", pourcentageFormatted);


                }

                // Affichage des index et leur nombr de fois sam not OK
                Map<String, Double> percentagesamnokMap = new HashMap<>();
                for (Map.Entry<Integer, Integer> entry : redHeadersCountSamMap.entrySet()) {
                    int index = entry.getKey();
                    int countbe = entry.getValue();


                    // Ajout à samnokIndexValueMap
                    samnokIndexValueMap.put(String.valueOf(index), countbe);

                    // Calcul du pourcentage
                    double pourentagesam = ((double) countbe / (numTrains.size())) * 100;
                    Double pourcentageFormatted = Math.round(pourentagesam * 100.0) / 100.0;
                    percentagesamnokMap.put(String.valueOf(index), pourcentageFormatted);
                }


                //sam not ok
                if (!Trainssamnok.isEmpty()) {

                    trainMapSam.put("nombre de train passé (sam nok)", numTrains.size());
                    trainMapSam.put("mr(sam nok)", typemr);
                    trainMapSam.put("nombre de train passé sam nok", countsamnok);
                    trainMapSam.put("index occultation et le total de fois de perturbation dans tous les trains", samnokIndexValueMap);
                    trainMapSam.put("pourcentage de perturbation par index d'un type mr", percentagesamnokMap);


                }


                //50592 not ok
                if (!Trains50592nok.isEmpty()) {
                    trainMap50592.put("nombre de train passé(50592 nok)", numTrains.size());
                    trainMap50592.put("mr(50592 nok)", typemr);
                    trainMap50592.put("nombre de train passé 50592 nok", count50592nok);
                    Map<String, Double> formattedPercentageMap = new HashMap<>();
                    for (Map.Entry<String, Double> entry : percentageMap.entrySet()) {
                        String entete = entry.getKey();
                        Double pourcentage = entry.getValue();
                        Double pourcentageFormatted = Math.round(pourcentage * 100.0) / 100.0;
                        formattedPercentageMap.put(entete, pourcentageFormatted);
                    }
                    trainMap50592.put("le pourcentage de chaque capteur", formattedPercentageMap);
                    trainMap50592.put("nom du capteur et le nombre de perturbations", m505952nokIndexValueMap);


                }

                //50592  ok
                if (!Trains50592ok.isEmpty()) {
                    trainMap50592.put("nombre de train passé(50592 ok )", numTrains.size());
                    trainMap50592.put("mr (50592 ok)", typemr);
                    trainMap50592.put("nombre de train passé 50592 ok", count50592ok);
                    Double pourcentageFormatted = Math.round(percentageok * 100.0) / 100.0;
                    trainMap50592.put("le poucentage de chaque type mr (50592 ok)", pourcentageFormatted);

                }
                if (!trainMapSam.isEmpty()) {

                    result.add(trainMapSam);
                }
                if (!trainMap50592.isEmpty()) {
                    result.add(trainMap50592);
                }
                if(!traintypemr.isEmpty()){
                    result.add(traintypemr);
                }

            }

        }








        else  {

            boolean samFound = false; // Variable pour indiquer si un sam correspondant a été trouvé

            boolean allSamsOk = true;

            for (Sam sam1 : samuniquement) {
                numTrains.add(sam1.getFileName());
            }
            for (Sam sam : sams) {


                samFound = true; // Un sam correspondant a été trouvé
                if (!sam.getStatutSAM().equals("OK") && statutSam.equals("NOK")) {
                    Trainssamnok.add(sam.getFileName());

                    if (sam.getNbOccultations() != null && sam.getNbOccultations().size() > 0) {
                        for (int i = 0; i < sam.getNbOccultations().size(); i++) {
                            int index = i;
                            if (!sam.getNbOccultations().get(i).equals(sam.getNbEssieux())) {

                                int occurrenceCount = redHeadersCountSamMap.getOrDefault(index, 0) + 1;
                                redHeadersCountSamMap.put(index, occurrenceCount);
                            } else {
                                redHeadersCountSamMap.put(index, 0);
                            }
                        }
                    }

                    allSamsOk = false;
                    break;
                }


                if (allSamsOk && statutSam.equals("OK")) {


                    Trainssamok.add(sam.getFileName());


                }


        } if (statutSam != null && !statutSam.isEmpty() && statutSam.equals("uniquement sam")) {
                for (Sam sam1 : samuniquement) {
                    Trainssamnok.add(sam1.getFileName());

                        if (!sam1.getStatutSAM().equals("OK")) {


                            if (sam1.getNbOccultations() != null && sam1.getNbOccultations().size() > 0) {
                                for (int i = 0; i < sam1.getNbOccultations().size(); i++) {
                                    int index = i;
                                    if (!sam1.getNbOccultations().get(i).equals(sam1.getNbEssieux())) {

                                        int occurrenceCount = redHeadersCountSamMap.getOrDefault(index, 0) + 1;
                                        redHeadersCountSamMap.put(index, occurrenceCount);
                                    } else {
                                        redHeadersCountSamMap.put(index, 0);
                                    }
                                }
                            }

                            allSamsOk = false;
                            break;
                        }


                        Trainssamok.add(sam1.getFileName());




                }


            }
            boolean all50592Ok = true;


            for (M_50592 m50592 : m50592s) {


                    Properties prop = new Properties();
                    InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                    prop.load(input);

                    String outputFolderPath = prop.getProperty("output.folder.path");

                String fileName = m50592.getFileName();
                // Extraire l'année et le mois du nom de fichier
                String[] parts = fileName.split("_");
                String[] dateParts = parts[1].split("\\.");
                String year = dateParts[0];
                String month = dateParts[1];

                // Construire le chemin complet du dossier correspondant à l'année et au mois
                String folderPath = outputFolderPath + File.separator + year + "-" + month;

                // Créer un objet File représentant le dossier correspondant à l'année et au mois
                File inputFile = new File(folderPath,m50592.getFileName());
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class); // read from input file
                    JsonNode parametreBENode = rootNode.get("ParametresBE");
                    if (!m50592.getStatut50592().equals("OK")) {
                        Trains50592nok.add(m50592.getFileName());

                        for (int i = 0; i < parametreBENode.size(); i++) {
                            JsonNode entete = parametreBENode.get(i).get(0);


                            boolean isRedHeader = false;


                            if (m50592.getBeR1().getxFond().get(i).equals("FF382A") || m50592.getBeR1().getyFond().get(i).equals("FF382A") || m50592.getBeR1().getzFond().get(i).equals("FF382A") || m50592.getBeR2().getxFond1().get(i).equals("FF382A") || m50592.getBeR2().getyFond1().get(i).equals("FF382A") || m50592.getBeR2().getzFond1().get(i).equals("FF382A")) {
                                isRedHeader = true;
                                String enteteValue = entete.asText();

                                // Mise à jour du compteur pour l'en-tête rouge
                                redHeadersCountMap.put(enteteValue, redHeadersCountMap.getOrDefault(enteteValue, 0) + 1);


                            }
                            if (!isRedHeader) {
                                String enteteValue = entete.asText();
                                redHeadersCountMap.putIfAbsent(enteteValue, 0);
                            }


                        }


                        all50592Ok = false;
                        break;
                    }
                    if (all50592Ok) {
                        Trains50592ok.add(m50592.getFileName());

                    }



            }

            if (statut50592 != null && !statut50592.isEmpty() && statut50592.equals("uniquement 50592")) {
                for (M_50592 m50592 : m50592s1) {




                        Properties prop = new Properties();
                        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                        prop.load(input);

                        String outputFolderPath = prop.getProperty("output.folder.path");

                    String fileName = m50592.getFileName();
                    // Extraire l'année et le mois du nom de fichier
                    String[] parts = fileName.split("_");
                    String[] dateParts = parts[1].split("\\.");
                    String year = dateParts[0];
                    String month = dateParts[1];

                    // Construire le chemin complet du dossier correspondant à l'année et au mois
                    String folderPath = outputFolderPath + File.separator + year + "-" + month;

                    // Créer un objet File représentant le dossier correspondant à l'année et au mois
                    File inputFile = new File(folderPath,m50592.getFileName());
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class); // read from input file
                        JsonNode parametreBENode = rootNode.get("ParametresBE");
                        if (!m50592.getStatut50592().equals("OK")) {
                            Trains50592nok.add(m50592.getFileName());

                            for (int i = 0; i < parametreBENode.size(); i++) {
                                JsonNode entete = parametreBENode.get(i).get(0);


                                boolean isRedHeader = false;


                                if (m50592.getBeR1().getxFond().get(i).equals("FF382A") || m50592.getBeR1().getyFond().get(i).equals("FF382A") || m50592.getBeR1().getzFond().get(i).equals("FF382A") || m50592.getBeR2().getxFond1().get(i).equals("FF382A") || m50592.getBeR2().getyFond1().get(i).equals("FF382A") || m50592.getBeR2().getzFond1().get(i).equals("FF382A")) {
                                    isRedHeader = true;
                                    String enteteValue = entete.asText();

                                    // Mise à jour du compteur pour l'en-tête rouge
                                    redHeadersCountMap.put(enteteValue, redHeadersCountMap.getOrDefault(enteteValue, 0) + 1);
                                }
                                if (!isRedHeader) {
                                    String enteteValue = entete.asText();
                                    redHeadersCountMap.putIfAbsent(enteteValue, 0);
                                }


                            }


                            all50592Ok = false;
                            break;
                        }
                        Trains50592ok.add(m50592.getFileName());




                }

            }


            Map<String, Object> trainMapSam = new HashMap<>();
            Map<String, Object> trainMap50592 = new HashMap<>();



            int countsamok = Trainssamok.size();
            int count50592ok = Trains50592ok.size();


            double pourcentagesamok = ((double) countsamok / (samuniquement.size())) * 100;
            double pourcentageFormattedsam = Math.round(pourcentagesamok * 100.0) / 100.0;

                double percentageok = ((double) count50592ok / (m50592s1.size())) * 100;
            double pourcentageFormatted50592 = Math.round(percentageok * 100.0) / 100.0;


            // Affichage des en-têtes rouges et leur nombre de fois 50592 nok
                Map<String, Double> percentageMap = new HashMap<>();
                for (Map.Entry<String, Integer> entry : redHeadersCountMap.entrySet()) {
                    String entete = entry.getKey();
                    Integer countbe = entry.getValue();

                    m505952nokIndexValueMap.put(entete, countbe);

                    if (!Trains50592nok.isEmpty()) {
                        // Calcul du pourcentage
                        double percentagenok = ((double) countbe / (m50592s1.size())) * 100;
                        double formattedPercentageMap = Math.round(percentagenok * 100.0) / 100.0;
                        percentageMap.put(entete, formattedPercentageMap);
                    }

                }


// sam ok
            if (!Trainssamok.isEmpty()) {

                trainMapSam.put("nombre de train passé (sam ok)", samuniquement.size());
                trainMapSam.put("nombre de train passé avec sam ok", countsamok);
                trainMapSam.put("pourcentage des sam ok", pourcentageFormattedsam);


            }

            // Affichage des index et leur nombr de fois sam nok
            Map<String, Double> percentagesamnokMap = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : redHeadersCountSamMap.entrySet()) {
                int index = entry.getKey();
                int countbe = entry.getValue();


                // Ajout à samnokIndexValueMap
                samnokIndexValueMap.put(String.valueOf(index), countbe);

                // Calcul du pourcentage
                double pourentagesam = ((double) countbe / (samuniquement.size())) * 100;
                double pourcentageFormatted = Math.round(pourentagesam * 100.0) / 100.0;
                percentagesamnokMap.put(String.valueOf(index), pourcentageFormatted);

            }


            //sam not ok
            if (!Trainssamnok.isEmpty()) {

                trainMapSam.put("nombre de train passé (sam nok)", samuniquement.size());
                trainMapSam.put("nombre de train passé sam nok", sams.size());
                trainMapSam.put("index occultation et le total de fois de perturbation dans tous les trains", samnokIndexValueMap);
                trainMapSam.put("pourcentage de perturbation par EV",percentagesamnokMap );


            }


            //50592 not ok
                if (!Trains50592nok.isEmpty()) {
                    trainMap50592.put("nombre de train passé (50592 nok)", m50592s1.size());
                    trainMap50592.put("nombre de train passé 50592 nok", m50592s.size());
                    trainMap50592.put("le pourcentage de chaque capteur", percentageMap);
                    trainMap50592.put("nom du capteur et le nombre de perturbations", m505952nokIndexValueMap);


                }

            //50592  ok
                if (!Trains50592ok.isEmpty()) {
                    trainMap50592.put("nombre de train passé (50592 ok )", m50592s1.size());
                    trainMap50592.put("nombre de train passé 50592 ok", count50592ok);
                    trainMap50592.put("le poucentage des 50592 (50592 ok)", pourcentageFormatted50592);

                }
            if (!trainMapSam.isEmpty()) {

                result.add(trainMapSam);
            }
                if (!trainMap50592.isEmpty()) {
                    result.add(trainMap50592);
                }
        }






        double sommePourcentage50592Ok = 0.0;
        int trains50592Ok = 0;
        int trains50592 = 0;
        Map<String, Integer> indexcapteurCountMap = new HashMap<>();
        int total50592nOk = 0;
        Map<String, Object> totalPourcentageMap50592nok = new HashMap<>();
        Map<String, Object> totalPourcentageMap50592ok = new HashMap<>();
        Map<String, Object> totalPourcentageMapSamnok = new HashMap<>();
        Map<String, Object> totalPourcentageMapSamok = new HashMap<>();
//50592 not ok
        for (Map<String, Object> resultMap50592 : result) {
            if (resultMap50592.containsKey("nom du capteur et le nombre de perturbations")) {

                Map<String, Integer> pourcentage50592NOkMap = (Map<String, Integer>) resultMap50592.get("nom du capteur et le nombre de perturbations");
                if (pourcentage50592NOkMap != null) {
                    int train50592nOk = (int) resultMap50592.get("nombre de train passé 50592 nok");

                    total50592nOk += train50592nOk;
                    for (Map.Entry<String, Integer> entry : pourcentage50592NOkMap.entrySet()) {
                        String capteur = entry.getKey();
                        int totalFoisPerturbation = entry.getValue();

                        int currentCount = indexcapteurCountMap.getOrDefault(capteur, 0);
                        indexcapteurCountMap.put(capteur, currentCount + totalFoisPerturbation);
                    }
                }
            }


            //50592 ok

            if (resultMap50592.containsKey("nombre de train passé 50592 ok") && resultMap50592.containsKey("nombre de train passé (50592 ok)")) {
                int train50592Ok = (int) resultMap50592.get("nombre de train passé 50592 ok");
                int train50592 = (int) resultMap50592.get("nombre de train passé (50592 ok)");

                trains50592Ok += train50592Ok;
                trains50592 += train50592;

        }
        }
        sommePourcentage50592Ok = ((double) trains50592Ok / trains50592) * 100;
        double pourcentageFormatted50592ok = Math.round(sommePourcentage50592Ok * 100.0) / 100.0;
        if (!Double.isNaN(sommePourcentage50592Ok)) {
            totalPourcentageMapSamok.put("le pourcentage de tous les 50592 ok et de tous les types mr", pourcentageFormatted50592ok);
        }


// 50592 nok

        Map<String, Object> totalPourcentage50592nok = new HashMap<>();
        double percentage50592 = 0.0;
        double pourcentageFormatted50592nok ;
// Calculate and display the percentages for each index of occultation
        for (Map.Entry<String, Integer> entry : indexcapteurCountMap.entrySet()) {
            String indexOccultation = entry.getKey();
            int totalFoisPerturbation = entry.getValue();
            percentage50592 = ((double) totalFoisPerturbation / total50592nOk) * 100;
             pourcentageFormatted50592nok = Math.round(percentage50592 * 100.0) / 100.0;
            totalPourcentage50592nok.put(indexOccultation, pourcentageFormatted50592nok);

        }

if(!indexcapteurCountMap.isEmpty()){
    totalPourcentageMap50592nok.put("total d'index capteurs", indexcapteurCountMap);
    totalPourcentageMap50592nok.put("pourcentage des capteurs dans tous les types mr", totalPourcentage50592nok);

}






            double sommePourcentageSamOk = 0.0;
            int trainsSamOk = 0;
            int trainsSam = 0;



//sam ok
            for (Map<String, Object> resultMap : result) {
                if (resultMap.containsKey("nombre de train passé avec sam ok") && resultMap.containsKey("nombre de train passé (sam ok)")) {
                    int trainSamOk = (int) resultMap.get("nombre de train passé avec sam ok");
                    int trainSam = (int) resultMap.get("nombre de train passé (sam ok)");

                    trainsSamOk += trainSamOk;
                    trainsSam += trainSam;
                }



            }

            sommePourcentageSamOk = ((double) trainsSamOk / trainsSam) * 100;
        Double pourcentageFormattedSamok = Math.round(sommePourcentageSamOk * 100.0) / 100.0;
        if (!Double.isNaN(sommePourcentageSamOk)) {
            totalPourcentageMapSamok.put("le pourcentage de tous les sam ok et de tous les types mr", pourcentageFormattedSamok);
        }


        //sam nok
            Map<String, Integer> indexOccultationCountMap = new HashMap<>();
            int totalSamnOk = 0;

            for (Map<String, Object> resultMap : result) {
                if (resultMap.containsKey("index occultation et le total de fois de perturbation dans tous les trains")) {
                    Map<String, Integer> indexOccultationMap = (Map<String, Integer>) resultMap.get("index occultation et le total de fois de perturbation dans tous les trains");
if(indexOccultationMap != null){
                    int trainSamnOk = (int) resultMap.get("nombre de train passé (sam nok)");

                    totalSamnOk += trainSamnOk;

                    for (Map.Entry<String, Integer> entry : indexOccultationMap.entrySet()) {
                        String indexOccultation = entry.getKey();
                        int totalFoisPerturbation = entry.getValue();

                        int currentCount = indexOccultationCountMap.getOrDefault(indexOccultation, 0);
                        indexOccultationCountMap.put(indexOccultation, currentCount + totalFoisPerturbation);
                    }
                }
                }
            }

            Map<String, Object> totalPourcentageSamnok = new HashMap<>();
            double percentage = 0.0;

// Calculate and display the percentages for each index of occultation
            for (Map.Entry<String, Integer> entry : indexOccultationCountMap.entrySet()) {
                String indexOccultation = entry.getKey();
                int totalFoisPerturbation = entry.getValue();
                percentage = ((double) totalFoisPerturbation / totalSamnOk) * 100;

                totalPourcentageSamnok.put(indexOccultation, percentage);

            }
if(!indexOccultationCountMap.isEmpty()){
    totalPourcentageMapSamnok.put("total d'index", indexOccultationCountMap);
    Map<String, Double> formattedPercentageMap = new HashMap<>();
    for (Map.Entry<String, Object> entry : totalPourcentageSamnok.entrySet()) {
        String entete = entry.getKey();
        Double pourcentage = (Double) entry.getValue();
        Double pourcentageFormatted = Math.round(pourcentage * 100.0) / 100.0;
        formattedPercentageMap.put(entete, pourcentageFormatted);
    }
    totalPourcentageMapSamnok.put("pourcentage des EV dans tous les types mr", formattedPercentageMap);

}





if(!totalPourcentageMapSamnok.isEmpty()){
    result.add(totalPourcentageMapSamnok);
}

if(!totalPourcentageMap50592nok.isEmpty()){
    result.add(totalPourcentageMap50592nok);
}
        if(!totalPourcentageMap50592ok.isEmpty()){
            result.add(totalPourcentageMap50592ok);
        }
       if(!totalPourcentageMapSamok.isEmpty()){
           result.add(totalPourcentageMapSamok);
       }




        if (result.isEmpty()) {
            // Le résultat est vide, vous pouvez renvoyer une réponse spécifique
            return ResponseEntity.noContent().build();
        }else {
            return ResponseEntity.ok(result);
        }

    }

// api pour recuperer tous les types mr
    /**
     * Récupère tous les types mr et leur catégories pour un site donné durant une période spécifiée.
     * @param site Le nom du site.
     * @param startDateFichier La date spécifiée (au format ISO DATE, ex : 2023-06-06).
     * @param FinDateFichier La date spécifiée (au format ISO DATE, ex : 2023-07-06)
     * @return Une réponse contenant une liste de maps représentant les pourcentage des trains selon le chois de paramètres specifiés.
     * @throws IOException En cas d'erreur lors de la manipulation des fichiers ou des propriétés.
     */
    @GetMapping("/dataBetweenrMr")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierBetweenRapportmr(
            @RequestParam("site") String site,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws Exception {
        logger.info("Fetching data for site: {}, start date: {}, end date: {}", site, startDate, endDate);

        // Convert LocalDate to Date
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        // Fetch trains within the specified site and date range
        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
        logger.info("Found {} trains for site: {}, start date: {}, end date: {}", trains.size(), site, startDate, endDate);

        // Map to store category counts and category counts by type
        Map<String, Integer> categoryCounts = new HashMap<>();
        Map<String, Map<String, Integer>> categoryCountsByType = new HashMap<>();

        // Iterate over trains and MRs to calculate counts
        for (Train train : trains) {
            for (Result  results  : train.getResults()) {
            String trainNumber = results.getEngine();
            List<Mr> mrs = mrRepository.findAllByNumTrain(trainNumber);

            for (Mr mr : mrs) {
                String mrType = mr.getMr();
                String category = getCategory(mrType);

                // Count total occurrences of each category
                categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);

                // Count occurrences of each type within each category
                Map<String, Integer> typeCounts = categoryCountsByType.getOrDefault(category, new HashMap<>());
                typeCounts.put(mrType, typeCounts.getOrDefault(mrType, 0) + 1);
                categoryCountsByType.put(category, typeCounts);
            }
        }
    }
        // Prepare the final result
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<String, Map<String, Integer>> categoryEntry : categoryCountsByType.entrySet()) {
            String category = categoryEntry.getKey();
            Map<String, Integer> typeCounts = categoryEntry.getValue();

            for (Map.Entry<String, Integer> typeEntry : typeCounts.entrySet()) {
                String type = typeEntry.getKey();
                Integer count = typeEntry.getValue();

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("category", category);
                resultMap.put("typeMR", type);
                resultMap.put("count", count);
                result.add(resultMap);
            }
        }

        // Add "Other" category with count of MRs not falling into any specific category
        Integer otherCount = categoryCounts.getOrDefault("Other", 0);
        if (otherCount > 0) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("category", "Other");
            resultMap.put("typeMR", "Other");
            resultMap.put("count", otherCount);
            result.add(resultMap);
        }
        // Check if the result is empty and return the appropriate response
        if (result.isEmpty()) {
            logger.info("No data found for site: {}, start date: {}, end date: {}", site, startDate, endDate);
            return ResponseEntity.noContent().build();
        }

        logger.info("Returning data for site: {}, start date: {}, end date: {}", site, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    private String getCategory(String mrType) {
        if (mrType.startsWith("B")) {
            return "BB";
        } else if (mrType.startsWith("C")) {
            return "CC";
        } else if (mrType.startsWith("Z")) {
            return "Z";
        } else {
            return "Autre";
        }
    }






        @Value("${output.folder.path}")
        private String outputFolderPath;

        @PostMapping("/upload")
        public String uploadFile(@RequestParam("pdfFile") MultipartFile file) {
            if (file.isEmpty()) {
                return "Le fichier est vide.";
            }

            try {
                // Récupérez le nom du fichier
                String fileName = file.getOriginalFilename();

                // Créez le chemin d'accès complet en combinant le répertoire de base et le nom de fichier
                Path filePath = Paths.get(outputFolderPath, fileName);


                // Enregistrez le fichier dans le répertoire spécifié
                Files.write(filePath, file.getBytes());

                return "Le fichier a été téléchargé avec succès.";
            } catch (IOException e) {
                return "Une erreur s'est produite lors du téléchargement du fichier.";
            }
        }








        @GetMapping("/download")
        public ResponseEntity<List<Map<String, Object>>> downloadFiles() {
            try {
                // Obtenir tous les fichiers PDF présents dans le répertoire
                File folder = new File(outputFolderPath);
                File[] files = folder.listFiles((dir, name) -> name.endsWith(".pdf"));

                if (files != null && files.length > 0) {
                    // Créer une liste pour stocker les informations des fichiers PDF
                    List<Map<String, Object>> fileList = new ArrayList<>();

                    for (File file : files) {
                        // Obtenir le nom du fichier
                        String fileName = file.getName();

                        // Créer une ressource à partir du contenu du fichier
                        Resource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));

                        // Créer une map pour stocker les informations du fichier
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("name", fileName);
                        fileInfo.put("content", resource.getInputStream().readAllBytes());

                        // Ajouter la map à la liste
                        fileList.add(fileInfo);
                    }

                    // Renvoyer la liste des fichiers avec leurs noms et leurs contenus
                    return ResponseEntity.ok().body(fileList);
                } else {
                    // Gérer le cas où aucun fichier n'est trouvé
                    return ResponseEntity.notFound().build();
                }
            } catch (Exception e) {
                // Gérer les erreurs de récupération des fichiers
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }


