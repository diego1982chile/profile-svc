package cl.dsoto.profile.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PROFILE_DETAILS")
@Getter
@Setter
@NoArgsConstructor
public class ProfileDetailsEntity {

    @Id
    private String profileId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    private String contactPhone;

    private Boolean whatsappEnabled;

    private String shortTitle;

    private String experience;

    private String rules;

    private Integer heightCm;

    private Integer weightKg;

    private String measurements;

    private String bodyType;

    private String hairColor;

    private String eyeColor;

    private Boolean smokes;

    private Boolean tattoos;

    private Boolean piercings;

    private String grooming;

    private String languages;
}
