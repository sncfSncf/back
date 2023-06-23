package org.example.controller;

import org.example.component.TrainAssembler;
import org.example.component.Utils;
import org.example.exception.ResourceNotFoundException;
import org.example.model.Train;
import org.example.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@CrossOrigin("http://localhost:3000/")

public class TrainController {
    @Autowired
    private Utils utils;
    private TrainRepository trainRepository;

    private TrainAssembler trainAssembler;

    public TrainController(TrainRepository trainRepository , TrainAssembler trainAssembler) {
        this.trainRepository = trainRepository;
        this.trainAssembler = trainAssembler;
    }

    @GetMapping("/train")
    public ResponseEntity<?> getAllTrain() {
        try {
            List<EntityModel<Train>> trains = trainRepository.findAll().stream()
                    .map(trainAssembler::toModel)
                    .collect(Collectors.toList());
            if (trains.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(CollectionModel.of(trains,
                    linkTo(methodOn(TrainController.class).getAllTrain()).withSelfRel()),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/train/{id}")
    public ResponseEntity<EntityModel<Train>> getTrainById(@PathVariable(value = "id") Long id) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impossible de trouver la configuration des marques habilitées par activité " + id));
        return new ResponseEntity<>(trainAssembler.toModel(train), HttpStatus.OK);
    }


    @GetMapping("/train/dateandsite")
    public ResponseEntity<List<EntityModel<Train>>> getTrainByDateAndSite(
            @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            @RequestParam("site") String site) {

        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Train> trainList = trainRepository.findByDateFichierAndSite(dateFichier, site);

        if (trainList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<EntityModel<Train>> trainModels = trainList.stream()
                    .map(trainAssembler::toModel)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(trainModels, HttpStatus.OK);
        }
    }

    @GetMapping(path = "/trainSites", produces = "application/json")
    public ResponseEntity<List<String>> getTrainSites() {
        List<Train> trains = trainRepository.findAll();
        List<String> sites = trains.stream()
                .map(Train::getSite)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(sites);
    }







}
