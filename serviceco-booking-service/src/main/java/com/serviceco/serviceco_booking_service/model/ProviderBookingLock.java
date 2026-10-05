package com.serviceco.serviceco_booking_service.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provider_booking_locks")
@NoArgsConstructor
@AllArgsConstructor
public class ProviderBookingLock {

    @Id
    @Column(name = "provider_id")
    private Long providerId;
}
