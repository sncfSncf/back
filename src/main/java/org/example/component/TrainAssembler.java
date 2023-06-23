package org.example.component;

import org.example.controller.TrainController;
import org.example.model.Train;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TrainAssembler implements RepresentationModelAssembler<Train, EntityModel<Train>> {







    @Override
    public EntityModel<Train> toModel(Train entity) {

        return  EntityModel.of(entity,
                WebMvcLinkBuilder.linkTo(methodOn(TrainController.class).getTrainById(entity.getId())).withSelfRel(),
                linkTo(methodOn(TrainController.class).getAllTrain()).withRel("TRAIN"));

    }



}
