package com.university.fintech.domain.mapper;

import com.university.fintech.domain.dto.PermissionDTO;
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
public interface PermissionMapper extends BaseMapper<PermissionEntity, PermissionDTO> {

    @Override
    @Mapping(target = "roleCodes", source = "roles", qualifiedByName = "mapRoleCodes")
    PermissionDTO toDto(PermissionEntity entity);

    @Override
    @Mapping(target = "roles", ignore = true)
    PermissionEntity toEntity(PermissionDTO dto);

    @Named("mapRoleCodes")
    default Set<String> mapRoleCodes(Set<RoleEntity> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(RoleEntity::getCode)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
