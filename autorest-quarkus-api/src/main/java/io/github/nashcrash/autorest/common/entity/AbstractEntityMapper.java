package io.github.nashcrash.autorest.common.entity;

import io.github.nashcrash.autorest.common.context.ContextBean;
import io.github.nashcrash.autorest.common.context.ContextHeader;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

import java.time.Instant;
import java.util.List;

@Mapper
public abstract class AbstractEntityMapper<ENTITY extends AbstractEntity, DTO extends AbstractDTO> {
    @Inject
    ContextBean contextBean;

    @Mapping(target = "id", ignore = true)
    public abstract ENTITY cloneToNewInstance(ENTITY entity);

    @Mapping(target = "id", source = "id")
    public abstract DTO toDto(ENTITY entity);

    public abstract ENTITY toEntity(DTO dto);

    @InheritConfiguration(
            name = "toEntity"
    )
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    public abstract void patchToEntity(DTO dto, @MappingTarget ENTITY entity);

    public abstract List<DTO> toDtos(List<ENTITY> entities);

    public abstract List<ENTITY> toEntities(List<DTO> dtos);

    @AfterMapping
    public  void addExtraEntityData(@MappingTarget ENTITY entity) {
        String username = contextBean.get(ContextHeader.X_USERNAME.getValue());
        Instant now = Instant.now();
        if (StringUtils.isEmpty(entity.getInsertionUser())) {
            entity.setInsertionUser(username);
        }
        if (entity.getInsertionDate() == null) {
            entity.setInsertionDate(now);
        }
        entity.setLastModifiedUser(username);
        entity.setLastModifiedDate(now);
        if (entity instanceof AbstractEntityHistorical historical) {
            if (historical.getStartValidityDate() == null) {
                historical.setStartValidityDate(now);
            }
        }
    }
}
