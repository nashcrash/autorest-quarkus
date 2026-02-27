package io.github.nashcrash.autorest.common.entity;

import io.github.nashcrash.autorest.common.exception.CustomException;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Builder(toBuilder = true)
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
        return Math.max(0, page);
    }
    public Integer getLimit() {
        return limit <= 0 ? DEFAULT_LIMIT : limit;
    }

    public Sort getSort() {
        Sort sort = Sort.empty();
        if (orderBy != null && orderDirection == null)
            throw new CustomException(Response.Status.BAD_REQUEST, EM_MISSING_ORDER_DIRECTION);
        if (orderBy != null && orderBy.length != orderDirection.length)
            throw new CustomException(Response.Status.BAD_REQUEST, EM_INSUFFICIENT_ORDER_DIRECTION);
        if (orderBy != null) {
            for (int i = 0; i < orderBy.length; i++) {
                sort = sort.and(orderBy[i], ORDER_DESC.equalsIgnoreCase(orderDirection[i]) ? Sort.Direction.Descending : Sort.Direction.Ascending);
            }
        }
        return sort;
    }
}
