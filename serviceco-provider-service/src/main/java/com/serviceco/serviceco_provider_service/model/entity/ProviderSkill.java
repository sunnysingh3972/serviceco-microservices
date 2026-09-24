package com.serviceco.serviceco_provider_service.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "provider_skills",
        indexes = {
                @Index(
                        name = "idx_skill_name",
                        columnList = "skill_name"
                ),
                @Index(
                        name = "idx_provider_id",
                        columnList = "provider_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "provider_id",
            nullable = false
    )
    private Provider provider;
}