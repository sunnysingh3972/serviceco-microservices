package com.serviceco.serviceco_booking_service.services;

import com.serviceco.serviceco_booking_service.client.ProviderClient;
import com.serviceco.serviceco_booking_service.exception.*;
import com.serviceco.serviceco_booking_service.mapper.BookingMapper;
import com.serviceco.serviceco_booking_service.model.Booking;
import com.serviceco.serviceco_booking_service.model.BookingStatus;
import com.serviceco.serviceco_booking_service.model.dto.BookingRequest;
import com.serviceco.serviceco_booking_service.model.dto.BookingResponse;
import com.serviceco.serviceco_booking_service.model.dto.ProviderResponse;
import com.serviceco.serviceco_booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ProviderClient providerClient;
    private final BookingMapper bookingMapper;
    private final BookingPersistenceService bookingPersistenceService;

    @Override
    public BookingResponse createBooking(BookingRequest request,String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency-Key is required"
            );
        }
        // 1. Get provider from Provider Service
        ProviderResponse provider =
                providerClient.getProvider(request.providerId());

        // 2. Check provider availability
        if (!"AVAILABLE".equalsIgnoreCase(provider.status())) {
            throw new ProviderNotAvailableException(
                    "Provider is not available for booking"
            );
        }

        // 3. Normalize requested skill
        String requestedSkill =
                request.serviceSkill().trim().toUpperCase();

        // 4. Check whether provider has the requested skill
        boolean hasSkill =
                provider.skills() != null &&
                        provider.skills()
                                .stream()
                                .anyMatch(skill ->
                                        requestedSkill.equalsIgnoreCase(skill)
                                );

        if (!hasSkill) {
            throw new ProviderSkillNotFoundException(
                    "Provider does not offer skill: " + requestedSkill
            );
        }

        // 5. Get provider's current hourly rate
        BigDecimal hourlyRate = provider.hourlyRate();

        // 6. Calculate booking time range
        LocalDateTime bookingStart = request.bookingDate();

        LocalDateTime bookingEnd =
                bookingStart.plusHours(request.durationHours());

        // 7. Check whether provider is already booked
        boolean hasConflict =
                bookingRepository.existsOverlappingBooking(
                        request.providerId(),
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

        // 8. Calculate total booking amount
        BigDecimal totalAmount =
                hourlyRate.multiply(
                        BigDecimal.valueOf(request.durationHours())
                );
        Optional<Booking> existingBooking =
                bookingRepository.findByCustomerIdAndIdempotencyKey(
                        request.customerId(),
                        idempotencyKey
                );
        if (existingBooking.isPresent()) {
            return bookingMapper.toResponse(
                    existingBooking.get()
            );
        }

        // 9. Create Booking entity
        Booking booking = Booking.builder()
                .customerId(request.customerId())
                .providerId(request.providerId())
                .serviceSkill(requestedSkill)
                .bookingDate(bookingStart)
                .bookingEnd(bookingEnd)
                .durationHours(request.durationHours())
                .hourlyRate(hourlyRate)
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .build();

        // 10. Save booking
        Booking savedBooking =
                bookingPersistenceService.saveBookingWithLock(
                        booking,
                        bookingStart,
                        bookingEnd
                );

        // 11. Return API response
        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public BookingResponse getBooking(Long bookingId) {
        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new BookingNotFoundException(
                                        "Booking not found with ID: " + bookingId
                                )
                        );

        return bookingMapper.toResponse(booking);
    }

    @Override
    public BookingResponse acceptBooking(Long bookingId) {
        Booking booking = getBookingEntity(bookingId);

        validateTransition(
                booking,
                BookingStatus.ACCEPTED
        );

        booking.setStatus(BookingStatus.ACCEPTED);

        return bookingMapper.toResponse(
                bookingRepository.save(booking)
        );
    }

    @Override
    public BookingResponse rejectBooking(Long bookingId) {
        Booking booking = getBookingEntity(bookingId);

        validateTransition(
                booking,
                BookingStatus.REJECTED
        );

        booking.setStatus(BookingStatus.REJECTED);

        return bookingMapper.toResponse(
                bookingRepository.save(booking)
        );
    }

    @Override
    public BookingResponse cancelBooking(Long bookingId) {


        Booking booking = getBookingEntity(bookingId);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long currentUserId =
                Long.parseLong(authentication.getName());

        if (!booking.getCustomerId().equals(currentUserId)) {
            throw new AccessDeniedException(
                    "You are not allowed to cancel this booking"
            );
        }
        validateTransition(
                booking,
                BookingStatus.CANCELLED
        );

        booking.setStatus(BookingStatus.CANCELLED);

        return bookingMapper.toResponse(
                bookingRepository.save(booking)
        );
    }

    @Override
    public BookingResponse completeBooking(Long bookingId) {
        Booking booking = getBookingEntity(bookingId);

        validateTransition(
                booking,
                BookingStatus.COMPLETED
        );

        booking.setStatus(BookingStatus.COMPLETED);

        return bookingMapper.toResponse(
                bookingRepository.save(booking)
        );
    }
    private Booking getBookingEntity(Long bookingId) {

        return bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with id: " + bookingId
                        )
                );
    }
    private void validateTransition(
            Booking booking,
            BookingStatus newStatus) {

        BookingStatus currentStatus = booking.getStatus();

        boolean valid = switch (currentStatus) {

            case PENDING ->
                    newStatus == BookingStatus.ACCEPTED
                            || newStatus == BookingStatus.REJECTED
                            || newStatus == BookingStatus.CANCELLED;

            case ACCEPTED ->
                    newStatus == BookingStatus.COMPLETED
                            || newStatus == BookingStatus.CANCELLED;

            case REJECTED, CANCELLED, COMPLETED ->
                    false;
        };

        if (!valid) {
            throw new InvalidBookingStatusTransitionException(
                    "Cannot change booking status from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }
    }
}