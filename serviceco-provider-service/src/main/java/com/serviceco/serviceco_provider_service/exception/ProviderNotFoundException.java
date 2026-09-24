package com.serviceco.serviceco_provider_service.exception;

public class ProviderNotFoundException extends RuntimeException{
    public ProviderNotFoundException(String message) {
        super(message);
    }
}
