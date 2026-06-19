package cl.dsoto.profile.services.impl;

import cl.dsoto.profile.entities.CommuneEntity;
import cl.dsoto.profile.entities.OfferedServiceEntity;
import cl.dsoto.profile.entities.ProfileAvailabilitySlotEntity;
import cl.dsoto.profile.entities.ProfileDetailsEntity;
import cl.dsoto.profile.entities.ProfileEntity;
import cl.dsoto.profile.entities.ProfileServiceModalityEntity;
import cl.dsoto.profile.entities.ProfileTagEntity;
import cl.dsoto.profile.entities.RateEntity;
import cl.dsoto.profile.mappers.ProfileMapper;
import cl.dsoto.profile.model.OfferedService;
import cl.dsoto.profile.model.Profile;
import cl.dsoto.profile.model.ProfileCompletion;
import cl.dsoto.profile.model.ProfileUpdate;
import cl.dsoto.profile.model.PublicationStatus;
import cl.dsoto.profile.model.Rate;
import cl.dsoto.profile.repositories.CommuneRepository;
import cl.dsoto.profile.repositories.ProfileRepository;
import cl.dsoto.profile.services.ProfileService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class DefaultProfileService implements ProfileService {

    private final ProfileRepository profileRepository;
    private final CommuneRepository communeRepository;
    private final ProfileMapper profileMapper;

    @ConfigProperty(name = "profile.media.default-storage-quota-bytes")
    Long defaultStorageQuota;

    public DefaultProfileService(
            ProfileRepository profileRepository,
            CommuneRepository communeRepository,
            ProfileMapper profileMapper
    ) {
        this.profileRepository = profileRepository;
        this.communeRepository = communeRepository;
        this.profileMapper = profileMapper;
    }

    @Override
    @Transactional
    public Profile createProfile(String userId, ProfileUpdate profileUpdate) {
        validateUserId(userId);
        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("profile already exists for user");
        }

        ProfileEntity profile = new ProfileEntity();
        profile.setUserId(userId);
        profile.setStorageQuota(defaultStorageQuota);
        apply(profile, profileUpdate);

        return toModel(profileRepository.save(profile));
    }

    @Override
    @Transactional
    public Optional<Profile> getProfileByUserId(String userId) {
        validateUserId(userId);
        return profileRepository.findByUserId(userId).map(this::toModel);
    }

    @Override
    @Transactional
    public Profile updateProfile(String userId, ProfileUpdate profileUpdate) {
        validateUserId(userId);
        ProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("profile not found"));

        apply(profile, profileUpdate);
        return toModel(profileRepository.save(profile));
    }

    @Override
    @Transactional
    public Optional<Profile> getPublishedProfile(String profileId) {
        if (profileId == null || profileId.isBlank()) {
            return Optional.empty();
        }

        return profileRepository.findById(profileId)
                .filter(profile -> profile.getPublicationStatus() == PublicationStatus.PUBLISHED)
                .map(this::toModel);
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
    }

    private void apply(ProfileEntity profile, ProfileUpdate update) {
        if (update == null) {
            throw new IllegalArgumentException("profile update is required");
        }

        profileMapper.updateEntity(update, profile);
        profile.setCommune(resolveCommune(update));
        replaceDetails(profile, update);
        replaceAvailability(profile, update);
        replaceModalities(profile, update);
        replaceTags(profile, update);
        replaceServices(profile, update.services());
        replaceRates(profile, update.rates());
    }

    private CommuneEntity resolveCommune(ProfileUpdate update) {
        if (update.countryCode() == null && update.regionCode() == null && update.communeCode() == null) {
            return null;
        }

        String countryCode = update.countryCode() == null ? "CL" : update.countryCode();
        if (update.regionCode() == null) {
            throw new IllegalArgumentException("regionCode is required");
        }

        if (update.communeCode() == null) {
            throw new IllegalArgumentException("communeCode is required");
        }

        return communeRepository.findActiveByCountryCodeAndRegionCodeAndCommuneCode(
                        countryCode,
                        update.regionCode(),
                        update.communeCode()
                )
                .orElseThrow(() -> new IllegalArgumentException("commune not found"));
    }

    private void replaceServices(ProfileEntity profile, List<OfferedService> services) {
        profile.getServices().clear();
        if (services == null) {
            return;
        }

        services.forEach(service -> {
            OfferedServiceEntity entity = profileMapper.toEntity(service);
            entity.setProfile(profile);
            profile.getServices().add(entity);
        });
    }

    private void replaceRates(ProfileEntity profile, List<Rate> rates) {
        profile.getRates().clear();
        if (rates == null) {
            return;
        }

        Map<String, OfferedServiceEntity> servicesById = profile.getServices().stream()
                .filter(service -> service.getServiceId() != null)
                .collect(Collectors.toMap(OfferedServiceEntity::getServiceId, Function.identity()));

        rates.forEach(rate -> {
            RateEntity entity = profileMapper.toEntity(rate);
            entity.setProfile(profile);
            entity.setService(rate.serviceId() == null ? null : servicesById.get(rate.serviceId()));
            profile.getRates().add(entity);
        });
    }

    private void replaceDetails(ProfileEntity profile, ProfileUpdate update) {
        if (update.details() == null) {
            profile.setDetails(null);
            return;
        }

        ProfileDetailsEntity details = profile.getDetails();
        if (details == null) {
            details = new ProfileDetailsEntity();
            details.setProfile(profile);
            profile.setDetails(details);
        }

        profileMapper.updateDetailsEntity(update.details(), details);
    }

    private void replaceAvailability(ProfileEntity profile, ProfileUpdate update) {
        profile.getAvailabilitySlots().clear();
        if (update.availability() == null) {
            return;
        }

        update.availability().forEach(slot -> {
            ProfileAvailabilitySlotEntity entity = profileMapper.toEntity(slot);
            entity.setProfile(profile);
            profile.getAvailabilitySlots().add(entity);
        });
    }

    private void replaceModalities(ProfileEntity profile, ProfileUpdate update) {
        profile.getModalities().clear();
        if (update.modalities() == null) {
            return;
        }

        int[] index = {0};
        update.modalities().stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(modality -> {
                    ProfileServiceModalityEntity entity = new ProfileServiceModalityEntity();
                    entity.setProfile(profile);
                    entity.setModality(modality);
                    entity.setActive(true);
                    entity.setDisplayOrder(++index[0]);
                    profile.getModalities().add(entity);
                });
    }

    private void replaceTags(ProfileEntity profile, ProfileUpdate update) {
        profile.getTags().clear();
        if (update.tags() == null) {
            return;
        }

        int[] index = {0};
        update.tags().stream()
                .map(this::normalizeTag)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(tag -> {
                    ProfileTagEntity entity = new ProfileTagEntity();
                    entity.setProfile(profile);
                    entity.setTag(tag);
                    entity.setDisplayOrder(++index[0]);
                    profile.getTags().add(entity);
                });
    }

    private String normalizeTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return null;
        }

        return tag.trim().toLowerCase();
    }

    private Profile toModel(ProfileEntity profile) {
        Profile model = profileMapper.toModel(profile);
        ProfileCompletion completion = calculateCompletion(profile);
        return new Profile(
                model.profileId(),
                model.userId(),
                model.displayName(),
                model.description(),
                model.age(),
                model.birthDate(),
                model.commune(),
                model.publicationStatus(),
                model.ageVerificationStatus(),
                model.storageQuota(),
                model.storageUsed(),
                model.details(),
                model.availability(),
                model.modalities(),
                model.tags(),
                completion,
                model.services(),
                model.rates()
        );
    }

    private ProfileCompletion calculateCompletion(ProfileEntity profile) {
        List<String> missingFields = new java.util.ArrayList<>();
        if (isBlank(profile.getDisplayName())) {
            missingFields.add("displayName");
        }
        if (isBlank(profile.getDescription())) {
            missingFields.add("description");
        }
        if (profile.getBirthDate() == null && profile.getAge() == null) {
            missingFields.add("birthDate");
        }
        if (profile.getCommune() == null) {
            missingFields.add("commune");
        }
        if (profile.getDetails() == null || Boolean.TRUE.equals(profile.getDetails().getWhatsappEnabled())
                && isBlank(profile.getDetails().getContactPhone())) {
            missingFields.add("contactPhone");
        }
        if (profile.getMedia().stream().noneMatch(media -> media.getMediaType() == cl.dsoto.profile.model.MediaType.PHOTO
                && media.getMediaStatus() == cl.dsoto.profile.model.MediaStatus.AVAILABLE)) {
            missingFields.add("photo");
        }
        if (profile.getServices().stream().noneMatch(service -> service.isActive() && !isBlank(service.getName()))) {
            missingFields.add("services");
        }
        if (profile.getRates().stream().noneMatch(rate -> rate.isActive() && !isBlank(rate.getLabel()))) {
            missingFields.add("rates");
        }
        if (profile.getModalities().stream().noneMatch(modality -> modality.getActive() == null || modality.getActive())) {
            missingFields.add("modalities");
        }
        if (profile.getAvailabilitySlots().stream().noneMatch(slot -> slot.getAvailable() == null || slot.getAvailable())) {
            missingFields.add("availability");
        }

        int total = 10;
        int percent = Math.round(((float) (total - missingFields.size()) / total) * 100);
        return new ProfileCompletion(missingFields.isEmpty(), percent, missingFields);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
