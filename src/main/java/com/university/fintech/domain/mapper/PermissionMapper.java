package com.university.fintech.domain.mapper;

import com.university.fintech.domain.dto.PermissionDTO;
import com.university.fintech.domain.entity.PermissionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PermissionMapper extends BaseMapper<PermissionEntity, PermissionDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    PermissionEntity toEntity(PermissionDTO dto);
}
