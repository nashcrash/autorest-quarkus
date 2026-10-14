package io.github.nashcrash.autorest.common.util;

import io.github.nashcrash.autorest.common.entity.FindDTO;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.core.Response;

public class SortUtils {
    public static Sort getSort(FindDTO findDTO) {
        Sort sort = Sort.empty();
        if (findDTO.getOrderBy() != null && findDTO.getOrderDirection() == null)
            throw new CustomException(Response.Status.BAD_REQUEST, FindDTO.EM_MISSING_ORDER_DIRECTION);
        if (findDTO.getOrderBy() != null && findDTO.getOrderBy().length != findDTO.getOrderDirection().length)
            throw new CustomException(Response.Status.BAD_REQUEST, FindDTO.EM_INSUFFICIENT_ORDER_DIRECTION);
        if (findDTO.getOrderBy() != null) {
            for (int i = 0; i < findDTO.getOrderBy().length; i++) {
                sort = sort.and(findDTO.getOrderBy()[i], FindDTO.ORDER_DESC.equalsIgnoreCase(findDTO.getOrderDirection()[i]) ? Sort.Direction.Descending : Sort.Direction.Ascending);
            }
        }
        return sort;
    }
}
