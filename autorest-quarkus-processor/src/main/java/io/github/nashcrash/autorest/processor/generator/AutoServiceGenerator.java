package io.github.nashcrash.autorest.processor.generator;

import com.squareup.javapoet.*;
import io.github.nashcrash.autorest.api.DatabaseType;
import io.github.nashcrash.autorest.processor.dto.GenericRestApiDTO;

import javax.lang.model.element.Modifier;

public class AutoServiceGenerator {

    public JavaFile generateService(GenericRestApiDTO genericRestApiDTO) {
        String entityName = genericRestApiDTO.getEntityName();
        TypeName dtoType = genericRestApiDTO.getDtoType();
        TypeName entityType = genericRestApiDTO.getEntityType();
        boolean isReactive = genericRestApiDTO.isReactive();
        String packageName = genericRestApiDTO.getPackageName();
        DatabaseType dbType = genericRestApiDTO.getDatabaseType();

        ParameterizedTypeName superClass = null;
        if (isReactive) {
            if (dbType == DatabaseType.NOSQL) {
                superClass = ParameterizedTypeName.get(ClassName.get("io.github.nashcrash.autorest.common.entity.reactive", "AbstractEntityReactiveMongoService"), entityType, dtoType);
            } else {
                superClass = ParameterizedTypeName.get(ClassName.get("io.github.nashcrash.autorest.common.entity.reactive", "AbstractEntityReactiveSqlService"), entityType, dtoType);
            }
        } else {
            if (dbType == DatabaseType.NOSQL) {
                superClass = ParameterizedTypeName.get(ClassName.get("io.github.nashcrash.autorest.common.entity.rest", "AbstractEntityRestMongoService"), entityType, dtoType);
            } else {
                superClass = ParameterizedTypeName.get(ClassName.get("io.github.nashcrash.autorest.common.entity.rest", "AbstractEntityRestSqlService"), entityType, dtoType);
            }
        }

        // 1. Define the Service Class
        TypeSpec.Builder serviceClass = TypeSpec.classBuilder(entityName + "Service")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ClassName.get("lombok.extern.slf4j", "Slf4j"))
                .addAnnotation(genericRestApiDTO.getGeneratedAnnotationSpec())
                .addAnnotation(ClassName.get("jakarta.enterprise.context", "ApplicationScoped"));
        if (dbType == DatabaseType.SQL) {
            serviceClass.addAnnotation(ClassName.get("jakarta.transaction", "Transactional"));
        }

        // 2. Class implementation
        serviceClass.superclass(superClass);

        return JavaFile.builder(packageName, serviceClass.build())
                .addFileComment("AUTO-GENERATED SERVICE - DO NOT EDIT")
                .build();
    }
}
