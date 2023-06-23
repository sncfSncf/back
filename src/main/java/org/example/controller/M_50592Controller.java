package org.example.controller;

import org.example.component.M50592Assembler;
import org.example.exception.ResourceNotFoundException;
import org.example.model.M_50592;
import org.example.repository.M_50592Repository;
import org.example.service.M_50592Service;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/50592")
public class M_50592Controller {

private M_50592Repository m50592Repository;

    private M_50592Service m50592Service;

    private M50592Assembler m50592Assembler;

    public M_50592Controller(M_50592Service m50592Service) {
        this.m50592Service = m50592Service;
    }

    @GetMapping("/list")
    public Iterable<M_50592> list() {
        return m50592Service.list();
    }



    @GetMapping({"/50592"})
    public ResponseEntity<?> getAll50592() {
        try {
            List<EntityModel<M_50592>> m50592s = m50592Repository.findAll().stream()
                    .map(m50592Assembler::toModel)
                    .collect(Collectors.toList());
            if (m50592s.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(CollectionModel.of(m50592s,
                    linkTo(methodOn(M_50592Controller.class).getAll50592()).withSelfRel()),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping({"/50592/{id}"})
    public ResponseEntity<EntityModel<M_50592>> get50592ById(@PathVariable("id") Long id) {
        M_50592 m50592 = m50592Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impossible de trouver la configuration des marques habilitées par activité " + id));

        return new ResponseEntity(m50592Assembler.toModel(m50592), HttpStatus.OK);
    }
}
