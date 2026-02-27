package io.github.nashcrash.autorest.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import io.github.nashcrash.autorest.api.Consumer;
import io.github.nashcrash.autorest.api.DatabaseType;
import io.github.nashcrash.autorest.api.ResourceAPI;
import io.github.nashcrash.autorest.api.Reactive;
import io.github.nashcrash.autorest.common.entity.AbstractEntityMongo;
import io.github.nashcrash.autorest.common.entity.AbstractEntitySQL;
import io.github.nashcrash.autorest.processor.dto.GenericRestApiDTO;
import io.github.nashcrash.autorest.processor.generator.AutoMapperGenerator;
import io.github.nashcrash.autorest.processor.generator.AutoRepositoryGenerator;
import io.github.nashcrash.autorest.processor.generator.AutoResourceGenerator;
import io.github.nashcrash.autorest.processor.generator.AutoServiceGenerator;
import jakarta.annotation.Generated;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * Main Annotation Processor for ResourceAPI.
 * It coordinates the generation of JAX-RS Resources, Services, and MapStruct Mappers.
 */
@SupportedAnnotationTypes("io.github.nashcrash.autorest.api.ResourceAPI")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class AutoRestProcessor extends AbstractProcessor {

    private AutoResourceGenerator restGenerator;
    private AutoServiceGenerator serviceGenerator;
    private AutoRepositoryGenerator repositoryGenerator;
    private AutoMapperGenerator mapperGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        // Initialize our logic generator
        this.restGenerator = new AutoResourceGenerator();
        this.serviceGenerator = new AutoServiceGenerator();
        this.repositoryGenerator = new AutoRepositoryGenerator();
        this.mapperGenerator = new AutoMapperGenerator();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(ResourceAPI.class)) {
            if (element instanceof TypeElement typeElement) {
                try {
                    processElement(typeElement);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "Failed to generate code for " + typeElement.getSimpleName() + ": " + e.getMessage()
                    );
                }
            }
        }
        // Return true to claim the annotation so other processors don't process it again
        return true;
    }

    private DatabaseType validateElement(TypeElement typeElement) throws IOException {
        TypeMirror abstractEntitySQLType = processingEnv.getElementUtils()
                .getTypeElement(AbstractEntitySQL.class.getCanonicalName())
                .asType();
        TypeMirror abstractEntityMongoType = processingEnv.getElementUtils()
                .getTypeElement(AbstractEntityMongo.class.getCanonicalName())
                .asType();

        if (processingEnv.getTypeUtils().isAssignable(typeElement.asType(), abstractEntitySQLType)) {
            return DatabaseType.SQL;
        } else if (processingEnv.getTypeUtils().isAssignable(typeElement.asType(), abstractEntityMongoType)) {
            return DatabaseType.NOSQL;
        } else {
            throw new IOException("@" + ResourceAPI.class.getSimpleName() + " can only be used on classes that extend AbstractEntity");
        }
    }

    private void processElement(TypeElement entityElement) throws IOException {
        ResourceAPI annotation = entityElement.getAnnotation(ResourceAPI.class);
        boolean isReactive = entityElement.getAnnotation(Reactive.class) != null;
        boolean hasConsumer = entityElement.getAnnotation(Consumer.class) != null;
        String packageName = processingEnv.getElementUtils().getPackageOf(entityElement).toString();
        TypeElement dtoElement = getDTOTypeElement(annotation);
        DatabaseType databaseType = validateElement(entityElement);
        TypeName entityType = getTypeName(entityElement);
        TypeName dtoType = getTypeName(dtoElement);
        String entityName = entityElement.getSimpleName().toString();

        GenericRestApiDTO genericRestApiDTO = GenericRestApiDTO.builder()
                .basePath(annotation.basePath())
                .packageName(packageName)
                .entityName(entityName)
                .entityElement(entityElement)
                .dtoElement(dtoElement)
                .entityType(entityType)
                .dtoType(dtoType)
                .databaseType(databaseType)
                .isReactive(isReactive)
                .hasConsumer(hasConsumer)
                .idFields(getIdFieldsSafe(entityElement))
                .generatedAnnotationSpec(getGeneratedAnnotation())
                .build();

        // 1. Genera il Repository
        if (checkExists(genericRestApiDTO, "Repository")) {
            JavaFile repoFile = repositoryGenerator.generateRepository(genericRestApiDTO);
            repoFile.writeTo(processingEnv.getFiler());
        }

        // 2. Genera il Mapper
        if (checkExists(genericRestApiDTO, "Mapper")) {
            JavaFile mapperFile = mapperGenerator.generateMapper(genericRestApiDTO);
            mapperFile.writeTo(processingEnv.getFiler());
        }

        // 3. Genera il Service
        if (checkExists(genericRestApiDTO, "Service")) {
            JavaFile serviceFile = serviceGenerator.generateService(genericRestApiDTO);
            serviceFile.writeTo(processingEnv.getFiler());
        }

        // 4. Genera la Resource
        if (checkExists(genericRestApiDTO, "Resource")) {
            JavaFile resourceFile = restGenerator.generateResource(genericRestApiDTO);
            resourceFile.writeTo(processingEnv.getFiler());
        }
    }

    private boolean checkExists(GenericRestApiDTO genericRestApiDTO, String suffix) {
        String fullClassName = genericRestApiDTO.getPackageName() + "." + genericRestApiDTO.getEntityName() + suffix;
        TypeElement existingElement = processingEnv.getElementUtils().getTypeElement(fullClassName);

        if (existingElement != null) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "SKIPPED : Class " + fullClassName + " already present"
            );
            return false;
        } else {
            return true;
        }
    }

    //Utility Methods

    private AnnotationSpec getGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", AutoRestProcessor.class.getCanonicalName())
                .addMember("date", "$S", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .build();
    }

    public TypeName getTypeName(TypeElement dto) {
        try {
            return ClassName.get(dto);
        } catch (MirroredTypeException mte) {
            return TypeName.get(mte.getTypeMirror());
        }
    }

    private TypeElement getDTOTypeElement(ResourceAPI annotation) {
        try {
            return processingEnv.getElementUtils().getTypeElement(annotation.dto().getCanonicalName());
        } catch (MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            return (TypeElement) classTypeMirror.asElement();
        }
    }

    //Non funziona
    private List<String> getIdFieldsSafe(TypeElement element) {
        return element.getAnnotationMirrors().stream()
                .filter(am -> am.getAnnotationType().toString().endsWith("ResourceAPI"))
                .flatMap(am -> am.getElementValues().entrySet().stream())
                .filter(e -> e.getKey().getSimpleName().toString().equals("idFields"))
                .map(e -> (List<?>) e.getValue().getValue())
                .flatMap(List::stream)
                .map(av -> av.toString().replaceAll("^\"|\"$", "")).toList();
    }
}
