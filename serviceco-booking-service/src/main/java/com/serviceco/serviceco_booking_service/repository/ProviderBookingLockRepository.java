package com.serviceco.serviceco_booking_service.repository;

import com.serviceco.serviceco_booking_service.model.ProviderBookingLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProviderBookingLockRepository extends JpaRepository<ProviderBookingLock, Long> {
    // Additional query methods can be defined here if needed
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM ProviderBookingLock p
            WHERE p.providerId = :providerId
            """)
    ProviderBookingLock findByProviderIdForUpdate(
            @Param("providerId") Long providerId
    );
    @Modifying
    @Query(
            value = """
                INSERT INTO provider_booking_locks (provider_id)
                VALUES (:providerId)
                ON DUPLICATE KEY UPDATE provider_id = provider_id
                """,
            nativeQuery = true
    )
    void ensureLockRowExists(
            @Param("providerId") Long providerId
    );
}
