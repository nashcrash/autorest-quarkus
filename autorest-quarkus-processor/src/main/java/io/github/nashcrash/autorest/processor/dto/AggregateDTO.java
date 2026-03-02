package io.github.nashcrash.autorest.processor.dto;

import com.squareup.javapoet.TypeName;
import io.github.nashcrash.autorest.common.entity.AccumulatorType;
import io.github.nashcrash.autorest.common.entity.FieldPair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AggregateDTO {
    private String name;
    private String path;
    private TypeElement dtoTypeElement;
    private TypeName dtoTypeName;
    private List<FieldPair> groupBy;
    private Map<AccumulatorType, FieldPair> aggregateBy;
}
