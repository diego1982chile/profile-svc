package cl.dsoto.profile.entities;

import cl.dsoto.profile.model.MediaStatus;
import cl.dsoto.profile.model.MediaType;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "MEDIA")
@Getter
@Setter
@NoArgsConstructor
public class MediaEntity {

    @Id
    private String mediaId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @Enumerated(EnumType.STRING)
    private MediaStatus mediaStatus = MediaStatus.PENDING;

    private String objectKey;

    private Long fileSize;

    private Integer displayOrder;

    private boolean primaryMedia;

    private Instant uploadedAt;

    @PrePersist
    void prePersist() {
        if (mediaId == null) {
            mediaId = UUID.randomUUID().toString();
        }
        if (uploadedAt == null) {
            uploadedAt = Instant.now();
        }
    }
}
