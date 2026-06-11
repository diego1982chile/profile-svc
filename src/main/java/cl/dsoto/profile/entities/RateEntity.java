package cl.dsoto.profile.entities;

import cl.dsoto.profile.model.DurationUnit;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "RATE")
@Getter
@Setter
@NoArgsConstructor
public class RateEntity {

    @Id
    private String rateId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private OfferedServiceEntity service;

    private String label;

    private BigDecimal amount;

    private String currency;

    private Integer durationAmount;

    @Enumerated(EnumType.STRING)
    private DurationUnit durationUnit;

    private Integer displayOrder;

    private boolean active = true;

    @PrePersist
    void prePersist() {
        if (rateId == null) {
            rateId = UUID.randomUUID().toString();
        }
    }
}
