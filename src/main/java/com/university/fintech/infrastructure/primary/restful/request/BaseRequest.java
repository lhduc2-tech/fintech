package com.university.fintech.infrastructure.primary.restful.request;


import com.university.fintech.shared.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BaseRequest {

    private Integer pageIndex;
    private Integer pageSize;
    private String sort;
    private String direction;

    public Pageable toPageable() {
        int page = getValidPage(pageIndex);
        int size = getValidSize(pageSize);
        Sort.Direction sortDirection = getSortDirection();
        if (!StringUtils.hasText(sort)) {
            return org.springframework.data.domain.PageRequest.of(page, size);
        }
        return org.springframework.data.domain.PageRequest.of(page, size, sortDirection, sort);
    }

    private Sort.Direction getSortDirection() {
        try {
            return Sort.Direction.fromString(direction);
        } catch (Exception e) {
            return Sort.Direction.ASC;
        }
    }

    private int getValidPage(Integer page) {
        if (page == null || page < 0) {
            return Constant.DEFAULT_PAGE;
        }
        return page;
    }

    private int getValidSize(Integer size) {
        if (size == null || size <= 0) {
            return Constant.DEFAULT_SIZE;
        }
        return size;
    }
}