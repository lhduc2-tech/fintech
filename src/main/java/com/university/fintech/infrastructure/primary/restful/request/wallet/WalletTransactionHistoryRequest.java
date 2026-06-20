package com.university.fintech.infrastructure.primary.restful.request.wallet;

import com.university.fintech.infrastructure.primary.restful.request.BaseRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class WalletTransactionHistoryRequest extends BaseRequest {

    private String type;

    private String status;

    private LocalDate from;

    private LocalDate to;
}
