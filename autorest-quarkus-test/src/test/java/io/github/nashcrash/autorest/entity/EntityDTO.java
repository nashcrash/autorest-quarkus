package io.github.nashcrash.autorest.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.nashcrash.autorest.common.entity.AbstractDTO;
import io.github.nashcrash.autorest.common.validator.MultipleDateTimeDeserializer;
import io.github.nashcrash.autorest.common.validator.MultipleDateTimeFormat;
import io.github.nashcrash.autorest.common.validator.MultipleDateTimeSerializer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
@RegisterForReflection
public class EntityDTO extends AbstractDTO {
    private String eventCode;
    private String transactionType;
    private Double value;
    private List<SubEntity> movements;
    @MultipleDateTimeFormat(
            patterns = {"yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd HH:mm:ss@Z(Europe/Rome)", "dd/MM/yyyy HH:mm:ss@Z(Europe/Rome)"},
            serializePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'@Z(UTC)"
    )
    @JsonDeserialize(using = MultipleDateTimeDeserializer.class)
    @JsonSerialize(using = MultipleDateTimeSerializer.class)
    private Date dataIn;
    @MultipleDateTimeFormat(
            patterns = {"yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd HH:mm:ss@Z(Europe/Rome)", "dd/MM/yyyy HH:mm:ss@Z(Europe/Rome)"},
            serializePattern = "yyyy-MM-dd HH:mm:ss"
    )
    @JsonDeserialize(using = MultipleDateTimeDeserializer.class)
    @JsonSerialize(using = MultipleDateTimeSerializer.class)
    private Date dataOut;
}
