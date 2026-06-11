package cl.dsoto.profile.config;

import cl.dsoto.profile.entities.ProfileEntity;
import cl.dsoto.profile.repositories.ProfileRepository;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Startup
@Singleton
public class ProfileFixtureInitializer {

    private static final List<FixtureProfile> FIXTURE_PROFILES = List.of(
            new FixtureProfile(
                    "provider.basic@example.com",
                    "Camila Rojas",
                    "Perfil base creado desde fixture de onboarding.",
                    LocalDate.of(1996, 3, 12),
                    "Santiago"
            ),
            new FixtureProfile(
                    "provider.profile@example.com",
                    "Valentina Torres",
                    "Perfil base para pruebas locales de profile-service.",
                    LocalDate.of(1994, 6, 20),
                    "Valparaiso"
            )
    );

    private final ProfileRepository profileRepository;

    @ConfigProperty(name = "profile.fixtures.enabled", defaultValue = "false")
    boolean fixturesEnabled;

    @ConfigProperty(name = "profile.media.default-storage-quota-bytes")
    Long defaultStorageQuota;

    public ProfileFixtureInitializer(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @PostConstruct
    void init() {
        if (!fixturesEnabled) {
            return;
        }

        FIXTURE_PROFILES.forEach(this::createIfMissing);
    }

    private void createIfMissing(FixtureProfile fixture) {
        if (profileRepository.findByUserId(fixture.userId()).isPresent()) {
            return;
        }

        ProfileEntity profile = new ProfileEntity();
        profile.setUserId(fixture.userId());
        profile.setDisplayName(fixture.displayName());
        profile.setDescription(fixture.description());
        profile.setBirthDate(fixture.birthDate());
        profile.setAge(ageFor(fixture.birthDate()));
        profile.setLocation(fixture.location());
        profile.setStorageQuota(defaultStorageQuota);

        profileRepository.save(profile);
    }

    private Integer ageFor(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private record FixtureProfile(
            String userId,
            String displayName,
            String description,
            LocalDate birthDate,
            String location
    ) {
    }
}
