package com.serviceco.serviceco_booking_service.controller;

import com.serviceco.serviceco_booking_service.model.dto.BookingRequest;
import com.serviceco.serviceco_booking_service.model.dto.BookingResponse;
import com.serviceco.serviceco_booking_service.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody BookingRequest request) {

        return bookingService.createBooking(request,idempotencyKey);
    }
    @GetMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public BookingResponse getBooking(@Valid@PathVariable Long bookingId) {
        return bookingService.getBooking(bookingId);
    }
    @PatchMapping("/{bookingId}/accept")
    public BookingResponse acceptBooking(
            @PathVariable Long bookingId) {

        return bookingService.acceptBooking(bookingId);
    }
    @PatchMapping("/{bookingId}/reject")
    public BookingResponse rejectBooking(
            @PathVariable Long bookingId) {

        return bookingService.rejectBooking(bookingId);
    }
    @PatchMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(
            @PathVariable Long bookingId) {

        return bookingService.cancelBooking(bookingId);
    }
    @PatchMapping("/{bookingId}/complete")
    public BookingResponse completeBooking(
            @PathVariable Long bookingId) {

        return bookingService.completeBooking(bookingId);
    }
}