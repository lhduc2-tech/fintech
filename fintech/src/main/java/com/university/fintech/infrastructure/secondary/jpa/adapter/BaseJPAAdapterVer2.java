package com.university.fintech.infrastructure.secondary.jpa.adapter;

import com.university.fintech.domain.mapper.BaseMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@SuppressWarnings("all")
public abstract class BaseJPAAdapterVer2<T, D, ID, M extends BaseMapper<T, D>, R extends JpaRepository<T, ID>> {

    protected R repository;

    protected M mapper;


    protected BaseJPAAdapterVer2() {
    }

    protected BaseJPAAdapterVer2(R repository, M mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<D> findAll() {
        List<T> entities = this.repository.findAll();
        return this.mapper.toListDto(entities);
    }

    public List<D> findAll(Specification<T> spec) {
        return this.mapper.toListDto(repository.findAll((Sort) spec));
    }

    public Page<D> findAll(Pageable pageable) {
        return this.repository.findAll(pageable).map(this.mapper::toDto);
    }

    public List<D> findAll(Sort sort) {
        List<T> entities = this.repository.findAll(sort);
        return this.mapper.toListDto(entities);
    }

    public D get(ID id) {
        T entity = this.repository.findById(id).orElse(null);
        return this.mapper.toDto(entity);
    }

    public D save(D dto) {
        T entity = this.mapper.toEntity(dto);
        return this.mapper.toDto(this.repository.save(entity));
    }

    public List<D> saveAll(List<D> dtos) {
        List<T> entities = this.mapper.toListEntity(dtos);
        return this.mapper.toListDto(this.repository.saveAll(entities));
    }

    public D saveAndFlush(D dto) {
        T entity = this.mapper.toEntity(dto);
        return this.mapper.toDto(this.repository.saveAndFlush(entity));
    }

    public List<D> saveAllAndFlush(List<D> dtos) {
        List<D> temp = this.saveAll(dtos);
        this.repository.flush();
        return temp;
    }

    public D update(D dto) {
        T entity = this.mapper.toEntity(dto);
        return this.mapper.toDto(this.repository.save(entity));
    }

    public void delete(ID id) {
        this.repository.deleteById(id);
    }
}