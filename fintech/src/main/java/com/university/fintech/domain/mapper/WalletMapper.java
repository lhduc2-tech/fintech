package com.university.fintech.domain.mapper;

import com.university.fintech.domain.dto.WalletDTO;
import com.university.fintech.domain.entity.WalletEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper extends BaseMapper<WalletEntity, WalletDTO> {

    @Override
    @Mapping(target = "userId", source = "user.id")
    WalletDTO toDto(WalletEntity entity);

    @Override
    @Mapping(target = "user", ignore = true)
    WalletEntity toEntity(WalletDTO dto);
}
