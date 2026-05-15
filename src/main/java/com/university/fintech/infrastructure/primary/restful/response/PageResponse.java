package com.university.fintech.infrastructure.primary.restful.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private int size;
    private long totalElements;
    private int totalPages;

    @Builder.Default
    private List<T> content = List.of();

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    public static <U, V> PageResponse<V> of(Page<U> page, Function<U, V> mapper) {
        return PageResponse.<V>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }
}