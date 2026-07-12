package com.university.fintech.domain.mapper;

import com.university.fintech.domain.dto.BankDTO;
import com.university.fintech.domain.entity.BankEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankMapper extends BaseMapper<BankEntity, BankDTO> {
}
