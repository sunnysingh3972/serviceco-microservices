package com.serviceco.serviceco_booking_service.mapper;

import com.serviceco.serviceco_booking_service.model.Booking;
import com.serviceco.serviceco_booking_service.model.dto.BookingResponse;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {

        return new BookingResponse(
                booking.getId(),
                booking.getCustomerId(),
                booking.getProviderId(),
                booking.getServiceSkill(),
                booking.getBookingDate(),
                booking.getBookingEnd(),
                booking.getDurationHours(),
                booking.getHourlyRate(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}