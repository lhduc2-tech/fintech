package com.university.fintech.domain.mapper;

import com.university.fintech.domain.dto.LinkedBankAccountDTO;
import com.university.fintech.domain.entity.LinkedBankAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LinkedBankAccountMapper extends BaseMapper<LinkedBankAccountEntity, LinkedBankAccountDTO> {

    @Override
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "bankId", source = "bank.id")
    @Mapping(target = "bankCode", source = "bank.bankCode")
    @Mapping(target = "bankName", source = "bank.bankName")
    @Mapping(target = "accountNumber", source = "bank.accountNumber")
    @Mapping(target = "accountHolderName", source = "bank.accountHolderName")
    LinkedBankAccountDTO toDto(LinkedBankAccountEntity entity);

    @Override
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "bank", ignore = true)
    LinkedBankAccountEntity toEntity(LinkedBankAccountDTO dto);
}
