package cl.dsoto.profile.entities;

import jakarta.persistence.Entity;
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
@Table(name = "OFFERED_SERVICE")
@Getter
@Setter
@NoArgsConstructor
public class OfferedServiceEntity {

    @Id
    private String serviceId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    private String name;

    private String description;

    private boolean active = true;

    @PrePersist
    void prePersist() {
        if (serviceId == null) {
            serviceId = UUID.randomUUID().toString();
        }
    }
}
