package org.example.controller;

import org.example.component.SamAssembler;
import org.example.component.Utils;
import org.example.exception.ResourceNotFoundException;
import org.example.model.Sam;
import org.example.repository.SamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class SamController {
    @Autowired
    private Utils utils;
    private final SamRepository samRepository;
    private final SamAssembler samAssembler;

    public SamController(SamRepository samRepository, SamAssembler samAssembler) {
        this.samRepository = samRepository;
        this.samAssembler = samAssembler;
    }

    @GetMapping("/SAM")
    public ResponseEntity<?> getAllSam() {
        try {
            List<EntityModel<Sam>> sams = samRepository.findAll().stream()
                    .map(samAssembler::toModel)
                    .collect(Collectors.toList());
            if (sams.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(CollectionModel.of(sams,
                    linkTo(methodOn(SamController.class).getAllSam()).withSelfRel()),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/SAM/{id}")
    public ResponseEntity<EntityModel<Sam>> getSamById(@PathVariable(value = "id") Long id) {
        Sam sam = samRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impossible de trouver la configuration des marques habilitées par activité " + id));
        return new ResponseEntity<>(samAssembler.toModel(sam), HttpStatus.OK);
    }




}