//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.example.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.example.component.MrAssembler;
import org.example.component.Utils;
import org.example.exception.ResourceNotFoundException;
import org.example.model.Mr;
import org.example.repository.MrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@CrossOrigin({"http://localhost:3000/"})
public class TypeMrController {
    @Autowired
    private Utils utils;
    private MrRepository mrRepository;
    private MrAssembler mrAssembler;

    public TypeMrController(Utils utils, MrRepository mrRepository, MrAssembler mrAssembler) {
        this.utils = utils;
        this.mrRepository = mrRepository;
        this.mrAssembler = mrAssembler;
    }

    @GetMapping({"/mr"})
    public ResponseEntity<?> getAllMr() {
        try {
            List<EntityModel<Mr>> mrs = mrRepository.findAll().stream()
                    .map(mrAssembler::toModel)
                    .collect(Collectors.toList());
            if (mrs.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(CollectionModel.of(mrs,
                    linkTo(methodOn(TypeMrController.class).getAllMr()).withSelfRel()),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({"/mr/{id}"})
    public ResponseEntity<EntityModel<Mr>> getMrById(@PathVariable("id") Long id) {
        Mr mr = mrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impossible de trouver la configuration des marques habilitées par activité " + id));

        return new ResponseEntity(mrAssembler.toModel(mr), HttpStatus.OK);
    }

    @GetMapping(path = {"/typemr"}, produces = {"application/json"}
    )
    public ResponseEntity<List<String>> getTypeMr() {
        List<Mr> mrs = this.mrRepository.findAll();
        List<String> typemr =mrs.stream()
                .map(Mr::getMr)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(typemr);
    }
}
