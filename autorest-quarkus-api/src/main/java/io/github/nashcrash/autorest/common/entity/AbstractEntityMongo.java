package io.github.nashcrash.autorest.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonId;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
public abstract class AbstractEntityMongo extends AbstractEntity {
    @BsonId
    private String id;
}
