package io.github.nashcrash.autorest.common.entity;

import io.github.nashcrash.autorest.common.context.ContextHeader;
import io.github.nashcrash.autorest.common.context.ContextManager;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

import java.time.Instant;
import java.util.List;

public interface AbstractEntityMapper<ENTITY extends AbstractEntity, DTO extends AbstractDTO> {
    DTO toDto(ENTITY entity);

    ENTITY toEntity(DTO dto);

    @InheritConfiguration(
            name = "toEntity"
    )
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    void patchToEntity(DTO dto, @MappingTarget ENTITY entity);

    List<DTO> toDtos(List<ENTITY> entities);

    List<ENTITY> toEntities(List<DTO> dtos);

    @AfterMapping
    default void addExtraEntityData(@MappingTarget ENTITY entity) {
        String username = ContextManager.getParameter(ContextHeader.X_USERNAME.getValue());
        Instant now = Instant.now();
        if (StringUtils.isEmpty(entity.getInsertionUser())) {
            entity.setInsertionUser(username);
        }
        if (entity.getInsertionDate() == null) {
            entity.setInsertionDate(now);
        }
        entity.setLastModifiedUser(username);
        entity.setLastModifiedDate(now);
    }
}
