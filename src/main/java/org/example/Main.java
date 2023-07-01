package org.example;



import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.DeserializationFeature;

import com.fasterxml.jackson.databind.JsonMappingException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.FileUtils;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.example.model.*;

import org.example.repository.M_50592Repository;

import org.example.repository.ResultRepository;

import org.example.repository.SamRepository;

import org.example.repository.TrainRepository;

import org.example.service.*;

import org.apache.poi.ss.usermodel.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;


import java.io.*;



import java.net.HttpURLConnection;

import java.net.MalformedURLException;

import java.net.ProtocolException;

import java.net.URL;

import java.nio.channels.FileChannel;

import java.nio.channels.FileLock;

import java.nio.channels.OverlappingFileLockException;

import java.nio.file.Files;

import java.nio.file.StandardCopyOption;

import java.sql.Time;

import java.text.ParseException;

import java.text.SimpleDateFormat;

import java.time.LocalDateTime;

import java.time.ZoneId;

import java.time.format.DateTimeFormatter;

import java.util.*;

import java.util.List;

import java.nio.file.*;

import java.util.concurrent.CopyOnWriteArrayList;

import java.util.concurrent.CountDownLatch;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.commons.io.FileUtils;

import org.apache.commons.io.IOCase;


import org.slf4j.LoggerFactory;


