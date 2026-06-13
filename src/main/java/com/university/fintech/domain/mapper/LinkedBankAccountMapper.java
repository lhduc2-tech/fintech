package com.university.fintech.domain.mapper;

import com.university.fintech.domain.dto.LinkedBankAccountDTO;
import com.university.fintech.domain.entity.LinkedBankAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LinkedBankAccountMapper extends BaseMapper<LinkedBankAccountEntity, LinkedBankAccountDTO> {

    @Override
    @Mapping(target = "userId", source = "user.id")
    LinkedBankAccountDTO toDto(LinkedBankAccountEntity entity);

    @Override
    @Mapping(target = "user", ignore = true)
    LinkedBankAccountEntity toEntity(LinkedBankAccountDTO dto);
}
