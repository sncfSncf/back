//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.example.component;

import org.example.controller.SamController;
import org.example.controller.TypeMrController;
import org.example.model.Mr;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MrAssembler implements RepresentationModelAssembler<Mr, EntityModel<Mr>> {
    @Override
    public EntityModel<Mr> toModel(Mr entity) {
        return EntityModel.of(entity,
                WebMvcLinkBuilder.linkTo(methodOn(TypeMrController.class).getMrById(entity.getId())).withSelfRel(),
                linkTo(methodOn(TypeMrController.class).getAllMr()).withRel("MR"));
    }
}
