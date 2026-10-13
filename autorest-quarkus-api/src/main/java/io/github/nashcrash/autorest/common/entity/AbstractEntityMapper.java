package io.github.nashcrash.autorest.common.entity;

import io.github.nashcrash.autorest.common.context.ContextBean;
import io.github.nashcrash.autorest.common.context.ContextHeader;
import io.github.nashcrash.autorest.common.util.IdUtils;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

import java.time.Instant;
import java.util.List;

public abstract class AbstractEntityMapper<ENTITY extends AbstractEntity, DTO extends AbstractDTO> {
    @Inject
    ContextBean contextBean;

    @Mapping(target = "id", ignore = true)
    public abstract ENTITY cloneToNewInstance(ENTITY entity);

    @Mapping(target = "id", source = "id")
    public abstract DTO toDto(ENTITY entity);

    public abstract ENTITY toEntity(DTO dto);

    /**
     * Applies the DTO onto an existing entity, ignoring null fields.
     * The technical id and the business key are never taken from the DTO: the id belongs to the target
     * instance (a new one when historizing), and the business key is immutable after creation.
     */
    @InheritConfiguration(
            name = "toEntity"
    )
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessKey", ignore = true)
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
        if (entity instanceof AbstractEntityMongo mongo && mongo.getId() == null) {
             mongo.setId(IdUtils.randomULID());
        }
        // The business key identifies the entity over time: generate it once, at creation.
        // When @ResourceAPI(businessKeyFields = {...}) is declared, the generated mapper overrides
        // this method and derives the key from those fields instead.
        if (entity.getBusinessKey() == null) {
            entity.setBusinessKey(IdUtils.randomULID());
        }
    }
}
