package cl.dsoto.profile.entities;

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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "PROFILE_AVAILABILITY_SLOT")
@Getter
@Setter
@NoArgsConstructor
public class ProfileAvailabilitySlotEntity {

    @Id
    private String availabilitySlotId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Boolean available;

    private Integer displayOrder;

    @PrePersist
    void prePersist() {
        if (availabilitySlotId == null) {
            availabilitySlotId = UUID.randomUUID().toString();
        }
    }
}
