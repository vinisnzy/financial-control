package com.vinisnzy.financial_control_api.exceptions;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode code;

    public BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }
}
