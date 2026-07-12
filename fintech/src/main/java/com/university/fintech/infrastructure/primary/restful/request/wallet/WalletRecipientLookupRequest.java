package com.university.fintech.infrastructure.primary.restful.request.wallet;

import com.university.fintech.shared.constant.Constant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalletRecipientLookupRequest {

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = Constant.PHONE_NUMBER_NUMERIC_PATTERN, message = Constant.MESSAGE_PHONE_NUMBER_NUMERIC_ONLY)
    @Pattern(regexp = Constant.PHONE_NUMBER_PATTERN, message = Constant.MESSAGE_PHONE_NUMBER_EXACTLY_10_DIGITS)
    private String recipientPhoneNumber;
}
