package com.serviceco.serviceco_booking_service.exception;

public class InvalidBookingStatusTransitionException extends RuntimeException {
    public InvalidBookingStatusTransitionException(String message) {
        super(message);
    }
}