import java.util.logging.ErrorManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SpringBootApplication

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);



    public static void main(String[] args) {

        SpringApplication.run(Main.class, args);



        logger.info("Le programme a démarré.");




    }


    boolean isIntelliJ = (System.getenv("IDEA_INITIAL_DIRECTORY") != null);

    private static void moveFileToFailureDirectory(File file, File failureDirectory) {
        boolean isIntelliJ = (System.getenv("IDEA_INITIAL_DIRECTORY") != null);
        // Vérifiez si le répertoire d'échec existe, sinon créez-le
        if (!failureDirectory.exists()) {
            failureDirectory.mkdirs();
        }

        // Obtenez le nom de fichier uniquement (sans le chemin)
        String fileName = file.getName();

        // Construisez le chemin du nouveau fichier dans le répertoire d'échec
        File newFile = new File(failureDirectory, fileName);

        // Déplacez le fichier vers le répertoire d'échec
        if (file.renameTo(newFile)) {
            String logMessagee = "Fichier déplacé avec succès vers le répertoire d'échec : " + newFile.getAbsolutePath();

            if (isIntelliJ) {
                System.out.println(logMessagee);  // Affiche dans la console (syso) d'IntelliJ
            } else {
                logger.info(logMessagee);  // Affiche dans le logger du serveur
            }

        } else {
            String logMessagee = "Échec du déplacement du fichier vers le répertoire d'échec : " + file.getAbsolutePath();

            if (isIntelliJ) {
                System.out.println(logMessagee);  // Affiche dans la console (syso) d'IntelliJ
            } else {
                logger.info(logMessagee);  // Affiche dans le logger du serveur
            }

        }
    }

    private static String getYearMonthFolderName(File jsonFile) {
        String jsonFileName = jsonFile.getName();
        String year = ""; // Obtenir l'année à partir du nom du fichier JSON
        String month = ""; // Obtenir le mois à partir du nom du fichier JSON

        // Extraire l'année et le mois à partir du nom du fichier JSON en utilisant des expressions régulières
        Pattern pattern = Pattern.compile(".*-(\\d{4})-(\\d{2}).*");
        Matcher matcher = pattern.matcher(jsonFileName);
        if (matcher.matches()) {
            year = matcher.group(1);
            month = matcher.group(2);
        }

        return year + File.separator + month; // Retourner le nom du dossier au format "année/mois"
    }

    private void deplacerFichier(File file, File outputFolder) throws InterruptedException {
        if (file.getName().toLowerCase().endsWith(".json") || file.getName().toLowerCase().endsWith(".xlsx")) {
            String fileName = file.getName();
            File targetFolder = getTargetFolder(outputFolder, fileName);
            File targetFile = new File(targetFolder, fileName);

            if (targetFile.exists()) {
                String logMessage = "Le fichier cible existe déjà : " + targetFile.getAbsolutePath();

                if (isIntelliJ) {
                    System.out.println(logMessage);
                } else {
                    logger.info(logMessage);
                }
            } else {
                try {
                    Thread.sleep(1000);
                    Files.move(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    String logMessage = "Le fichier a été déplacé avec succès : " + fileName;

                    if (isIntelliJ) {
                        System.out.println(logMessage);
                    } else {
                        logger.info(logMessage);
                    }
                } catch (FileSystemException ex) {
                    String logMessage = "Erreur lors du déplacement du fichier : " + ex.getMessage();

                    if (isIntelliJ) {
                        System.out.println(logMessage);
                    } else {
                        logger.info(logMessage);
                    }

                    int maxAttempts = 3;
                    int attempt = 0;

                    while (attempt < maxAttempts) {
                        attempt++;

                        try {
                            Thread.sleep(1000);
                            Files.move(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            String logMessag = "Le fichier a été déplacé avec succès après une nouvelle tentative : " + fileName;

                            if (isIntelliJ) {
                                System.out.println(logMessag);
                            } else {
                                logger.info(logMessag);
                            }

                            break;
                        } catch (IOException | InterruptedException exx) {
                            String logMessa = "Erreur lors du déplacement du fichier (tentative " + attempt + " sur " + maxAttempts + ") : " + exx.getMessage();

                            if (isIntelliJ) {
                                System.out.println(logMessa);
                            } else {
                                logger.info(logMessa);
                            }
                        }
                    }

                    if (attempt == maxAttempts) {
                        String logMessagee = "Impossible de déplacer le fichier après " + maxAttempts + " tentatives.";

                        if (isIntelliJ) {
                            System.out.println(logMessagee);
                        } else {
                            logger.info(logMessagee);
                        }
                    }
                } catch (IOException ex) {
                    String logMessage = "Erreur lors du déplacement du fichier : " + ex.getMessage();

                    if (isIntelliJ) {
                        System.out.println(logMessage);
                    } else {
                        logger.info(logMessage);
                    }
                }
            }
        }
    }

    private File getTargetFolder(File outputFolder, String fileName) {
        // Extraire l'année et le mois du nom de fichier
        String[] parts = fileName.split("_");
        String[] dateParts = parts[1].split("\\.");
        String year = dateParts[0];
        String month = dateParts[1];

        // Créer le répertoire année-mois s'il n'existe pas déjà
        String folderName = year + "-" + month;
        File targetFolder = new File(outputFolder, folderName);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        return targetFolder;
    }















    @Bean

    CommandLineRunner runner(SamService samService, M_50592Service m50592Service , TrainService trainService, MrService mrService , SamRepository samRepository , M_50592Repository m50592Repository , TrainRepository trainRepository , ResultRepository resultRepository , ResultService resultService) {

        return args -> {



            Properties prop = new Properties();

            InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");

            prop.load(input);


            String inputFolderPath = prop.getProperty("input.folder.path");

            String outputFolderPath = prop.getProperty("output.folder.path");

            String echecFolderPath = prop.getProperty("echec.folder.path");


            File outputFolder = new File(outputFolderPath);

            File inputFolder = new File(inputFolderPath);

            ObjectMapper mapper = new ObjectMapper();

            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);



            List<File> filesToMove = new ArrayList<>();


// Créer une variable de contrôle pour arrêter la surveillance du répertoire

            AtomicBoolean isRunning = new AtomicBoolean(true);






            boolean isIntelliJ = (System.getenv("IDEA_INITIAL_DIRECTORY") != null);





// Créer un objet WatchService dans un thread séparé

            Thread watchThread = new Thread(() -> {

                try {

                    WatchService watchService = null;

                    try {

                        watchService = FileSystems.getDefault().newWatchService();

                    } catch (IOException e) {

                        throw new RuntimeException(e);

                    }

                    Path inputFolderPathh = inputFolder.toPath();

                    try {

                        inputFolderPathh.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

                    } catch (IOException e) {

                        throw new RuntimeException(e);

                    }



                    while (isRunning.get()) {


                        WatchKey key;

                        try {

                            key = watchService.take();

                        } catch (InterruptedException e) {

                            String logMessage = "Erreur lors de la surveillance du répertoire : " + e.getMessage();

                            if (isIntelliJ) {
                                System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                            } else {
                                logger.info(logMessage);  // Affiche dans le logger du serveur
                            }



                            break;

                        }



// Parcourir les événements

                        for (WatchEvent<?> event : key.pollEvents()) {

                            WatchEvent.Kind<?> kind = event.kind();


// Vérifier si un nouveau fichier a été créé dans le répertoire "input"

                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {

                                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;

                                Path filePath = inputFolderPathh.resolve(pathEvent.context());


                                File newFile = filePath.toFile();

                                filesToMove.add(newFile);



                            }


                        }


// Liste pour stocker les numéros de train traités

                        List<String> processedTrainNumberss = new ArrayList<>();


// Lire les données de la base de données pour la comparaison avec les nouvelles données

                        List<Mr> allMrDatas = mrService.findAll();

                        for (Mr mr : allMrDatas) {

                            processedTrainNumberss.add(mr.getNumTrain());

                        }


// Lire les fichiers Excel et mettre à jour les données des trains correspondants

                        File[] excelFiless = inputFolder.listFiles((dir, name) -> name.endsWith(".xlsx"));
                        String regex2 = "IHM\\s*_Base\\s*de\\s*données\\s*SYRENE_V\\d+\\.xlsx";



                        if (excelFiless != null) {

                            for (File excelFile : excelFiless) {
                                if (!excelFile.getName().matches(regex2)) {
                                    // Le nom de fichier ne correspond pas au format spécifié
                                    File targetFileechec = new File(echecFolderPath, excelFile.getName());
                                    try {
                                        String logMessage = "Le nom de fichier " + excelFile.getName() + " ne correspond pas au format spécifié ";
                                        Files.move(excelFile.toPath(), targetFileechec.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                        String logMessageerreur = "Le déplacement du fichier " + excelFile.getName() + " dans le répertoire d'echec "+targetFileechec.getAbsolutePath();
                                        if (isIntelliJ) {
                                            System.out.println(logMessage);
                                            System.out.println(logMessageerreur);// Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMessage);  // Affiche dans le logger du serveur
                                            logger.info(logMessageerreur);
                                        }
                                    } catch (IOException e) {
                                        String logMessage = "Erreur lors du déplacement du fichier vers le dossier 'echec' : " + e.getMessage();

                                        if (isIntelliJ) {
                                            System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMessage);  // Affiche dans le logger du serveur
                                        }
                                    }
                                } else {
                                File targetFile = new File(outputFolder, excelFile.getName());

                                if (targetFile.exists()) {
                                    File targetFileechec = new File(echecFolderPath, excelFile.getName());
                                    try {
                                        String logMessage = "Le fichier cible existe déjà : " + targetFileechec.getAbsolutePath();
                                        Files.move(excelFile.toPath(), targetFileechec.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        String logMessageerreur = "Le déplacement du fichier " + excelFile.getName() + " dans le répertoire d'echec "+targetFileechec.getAbsolutePath();
                                        if (isIntelliJ) {
                                            System.out.println(logMessage);
                                            System.out.println(logMessageerreur);// Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMessage);  // Affiche dans le logger du serveur
                                            logger.info(logMessageerreur);
                                        }


                                    } catch (IOException e) {

                                        String logMessage = "Erreur lors du déplacement du fichier existant vers le dossier 'echec' : " + e.getMessage();

                                        if (isIntelliJ) {
                                            System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMessage);  // Affiche dans le logger du serveur
                                        }

                                    }
                                } else {

                                    try (FileInputStream excelStream = new FileInputStream(excelFile)) {


                                        Workbook workbook = new XSSFWorkbook(excelStream);

                                        Sheet sheet = workbook.getSheetAt(0);

                                        for (Row row : sheet) {

                                            if (row.getRowNum() > 0) {

                                                Cell numTrainCell = row.getCell(0);

                                                String numTrain = null;

                                                if (numTrainCell.getCellType() == CellType.STRING) {

                                                    numTrain = numTrainCell.getStringCellValue();

                                                } else if (numTrainCell.getCellType() == CellType.NUMERIC) {

                                                    numTrain = String.valueOf((int) numTrainCell.getNumericCellValue());

                                                }


                                                String mr = row.getCell(1).getStringCellValue();


                                                if (!processedTrainNumberss.contains(numTrain)) {


// Si le numéro de train n'a pas encore été traité, ajouter une nouvelle entrée dans la base de données

                                                    Mr newMr = new Mr();

                                                    newMr.setMr(mr);

                                                    newMr.setNumTrain(numTrain);

                                                    mrService.save(newMr);


                                                }

                                            }

                                        }


                                    } catch (IOException e) {
                                        String logMessage = "Erreur lors de la lecture du fichier Excel : " + excelFile.getAbsolutePath() + " , " + e;

                                        if (isIntelliJ) {
                                            System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.error(logMessage);  // Affiche dans le logger du serveur
                                        }


                                    }
                                }

                            }
                                deplacerFichier(excelFile,outputFolder);
                        }



                            filesToMove.clear(); // Vider la liste filesToMove









                            inputFolderPathh.register(watchService,

                                    StandardWatchEventKinds.ENTRY_CREATE,

                                    StandardWatchEventKinds.ENTRY_MODIFY,

                                    StandardWatchEventKinds.ENTRY_DELETE);

                        }







                        EnvloppeData enveloppeDatas = new EnvloppeData();

// Lire tous les fichiers commençant par 'Sam'

                        File[] samFiless = inputFolder.listFiles((dir, name) -> name.startsWith("SAM005") && name.endsWith(".json"));
                        String regex1 = "SAM005-.+_\\d{4}\\.\\d{2}\\.\\d{2}_\\d{2}h\\d{2}m\\d{2}s\\.json";


                        if (samFiless != null ) {

                            for (File samFile : samFiless) {
                                if (!samFile.getName().matches(regex1)) {
                                    // Le nom de fichier ne correspond pas au format spécifié
                                    File targetFileechec = new File(echecFolderPath, samFile.getName());
                                    try {
                                        String logMessage = "Le nom de fichier " + samFile.getName() + " ne correspond pas au format spécifié ";
                                        Files.move(samFile.toPath(), targetFileechec.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                        String logMessageerreur = "Le déplacement du fichier " + samFile.getName() + " dans le répertoire d'echec "+targetFileechec.getAbsolutePath()+" est OK";
                                        if (isIntelliJ) {
                                            System.out.println(logMessage);
                                            System.out.println(logMessageerreur);// Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMessage);  // Affiche dans le logger du serveur
                                            logger.info(logMessageerreur);
                                        }
                                    } catch (IOException e) {
                                        String logMessage = "Erreur lors du déplacement du fichier vers le dossier 'echec' : " + e.getMessage();

                                        if (isIntelliJ) {
                                            System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMessage);  // Affiche dans le logger du serveur
                                        }
                                    }
                                } else {
                                File targetFile = new File(outputFolder, samFile.getName());

                                if (targetFile.exists()) {
                                    File targetFileechec = new File(echecFolderPath, samFile.getName());
                                    try {
                                        String logMessage = "Le fichier cible existe déjà : " + targetFileechec.getAbsolutePath();
                                        Files.move(samFile.toPath(), targetFileechec.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        String logMessageerreur = "Le déplacement du fichier " + samFile.getName() + " dans le répertoire d'echec "+targetFileechec.getAbsolutePath()+" est OK";
                                        if (isIntelliJ) {
                                            System.out.println(logMessage);
                                            System.out.println(logMessageerreur);// Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMessage);  // Affiche dans le logger du serveur
                                            logger.info(logMessageerreur);
                                        }


                                    } catch (IOException e) {

                                        String logMessage = "Erreur lors du déplacement du fichier existant vers le dossier 'echec' : " + e.getMessage();

                                        if (isIntelliJ) {
                                            System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMessage);  // Affiche dans le logger du serveur
                                        }

                                    }
                                } else {
// Charger les enveloppes à partir du fichier JSON
                                    String logMessage = "Le fichier " + samFile.getName() + " est OK";

                                    if (isIntelliJ) {
                                        System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                    } else {
                                        logger.info(logMessage);  // Affiche dans le logger du serveur
                                    }

                                    TypeReference<List<Sam>> samTypeRef = new TypeReference<List<Sam>>() {
                                    };


                                    try (InputStream samStream = new FileInputStream(samFile)) {

                                        List<Sam> samss = mapper.readValue(samStream, samTypeRef);

// Déclarer une variable pour suivre l'incrémentation de NbOccultations
                                        int counter = 0;
                                        for (Sam sam : samss) {
                                            logger.info("le fichier à traiter " + sam.getFileName());
                                            System.out.println("le fichier à traiter " + sam.getFileName());

                                            sam.checkOccultations();

                                            sam.setFileName(samFile.getName()); // Définir le nom de fichier dans l'objet M_50592

                                            sam.loadStartingWithSam(samFile.getName());

                                            sam.loadSite(samFile.getName());

                                            NbOccultations nbOccultations = new NbOccultations();
                                            nbOccultations.setNbOccultations(++counter);
                                            if (sam.getStatutSAM().equals("OK")) {

                                                sam.setUrlSam(null); // Définir l'URL à null

                                            }





                                            samService.save(sam);

                                            if (samService.findById(sam.getId()) != null) {
                                                logger.info("Le fichier a été enregistré dans la base de données: " + sam.getFileName());
                                                System.out.println("Le fichier a été enregistré dans la base de données: " + sam.getFileName());
                                                deplacerFichier(samFile, outputFolder);
                                                File targetFolder = getTargetFolder(outputFolder, samFile.getName());
                                                String jsonFilePath = new File(targetFolder, samFile.getName()).getAbsolutePath();
                                                String jsonFileName = sam.getFileName().substring(0, sam.getFileName().lastIndexOf('.'));
                                                File outputFolderFile = new File(jsonFilePath).getParentFile(); // Obtenir le dossier parent du fichier JSON



                                                if (sam.getStatutSAM().equals("NOK")) {

                                                    File outputFolderEnvelope = new File(outputFolderFile, sam.getFileName().replace(".json", "") + "_enveloppes");
                                                    outputFolderEnvelope.mkdir();
                                                    String logMessa = "Création du dossier contenant les capteurs {} => OK" + " , " + outputFolderEnvelope;
                                                    if (isIntelliJ) {
                                                        System.out.println(logMessa);  // Affiche dans la console (syso) d'IntelliJ
                                                    } else {
                                                        logger.info(logMessa);  // Affiche dans le logger du serveur
                                                    }

                                                    // Mettre à jour le chemin avec le nouveau dossier créé
                                                    String urlsam = outputFolderEnvelope.getAbsolutePath().replace("\\", "/");
                                                    sam.setUrlSam(urlsam);
                                                    samService.save(sam);

                                                    for (int i = 1; i <= sam.getNbOccultations().size(); i++) {
                                                        // Charger le fichier JSON depuis le nouvel emplacement
                                                        File samFileNewLocation = new File(outputFolderFile, samFile.getName());

                                                        enveloppeDatas.loadFromJson(samFileNewLocation, i);

                                                        // Créer le nom du fichier de sortie pour ce traitement spécifique
                                                        String outputFileName = samFile.getName().replace("SAM005", "SAMCapteur" + i);
                                                        File outputFile = new File(outputFolderEnvelope, outputFileName);

                                                        String logMessageok = "Création du capteur {} => OK" + " , " + outputFileName;
                                                        if (isIntelliJ) {
                                                            System.out.println(logMessageok);  // Affiche dans la console (syso) d'IntelliJ
                                                        } else {
                                                            logger.info(logMessageok);  // Affiche dans le logger du serveur
                                                        }

                                                        // Vérifier si le fichier de sortie existe déjà
                                                        if (!outputFile.exists()) {
                                                            double step = 6.0; // step peut être changé selon vos besoins
                                                            enveloppeDatas.saveSampledToJson(outputFile, step);
                                                        }
                                                    }
                                                }
                                                }
                                            else {
                                                logger.error("Erreur lors de l'enregistrement du fichier dans la base de données: " + sam.getFileName());
                                                System.out.println("Erreur lors de l'enregistrement du fichier dans la base de données: " + sam.getFileName());
                                            }


                                            Set<String> existingResultIdss = new HashSet<>();
                                            DateTimeFormatter formatterrs = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

                                            LocalDateTime samDateTime = LocalDateTime.parse(sam.getDateFichier() + "T" + sam.getHeureFichier());

                                            String url = "https://test01.rd-vision-dev.com/get_images?system=2&dateFrom=" +
                                                    samDateTime.minusMinutes(1) + "&dateTo=" + samDateTime.plusMinutes(1);

                                            logger.info("l'url passé est "+url);
                                            URL jsonUrl;

                                            try {

                                                jsonUrl = new URL(url);


                                            } catch (MalformedURLException e) {
                                                throw new RuntimeException(e);
                                            }

                                            HttpURLConnection connection = null;

                                            try {

                                                connection = (HttpURLConnection) jsonUrl.openConnection();

                                            } catch (IOException e) {

                                                throw new RuntimeException(e);

                                            }

                                            try {

                                                connection.setRequestMethod("GET");

                                            } catch (ProtocolException e) {

                                                throw new RuntimeException(e);

                                            }


// Ajouter le header Authorization avec le token

                                            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoidGVzdCIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL2VtYWlsYWRkcmVzcyI6InRlc3QudXNlckB0ZXN0LmNvbSIsImV4cCI6MTY5NjYwMDY5MiwiaXNzIjoiand0dGVzdC5jb20iLCJhdWQiOiJ0cnlzdGFud2lsY29jay5jb20ifQ.LQ6yfa0InJi6N5GjRfVcA8XMZtZZef0PswrM2Io7l-g";

                                            connection.setRequestProperty("Authorization", "Bearer " + token);


                                            try {

                                                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                                    InputStream inputStream = connection.getInputStream();

                                                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                                                    StringBuilder response = new StringBuilder();

                                                    String line;


                                                    while ((line = bufferedReader.readLine()) != null) {
                                                        response.append(line);

                                                    }

                                                    bufferedReader.close();

                                                    inputStream.close();


// Mapper le JSON sur un objet Train

                                                    Train train = mapper.readValue(response.toString(), Train.class);


                                                    List<Result> results = train.getResults();

                                                    int size = results.size();


                                                    for (int i = 0; i < size; i++) {

                                                        Result result = results.get(i);

                                                        String dateid = result.getDate();


                                                        if (existingResultIdss.contains(dateid)) {


                                                            continue;

                                                        }


                                                        String dateTimeString = dateid.substring(0, 19);

                                                        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatterrs);

                                                        Date formattedDateTime = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());


// extraire la date et la convertir en java.util.Date

                                                        String[] parts = dateTimeString.split("T");

                                                        String datePart = parts[0]; // "2023-04-14"

                                                        String timePart = parts[1]; // "14:04:05"


                                                        SimpleDateFormat dateFormatterr = new SimpleDateFormat("yyyy-MM-dd");

                                                        Date datefichier = dateFormatterr.parse(datePart);


                                                        SimpleDateFormat timeFormatterr = new SimpleDateFormat("HH:mm:ss");

                                                        Date timefichier = timeFormatterr.parse(timePart);


// Ajoutez l'ID du résultat à la liste des résultats existants

                                                        existingResultIdss.add(dateid);


// Convertir les objets Date en objets Time

                                                        Time heurefichier = new Time(timefichier.getTime());


// Vérifier si une instance de Train avec la même date, heure et site existe déjà

                                                        List<Train> existingTrain = trainRepository.findBySiteAndDateFichierAndHeureFichier("Chevilly", datefichier, heurefichier);

                                                        if (!existingTrain.isEmpty()) {


                                                            continue;

                                                        }


                                                        Train trainInstance = new Train(); // Créer une nouvelle instance de Train

                                                        trainInstance.setDateFichier(datefichier);

                                                        trainInstance.setHeureFichier(timefichier);

                                                        trainInstance.setSite("Chevilly");


                                                        result.setTrain(trainInstance); // Définir la relation train dans Result


                                                        trainInstance.getResults().add(result);


                                                        trainService.save(trainInstance); // Sauvegarder chaque instance de Train séparément

                                                        resultService.save(result); // Sauvegarder chaque instance de Result séparément


                                                    }

                                                } else {

                                                    String logMesg = "Error response code: " + connection.getResponseCode();

                                                    if (isIntelliJ) {
                                                        System.out.println(logMesg);  // Affiche dans la console (syso) d'IntelliJ
                                                    } else {
                                                        logger.info(logMesg);  // Affiche dans le logger du serveur
                                                    }


                                                }

                                            } catch (IOException e) {

                                                throw new RuntimeException(e);

                                            } catch (ParseException e) {

                                                throw new RuntimeException(e);

                                            } finally {

                                                connection.disconnect();

                                            }


                                        }


                                    } catch (JsonParseException e) {
                                        // Déplacer le fichier JSON dans le répertoire d'échec
                                        moveFileToFailureDirectory(samFile, new File(echecFolderPath));
                                        String logMesg = "Erreur lors de la lecture du fichier " + samFile.getName() + " : " + e.getMessage() + " , " + e;

                                        if (isIntelliJ) {
                                            System.out.println(logMesg);  // Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMesg);  // Affiche dans le logger du serveur
                                        }

                                    } catch (IOException e) {

                                        String logMesg = "Erreur lors de la lecture du fichier " + samFile.getName() + " : " + e.getMessage() + " , " + e;

                                        if (isIntelliJ) {
                                            System.out.println(logMesg);  // Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMesg);  // Affiche dans le logger du serveur
                                        }

                                    }
                                }

                            }

                        }

                        }





// Lire tous les fichiers commençant par '50592'

                        File[] m50592Filess = inputFolder.listFiles((dir, name) -> name.startsWith("50592") && name.endsWith(".json"));
                        String regex = "50592-.+_\\d{4}\\.\\d{2}\\.\\d{2}_\\d{2}h\\d{2}m\\d{2}s_Résultats-1xTint\\.json";


                        if (m50592Filess != null) {

                            for (File m50592File : m50592Filess) {
                                if (!m50592File.getName().matches(regex)) {
                                    // Le nom de fichier ne correspond pas au format spécifié
                                    File targetFileechec = new File(echecFolderPath, m50592File.getName());
                                    try {
                                        String logMessage = "Le nom de fichier " + m50592File.getName() + " ne correspond pas au format spécifié ";
                                        Files.move(m50592File.toPath(), targetFileechec.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        String logMessageerreur = "Le déplacement du fichier " + m50592File.getName() + " dans le répertoire d'echec "+targetFileechec.getAbsolutePath()+" est OK";
                                        if (isIntelliJ) {
                                            System.out.println(logMessage);
                                            System.out.println(logMessageerreur);// Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMessage);  // Affiche dans le logger du serveur
                                            logger.info(logMessageerreur);
                                        }
                                    } catch (IOException e) {
                                        String logMessage = "Erreur lors du déplacement du fichier vers le dossier 'echec' : " + e.getMessage();

                                        if (isIntelliJ) {
                                            System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMessage);  // Affiche dans le logger du serveur
                                        }
                                    }
                                } else {
                                    File targetFile = new File(outputFolder, m50592File.getName());

                                    if (targetFile.exists()) {
                                        File targetFileechec = new File(echecFolderPath, m50592File.getName());
                                        try {
                                            String logMessage = "Le fichier cible existe déjà : " + targetFileechec.getAbsolutePath();
                                            Files.move(m50592File.toPath(), targetFileechec.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                            String logMessageerreur = "Le déplacement du fichier " + m50592File.getName() + " dans le répertoire d'echec "+targetFileechec.getAbsolutePath()+" est OK";
                                            if (isIntelliJ) {
                                                System.out.println(logMessage);
                                                System.out.println(logMessageerreur);// Affiche dans la console (syso) d'IntelliJ
                                            } else {
                                                logger.info(logMessage);  // Affiche dans le logger du serveur
                                                logger.info(logMessageerreur);
                                            }


                                        } catch (IOException e) {

                                            String logMessage = "Erreur lors du déplacement du fichier existant vers le dossier 'echec' : " + e.getMessage();

                                            if (isIntelliJ) {
                                                System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                            } else {
                                                logger.info(logMessage);  // Affiche dans le logger du serveur
                                            }

                                        }
                                    } else {
                                        String logMesg = "Le fichier " + m50592File.getName() + " est OK";

                                        if (isIntelliJ) {
                                            System.out.println(logMesg);  // Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMesg);  // Affiche dans le logger du serveur
                                        }

                                        TypeReference<List<M_50592>> m50592TypeRef = new TypeReference<List<M_50592>>() {
                                        };


                                        try (InputStream m50592Stream = new FileInputStream(m50592File)) {

                                            List<M_50592> m_50592s = mapper.readValue(m50592Stream, m50592TypeRef);


                                            for (M_50592 m_50592 : m_50592s) {
                                                logger.info("le fichier à traiter " + m_50592.getFileName());
                                                System.out.println("le fichier à traiter " + m_50592.getFileName());

                                                m_50592.setFileName(m50592File.getName()); // Définir le nom de fichier dans l'objet M_50592

                                                m_50592.loadStartingWith50592(m50592File.getName());

                                                m_50592.loadSite(m50592File.getName());


                                                Environnement env = m_50592.getEnvironnement();

                                                String[] villes = env.extraireVilles(env.getSens());

                                                if (villes != null) {

                                                    env.setVilleDepart(villes[0]);

                                                    env.setVilleArrivee(villes[1]);

                                                }


                                                if (m_50592.getBeR1().getxFond().contains("FF382A") || m_50592.getBeR1().getyFond().contains("FF382A") || m_50592.getBeR1().getzFond().contains("FF382A") || m_50592.getBeR2().getxFond1().contains("FF382A") || m_50592.getBeR2().getyFond1().contains("FF382A") || m_50592.getBeR2().getzFond1().contains("FF382A") || m_50592.getBlR1().getxFondl().contains("FF382A") || m_50592.getBlR1().getyFondl().contains("FF382A") || m_50592.getBlR1().getzFondl().contains("FF382A") || m_50592.getBlR2().getxFondl2().contains("FF382A") || m_50592.getBlR2().getyFondl2().contains("FF382A") || m_50592.getBlR2().getzFondl2().contains("FF382A")) {

                                                    m_50592.setStatut50592("NOK");

                                                } else {

                                                    m_50592.setStatut50592("OK");

                                                }



                                                m50592Service.save(m_50592);
                                                if (m50592Service.findById(m_50592.getId()) != null) {
                                                    logger.info("Le fichier a été enregistré dans la base de données: " + m_50592.getFileName());
                                                    System.out.println("Le fichier a été enregistré dans la base de données: " + m_50592.getFileName());
                                                    deplacerFichier(m50592File, outputFolder);
                                                    File targetFolder = getTargetFolder(outputFolder, m50592File.getName());
                                                    String jsonFilePath = new File(targetFolder, m50592File.getName()).getAbsolutePath();
                                                    String jsonFileName = m_50592.getFileName().substring(0, m_50592.getFileName().lastIndexOf('.'));
                                                    File outputFolderFile = new File(jsonFilePath).getParentFile(); // Obtenir le dossier parent du fichier JSON
                                                    String jsonFolderPath = new File(outputFolderFile, jsonFileName).getAbsolutePath(); // Récupérer le chemin du dossier correspondant au fichier JSON
                                                    File outputFolderFilee = new File(jsonFolderPath); // Créer un objet File pour le dossier correspondant

                                                    // Mettre à jour le chemin avec le nouveau dossier créé
                                                    String url50592 = jsonFolderPath.replace("\\", "/");
                                                    m_50592.setUrl50592(url50592);






// Vérifier si le nom du fichier image correspondant contient le nom du fichier JSON

                                                    File[] imageFiles = inputFolder.listFiles((dir, name) -> name.contains(jsonFileName) && (name.endsWith(".png") || name.endsWith(".bmp")));

                                                    if (imageFiles.length > 0) {


                                                        String logMesgg = "Il y a des images, création du répertoire {} a été créé => OK" + " , " + outputFolderFilee;

                                                        if (isIntelliJ) {
                                                            System.out.println(logMesgg);  // Affiche dans la console (syso) d'IntelliJ
                                                        } else {
                                                            logger.info(logMesgg);  // Affiche dans le logger du serveur
                                                        }


// Créer le dossier correspondant au fichier JSON

                                                        // Création récursive du dossier correspondant au fichier JSON
                                                        boolean folderCreated = outputFolderFilee.mkdir(); // Utiliser mkdirs() sur le parent directement


                                                        if (folderCreated) {


// Déplacer les fichiers d'image dans le dossier correspondant

                                                            for (File imageFile : imageFiles) {

                                                                try {
                                                                    Thread.sleep(1000);
                                                                } catch (InterruptedException e) {
                                                                    e.printStackTrace();
                                                                }


                                                                File destFile = new File(outputFolderFilee, imageFile.getName());

                                                                boolean fileMoved = imageFile.renameTo(destFile);

                                                                if (fileMoved) {
                                                                    String logMsgimage = "Déplacement de l'image {} => OK" + " , " + imageFile.getName();

                                                                    if (isIntelliJ) {
                                                                        System.out.println(logMsgimage);  // Affiche dans la console (syso) d'IntelliJ
                                                                    } else {
                                                                        logger.info(logMsgimage);  // Affiche dans le logger du serveur
                                                                    }

                                                                } else {
                                                                    String logMsgimage = "Impossible de déplacer le fichier image : " + imageFile.getAbsolutePath();

                                                                    if (isIntelliJ) {
                                                                        System.out.println(logMsgimage);  // Affiche dans la console (syso) d'IntelliJ
                                                                    } else {
                                                                        logger.info(logMsgimage);  // Affiche dans le logger du serveur
                                                                    }


                                                                }

                                                            }


                                                        } else {
                                                            String logMsgimage = "Impossible de créer le dossier : " + outputFolderFile.getAbsolutePath();

                                                            if (isIntelliJ) {
                                                                System.out.println(logMsgimage);  // Affiche dans la console (syso) d'IntelliJ
                                                            } else {
                                                                logger.info(logMsgimage);  // Affiche dans le logger du serveur
                                                            }


                                                        }

                                                    }




                                                    // Enregistrer l'objet m_50592 dans la base de données
                                                    m50592Service.save(m_50592);




                                                } else {
                                                    logger.error("Erreur lors de l'enregistrement du fichier dans la base de données: " + m_50592.getFileName());
                                                    System.out.println("Erreur lors de l'enregistrement du fichier dans la base de données: " + m_50592.getFileName());
                                                }


                                                //train

                                                Set<String> existingResultIdss = new HashSet<>();
                                                DateTimeFormatter formatterrs = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                                                LocalDateTime m50592DateTime = LocalDateTime.parse(m_50592.getDateFichier() + "T" + m_50592.getHeureFichier());

                                                String url = "https://test01.rd-vision-dev.com/get_images?system=2&dateFrom=" +
                                                        m50592DateTime.minusMinutes(1) + "&dateTo=" + m50592DateTime.plusMinutes(1);

                                                logger.info("l'url passé est "+url);
                                                URL jsonUrl;

                                                try {

                                                    jsonUrl = new URL(url);


                                                } catch (MalformedURLException e) {
                                                    throw new RuntimeException(e);
                                                }

                                                HttpURLConnection connection = null;

                                                try {

                                                    connection = (HttpURLConnection) jsonUrl.openConnection();

                                                } catch (IOException e) {

                                                    throw new RuntimeException(e);

                                                }

                                                try {

                                                    connection.setRequestMethod("GET");

                                                } catch (ProtocolException e) {

                                                    throw new RuntimeException(e);

                                                }


// Ajouter le header Authorization avec le token

                                                String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoidGVzdCIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL2VtYWlsYWRkcmVzcyI6InRlc3QudXNlckB0ZXN0LmNvbSIsImV4cCI6MTY5NjYwMDY5MiwiaXNzIjoiand0dGVzdC5jb20iLCJhdWQiOiJ0cnlzdGFud2lsY29jay5jb20ifQ.LQ6yfa0InJi6N5GjRfVcA8XMZtZZef0PswrM2Io7l-g";

                                                connection.setRequestProperty("Authorization", "Bearer " + token);


                                                try {

                                                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                                        InputStream inputStream = connection.getInputStream();

                                                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                                                        StringBuilder response = new StringBuilder();

                                                        String line;


                                                        while ((line = bufferedReader.readLine()) != null) {
                                                            response.append(line);

                                                        }

                                                        bufferedReader.close();

                                                        inputStream.close();


// Mapper le JSON sur un objet Train

                                                        Train train = mapper.readValue(response.toString(), Train.class);


                                                        List<Result> results = train.getResults();

                                                        int size = results.size();


                                                        for (int i = 0; i < size; i++) {

                                                            Result result = results.get(i);

                                                            String dateid = result.getDate();


// Effectuez une vérification pour déterminer si l'ID du résultat existe déjà

                                                            if (existingResultIdss.contains(dateid)) {


                                                                continue;

                                                            }


                                                            String dateTimeString = dateid.substring(0, 19);

                                                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatterrs);

                                                            Date formattedDateTime = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());


// extraire la date et la convertir en java.util.Date

                                                            String[] parts = dateTimeString.split("T");

                                                            String datePart = parts[0]; // "2023-04-14"

                                                            String timePart = parts[1]; // "14:04:05"


                                                            SimpleDateFormat dateFormatterr = new SimpleDateFormat("yyyy-MM-dd");

                                                            Date datefichier = dateFormatterr.parse(datePart);


                                                            SimpleDateFormat timeFormatterr = new SimpleDateFormat("HH:mm:ss");

                                                            Date timefichier = timeFormatterr.parse(timePart);


// Ajoutez l'ID du résultat à la liste des résultats existants

                                                            existingResultIdss.add(dateid);


// Convertir les objets Date en objets Time

                                                            Time heurefichier = new Time(timefichier.getTime());


// Vérifier si une instance de Train avec la même date, heure et site existe déjà

                                                            List<Train> existingTrain = trainRepository.findBySiteAndDateFichierAndHeureFichier("Chevilly", datefichier, heurefichier);

                                                            if (!existingTrain.isEmpty()) {


                                                                continue;

                                                            }


                                                            Train trainInstance = new Train(); // Créer une nouvelle instance de Train

                                                            trainInstance.setDateFichier(datefichier);

                                                            trainInstance.setHeureFichier(timefichier);

                                                            trainInstance.setSite("Chevilly");


                                                            result.setTrain(trainInstance); // Définir la relation train dans Result


                                                            trainInstance.getResults().add(result);


                                                            trainService.save(trainInstance); // Sauvegarder chaque instance de Train séparément

                                                            resultService.save(result); // Sauvegarder chaque instance de Result séparément


                                                        }

                                                    } else {
                                                        String logMessagetrain = "Error response code: " + connection.getResponseCode();

                                                        if (isIntelliJ) {
                                                            System.out.println(logMessagetrain);  // Affiche dans la console (syso) d'IntelliJ
                                                        } else {
                                                            logger.info(logMessagetrain);  // Affiche dans le logger du serveur
                                                        }


                                                    }

                                                } catch (IOException e) {

                                                    throw new RuntimeException(e);

                                                } catch (ParseException e) {

                                                    throw new RuntimeException(e);

                                                } finally {

                                                    connection.disconnect();

                                                }


                                            }


                                        } catch (JsonParseException e) {
                                            // Déplacer le fichier JSON dans le répertoire d'échec
                                            moveFileToFailureDirectory(m50592File, new File(echecFolderPath));

                                            String logMessagetrain = "Erreur lors de la lecture du fichier " + m50592File.getName() + " : " + e.getMessage() + " , " + e;

                                            if (isIntelliJ) {
                                                System.out.println(logMessagetrain);  // Affiche dans la console (syso) d'IntelliJ
                                            } else {
                                                logger.info(logMessagetrain);  // Affiche dans le logger du serveur
                                            }


                                        } catch (IOException e) {

                                            String logMessagetrain = "Erreur lors de la lecture du fichier " + m50592File.getName() + " : " + e.getMessage() + " , " + e;

                                            if (isIntelliJ) {
                                                System.out.println(logMessagetrain);  // Affiche dans la console (syso) d'IntelliJ
                                            } else {
                                                logger.info(logMessagetrain);  // Affiche dans le logger du serveur
                                            }

                                        }


                                    }

                                }
                            }



                        }

//le cas ou les fichiers ont etait déposé après les fichiers json

                        // Vérifier les fichiers d'images correspondants dans le dossier "input"
                        File[] imageFiles = inputFolder.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".bmp"));
                        if (imageFiles != null && imageFiles.length > 0) {
                            // Parcourir les dossiers de l'output
                            File[] yearMonthFolders = outputFolder.listFiles(File::isDirectory);
                            if (yearMonthFolders != null) {
                                for (File yearMonthFolder : yearMonthFolders) {
                                    // Extraire l'année et le mois à partir du nom du dossier
                                    String yearMonth = yearMonthFolder.getName();

                                    // Créer un filtre de noms de fichiers pour les fichiers commençant par "50592" et se terminant par ".json"
                                    FilenameFilter jsonFilter = (dir, name) -> name.startsWith("50592") && name.endsWith(".json");

                                    // Rechercher les fichiers JSON correspondant au filtre dans le dossier "yearMonthFolder"
                                    File[] jsonFiles = yearMonthFolder.listFiles(jsonFilter);
                                    if (jsonFiles != null) {
                                        for (File jsonFile : jsonFiles) {
                                            String jsonFileName = jsonFile.getName();
                                            String jsonFileBaseName = jsonFileName.substring(0, jsonFileName.lastIndexOf('.'));

                                            for (File imageFile : imageFiles) {
                                                // Vérifier si le nom du fichier image correspondant contient le nom du fichier JSON
                                                if (imageFile.getName().contains(jsonFileBaseName)) {
                                                    // Créer le dossier correspondant au fichier JSON
                                                    File jsonFolder = new File(yearMonthFolder, jsonFileBaseName);
                                                    if (!jsonFolder.exists() && jsonFolder.mkdir()) {
                                                        System.out.println("Dossier créé : " + jsonFolder.getAbsolutePath());
                                                    }

                                                    // Déplacer l'image dans le dossier correspondant
                                                    File destFile = new File(jsonFolder, imageFile.getName());
                                                    if (imageFile.renameTo(destFile)) {
                                                        System.out.println("Image déplacée avec succès : " + imageFile.getAbsolutePath());
                                                    } else {
                                                        System.out.println("Échec du déplacement de l'image : " + imageFile.getAbsolutePath());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        filesToMove.clear(); // Vider la liste filesToMove









                        inputFolderPathh.register(watchService,

                                StandardWatchEventKinds.ENTRY_CREATE,

                                StandardWatchEventKinds.ENTRY_MODIFY,

                                StandardWatchEventKinds.ENTRY_DELETE);





                        key.reset(); // Réinitialiser la clé pour continuer à surveiller les événements


                    }


                } catch (RuntimeException e) {



                } catch (InterruptedException e) {

                    throw new RuntimeException(e);

                } catch (IOException e) {

                    throw new RuntimeException(e);

                }


            });



// Démarrer les threads de surveillance et de traitement

            watchThread.start();





// Déplacer les fichiers existants dans le répertoire "input" avant de commencer la surveillance

            File[] existingFiles = inputFolder.listFiles();




// Vérifier si le répertoire "existing" n'est pas vide

            if (existingFiles != null && existingFiles.length > 0 ) {


                // Liste pour stocker les numéros de train traités

                List<String> processedTrainNumberss = new ArrayList<>();


// Lire les données de la base de données pour la comparaison avec les nouvelles données

                List<Mr> allMrDatas = mrService.findAll();

                for (Mr mr : allMrDatas) {

                    processedTrainNumberss.add(mr.getNumTrain());

                }


// Lire les fichiers Excel et mettre à jour les données des trains correspondants

                File[] excelFiless = inputFolder.listFiles((dir, name) -> name.endsWith(".xlsx"));
                String regex2 = "IHM\\s*_Base\\s*de\\s*données\\s*SYRENE_V\\d+\\.xlsx";



                if (excelFiless != null) {

                    for (File excelFile : excelFiless) {
                        if (!excelFile.getName().matches(regex2)) {
                            // Le nom de fichier ne correspond pas au format spécifié
                            File targetFileechec = new File(echecFolderPath, excelFile.getName());
                            try {
                                String logMessage = "Le nom de fichier " + excelFile.getName() + " ne correspond pas au format spécifié ";
                                Files.move(excelFile.toPath(), targetFileechec.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                String logMessageerreur = "Le déplacement du fichier " + excelFile.getName() + " dans le répertoire d'echec "+targetFileechec.getAbsolutePath()+" est OK";
                                if (isIntelliJ) {
                                    System.out.println(logMessage);
                                    System.out.println(logMessageerreur);// Affiche dans la console (syso) d'IntelliJ
                                } else {
                                    logger.info(logMessage);  // Affiche dans le logger du serveur
                                    logger.info(logMessageerreur);
                                }
                            } catch (IOException e) {
                                String logMessage = "Erreur lors du déplacement du fichier vers le dossier 'echec' : " + e.getMessage();

                                if (isIntelliJ) {
                                    System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                } else {
                                    logger.info(logMessage);  // Affiche dans le logger du serveur
                                }
                            }
                        } else {
                            File targetFile = new File(outputFolder, excelFile.getName());

                            if (targetFile.exists()) {
                                File targetFileechec = new File(echecFolderPath, excelFile.getName());
                                try {
                                    String logMessage = "Le fichier cible existe déjà : " + targetFileechec.getAbsolutePath();
                                    Files.move(excelFile.toPath(), targetFileechec.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    String logMessageerreur = "Le déplacement du fichier " + excelFile.getName() + " dans le répertoire d'echec "+targetFileechec.getAbsolutePath()+" est OK";
                                    if (isIntelliJ) {
                                        System.out.println(logMessage);
                                        System.out.println(logMessageerreur);// Affiche dans la console (syso) d'IntelliJ
                                    } else {
                                        logger.info(logMessage);  // Affiche dans le logger du serveur
                                        logger.info(logMessageerreur);
                                    }


                                } catch (IOException e) {

                                    String logMessage = "Erreur lors du déplacement du fichier existant vers le dossier 'echec' : " + e.getMessage();

                                    if (isIntelliJ) {
                                        System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                    } else {
                                        logger.info(logMessage);  // Affiche dans le logger du serveur
                                    }

                                }
                            } else {

                                try (FileInputStream excelStream = new FileInputStream(excelFile)) {
                                    System.out.println("logMessage");

                                    Workbook workbook = new XSSFWorkbook(excelStream);

                                    Sheet sheet = workbook.getSheetAt(0);

                                    for (Row row : sheet) {

                                        if (row.getRowNum() > 0) {

                                            Cell numTrainCell = row.getCell(0);

                                            String numTrain = null;

                                            if (numTrainCell.getCellType() == CellType.STRING) {

                                                numTrain = numTrainCell.getStringCellValue();

                                            } else if (numTrainCell.getCellType() == CellType.NUMERIC) {

                                                numTrain = String.valueOf((int) numTrainCell.getNumericCellValue());

                                            }


                                            String mr = row.getCell(1).getStringCellValue();


                                            if (!processedTrainNumberss.contains(numTrain)) {


// Si le numéro de train n'a pas encore été traité, ajouter une nouvelle entrée dans la base de données

                                                Mr newMr = new Mr();

                                                newMr.setMr(mr);

                                                newMr.setNumTrain(numTrain);

                                                mrService.save(newMr);


                                            }

                                        }





                                    }


                                } catch (IOException e) {
                                    String logMessage = "Erreur lors de la lecture du fichier Excel : " + excelFile.getAbsolutePath() + " , " + e;

                                    if (isIntelliJ) {
                                        System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                    } else {
                                        logger.error(logMessage);  // Affiche dans le logger du serveur
                                    }


                                }
                            }


                        }

                        //deplacement de fichier dans output
                        File targetFolder = getTargetFolder(outputFolder, excelFile.getName());
                        File targetFileexcel = new File(targetFolder, excelFile.getName());
                        try {
                            Files.move(excelFile.toPath(), targetFileexcel.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            String logMessage = "Le fichier " + excelFile.getName() + " a été déplacé vers " + targetFileexcel.getAbsolutePath();
                            if (isIntelliJ) {
                                System.out.println(logMessage);
                            } else {
                                logger.info(logMessage);
                            }
                        } catch (IOException e) {
                            // Gérer l'exception en cas d'erreur de déplacement du fichier
                            e.printStackTrace();
                        }
                    }

                }





                EnvloppeData enveloppeDatas = new EnvloppeData();

// Lire tous les fichiers commençant par 'Sam'

                File[] samFiless = inputFolder.listFiles((dir, name) -> name.startsWith("SAM005") && name.endsWith(".json"));
                String regex1 = "SAM005-.+_\\d{4}\\.\\d{2}\\.\\d{2}_\\d{2}h\\d{2}m\\d{2}s.json";
                if (samFiless != null ) {

                    for (File samFile : samFiless) {

                        if (!samFile.getName().matches(regex1)) {
                            // Le nom de fichier ne correspond pas au format spécifié
                            File targetFileechec = new File(echecFolderPath, samFile.getName());
                            try {
                                String logMessage = "Le nom de fichier " + samFile.getName() + " ne correspond pas au format spécifié ";
                                Files.move(samFile.toPath(), targetFileechec.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                String logMessageerreur = "Le déplacement du fichier " + samFile.getName() + " dans le répertoire d'echec "+targetFileechec.getAbsolutePath()+" est OK";
                                if (isIntelliJ) {
                                    System.out.println(logMessage);
                                    System.out.println(logMessageerreur);// Affiche dans la console (syso) d'IntelliJ
                                } else {
                                    logger.info(logMessage);  // Affiche dans le logger du serveur
                                    logger.info(logMessageerreur);
                                }
                            } catch (IOException e) {
                                String logMessage = "Erreur lors du déplacement du fichier vers le dossier 'echec' : " + e.getMessage();

                                if (isIntelliJ) {
                                    System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                } else {
                                    logger.info(logMessage);  // Affiche dans le logger du serveur
                                }
                            }
                        } else {
                            File targetFile = new File(outputFolder, samFile.getName());

                            if (targetFile.exists()) {
                                File targetFileechec = new File(echecFolderPath, samFile.getName());
                                try {
                                    String logMessage = "Le fichier cible existe déjà : " + targetFileechec.getAbsolutePath();
                                    Files.move(samFile.toPath(), targetFileechec.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    String logMessageerreur = "Le déplacement du fichier " + samFile.getName() + " dans le répertoire d'echec "+targetFileechec.getAbsolutePath()+" est OK";
                                    if (isIntelliJ) {
                                        System.out.println(logMessage);
                                        System.out.println(logMessageerreur);// Affiche dans la console (syso) d'IntelliJ
                                    } else {
                                        logger.info(logMessage);  // Affiche dans le logger du serveur
                                        logger.info(logMessageerreur);
                                    }


                                } catch (IOException e) {

                                    String logMessage = "Erreur lors du déplacement du fichier existant vers le dossier 'echec' : " + e.getMessage();

                                    if (isIntelliJ) {
                                        System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                    } else {
                                        logger.info(logMessage);  // Affiche dans le logger du serveur
                                    }

                                }
                            } else {
// Charger les enveloppes à partir du fichier JSON

                                String logMessagetrain = "Le fichier " + samFile.getName() + " est OK";

                                if (isIntelliJ) {
                                    System.out.println(logMessagetrain);  // Affiche dans la console (syso) d'IntelliJ
                                } else {
                                    logger.info(logMessagetrain);  // Affiche dans le logger du serveur
                                }

                                TypeReference<List<Sam>> samTypeRef = new TypeReference<List<Sam>>() {
                                };


                                try (InputStream samStream = new FileInputStream(samFile)) {

                                    List<Sam> samss = mapper.readValue(samStream, samTypeRef);

// Déclarer une variable pour suivre l'incrémentation de NbOccultations
                                    int counter = 0;
                                    for (Sam sam : samss) {

                                        logger.info("le fichier à traiter " + sam.getFileName());
                                        System.out.println("le fichier à traiter " + sam.getFileName());
                                        sam.checkOccultations();

                                        sam.setFileName(samFile.getName()); // Définir le nom de fichier dans l'objet M_50592

                                        sam.loadStartingWithSam(samFile.getName());

                                        sam.loadSite(samFile.getName());

                                        NbOccultations nbOccultations = new NbOccultations();
                                        nbOccultations.setNbOccultations(++counter);
                                        if (sam.getStatutSAM().equals("OK")) {

                                            sam.setUrlSam(null); // Définir l'URL à null

                                        }

                                        samService.save(sam);

                                        if (samService.findById(sam.getId()) != null) {
                                            logger.info("Le fichier a été enregistré dans la base de données: " + sam.getFileName());
                                            System.out.println("Le fichier a été enregistré dans la base de données: " + sam.getFileName());
                                            deplacerFichier(samFile, outputFolder);
                                            File targetFolder = getTargetFolder(outputFolder, samFile.getName());
                                            String jsonFilePath = new File(targetFolder, samFile.getName()).getAbsolutePath();
                                            String jsonFileName = sam.getFileName().substring(0, sam.getFileName().lastIndexOf('.'));
                                            File outputFolderFile = new File(jsonFilePath).getParentFile(); // Obtenir le dossier parent du fichier JSON



                                            if (sam.getStatutSAM().equals("NOK")) {

                                                File outputFolderEnvelope = new File(outputFolderFile, sam.getFileName().replace(".json", "") + "_enveloppes");
                                                outputFolderEnvelope.mkdir();
                                                String logMessa = "Création du dossier contenant les capteurs {} => OK" + " , " + outputFolderEnvelope;
                                                if (isIntelliJ) {
                                                    System.out.println(logMessa);  // Affiche dans la console (syso) d'IntelliJ
                                                } else {
                                                    logger.info(logMessa);  // Affiche dans le logger du serveur
                                                }

                                                // Mettre à jour le chemin avec le nouveau dossier créé
                                                String urlsam = outputFolderEnvelope.getAbsolutePath().replace("\\", "/");
                                                sam.setUrlSam(urlsam);
                                                samService.save(sam);

                                                for (int i = 1; i <= sam.getNbOccultations().size(); i++) {
                                                    // Charger le fichier JSON depuis le nouvel emplacement
                                                    File samFileNewLocation = new File(outputFolderFile, samFile.getName());

                                                    enveloppeDatas.loadFromJson(samFileNewLocation, i);

                                                    // Créer le nom du fichier de sortie pour ce traitement spécifique
                                                    String outputFileName = samFile.getName().replace("SAM005", "SAMCapteur" + i);
                                                    File outputFile = new File(outputFolderEnvelope, outputFileName);

                                                    String logMessageok = "Création du capteur {} => OK" + " , " + outputFileName;
                                                    if (isIntelliJ) {
                                                        System.out.println(logMessageok);  // Affiche dans la console (syso) d'IntelliJ
                                                    } else {
                                                        logger.info(logMessageok);  // Affiche dans le logger du serveur
                                                    }

                                                    // Vérifier si le fichier de sortie existe déjà
                                                    if (!outputFile.exists()) {
                                                        double step = 6.0; // step peut être changé selon vos besoins
                                                        enveloppeDatas.saveSampledToJson(outputFile, step);
                                                    }
                                                }
                                            }
                                        } else {
                                            logger.error("Erreur lors de l'enregistrement du fichier dans la base de données: " + sam.getFileName());
                                            System.out.println("Erreur lors de l'enregistrement du fichier dans la base de données: " + sam.getFileName());
                                        }

                                        Set<String> existingResultIdss = new HashSet<>();
                                        DateTimeFormatter formatterrs = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

                                        LocalDateTime samDateTime = LocalDateTime.parse(sam.getDateFichier() + "T" + sam.getHeureFichier());

                                        String url = "https://test01.rd-vision-dev.com/get_images?system=2&dateFrom=" +
                                                samDateTime.minusMinutes(1) + "&dateTo=" + samDateTime.plusMinutes(1);

                                        logger.info("l'url passé est "+url);
                                        URL jsonUrl;

                                        try {

                                            jsonUrl = new URL(url);


                                        } catch (MalformedURLException e) {
                                            throw new RuntimeException(e);
                                        }

                                        HttpURLConnection connection = null;

                                        try {

                                            connection = (HttpURLConnection) jsonUrl.openConnection();

                                        } catch (IOException e) {

                                            throw new RuntimeException(e);

                                        }

                                        try {

                                            connection.setRequestMethod("GET");

                                        } catch (ProtocolException e) {

                                            throw new RuntimeException(e);

                                        }


// Ajouter le header Authorization avec le token

                                        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoidGVzdCIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL2VtYWlsYWRkcmVzcyI6InRlc3QudXNlckB0ZXN0LmNvbSIsImV4cCI6MTY5NjYwMDY5MiwiaXNzIjoiand0dGVzdC5jb20iLCJhdWQiOiJ0cnlzdGFud2lsY29jay5jb20ifQ.LQ6yfa0InJi6N5GjRfVcA8XMZtZZef0PswrM2Io7l-g";

                                        connection.setRequestProperty("Authorization", "Bearer " + token);


                                        try {

                                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                                InputStream inputStream = connection.getInputStream();

                                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                                                StringBuilder response = new StringBuilder();

                                                String line;

                                                while ((line = bufferedReader.readLine()) != null) {

                                                    response.append(line);


                                                }

                                                bufferedReader.close();

                                                inputStream.close();


// Mapper le JSON sur un objet Train

                                                Train train = mapper.readValue(response.toString(), Train.class);


                                                List<Result> results = train.getResults();

                                                int size = results.size();


                                                for (int i = 0; i < size; i++) {

                                                    Result result = results.get(i);

                                                    String dateid = result.getDate();


// Effectuez une vérification pour déterminer si l'ID du résultat existe déjà

                                                    if (existingResultIdss.contains(dateid)) {


                                                        continue;

                                                    }


                                                    String dateTimeString = dateid.substring(0, 19);

                                                    LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatterrs);

                                                    Date formattedDateTime = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());


// extraire la date et la convertir en java.util.Date

                                                    String[] parts = dateTimeString.split("T");

                                                    String datePart = parts[0]; // "2023-04-14"

                                                    String timePart = parts[1]; // "14:04:05"


                                                    SimpleDateFormat dateFormatterr = new SimpleDateFormat("yyyy-MM-dd");

                                                    Date datefichier = dateFormatterr.parse(datePart);


                                                    SimpleDateFormat timeFormatterr = new SimpleDateFormat("HH:mm:ss");

                                                    Date timefichier = timeFormatterr.parse(timePart);


// Ajoutez l'ID du résultat à la liste des résultats existants

                                                    existingResultIdss.add(dateid);


// Convertir les objets Date en objets Time

                                                    Time heurefichier = new Time(timefichier.getTime());


// Vérifier si une instance de Train avec la même date, heure et site existe déjà

                                                    List<Train> existingTrain = trainRepository.findBySiteAndDateFichierAndHeureFichier("Chevilly", datefichier, heurefichier);

                                                    if (!existingTrain.isEmpty()) {


                                                        continue;

                                                    }


                                                    Train trainInstance = new Train(); // Créer une nouvelle instance de Train

                                                    trainInstance.setDateFichier(datefichier);

                                                    trainInstance.setHeureFichier(timefichier);

                                                    trainInstance.setSite("Chevilly");


                                                    result.setTrain(trainInstance); // Définir la relation train dans Result


                                                    trainInstance.getResults().add(result);


                                                    trainService.save(trainInstance); // Sauvegarder chaque instance de Train séparément

                                                    resultService.save(result); // Sauvegarder chaque instance de Result séparément


                                                }

                                            } else {
                                                String logMessagesam = "Error response code: " + connection.getResponseCode();

                                                if (isIntelliJ) {
                                                    System.out.println(logMessagesam);  // Affiche dans la console (syso) d'IntelliJ
                                                } else {
                                                    logger.info(logMessagesam);  // Affiche dans le logger du serveur
                                                }


                                            }

                                        } catch (IOException e) {

                                            throw new RuntimeException(e);

                                        } catch (ParseException e) {

                                            throw new RuntimeException(e);

                                        } finally {

                                            connection.disconnect();

                                        }

                                    }


                                } catch (JsonParseException e) {
                                    // Déplacer le fichier JSON dans le répertoire d'échec
                                    moveFileToFailureDirectory(samFile, new File(echecFolderPath));
                                    String logMessagesam = "Erreur lors de la lecture du fichier " + samFile.getName() + " : " + e.getMessage() + " , " + e;

                                    if (isIntelliJ) {
                                        System.out.println(logMessagesam);  // Affiche dans la console (syso) d'IntelliJ
                                    } else {
                                        logger.info(logMessagesam);  // Affiche dans le logger du serveur
                                    }

                                } catch (IOException e) {

                                    String logMessagesam = "Erreur lors de la lecture du fichier " + samFile.getName() + " : " + e.getMessage() + " , " + e;

                                    if (isIntelliJ) {
                                        System.out.println(logMessagesam);  // Affiche dans la console (syso) d'IntelliJ
                                    } else {
                                        logger.info(logMessagesam);  // Affiche dans le logger du serveur
                                    }

                                }
                            }
                        }
                    }
                }





// Lire tous les fichiers commençant par '50592'

                File[] m50592Filess = inputFolder.listFiles((dir, name) -> name.startsWith("50592") && name.endsWith(".json"));
                String regex = "50592-.+_\\d{4}\\.\\d{2}\\.\\d{2}_\\d{2}h\\d{2}m\\d{2}s_Résultats-1xTint\\.json";

                if (m50592Filess != null) {

                    for (File m50592File : m50592Filess) {
                        if (!m50592File.getName().matches(regex)) {
                            // Le nom de fichier ne correspond pas au format spécifié
                            File targetFileechec = new File(echecFolderPath, m50592File.getName());
                            try {
                                String logMessage = "Le nom de fichier " + m50592File.getName() + " ne correspond pas au format spécifié ";
                                Files.move(m50592File.toPath(), targetFileechec.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                String logMessageerreur = "Le déplacement du fichier " + m50592File.getName() + " dans le répertoire d'echec "+targetFileechec.getAbsolutePath()+" est OK";
                                if (isIntelliJ) {
                                    System.out.println(logMessage);
                                    System.out.println(logMessageerreur);// Affiche dans la console (syso) d'IntelliJ
                                } else {
                                    logger.info(logMessage);  // Affiche dans le logger du serveur
                                    logger.info(logMessageerreur);
                                }
                            } catch (IOException e) {
                                String logMessage = "Erreur lors du déplacement du fichier vers le dossier 'echec' : " + e.getMessage();

                                if (isIntelliJ) {
                                    System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                } else {
                                    logger.info(logMessage);  // Affiche dans le logger du serveur
                                }
                            }
                        } else {
                            // Le nom de fichier correspond au format spécifié

                        File targetFile = new File(outputFolder, m50592File.getName());

                        if (targetFile.exists()) {
                            File targetFileechec = new File(echecFolderPath, m50592File.getName());
                            try {
                                String logMessage = "Le fichier cible existe déjà : " + targetFileechec.getAbsolutePath();
                                Files.move(m50592File.toPath(), targetFileechec.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                String logMessageerreur = "Le déplacement du fichier " + m50592File.getName() + " dans le répertoire d'echec "+targetFileechec.getAbsolutePath()+" est OK";
                                if (isIntelliJ) {
                                    System.out.println(logMessage);
                                    System.out.println(logMessageerreur);// Affiche dans la console (syso) d'IntelliJ
                                } else {
                                    logger.info(logMessage);  // Affiche dans le logger du serveur
                                    logger.info(logMessageerreur);
                                }


                            } catch (IOException e) {

                                String logMessage = "Erreur lors du déplacement du fichier existant vers le dossier 'echec' : " + e.getMessage();

                                if (isIntelliJ) {
                                    System.out.println(logMessage);  // Affiche dans la console (syso) d'IntelliJ
                                } else {
                                    logger.info(logMessage);  // Affiche dans le logger du serveur
                                }

                            }
                        } else {

                            String logMessagesam = "Le fichier " + m50592File.getName() + " est OK";

                            if (isIntelliJ) {
                                System.out.println(logMessagesam);  // Affiche dans la console (syso) d'IntelliJ
                            } else {
                                logger.info(logMessagesam);  // Affiche dans le logger du serveur
                            }


                            TypeReference<List<M_50592>> m50592TypeRef = new TypeReference<List<M_50592>>() {
                            };


                            try (InputStream m50592Stream = new FileInputStream(m50592File)) {

                                List<M_50592> m_50592s = mapper.readValue(m50592Stream, m50592TypeRef);


                                for (M_50592 m_50592 : m_50592s) {
                                    logger.info("le fichier à traiter " + m_50592.getFileName());
                                    System.out.println("le fichier à traiter " + m_50592.getFileName());

                                    m_50592.setFileName(m50592File.getName()); // Définir le nom de fichier dans l'objet M_50592

                                    m_50592.loadStartingWith50592(m50592File.getName());

                                    m_50592.loadSite(m50592File.getName());


                                    Environnement env = m_50592.getEnvironnement();

                                    String[] villes = env.extraireVilles(env.getSens());

                                    if (villes != null) {

                                        env.setVilleDepart(villes[0]);

                                        env.setVilleArrivee(villes[1]);

                                    }


                                    if (m_50592.getBeR1().getxFond().contains("FF382A") || m_50592.getBeR1().getyFond().contains("FF382A") || m_50592.getBeR1().getzFond().contains("FF382A") || m_50592.getBeR2().getxFond1().contains("FF382A") || m_50592.getBeR2().getyFond1().contains("FF382A") || m_50592.getBeR2().getzFond1().contains("FF382A") || m_50592.getBlR1().getxFondl().contains("FF382A") || m_50592.getBlR1().getyFondl().contains("FF382A") || m_50592.getBlR1().getzFondl().contains("FF382A") || m_50592.getBlR2().getxFondl2().contains("FF382A") || m_50592.getBlR2().getyFondl2().contains("FF382A") || m_50592.getBlR2().getzFondl2().contains("FF382A")) {

                                        m_50592.setStatut50592("NOK");

                                    } else {

                                        m_50592.setStatut50592("OK");

                                    }




                                    m50592Service.save(m_50592);

                                    if (m50592Service.findById(m_50592.getId()) != null) {
                                        logger.info("Le fichier a été enregistré dans la base de données: " + m_50592.getFileName());
                                        System.out.println("Le fichier a été enregistré dans la base de données: " + m_50592.getFileName());
                                        //deplacement de fichier dans output
                                        File targetFolder = getTargetFolder(outputFolder, m50592File.getName());
                                        File targetFileexcel = new File(targetFolder, m50592File.getName());
                                        try {
                                            Files.move(m50592File.toPath(), targetFileexcel.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                            String logMessage = "Le fichier " + m50592File.getName() + " a été déplacé vers " + targetFileexcel.getAbsolutePath();
                                            if (isIntelliJ) {
                                                System.out.println(logMessage);
                                            } else {
                                                logger.info(logMessage);
                                            }
                                            String jsonFilePath = new File(targetFolder, m50592File.getName()).getAbsolutePath();
                                            String jsonFileName = m_50592.getFileName().substring(0, m_50592.getFileName().lastIndexOf('.'));
                                            File outputFolderFile = new File(jsonFilePath).getParentFile(); // Obtenir le dossier parent du fichier JSON
                                            String jsonFolderPath = new File(outputFolderFile, jsonFileName).getAbsolutePath(); // Récupérer le chemin du dossier correspondant au fichier JSON
                                            File outputFolderFilee = new File(jsonFolderPath); // Créer un objet File pour le dossier correspondant

                                            // Mettre à jour le chemin avec le nouveau dossier créé
                                            String url50592 = jsonFolderPath.replace("\\", "/");
                                            m_50592.setUrl50592(url50592);






// Vérifier si le nom du fichier image correspondant contient le nom du fichier JSON

                                            File[] imageFiles = inputFolder.listFiles((dir, name) -> name.contains(jsonFileName) && (name.endsWith(".png") || name.endsWith(".bmp")));

                                            if (imageFiles.length > 0) {


                                                String logMesgg = "Il y a des images, création du répertoire {} a été créé => OK" + " , " + outputFolderFilee;

                                                if (isIntelliJ) {
                                                    System.out.println(logMesgg);  // Affiche dans la console (syso) d'IntelliJ
                                                } else {
                                                    logger.info(logMesgg);  // Affiche dans le logger du serveur
                                                }


// Créer le dossier correspondant au fichier JSON

                                                // Création récursive du dossier correspondant au fichier JSON
                                                boolean folderCreated = outputFolderFilee.mkdir(); // Utiliser mkdirs() sur le parent directement


                                                if (folderCreated) {


// Déplacer les fichiers d'image dans le dossier correspondant

                                                    for (File imageFile : imageFiles) {

                                                        try {
                                                            Thread.sleep(1000);
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }


                                                        File destFile = new File(outputFolderFilee, imageFile.getName());

                                                        boolean fileMoved = imageFile.renameTo(destFile);

                                                        if (fileMoved) {
                                                            String logMsgimage = "Déplacement de l'image {} => OK" + " , " + imageFile.getName();

                                                            if (isIntelliJ) {
                                                                System.out.println(logMsgimage);  // Affiche dans la console (syso) d'IntelliJ
                                                            } else {
                                                                logger.info(logMsgimage);  // Affiche dans le logger du serveur
                                                            }

                                                        } else {
                                                            String logMsgimage = "Impossible de déplacer le fichier image : " + imageFile.getAbsolutePath();

                                                            if (isIntelliJ) {
                                                                System.out.println(logMsgimage);  // Affiche dans la console (syso) d'IntelliJ
                                                            } else {
                                                                logger.info(logMsgimage);  // Affiche dans le logger du serveur
                                                            }


                                                        }

                                                    }


                                                } else {
                                                    String logMsgimage = "Impossible de créer le dossier : " + outputFolderFile.getAbsolutePath();

                                                    if (isIntelliJ) {
                                                        System.out.println(logMsgimage);  // Affiche dans la console (syso) d'IntelliJ
                                                    } else {
                                                        logger.info(logMsgimage);  // Affiche dans le logger du serveur
                                                    }


                                                }

                                            }




                                            // Enregistrer l'objet m_50592 dans la base de données
                                            m50592Service.save(m_50592);
                                        } catch (IOException e) {
                                            // Gérer l'exception en cas d'erreur de déplacement du fichier
                                            e.printStackTrace();
                                        }



                                    } else {
                                        logger.error("Erreur lors de l'enregistrement du fichier dans la base de données: " + m_50592.getFileName());
                                        System.out.println("Erreur lors de l'enregistrement du fichier dans la base de données: " + m_50592.getFileName());
                                    }
                                    //train

                                    Set<String> existingResultIdss = new HashSet<>();
                                    DateTimeFormatter formatterrs = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                                    LocalDateTime m50592DateTime = LocalDateTime.parse(m_50592.getDateFichier() + "T" + m_50592.getHeureFichier());

                                    String url = "https://test01.rd-vision-dev.com/get_images?system=2&dateFrom=" +
                                            m50592DateTime.minusMinutes(1) + "&dateTo=" + m50592DateTime.plusMinutes(1);
                                    logger.info("l'url passé est "+url);

                                    URL jsonUrl;

                                    try {

                                        jsonUrl = new URL(url);


                                    } catch (MalformedURLException e) {
                                        throw new RuntimeException(e);
                                    }

                                    HttpURLConnection connection = null;

                                    try {

                                        connection = (HttpURLConnection) jsonUrl.openConnection();

                                    } catch (IOException e) {

                                        throw new RuntimeException(e);

                                    }

                                    try {

                                        connection.setRequestMethod("GET");

                                    } catch (ProtocolException e) {

                                        throw new RuntimeException(e);

                                    }


// Ajouter le header Authorization avec le token

                                    String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoidGVzdCIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL2VtYWlsYWRkcmVzcyI6InRlc3QudXNlckB0ZXN0LmNvbSIsImV4cCI6MTY5NjYwMDY5MiwiaXNzIjoiand0dGVzdC5jb20iLCJhdWQiOiJ0cnlzdGFud2lsY29jay5jb20ifQ.LQ6yfa0InJi6N5GjRfVcA8XMZtZZef0PswrM2Io7l-g";

                                    connection.setRequestProperty("Authorization", "Bearer " + token);


                                    try {

                                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                            InputStream inputStream = connection.getInputStream();

                                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                                            StringBuilder response = new StringBuilder();

                                            String line;

                                            while ((line = bufferedReader.readLine()) != null) {
                                                response.append(line);

                                            }

                                            bufferedReader.close();

                                            inputStream.close();


// Mapper le JSON sur un objet Train

                                            Train train = mapper.readValue(response.toString(), Train.class);


                                            List<Result> results = train.getResults();

                                            int size = results.size();


                                            for (int i = 0; i < size; i++) {

                                                Result result = results.get(i);

                                                String dateid = result.getDate();


// Effectuez une vérification pour déterminer si l'ID du résultat existe déjà

                                                if (existingResultIdss.contains(dateid)) {


                                                    continue;

                                                }


                                                String dateTimeString = dateid.substring(0, 19);

                                                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatterrs);

                                                Date formattedDateTime = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());


// extraire la date et la convertir en java.util.Date

                                                String[] parts = dateTimeString.split("T");

                                                String datePart = parts[0]; // "2023-04-14"

                                                String timePart = parts[1]; // "14:04:05"


                                                SimpleDateFormat dateFormatterr = new SimpleDateFormat("yyyy-MM-dd");

                                                Date datefichier = dateFormatterr.parse(datePart);


                                                SimpleDateFormat timeFormatterr = new SimpleDateFormat("HH:mm:ss");

                                                Date timefichier = timeFormatterr.parse(timePart);


// Ajoutez l'ID du résultat à la liste des résultats existants

                                                existingResultIdss.add(dateid);


// Convertir les objets Date en objets Time

                                                Time heurefichier = new Time(timefichier.getTime());


// Vérifier si une instance de Train avec la même date, heure et site existe déjà

                                                List<Train> existingTrain = trainRepository.findBySiteAndDateFichierAndHeureFichier("Chevilly", datefichier, heurefichier);

                                                if (!existingTrain.isEmpty()) {


                                                    continue;

                                                }


                                                Train trainInstance = new Train(); // Créer une nouvelle instance de Train

                                                trainInstance.setDateFichier(datefichier);

                                                trainInstance.setHeureFichier(timefichier);

                                                trainInstance.setSite("Chevilly");


                                                result.setTrain(trainInstance); // Définir la relation train dans Result


                                                trainInstance.getResults().add(result);


                                                trainService.save(trainInstance); // Sauvegarder chaque instance de Train séparément

                                                resultService.save(result); // Sauvegarder chaque instance de Result séparément


                                            }

                                        } else {
                                            String logMessagenk = "Error response code: " + connection.getResponseCode();

                                            if (isIntelliJ) {
                                                System.out.println(logMessagenk);  // Affiche dans la console (syso) d'IntelliJ
                                            } else {
                                                logger.info(logMessagenk);  // Affiche dans le logger du serveur
                                            }


                                        }

                                    } catch (IOException e) {

                                        throw new RuntimeException(e);

                                    } catch (ParseException e) {

                                        throw new RuntimeException(e);

                                    } finally {

                                        connection.disconnect();

                                    }


                                }


                            } catch (JsonParseException e) {
                                // Déplacer le fichier JSON dans le répertoire d'échec
                                moveFileToFailureDirectory(m50592File, new File(echecFolderPath));
                                String logMessagenk = "Erreur lors de la lecture du fichier " + m50592File.getName() + " : " + e.getMessage() + " , " + e;

                                if (isIntelliJ) {
                                    System.out.println(logMessagenk);  // Affiche dans la console (syso) d'IntelliJ
                                } else {
                                    logger.info(logMessagenk);  // Affiche dans le logger du serveur
                                }

                            } catch (IOException e) {
                                String logMessagenk = "Erreur lors de la lecture du fichier " + m50592File.getName() + " : " + e.getMessage() + " , " + e;

                                if (isIntelliJ) {
                                    System.out.println(logMessagenk);  // Affiche dans la console (syso) d'IntelliJ
                                } else {
                                    logger.info(logMessagenk);  // Affiche dans le logger du serveur
                                }
                            }


                        }


                    }
                }
                }
                File[] m50592Filesimages = outputFolder.listFiles((dir, name) -> name.startsWith("50592") && name.endsWith(".json"));

                if (m50592Filesimages != null) {
                    for (File m50592 : m50592Filesimages) {
                        String jsonFileName = m50592.getName().substring(0, m50592.getName().lastIndexOf('.'));


// Vérifier si le nom du fichier image correspondant contient le nom du fichier JSON

                        File[] imageFiles = inputFolder.listFiles((dir, name) -> name.contains(jsonFileName) && (name.endsWith(".png") || name.endsWith(".bmp")));

                        if (imageFiles.length > 0) {

                            File outputFolderFile = new File(outputFolder, jsonFileName);
                            String logMesgg = "Il y a des images, création du répertoire {} a été créé => OK" + " , " + outputFolderFile.getName();

                            if (isIntelliJ) {
                                System.out.println(logMesgg);  // Affiche dans la console (syso) d'IntelliJ
                            } else {
                                logger.info(logMesgg);  // Affiche dans le logger du serveur
                            }


// Créer le dossier correspondant au fichier JSON

                            boolean folderCreated = outputFolderFile.mkdir();

                            if (folderCreated) {


// Déplacer les fichiers d'image dans le dossier correspondant

                                for (File imageFile : imageFiles) {

                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }


                                    File destFile = new File(outputFolderFile, imageFile.getName());

                                    boolean fileMoved = imageFile.renameTo(destFile);

                                    if (fileMoved) {
                                        String logMsgimage = "Déplacement de l'image {} => OK" + " , " + imageFile.getName();

                                        if (isIntelliJ) {
                                            System.out.println(logMsgimage);  // Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMsgimage);  // Affiche dans le logger du serveur
                                        }

                                    } else {
                                        String logMsgimage = "Impossible de déplacer le fichier image : " + imageFile.getAbsolutePath();

                                        if (isIntelliJ) {
                                            System.out.println(logMsgimage);  // Affiche dans la console (syso) d'IntelliJ
                                        } else {
                                            logger.info(logMsgimage);  // Affiche dans le logger du serveur
                                        }


                                    }

                                }


                            } else {
                                String logMsgimage = "Impossible de créer le dossier : " + outputFolderFile.getAbsolutePath();

                                if (isIntelliJ) {
                                    System.out.println(logMsgimage);  // Affiche dans la console (syso) d'IntelliJ
                                } else {
                                    logger.info(logMsgimage);  // Affiche dans le logger du serveur
                                }


                            }

                        }
                    }

                }







            }

        };






    }









}