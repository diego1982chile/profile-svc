package cl.dsoto.profile.config;

import cl.dsoto.profile.entities.OfferedServiceEntity;
import cl.dsoto.profile.entities.ProfileAvailabilitySlotEntity;
import cl.dsoto.profile.entities.ProfileDetailsEntity;
import cl.dsoto.profile.entities.ProfileEntity;
import cl.dsoto.profile.entities.ProfileServiceModalityEntity;
import cl.dsoto.profile.entities.ProfileTagEntity;
import cl.dsoto.profile.entities.RateEntity;
import cl.dsoto.profile.model.DurationUnit;
import cl.dsoto.profile.model.ServiceModality;
import cl.dsoto.profile.repositories.CommuneRepository;
import cl.dsoto.profile.repositories.ProfileRepository;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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
                    "CL",
                    "13",
                    "13101",
                    FixtureDetails.basic(),
                    List.of(ServiceModality.OWN_PLACE, ServiceModality.HOTEL, ServiceModality.OUTCALL),
                    List.of("masajes", "elegante", "discreta", "santiago"),
                    List.of(
                            new FixtureAvailability(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(22, 0), true),
                            new FixtureAvailability(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(22, 0), true),
                            new FixtureAvailability(DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(22, 0), true),
                            new FixtureAvailability(DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(22, 0), true),
                            new FixtureAvailability(DayOfWeek.FRIDAY, LocalTime.of(11, 0), LocalTime.of(23, 0), true),
                            new FixtureAvailability(DayOfWeek.SATURDAY, LocalTime.of(12, 0), LocalTime.of(21, 0), true)
                    ),
                    List.of(
                            new FixtureService("Masaje relax", "Sesión tranquila y discreta.", true),
                            new FixtureService("Cena o acompañamiento", "Acompañamiento social con reserva previa.", true)
                    ),
                    List.of(
                            new FixtureRate("30 minutos", new BigDecimal("50000"), "CLP", 30, DurationUnit.MINUTES, 1, true),
                            new FixtureRate("1 hora", new BigDecimal("90000"), "CLP", 1, DurationUnit.HOURS, 2, true)
                    )
            ),
            new FixtureProfile(
                    "provider.profile@example.com",
                    "Valentina Torres",
                    "Perfil base para pruebas locales de profile-service.",
                    LocalDate.of(1994, 6, 20),
                    "CL",
                    "05",
                    "05101",
                    FixtureDetails.profile(),
                    List.of(ServiceModality.HOTEL, ServiceModality.ONLINE, ServiceModality.TO_AGREE),
                    List.of("valparaiso", "online", "conversacion", "premium"),
                    List.of(
                            new FixtureAvailability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), true),
                            new FixtureAvailability(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), true),
                            new FixtureAvailability(DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(22, 0), true),
                            new FixtureAvailability(DayOfWeek.SUNDAY, LocalTime.of(16, 0), LocalTime.of(21, 0), true)
                    ),
                    List.of(
                            new FixtureService("Videollamada", "Atención online con coordinación previa.", true),
                            new FixtureService("Acompañamiento", "Salida social y conversación.", true)
                    ),
                    List.of(
                            new FixtureRate("45 minutos online", new BigDecimal("65000"), "CLP", 45, DurationUnit.MINUTES, 1, true),
                            new FixtureRate("2 horas", new BigDecimal("150000"), "CLP", 2, DurationUnit.HOURS, 2, true)
                    )
            )
    );

    private final ProfileRepository profileRepository;
    private final CommuneRepository communeRepository;
    @SuppressWarnings("unused")
    private final LocationCatalogInitializer locationCatalogInitializer;

    @ConfigProperty(name = "profile.fixtures.enabled", defaultValue = "false")
    boolean fixturesEnabled;

    @ConfigProperty(name = "profile.media.default-storage-quota-bytes")
    Long defaultStorageQuota;

    public ProfileFixtureInitializer(
            ProfileRepository profileRepository,
            CommuneRepository communeRepository,
            LocationCatalogInitializer locationCatalogInitializer
    ) {
        this.profileRepository = profileRepository;
        this.communeRepository = communeRepository;
        this.locationCatalogInitializer = locationCatalogInitializer;
    }

    @PostConstruct
    void init() {
        if (!fixturesEnabled) {
            return;
        }

        FIXTURE_PROFILES.forEach(this::createIfMissing);
    }

    private void createIfMissing(FixtureProfile fixture) {
        ProfileEntity profile = profileRepository.findByUserId(fixture.userId())
                .orElseGet(ProfileEntity::new);
        profile.setUserId(fixture.userId());
        profile.setDisplayName(fixture.displayName());
        profile.setDescription(fixture.description());
        profile.setBirthDate(fixture.birthDate());
        profile.setAge(ageFor(fixture.birthDate()));
        profile.setCommune(communeRepository.findActiveByCountryCodeAndRegionCodeAndCommuneCode(
                fixture.countryCode(),
                fixture.regionCode(),
                fixture.communeCode()
        ).orElseThrow(() -> new IllegalStateException("fixture commune not found")));
        profile.setStorageQuota(defaultStorageQuota);
        applyDetails(profile, fixture.details());
        replaceAvailability(profile, fixture.availability());
        replaceModalities(profile, fixture.modalities());
        replaceTags(profile, fixture.tags());
        replaceServices(profile, fixture.services());
        replaceRates(profile, fixture.rates());

        profileRepository.save(profile);
    }

    private void applyDetails(ProfileEntity profile, FixtureDetails fixture) {
        ProfileDetailsEntity details = profile.getDetails();
        if (details == null) {
            details = new ProfileDetailsEntity();
            details.setProfile(profile);
            profile.setDetails(details);
        }

        details.setContactPhone(fixture.contactPhone());
        details.setWhatsappEnabled(fixture.whatsappEnabled());
        details.setShortTitle(fixture.shortTitle());
        details.setExperience(fixture.experience());
        details.setRules(fixture.rules());
        details.setHeightCm(fixture.heightCm());
        details.setWeightKg(fixture.weightKg());
        details.setMeasurements(fixture.measurements());
        details.setBodyType(fixture.bodyType());
        details.setHairColor(fixture.hairColor());
        details.setEyeColor(fixture.eyeColor());
        details.setSmokes(fixture.smokes());
        details.setTattoos(fixture.tattoos());
        details.setPiercings(fixture.piercings());
        details.setGrooming(fixture.grooming());
        details.setLanguages(fixture.languages());
    }

    private void replaceAvailability(ProfileEntity profile, List<FixtureAvailability> availability) {
        profile.getAvailabilitySlots().clear();
        for (int i = 0; i < availability.size(); i++) {
            FixtureAvailability fixture = availability.get(i);
            ProfileAvailabilitySlotEntity slot = new ProfileAvailabilitySlotEntity();
            slot.setProfile(profile);
            slot.setDayOfWeek(fixture.dayOfWeek());
            slot.setStartTime(fixture.startTime());
            slot.setEndTime(fixture.endTime());
            slot.setAvailable(fixture.available());
            slot.setDisplayOrder(i + 1);
            profile.getAvailabilitySlots().add(slot);
        }
    }

    private void replaceModalities(ProfileEntity profile, List<ServiceModality> modalities) {
        profile.getModalities().clear();
        for (int i = 0; i < modalities.size(); i++) {
            ProfileServiceModalityEntity modality = new ProfileServiceModalityEntity();
            modality.setProfile(profile);
            modality.setModality(modalities.get(i));
            modality.setActive(true);
            modality.setDisplayOrder(i + 1);
            profile.getModalities().add(modality);
        }
    }

    private void replaceTags(ProfileEntity profile, List<String> tags) {
        profile.getTags().clear();
        for (int i = 0; i < tags.size(); i++) {
            ProfileTagEntity tag = new ProfileTagEntity();
            tag.setProfile(profile);
            tag.setTag(tags.get(i));
            tag.setDisplayOrder(i + 1);
            profile.getTags().add(tag);
        }
    }

    private void replaceServices(ProfileEntity profile, List<FixtureService> services) {
        profile.getServices().clear();
        for (FixtureService fixture : services) {
            OfferedServiceEntity service = new OfferedServiceEntity();
            service.setProfile(profile);
            service.setName(fixture.name());
            service.setDescription(fixture.description());
            service.setActive(fixture.active());
            profile.getServices().add(service);
        }
    }

    private void replaceRates(ProfileEntity profile, List<FixtureRate> rates) {
        profile.getRates().clear();
        for (FixtureRate fixture : rates) {
            RateEntity rate = new RateEntity();
            rate.setProfile(profile);
            rate.setLabel(fixture.label());
            rate.setAmount(fixture.amount());
            rate.setCurrency(fixture.currency());
            rate.setDurationAmount(fixture.durationAmount());
            rate.setDurationUnit(fixture.durationUnit());
            rate.setDisplayOrder(fixture.displayOrder());
            rate.setActive(fixture.active());
            profile.getRates().add(rate);
        }
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
            String countryCode,
            String regionCode,
            String communeCode,
            FixtureDetails details,
            List<ServiceModality> modalities,
            List<String> tags,
            List<FixtureAvailability> availability,
            List<FixtureService> services,
            List<FixtureRate> rates
    ) {
    }

    private record FixtureDetails(
            String contactPhone,
            Boolean whatsappEnabled,
            String shortTitle,
            String experience,
            String rules,
            Integer heightCm,
            Integer weightKg,
            String measurements,
            String bodyType,
            String hairColor,
            String eyeColor,
            Boolean smokes,
            Boolean tattoos,
            Boolean piercings,
            String grooming,
            String languages
    ) {
        static FixtureDetails basic() {
            return new FixtureDetails(
                    "+56912345678",
                    true,
                    "Atención elegante y discreta",
                    "Cinco años de experiencia en atención privada y acompañamiento social.",
                    "Reservas con anticipación y trato respetuoso.",
                    168,
                    58,
                    "90-60-90",
                    "Delgada",
                    "Castaño",
                    "Café",
                    false,
                    true,
                    false,
                    "Depilada",
                    "Español, Inglés"
            );
        }

        static FixtureDetails profile() {
            return new FixtureDetails(
                    "+56987654321",
                    true,
                    "Conversación, presencia y reserva",
                    "Experiencia en encuentros sociales, viajes cortos y atención online.",
                    "Coordinación previa; no se aceptan solicitudes fuera de acuerdo.",
                    170,
                    60,
                    "92-62-94",
                    "Atlética",
                    "Negro",
                    "Miel",
                    false,
                    false,
                    true,
                    "Natural",
                    "Español, Portugués"
            );
        }
    }

    private record FixtureAvailability(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Boolean available
    ) {
    }

    private record FixtureService(
            String name,
            String description,
            boolean active
    ) {
    }

    private record FixtureRate(
            String label,
            BigDecimal amount,
            String currency,
            Integer durationAmount,
            DurationUnit durationUnit,
            Integer displayOrder,
            boolean active
    ) {
    }
}
