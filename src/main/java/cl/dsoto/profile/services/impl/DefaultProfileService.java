package cl.dsoto.profile.services.impl;

import cl.dsoto.profile.entities.CommuneEntity;
import cl.dsoto.profile.entities.OfferedServiceEntity;
import cl.dsoto.profile.entities.ProfileEntity;
import cl.dsoto.profile.entities.RateEntity;
import cl.dsoto.profile.mappers.ProfileMapper;
import cl.dsoto.profile.model.OfferedService;
import cl.dsoto.profile.model.Profile;
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

        return profileMapper.toModel(profileRepository.save(profile));
    }

    @Override
    @Transactional
    public Optional<Profile> getProfileByUserId(String userId) {
        validateUserId(userId);
        return profileRepository.findByUserId(userId).map(profileMapper::toModel);
    }

    @Override
    @Transactional
    public Profile updateProfile(String userId, ProfileUpdate profileUpdate) {
        validateUserId(userId);
        ProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("profile not found"));

        apply(profile, profileUpdate);
        return profileMapper.toModel(profileRepository.save(profile));
    }

    @Override
    @Transactional
    public Optional<Profile> getPublishedProfile(String profileId) {
        if (profileId == null || profileId.isBlank()) {
            return Optional.empty();
        }

        return profileRepository.findById(profileId)
                .filter(profile -> profile.getPublicationStatus() == PublicationStatus.PUBLISHED)
                .map(profileMapper::toModel);
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
}
