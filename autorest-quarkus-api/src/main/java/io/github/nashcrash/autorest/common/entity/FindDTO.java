package io.github.nashcrash.autorest.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
@Jacksonized
public class FindDTO {
    public static final String ORDER_DESC = "DESC";
    public static final String ORDER_ASC = "ASC";
    public static final String EM_MISSING_ORDER_DIRECTION = "Missing orderDirection";
    public static final String EM_INSUFFICIENT_ORDER_DIRECTION = "Insufficient orderDirection";
    public static final Integer DEFAULT_LIMIT = 100;
    private String query;
    private String[] orderBy;
    private String[] orderDirection;
    private Integer page;
    private Integer limit;

    public Integer getPage() {
        return (page == null || page < 0) ? 0 : page;
    }

    public Integer getLimit() {
        return (limit == null || limit <= 0) ? DEFAULT_LIMIT : limit;
    }
}
