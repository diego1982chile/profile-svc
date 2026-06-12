package cl.dsoto.profile.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "REGION")
@Getter
@Setter
@NoArgsConstructor
public class RegionEntity {

    @Id
    private String regionId;

    private String countryCode;

    private String regionCode;

    private String name;

    private Integer displayOrder;

    private boolean active = true;
}
