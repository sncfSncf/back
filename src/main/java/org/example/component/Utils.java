package org.example.component;

import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.walking.spi.AttributeDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

@Component("utils")
public class Utils {

    @Autowired
    SessionFactory sessionFactory;

    public Map<String, Map<String, Object>> getTypeChamps(Class clazz){
        Map<String, EntityPersister> persisterMap = sessionFactory.getSessionFactory().getEntityPersisters();
        EntityPersister entity = persisterMap.get(clazz.getName());
//        System.out.println(persisterMap);
//        System.out.println(ConfADGPriorite.class.getName());
//        for(Map.Entry<String,EntityPersister> entity : persisterMap.entrySet()) {
//            Class targetClass = entity.getValue().getMappedClass();
        SingleTableEntityPersister persister = (SingleTableEntityPersister) entity;
        Iterable<AttributeDefinition> attributes = persister.getAttributes();
        String entityName = clazz.getSimpleName();//Entity的名称 table name in entity
        String tableName = persister.getTableName();//Entity对应的表的英文名 table name in DB
        Map<String, Map<String, Object>> result = new HashMap<>();
        System.out.println("Class Name：" + entityName + " => Table Name：" + tableName);


        //Fields
        for (AttributeDefinition attr : attributes) {
            String propertyName = attr.getName(); //在entity中的属性名称 fields names in entity
            String[] columnName = persister.getPropertyColumnNames(propertyName); //对应数据库表中的字段名 fields names in DB
            String type = "";
            PropertyDescriptor targetPd = BeanUtils.getPropertyDescriptor(clazz, propertyName);
            if (targetPd != null) {
                type = targetPd.getPropertyType().getSimpleName();
            }
            Boolean nullable = attr.isNullable();
            Map<String, Object> res = new HashMap<>();
            System.out.println("Field Name：" + propertyName + " => Type：" + type + " => Field Name in DB：" + columnName[0]);
            res.put("Field Name in DB: ", columnName[0]);
            res.put("Type", type);
            res.put("Nullable", nullable);
            result.put(propertyName, res);
        }
//        result.put("Class Name：" + entityName + " => Table Name：" + tableName);
        return result;
    }
}

