package com.serviceco.serviceco_booking_service.repository;

import com.serviceco.serviceco_booking_service.model.Booking;
import com.serviceco.serviceco_booking_service.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Booking b
            WHERE b.providerId = :providerId
              AND b.status IN :activeStatuses
              AND b.bookingDate < :requestedEnd
              AND b.bookingEnd > :requestedStart
            """)
    boolean existsOverlappingBooking(
            @Param("providerId") Long providerId,
            @Param("requestedStart") LocalDateTime requestedStart,
            @Param("requestedEnd") LocalDateTime requestedEnd,
            @Param("activeStatuses") List<BookingStatus> activeStatuses
    );
    Optional<Booking> findByCustomerIdAndIdempotencyKey(
            Long customerId,
            String idempotencyKey
    );
}
