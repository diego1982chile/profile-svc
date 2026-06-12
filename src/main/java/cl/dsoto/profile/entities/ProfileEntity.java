package cl.dsoto.profile.entities;

import cl.dsoto.profile.model.AgeVerificationStatus;
import cl.dsoto.profile.model.PublicationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "PROFILE")
@Getter
@Setter
@NoArgsConstructor
public class ProfileEntity {

    @Id
    private String profileId;

    private String userId;

    private String displayName;

    private String description;

    private Integer age;

    private LocalDate birthDate;

    @ManyToOne
    @JoinColumn(name = "commune_id")
    private CommuneEntity commune;

    @Enumerated(EnumType.STRING)
    private PublicationStatus publicationStatus = PublicationStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    private AgeVerificationStatus ageVerificationStatus = AgeVerificationStatus.NOT_STARTED;

    private Long storageQuota = 524288000L;

    private Long storageUsed = 0L;

    private Instant createdAt;

    private Instant updatedAt;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfferedServiceEntity> services = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RateEntity> rates = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaEntity> media = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (profileId == null) {
            profileId = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
