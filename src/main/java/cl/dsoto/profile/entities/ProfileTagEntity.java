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
@Table(name = "PROFILE_TAG")
@Getter
@Setter
@NoArgsConstructor
public class ProfileTagEntity {

    @Id
    private String tagId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    private String tag;

    private Integer displayOrder;

    @PrePersist
    void prePersist() {
        if (tagId == null) {
            tagId = UUID.randomUUID().toString();
        }
    }
}
