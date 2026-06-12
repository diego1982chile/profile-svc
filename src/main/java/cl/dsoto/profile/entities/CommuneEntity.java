package cl.dsoto.profile.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "COMMUNE")
@Getter
@Setter
@NoArgsConstructor
public class CommuneEntity {

    @Id
    private String communeId;

    private String countryCode;

    private String communeCode;

    private String name;

    private Integer displayOrder;

    private boolean active = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "region_id")
    private RegionEntity region;
}
