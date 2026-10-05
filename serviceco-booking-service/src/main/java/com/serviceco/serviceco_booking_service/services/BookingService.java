package com.serviceco.serviceco_booking_service.services;

import com.serviceco.serviceco_booking_service.model.dto.BookingRequest;
import com.serviceco.serviceco_booking_service.model.dto.BookingResponse;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request,String idempotencyKey);
    BookingResponse getBooking(Long bookingId);
    BookingResponse acceptBooking(Long bookingId);
    BookingResponse rejectBooking(Long bookingId);
    BookingResponse cancelBooking(Long bookingId);
    BookingResponse completeBooking(Long bookingId);
}
