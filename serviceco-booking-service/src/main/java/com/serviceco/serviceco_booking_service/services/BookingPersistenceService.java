package com.serviceco.serviceco_booking_service.services;

import com.serviceco.serviceco_booking_service.exception.BookingConflictException;
import com.serviceco.serviceco_booking_service.model.Booking;
import com.serviceco.serviceco_booking_service.model.BookingStatus;
import com.serviceco.serviceco_booking_service.model.ProviderBookingLock;
import com.serviceco.serviceco_booking_service.repository.BookingRepository;
import com.serviceco.serviceco_booking_service.repository.ProviderBookingLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingPersistenceService {

    private final BookingRepository bookingRepository;
    private final ProviderBookingLockRepository lockRepository;

    @Transactional
    public Booking saveBookingWithLock(
            Booking booking,
            LocalDateTime bookingStart,
            LocalDateTime bookingEnd) {

        Long providerId = booking.getProviderId();

        // 1. Ensure the provider has a lock row
        lockRepository.ensureLockRowExists(providerId);

        // 2. Acquire pessimistic lock
        ProviderBookingLock lock =
                lockRepository.findByProviderIdForUpdate(providerId);

        // 3. Check for overlapping active bookings
        boolean hasConflict =
                bookingRepository.existsOverlappingBooking(
                        providerId,
                        bookingStart,
                        bookingEnd,
                        List.of(
                                BookingStatus.PENDING,
                                BookingStatus.ACCEPTED
                        )
                );

        if (hasConflict) {
            throw new BookingConflictException(
                    "Provider is already booked for the requested time"
            );
        }

        // 4. Save booking
        return bookingRepository.save(booking);
    }
}