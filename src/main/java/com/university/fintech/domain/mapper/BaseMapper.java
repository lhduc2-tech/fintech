package com.university.fintech.domain.mapper;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BaseMapper<E, D> {

    E toEntity(D dto);

    D toDto(E entity);

    List<E> toListEntity(List<D> dtoList);

    List<D> toListDto(List<E> entities);
}