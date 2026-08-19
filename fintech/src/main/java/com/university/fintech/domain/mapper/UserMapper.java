package com.university.fintech.domain.mapper;


import com.university.fintech.domain.dto.UserDTO;
import com.university.fintech.domain.entity.PermissionEntity;
import com.university.fintech.domain.entity.RoleEntity;
import com.university.fintech.domain.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends BaseMapper<UserEntity, UserDTO> {

    @Override
    @Mapping(target = "roleCodes", source = "roles", qualifiedByName = "mapRoleCodes")
    @Mapping(target = "permissionCodes", source = "roles", qualifiedByName = "mapPermissionCodes")
    UserDTO toDto(UserEntity entity);

    @Override
    @BeanMapping(builder = @org.mapstruct.Builder(disableBuilder = true))
    @Mapping(target = "roles", ignore = true)
    UserEntity toEntity(UserDTO dto);

    @Named("mapRoleCodes")
    default Set<String> mapRoles(Set<RoleEntity> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(RoleEntity::getCode)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Named("mapPermissionCodes")
    default Set<String> mapPermissions(Set<RoleEntity> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles.stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getCode)
                .collect(Collectors.toCollection(HashSet::new));
    }
}