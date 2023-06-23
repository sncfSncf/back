package org.example.component;

import org.example.controller.M_50592Controller;
import org.example.model.M_50592;



import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class M50592Assembler implements RepresentationModelAssembler<M_50592, EntityModel<M_50592>> {



    @Override
    public EntityModel<M_50592> toModel(M_50592 entity) {
        return EntityModel.of(entity,
                WebMvcLinkBuilder.linkTo(methodOn(M_50592Controller.class).get50592ById(entity.getId())).withSelfRel(),
                linkTo(methodOn(M_50592Controller.class).getAll50592()).withRel("50592"));
    }
}
