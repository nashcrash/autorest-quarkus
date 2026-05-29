package io.github.nashcrash.autorest.processor.generator;

import com.squareup.javapoet.*;
import io.github.nashcrash.autorest.common.client.CustomClientExceptionMapper;
import io.github.nashcrash.autorest.common.client.CustomClientHeaderFactory;
import io.github.nashcrash.autorest.common.entity.ResultDTO;
import io.github.nashcrash.autorest.processor.AutoRestProcessor;
import io.github.nashcrash.autorest.processor.dto.AggregateDTO;
import io.github.nashcrash.autorest.processor.dto.GenericRestApiDTO;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class AutoClientGenerator {
    protected ProcessingEnvironment processingEnv;

    public JavaFile generateClient(GenericRestApiDTO genericRestApiDTO) {
        String entityName = genericRestApiDTO.getEntityName();
        TypeName dtoType = genericRestApiDTO.getDtoType();
        boolean isReactive = genericRestApiDTO.isReactive();

        TypeElement superClass = null;
        if (isReactive) {
            superClass = processingEnv.getElementUtils().getTypeElement("io.github.nashcrash.autorest.common.entity.reactive.ReactiveResource");
        } else {
            superClass = processingEnv.getElementUtils().getTypeElement("io.github.nashcrash.autorest.common.entity.rest.RestResource");
        }

        ClassName registerClientHeaders = ClassName.get("org.eclipse.microprofile.rest.client.annotation", "RegisterClientHeaders");
        ClassName registerProvider = ClassName.get("org.eclipse.microprofile.rest.client.annotation", "RegisterProvider");
        ClassName registerRestClient = ClassName.get("org.eclipse.microprofile.rest.client.inject", "RegisterRestClient");
        TypeSpec.Builder mapperInterface = TypeSpec.interfaceBuilder(entityName + "Client")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(genericRestApiDTO.getGeneratedAnnotationSpec())
                .addAnnotation(AnnotationSpec.builder(Path.class).addMember("value", "$S", genericRestApiDTO.getBasePath()).build())
                .addAnnotation(AnnotationSpec.builder(registerRestClient).addMember("configKey", "$S", genericRestApiDTO.getConfigKey()).build())
                .addAnnotation(AnnotationSpec.builder(registerClientHeaders).addMember("value", "$T.class", CustomClientHeaderFactory.class).build())
                .addAnnotation(AnnotationSpec.builder(registerProvider).addMember("value", "$T.class", CustomClientExceptionMapper.class).build())
                .addAnnotation(ClassName.get("jakarta.enterprise.context", "ApplicationScoped"))
                .addMethods(overrideMethods(superClass, dtoType));

        // 3. Add aggregate methods
        if (genericRestApiDTO.getAggregate() != null) {
            for (AggregateDTO aggregateDTO : genericRestApiDTO.getAggregate()) {
                mapperInterface.addMethod(generateAggregateGet(genericRestApiDTO, aggregateDTO));
                mapperInterface.addMethod(generateAggregatePost(genericRestApiDTO, aggregateDTO));
                mapperInterface.addMethod(generateAggregateAndCountPost(genericRestApiDTO, aggregateDTO));
            }
        }

        return JavaFile.builder(genericRestApiDTO.getPackageName(), mapperInterface.build())
                .addFileComment(AutoRestProcessor.AUTO_GENERATED)
                .build();
    }

    private List<MethodSpec> overrideMethods(TypeElement baseInterface, TypeName concreteDto) {
        List<MethodSpec> methods = new ArrayList<>();

        for (ExecutableElement method : javax.lang.model.util.ElementFilter.methodsIn(baseInterface.getEnclosedElements())) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    ;

            for (AnnotationMirror ann : method.getAnnotationMirrors()) {
                methodBuilder.addAnnotation(AnnotationSpec.get(ann));
            }
            methodBuilder.returns(replaceType(TypeName.get(method.getReturnType()), concreteDto));

            for (VariableElement param : method.getParameters()) {
                ParameterSpec.Builder paramBuilder = ParameterSpec.builder(
                        replaceType(TypeName.get(param.asType()), concreteDto),
                        param.getSimpleName().toString()
                );

                // Copiamo le annotazioni del parametro (@PathParam, @QueryParam, ecc.)
                for (AnnotationMirror ann : param.getAnnotationMirrors()) {
                    paramBuilder.addAnnotation(AnnotationSpec.get(ann));
                }
                methodBuilder.addParameter(paramBuilder.build());
            }

            methods.add(methodBuilder.build());
        }
        return methods;
    }

    /**
     * Helper per sostituire il parametro generico "DTO" con il tipo concreto
     */
    private TypeName replaceType(TypeName type, TypeName concrete) {
        if (type.toString().equals("DTO")) {
            return concrete;
        }
        if (type instanceof ParameterizedTypeName pType) {
            List<TypeName> newArgs = new ArrayList<>();

            for (TypeName arg : pType.typeArguments) {
                newArgs.add(replaceType(arg, concrete));
            }

            return ParameterizedTypeName.get(pType.rawType, newArgs.toArray(new TypeName[0]));
        }
        if (type instanceof com.squareup.javapoet.ArrayTypeName aType) {
            return com.squareup.javapoet.ArrayTypeName.of(replaceType(aType.componentType, concrete));
        }
        return type;
    }

    private MethodSpec generateAggregateGet(GenericRestApiDTO genericRestApiDTO, AggregateDTO aggregateDTO) {
        TypeName returnType = genericRestApiDTO.isReactive()
                ? wrapToUni(ParameterizedTypeName.get(
                ClassName.get(List.class),
                aggregateDTO.getDtoTypeName()))
                : ParameterizedTypeName.get(
                ClassName.get(List.class),
                aggregateDTO.getDtoTypeName());

        return MethodSpec.methodBuilder(checkMethodName(aggregateDTO.getName(), "aggregate") + "Get")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(ClassName.get("jakarta.ws.rs", "GET"))
                .addAnnotation(AnnotationSpec.builder(Path.class)
                        .addMember("value", "$S", aggregateDTO.getPath())
                        .build())
                .addAnnotation(AnnotationSpec.builder(Operation.class)
                        .addMember(
                                "summary",
                                "$S",
                                "Aggregate by " + aggregateDTO.getDtoTypeElement().getSimpleName()
                                        + " with filtering and pagination")
                        .build())
                .addParameter(ParameterSpec.builder(String.class, "query")
                        .addAnnotation(AnnotationSpec.builder(QueryParam.class)
                                .addMember("value", "$S", "query")
                                .build())
                        .build())
                .addParameter(ParameterSpec.builder(String[].class, "orderBy")
                        .addAnnotation(AnnotationSpec.builder(QueryParam.class)
                                .addMember("value", "$S", "orderBy")
                                .build())
                        .build())
                .addParameter(ParameterSpec.builder(String[].class, "orderDirection")
                        .addAnnotation(AnnotationSpec.builder(QueryParam.class)
                                .addMember("value", "$S", "orderDirection")
                                .build())
                        .build())
                .addParameter(ParameterSpec.builder(int.class, "page")
                        .addAnnotation(AnnotationSpec.builder(QueryParam.class)
                                .addMember("value", "$S", "page")
                                .build())
                        .build())
                .addParameter(ParameterSpec.builder(int.class, "limit")
                        .addAnnotation(AnnotationSpec.builder(QueryParam.class)
                                .addMember("value", "$S", "limit")
                                .build())
                        .build())
                .returns(returnType)
                // Interface method: no implementation
                .build();
    }

    private MethodSpec generateAggregatePost(GenericRestApiDTO genericRestApiDTO, AggregateDTO aggregateDTO) {
        TypeName returnType = genericRestApiDTO.isReactive()
                ? wrapToUni(ParameterizedTypeName.get(
                ClassName.get(List.class),
                aggregateDTO.getDtoTypeName()))
                : ParameterizedTypeName.get(
                ClassName.get(List.class),
                aggregateDTO.getDtoTypeName());

        return MethodSpec.methodBuilder(checkMethodName(aggregateDTO.getName(), "aggregate") + "Post")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(ClassName.get("jakarta.ws.rs", "POST"))
                .addAnnotation(AnnotationSpec.builder(Path.class)
                        .addMember("value", "$S", aggregateDTO.getPath())
                        .build())
                .addAnnotation(AnnotationSpec.builder(Operation.class)
                        .addMember(
                                "summary",
                                "$S",
                                "Aggregate by " + aggregateDTO.getDtoTypeElement().getSimpleName()
                                        + " with filtering and pagination")
                        .build())
                .addParameter(ParameterSpec.builder(
                                ClassName.get("io.github.nashcrash.autorest.common.entity", "FindDTO"),
                                "findDto")
                        .build())
                .returns(returnType)
                // Interface method: no implementation
                .build();
    }

    private MethodSpec generateAggregateAndCountPost(GenericRestApiDTO genericRestApiDTO,
                                                     AggregateDTO aggregateDTO) {

        TypeName returnType = genericRestApiDTO.isReactive()
                ? wrapToUni(ParameterizedTypeName.get(
                ClassName.get(ResultDTO.class),
                aggregateDTO.getDtoTypeName()))
                : ParameterizedTypeName.get(
                ClassName.get(ResultDTO.class),
                aggregateDTO.getDtoTypeName());

        return MethodSpec.methodBuilder(
                        checkMethodName(
                                aggregateDTO.getName() + "AndCount",
                                "aggregateAndCount") + "Post")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(ClassName.get("jakarta.ws.rs", "POST"))
                .addAnnotation(AnnotationSpec.builder(Path.class)
                        .addMember("value", "$S", aggregateDTO.getPath() + "AndCount")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Operation.class)
                        .addMember(
                                "summary",
                                "$S",
                                "Aggregate and count by "
                                        + aggregateDTO.getDtoTypeElement().getSimpleName()
                                        + " with filtering and pagination")
                        .build())
                .addParameter(ParameterSpec.builder(
                                ClassName.get(
                                        "io.github.nashcrash.autorest.common.entity",
                                        "FindDTO"),
                                "findDto")
                        .build())
                .returns(returnType)
                // Interface method: signature only, no implementation
                .build();
    }

    public String checkMethodName(String name, String defaultName) {
        if (name == null) return defaultName;
        name = name.replaceAll("[^a-zA-Z0-9]", "");
        return name.isEmpty() ? defaultName : name;
    }

    /**
     * Avvolge un TypeName esistente in un io.smallrye.mutiny.Uni.
     * Esempio: String -> Uni<String>
     */
    public TypeName wrapToUni(TypeName typeToWrap) {
        ClassName uniRaw = ClassName.get("io.smallrye.mutiny", "Uni");
        TypeName boxedType = typeToWrap.isPrimitive() ? typeToWrap.box() : typeToWrap;
        return ParameterizedTypeName.get(uniRaw, boxedType);
    }
}
