//package org.example.component;
//
//
//import org.springframework.hateoas.EntityModel;
//import org.springframework.hateoas.Link;
//import org.springframework.hateoas.server.RepresentationModelAssembler;
//import org.springframework.stereotype.Component;
//
//@Component
//public class SamTrainAssembler implements RepresentationModelAssembler<T , EntityModel<T>> {
//
//    private final EntityLinks entityLinks;
//
//    public EntityModelAssembler(EntityLinks entityLinks) {
//        this.entityLinks = entityLinks;
//    }
//
//    @Override
//    public EntityModel<T> toModel(T entity) {
//        Class<?> entityClass = entity.getClass();
//        Link selfLink = entityLinks.linkFor(entityClass)
//                .slash(getId(entity))
//                .withSelfRel();
//
//        Link collectionLink = entityLinks.linkForCollectionOf(entityClass)
//                .withRel(getCollectionName(entityClass));
//
//        return EntityModel.of(entity, selfLink, collectionLink);
//    }
//
//    private String getId(T entity) {
//        //TODO: Implement a way to get the ID of the entity
//        return "TODO";
//    }
//
//    private String getCollectionName(Class<?> entityClass) {
//        //TODO: Implement a way to get the name of the collection of entities
//        return "TODO";
//    }
//
//}
