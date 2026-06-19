package cl.dsoto.profile.entities;

import cl.dsoto.profile.model.ServiceModality;
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

import java.util.UUID;

@Entity
@Table(name = "PROFILE_SERVICE_MODALITY")
@Getter
@Setter
@NoArgsConstructor
public class ProfileServiceModalityEntity {

    @Id
    private String modalityId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    @Enumerated(EnumType.STRING)
    private ServiceModality modality;

    private Boolean active;

    private Integer displayOrder;

    @PrePersist
    void prePersist() {
        if (modalityId == null) {
            modalityId = UUID.randomUUID().toString();
        }
    }
}
