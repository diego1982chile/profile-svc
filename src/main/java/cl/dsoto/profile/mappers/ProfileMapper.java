package cl.dsoto.profile.mappers;

import cl.dsoto.profile.entities.OfferedServiceEntity;
import cl.dsoto.profile.entities.ProfileAvailabilitySlotEntity;
import cl.dsoto.profile.entities.ProfileDetailsEntity;
import cl.dsoto.profile.entities.ProfileEntity;
import cl.dsoto.profile.entities.ProfileServiceModalityEntity;
import cl.dsoto.profile.entities.ProfileTagEntity;
import cl.dsoto.profile.entities.RateEntity;
import cl.dsoto.profile.model.OfferedService;
import cl.dsoto.profile.model.Profile;
import cl.dsoto.profile.model.ProfileAvailabilitySlot;
import cl.dsoto.profile.model.ProfileCompletion;
import cl.dsoto.profile.model.ProfileDetails;
import cl.dsoto.profile.model.ProfileUpdate;
import cl.dsoto.profile.model.Rate;
import cl.dsoto.profile.model.ServiceModality;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "cdi", uses = LocationMapper.class)
public interface ProfileMapper {

    @Mapping(target = "availability", source = "availabilitySlots")
    @Mapping(target = "completion", expression = "java(defaultCompletion())")
    Profile toModel(ProfileEntity profile);

    ProfileDetails toModel(ProfileDetailsEntity details);

    ProfileAvailabilitySlot toModel(ProfileAvailabilitySlotEntity availabilitySlot);

    OfferedService toModel(OfferedServiceEntity service);

    @Mapping(target = "serviceId", source = "service.serviceId")
    Rate toModel(RateEntity rate);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "age", source = "age")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "commune", ignore = true)
    void updateEntity(ProfileUpdate update, @MappingTarget ProfileEntity profile);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "contactPhone", source = "contactPhone")
    @Mapping(target = "whatsappEnabled", source = "whatsappEnabled")
    @Mapping(target = "shortTitle", source = "shortTitle")
    @Mapping(target = "experience", source = "experience")
    @Mapping(target = "rules", source = "rules")
    @Mapping(target = "heightCm", source = "heightCm")
    @Mapping(target = "weightKg", source = "weightKg")
    @Mapping(target = "measurements", source = "measurements")
    @Mapping(target = "bodyType", source = "bodyType")
    @Mapping(target = "hairColor", source = "hairColor")
    @Mapping(target = "eyeColor", source = "eyeColor")
    @Mapping(target = "smokes", source = "smokes")
    @Mapping(target = "tattoos", source = "tattoos")
    @Mapping(target = "piercings", source = "piercings")
    @Mapping(target = "grooming", source = "grooming")
    @Mapping(target = "languages", source = "languages")
    void updateDetailsEntity(ProfileDetails details, @MappingTarget ProfileDetailsEntity entity);

    @Mapping(target = "profile", ignore = true)
    OfferedServiceEntity toEntity(OfferedService service);

    @Mapping(target = "profile", ignore = true)
    ProfileAvailabilitySlotEntity toEntity(ProfileAvailabilitySlot availabilitySlot);

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "service", ignore = true)
    RateEntity toEntity(Rate rate);

    default List<ServiceModality> toModelModalities(List<ProfileServiceModalityEntity> modalities) {
        if (modalities == null) {
            return List.of();
        }

        return modalities.stream()
                .filter(modality -> modality.getActive() == null || modality.getActive())
                .map(ProfileServiceModalityEntity::getModality)
                .filter(Objects::nonNull)
                .toList();
    }

    default List<String> toModelTags(List<ProfileTagEntity> tags) {
        if (tags == null) {
            return List.of();
        }

        return tags.stream()
                .map(ProfileTagEntity::getTag)
                .filter(Objects::nonNull)
                .toList();
    }

    default ProfileCompletion defaultCompletion() {
        return new ProfileCompletion(false, 0, List.of());
    }
}
