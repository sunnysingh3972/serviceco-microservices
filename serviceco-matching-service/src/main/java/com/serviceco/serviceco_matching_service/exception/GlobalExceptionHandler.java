package com.serviceco.serviceco_matching_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import feign.FeignException;
import feign.RetryableException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProviderServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleProviderServiceUnavailable(
            ProviderServiceUnavailableException ex) {

        Map<String, Object> errorResponse = Map.of(
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "Service Unavailable",
                "message", ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(errorResponse);
    }
@ExceptionHandler(FeignException.class)
public ResponseEntity<Map<String, Object>> handleFeignException(
        Exception ex) {

    boolean unavailable = isProviderServiceUnavailable(ex);

    HttpStatus status = unavailable
            ? HttpStatus.SERVICE_UNAVAILABLE
            : HttpStatus.BAD_GATEWAY;

    String message = unavailable
            ? "Provider Service is currently unavailable. Please try again later."
            : "Failed to communicate with Provider Service.";

    Map<String, Object> body = Map.of(
            "status", status.value(),
            "error", status.getReasonPhrase(),
            "message", message
    );

    return ResponseEntity.status(status).body(body);
}

private boolean isProviderServiceUnavailable(Throwable ex) {
    Throwable current = ex;

    while (current != null) {
        if (current instanceof RetryableException) {
            return true;
        }

        if (current instanceof FeignException feignException
                && (feignException.status() == 503
                || feignException.status() == -1)) {
            return true;
        }

        current = current.getCause();
    }

    return false;
}
}