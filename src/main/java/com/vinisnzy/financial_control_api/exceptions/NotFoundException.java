package com.vinisnzy.financial_control_api.exceptions;

public class NotFoundException extends RuntimeException {

    private NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException(resource + " not found with: " + id);
    }
}
