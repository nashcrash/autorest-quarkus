package io.github.nashcrash.autorest.processor.dto;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;
import io.github.nashcrash.autorest.api.DatabaseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.lang.model.element.TypeElement;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class GenericRestApiDTO {
    private String packageName;
    private String basePath;
    private String entityName;
    private TypeElement entityElement;
    private TypeElement dtoElement;
    private DatabaseType databaseType;
    private boolean isReactive;
    private boolean hasConsumer;
    private List<String> idFields;
    private TypeName entityType;
    private TypeName dtoType;
    private AnnotationSpec generatedAnnotationSpec;
}
