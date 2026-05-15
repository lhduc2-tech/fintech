package com.university.fintech.domain.mapper;


import com.university.fintech.domain.dto.RoleDTO;
import com.university.fintech.domain.entity.PermissionEntity;
import com.university.fintech.domain.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper extends BaseMapper<RoleEntity, RoleDTO> {

    @Override
    @Mapping(target = "permissionCodes", source = "permissions", qualifiedByName = "mapPermissionCodes")
    RoleDTO toDto(RoleEntity entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    RoleEntity toEntity(RoleDTO dto);

    @Named("mapPermissionCodes")
    default Set<String> mapPermissionCodes(Set<PermissionEntity> permissions) {
        if (permissions == null) {
            return Collections.emptySet();
        }
        return permissions.stream()
                .map(PermissionEntity::getCode)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
