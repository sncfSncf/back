package org.example.component;

import org.example.controller.SamController;
import org.example.model.Sam;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
@Component
public class SamAssembler implements RepresentationModelAssembler<Sam, EntityModel<Sam>> {
    @Override
    public EntityModel<Sam> toModel(Sam entity) {
        return EntityModel.of(entity,
                WebMvcLinkBuilder.linkTo(methodOn(SamController.class).getSamById(entity.getId())).withSelfRel(),
                linkTo(methodOn(SamController.class).getAllSam()).withRel("SAM"));
    }
}
