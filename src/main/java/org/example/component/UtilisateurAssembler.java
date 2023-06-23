package org.example.component;

import org.example.controller.UtilisateurController;
import org.example.model.Utilisateur;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UtilisateurAssembler implements RepresentationModelAssembler<Utilisateur, EntityModel<Utilisateur>> {



    @Override
    public EntityModel<Utilisateur> toModel(Utilisateur entity) {
        return EntityModel.of(entity,
                WebMvcLinkBuilder.linkTo(methodOn(UtilisateurController.class).getUserById(entity.getId())).withSelfRel(),
                linkTo(methodOn(UtilisateurController.class).getAllUser()).withSelfRel());
    }
}
