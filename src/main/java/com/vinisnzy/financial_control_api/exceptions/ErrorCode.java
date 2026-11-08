package com.vinisnzy.financial_control_api.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INSTALLMENT_HAS_PAID_PARTS(HttpStatus.CONFLICT, "Installment has paid parts"),
    USER_WITH_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "User already exists"),
    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "Invalid period");

    private final HttpStatus status;
    private final String title;
}
