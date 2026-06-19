package cl.dsoto.profile.webservice.resources;

import cl.dsoto.profile.model.AgeVerificationStatus;
import cl.dsoto.profile.model.DurationUnit;
import cl.dsoto.profile.model.Profile;
import cl.dsoto.profile.model.ProfileCompletion;
import cl.dsoto.profile.model.ServiceModality;
import cl.dsoto.profile.model.PublicationStatus;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ProfileResource(
        String profileId,
        String userId,
        String displayName,
        String description,
        Integer age,
        LocalDate birthDate,
        CommuneResource commune,
        PublicationStatus publicationStatus,
        AgeVerificationStatus ageVerificationStatus,
        Long storageQuota,
        Long storageUsed,
        ProfileDetailsResource details,
        List<AvailabilitySlotResource> availability,
        List<ServiceModality> modalities,
        List<String> tags,
        ProfileCompletion completion,
        List<OfferedServiceResource> services,
        List<RateResource> rates
) {

    public static ProfileResource from(Profile profile) {
        return new ProfileResource(
                profile.profileId(),
                profile.userId(),
                profile.displayName(),
                profile.description(),
                profile.age(),
                profile.birthDate(),
                profile.commune() == null ? null : CommuneResource.from(profile.commune()),
                profile.publicationStatus(),
                profile.ageVerificationStatus(),
                profile.storageQuota(),
                profile.storageUsed(),
                profile.details() == null ? null : ProfileDetailsResource.from(profile.details()),
                profile.availability().stream()
                        .map(AvailabilitySlotResource::from)
                        .toList(),
                profile.modalities(),
                profile.tags(),
                profile.completion(),
                profile.services().stream()
                        .map(service -> new OfferedServiceResource(
                                service.serviceId(),
                                service.name(),
                                service.description(),
                                service.active()
                        ))
                        .toList(),
                profile.rates().stream()
                        .map(rate -> new RateResource(
                                rate.rateId(),
                                rate.serviceId(),
                                rate.label(),
                                rate.amount(),
                                rate.currency(),
                                rate.durationAmount(),
                                rate.durationUnit(),
                                rate.displayOrder(),
                                rate.active()
                        ))
                        .toList()
        );
    }

    public record ProfileDetailsResource(
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

        static ProfileDetailsResource from(cl.dsoto.profile.model.ProfileDetails details) {
            return new ProfileDetailsResource(
                    details.contactPhone(),
                    details.whatsappEnabled(),
                    details.shortTitle(),
                    details.experience(),
                    details.rules(),
                    details.heightCm(),
                    details.weightKg(),
                    details.measurements(),
                    details.bodyType(),
                    details.hairColor(),
                    details.eyeColor(),
                    details.smokes(),
                    details.tattoos(),
                    details.piercings(),
                    details.grooming(),
                    details.languages()
            );
        }
    }

    public record AvailabilitySlotResource(
            String availabilitySlotId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Boolean available,
            Integer displayOrder
    ) {

        static AvailabilitySlotResource from(cl.dsoto.profile.model.ProfileAvailabilitySlot slot) {
            return new AvailabilitySlotResource(
                    slot.availabilitySlotId(),
                    slot.dayOfWeek(),
                    slot.startTime(),
                    slot.endTime(),
                    slot.available(),
                    slot.displayOrder()
            );
        }
    }

    public record OfferedServiceResource(
            String serviceId,
            String name,
            String description,
            boolean active
    ) {
    }

    public record RateResource(
            String rateId,
            String serviceId,
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
