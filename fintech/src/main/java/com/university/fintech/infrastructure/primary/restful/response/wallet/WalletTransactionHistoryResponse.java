package com.university.fintech.infrastructure.primary.restful.response.wallet;

import com.university.fintech.infrastructure.primary.restful.response.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
public class WalletTransactionHistoryResponse extends PageResponse<WalletTransactionHistoryResponse.Item> {

    public static <T> WalletTransactionHistoryResponse fromPage(Page<T> page, Function<T, Item> mapper) {
        WalletTransactionHistoryResponse response = new WalletTransactionHistoryResponse();
        response.setContent(page.getContent().stream().map(mapper).toList());
        response.setSize(page.getSize());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        return response;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        private BigDecimal amount;

        private String type;

        private String status;

        private OffsetDateTime updatedAt;

        private String description;
    }
}
