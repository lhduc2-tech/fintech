package com.university.fintech.domain.mapper;

import com.university.fintech.domain.dto.WalletTransactionDTO;
import com.university.fintech.domain.entity.WalletTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletTransactionMapper extends BaseMapper<WalletTransactionEntity, WalletTransactionDTO> {

    @Override
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "linkedBankAccountId", source = "linkedBankAccount.id")
    @Mapping(target = "counterpartyWalletId", source = "counterpartyWallet.id")
    @Mapping(target = "counterpartyUserId", source = "counterpartyUser.id")
    WalletTransactionDTO toDto(WalletTransactionEntity entity);

    @Override
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "linkedBankAccount", ignore = true)
    @Mapping(target = "counterpartyWallet", ignore = true)
    @Mapping(target = "counterpartyUser", ignore = true)
    WalletTransactionEntity toEntity(WalletTransactionDTO dto);
}
