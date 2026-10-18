package com.serviceco.serviceco_matching_service.exception;

public class ProviderServiceUnavailableException extends RuntimeException {

    public ProviderServiceUnavailableException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}