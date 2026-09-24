package com.serviceco.serviceco_provider_service.model.entity;

import com.serviceco.serviceco_provider_service.utility.ProviderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private  String name;
    @Column(nullable = false, unique = true)
    private  String email;
    @Column(nullable = false)
    private  String phoneNumber;
    private  Integer experience;
    @Column(nullable = false)
    private  String location;
    @Column(name = "hourly_rate", nullable = false)
    private BigDecimal hourlyRate;
    private Double rating;
    @Enumerated(EnumType.STRING)
    @Column( nullable = false)
    private ProviderStatus status;
    @OneToMany(
            mappedBy = "provider",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ProviderSkill> skills = new ArrayList<>();

}
