package io.github.nashcrash.autorest.processor.generator;

import com.squareup.javapoet.*;
import io.github.nashcrash.autorest.api.DatabaseType;
import io.github.nashcrash.autorest.processor.dto.GenericRestApiDTO;

import javax.lang.model.element.Modifier;

public class AutoRepositoryGenerator {

    public JavaFile generateRepository(GenericRestApiDTO genericRestApiDTO) {
        String entityName = genericRestApiDTO.getEntityName();
        DatabaseType dbType = genericRestApiDTO.getDatabaseType();
        TypeName entityType = genericRestApiDTO.getEntityType();

        ParameterizedTypeName superClass = null;
        if (genericRestApiDTO.isReactive()) {
            if (dbType == DatabaseType.NOSQL) {
                superClass = ParameterizedTypeName.get(ClassName.get("io.github.nashcrash.autorest.common.entity.reactive", "AbstractEntityReactiveMongoRepository"), entityType);
            } else {
                superClass = ParameterizedTypeName.get(ClassName.get("io.github.nashcrash.autorest.common.entity.reactive", "AbstractEntityReactiveSqlRepository"), entityType);
            }
        } else {
            if (dbType == DatabaseType.NOSQL) {
                superClass = ParameterizedTypeName.get(ClassName.get("io.github.nashcrash.autorest.common.entity.rest", "AbstractEntityRestMongoRepository"), entityType);
            } else {
                superClass = ParameterizedTypeName.get(ClassName.get("io.github.nashcrash.autorest.common.entity.reactive", "AbstractEntityRestSqlRepository"), entityType);
            }
        }

        TypeSpec repositoryClass = TypeSpec.classBuilder(entityName + "Repository")
                .addModifiers(Modifier.PUBLIC)
                .superclass(superClass)
                .addAnnotation(genericRestApiDTO.getGeneratedAnnotationSpec())
                .addAnnotation(ClassName.get("jakarta.enterprise.context", "ApplicationScoped"))
                .build();

        return JavaFile.builder(genericRestApiDTO.getPackageName(), repositoryClass)
                .addFileComment("AUTO-GENERATED REPOSITORY")
                .build();
    }
}
