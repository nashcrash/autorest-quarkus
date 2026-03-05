package io.github.nashcrash.autorest.processor.generator;

import com.squareup.javapoet.*;
import io.github.nashcrash.autorest.common.client.CustomClientExceptionMapper;
import io.github.nashcrash.autorest.common.client.CustomClientHeaderFactory;
import io.github.nashcrash.autorest.processor.AutoRestProcessor;
import io.github.nashcrash.autorest.processor.dto.GenericRestApiDTO;
import jakarta.ws.rs.Path;
import lombok.AllArgsConstructor;

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

        return JavaFile.builder(genericRestApiDTO.getPackageName(), mapperInterface.build())
                .addFileComment(AutoRestProcessor.AUTO_GENERATED)
                .build();
    }

    private List<MethodSpec> overrideMethods(TypeElement baseInterface, TypeName concreteDto) {
        List<MethodSpec> methods = new ArrayList<>();

        for (ExecutableElement method : javax.lang.model.util.ElementFilter.methodsIn(baseInterface.getEnclosedElements())) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

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
}
