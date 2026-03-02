package io.github.nashcrash.autorest.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.client.model.Sorts;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

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
        return Math.max(0, page);
    }

    public Integer getLimit() {
        return limit <= 0 ? DEFAULT_LIMIT : limit;
    }

    @JsonIgnore
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

    @JsonIgnore
    public Bson getBsonSort() {
        Bson sort = Sorts.ascending("_id");
        if (orderBy != null && orderDirection == null)
            throw new CustomException(Response.Status.BAD_REQUEST, EM_MISSING_ORDER_DIRECTION);
        if (orderBy != null && orderBy.length != orderDirection.length)
            throw new CustomException(Response.Status.BAD_REQUEST, EM_INSUFFICIENT_ORDER_DIRECTION);
        if (orderBy != null && orderBy.length > 0) {
            List<Bson> sorts = new ArrayList<>();
            for (int i = 0; i < orderBy.length; i++) {
                sorts.add(ORDER_DESC.equalsIgnoreCase(orderDirection[i]) ? Sorts.descending(orderBy[i]) : Sorts.ascending(orderBy[i]));
            }
            sort = Sorts.orderBy(sorts);
        }
        return sort;
    }
}
