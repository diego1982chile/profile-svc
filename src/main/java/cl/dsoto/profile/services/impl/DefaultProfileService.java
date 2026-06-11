package cl.dsoto.profile.services.impl;

import cl.dsoto.profile.entities.OfferedServiceEntity;
import cl.dsoto.profile.entities.ProfileEntity;
import cl.dsoto.profile.entities.RateEntity;
import cl.dsoto.profile.model.OfferedService;
import cl.dsoto.profile.model.Profile;
import cl.dsoto.profile.model.ProfileUpdate;
import cl.dsoto.profile.model.PublicationStatus;
import cl.dsoto.profile.model.Rate;
import cl.dsoto.profile.repositories.ProfileRepository;
import cl.dsoto.profile.services.ProfileService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class DefaultProfileService implements ProfileService {

    private final ProfileRepository profileRepository;

    @ConfigProperty(name = "profile.media.default-storage-quota-bytes")
    Long defaultStorageQuota;

    public DefaultProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
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

        profile.setDisplayName(update.displayName());
        profile.setDescription(update.description());
        profile.setAge(update.age());
        profile.setBirthDate(update.birthDate());
        profile.setLocation(update.location());
        replaceServices(profile, update.services());
        replaceRates(profile, update.rates());
    }

    private void replaceServices(ProfileEntity profile, List<OfferedService> services) {
        profile.getServices().clear();
        if (services == null) {
            return;
        }

        services.forEach(service -> {
            OfferedServiceEntity entity = new OfferedServiceEntity();
            entity.setServiceId(service.serviceId());
            entity.setProfile(profile);
            entity.setName(service.name());
            entity.setDescription(service.description());
            entity.setActive(service.active());
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
            RateEntity entity = new RateEntity();
            entity.setRateId(rate.rateId());
            entity.setProfile(profile);
            entity.setService(rate.serviceId() == null ? null : servicesById.get(rate.serviceId()));
            entity.setLabel(rate.label());
            entity.setAmount(rate.amount());
            entity.setCurrency(rate.currency());
            entity.setDurationAmount(rate.durationAmount());
            entity.setDurationUnit(rate.durationUnit());
            entity.setDisplayOrder(rate.displayOrder());
            entity.setActive(rate.active());
            profile.getRates().add(entity);
        });
    }

    private Profile toModel(ProfileEntity profile) {
        return new Profile(
                profile.getProfileId(),
                profile.getUserId(),
                profile.getDisplayName(),
                profile.getDescription(),
                profile.getAge(),
                profile.getBirthDate(),
                profile.getLocation(),
                profile.getPublicationStatus(),
                profile.getAgeVerificationStatus(),
                profile.getStorageQuota(),
                profile.getStorageUsed(),
                profile.getServices().stream()
                        .map(service -> new OfferedService(
                                service.getServiceId(),
                                service.getName(),
                                service.getDescription(),
                                service.isActive()
                        ))
                        .toList(),
                profile.getRates().stream()
                        .map(rate -> new Rate(
                                rate.getRateId(),
                                rate.getService() == null ? null : rate.getService().getServiceId(),
                                rate.getLabel(),
                                rate.getAmount(),
                                rate.getCurrency(),
                                rate.getDurationAmount(),
                                rate.getDurationUnit(),
                                rate.getDisplayOrder(),
                                rate.isActive()
                        ))
                        .toList()
        );
    }
}
