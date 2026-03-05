package io.github.nashcrash.autorest.processor.generator;

import com.squareup.javapoet.*;
import io.github.nashcrash.autorest.processor.AutoRestProcessor;
import io.github.nashcrash.autorest.processor.dto.GenericRestApiDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.List;

@AllArgsConstructor
public class AutoMapperGenerator {
    protected ProcessingEnvironment processingEnv;

    public JavaFile generateMapper(GenericRestApiDTO genericRestApiDTO) {
        TypeName entityType = genericRestApiDTO.getEntityType();
        TypeName dtoType = genericRestApiDTO.getDtoType();
        String entityName = genericRestApiDTO.getEntityName();

        ParameterizedTypeName superParameterizedTypeName = ParameterizedTypeName.get(ClassName.get("io.github.nashcrash.autorest.common.entity", "AbstractEntityMapper"), entityType, dtoType);

        TypeSpec.Builder mapperInterface = TypeSpec.interfaceBuilder(entityName + "Mapper")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(superParameterizedTypeName)
                .addAnnotation(genericRestApiDTO.getGeneratedAnnotationSpec())
                .addAnnotation(AnnotationSpec.builder(ClassName.get("org.mapstruct", "Mapper"))
                        .addMember("componentModel", "$S", "jakarta") // Integrate MapStruct with Quarkus CDI
                        .build());

        if (genericRestApiDTO.getIdFields() != null && !genericRestApiDTO.getIdFields().isEmpty()) {
            mapperInterface.addMethod(generateExtraEntityData(genericRestApiDTO));
        }

        return JavaFile.builder(genericRestApiDTO.getPackageName(), mapperInterface.build())
                .addFileComment(AutoRestProcessor.AUTO_GENERATED)
                .build();
    }

    private MethodSpec generateExtraEntityData(GenericRestApiDTO genericRestApiDTO) {
        TypeElement entityElement = genericRestApiDTO.getEntityElement();
        TypeName entityType = genericRestApiDTO.getEntityType();
        ClassName superClassName = ClassName.get("io.github.nashcrash.autorest.common.entity", "AbstractEntityMapper");
        ClassName idUtils = ClassName.get("io.github.nashcrash.autorest.common.util", "IdUtils");
        ClassName mappingTarget = ClassName.get("org.mapstruct", "MappingTarget");

        List<String> params = genericRestApiDTO.getIdFields().stream()
                .map(e -> "entity." + toGetterName(e, entityElement))
                .toList();

        MethodSpec.Builder createMethod = MethodSpec.methodBuilder("addExtraEntityData")
                .addAnnotation(ClassName.get(Override.class))
                .addAnnotation(ClassName.get("org.mapstruct", "AfterMapping"))
                .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                .returns(TypeName.VOID)
                .addParameter(ParameterSpec.builder(entityType, "entity")
                        .addAnnotation(mappingTarget)
                        .build())
                .addStatement("$T.super.addExtraEntityData(entity)", superClassName)
                .addComment("Set a logical ID")
                .addStatement("entity.setId($T.generateId($N))", idUtils, StringUtils.join(params, ", "));

        return createMethod.build();
    }

    /**
     * Returns the name of the getter according to Lombok/JavaBeans conventions.
     *
     * @param fieldName    The name of the field (e.g. "start")
     * @param classElement The type of the class
     * @return The name of the getter (e.g. "getInizio" or "isActive")
     */
    public String toGetterName(String fieldName, TypeElement classElement) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }
        TypeMirror fieldType = getFieldType(classElement, fieldName);

        // Trasformiamo la prima lettera in Maiuscolo (es. inizio -> Inizio)
        String capitalized = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        // Gestione speciale per i booleani primitivi (boolean)
        // Nota: i booleani oggetto (Boolean) usano comunque "get"
        if (fieldType.getKind() == TypeKind.BOOLEAN) {
            return "is" + capitalized + "()";
        }

        return "get" + capitalized + "()";
    }

    private TypeMirror getFieldType(TypeElement classElement, String fieldName) {
        java.util.Optional<TypeMirror> fieldType = javax.lang.model.util.ElementFilter.fieldsIn(classElement.getEnclosedElements())
                .stream()
                .filter(field -> field.getSimpleName().toString().equals(fieldName))
                .map(javax.lang.model.element.Element::asType)
                .findFirst();

        if (fieldType.isPresent()) {
            return fieldType.get();
        }
        //Search in superclass
        javax.lang.model.type.TypeMirror superclassMirror = classElement.getSuperclass();

        // Check that the superclass exists and is not Object (which has Kind NONE or is java.lang.Object)
        if (superclassMirror.getKind() == javax.lang.model.type.TypeKind.DECLARED) {
            javax.lang.model.element.TypeElement superclassElement = (javax.lang.model.element.TypeElement) processingEnv.getTypeUtils().asElement(superclassMirror);
            if (!superclassElement.getQualifiedName().toString().equals("java.lang.Object")) {
                return getFieldType(superclassElement, fieldName);
            }
        }
        //else
        String errorMessage="Field " + fieldName + " not found in " + classElement.getSimpleName() + " or its superclasses";
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Failed to generate code for " + classElement.getSimpleName() + "Mapper: " + errorMessage
        );
        throw new IllegalArgumentException(errorMessage);
    }
}
