package cl.dsoto.profile.webservice.resources;

import cl.dsoto.profile.model.AgeVerificationStatus;
import cl.dsoto.profile.model.DurationUnit;
import cl.dsoto.profile.model.Profile;
import cl.dsoto.profile.model.PublicationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProfileResource(
        String profileId,
        String userId,
        String displayName,
        String description,
        Integer age,
        LocalDate birthDate,
        String location,
        PublicationStatus publicationStatus,
        AgeVerificationStatus ageVerificationStatus,
        Long storageQuota,
        Long storageUsed,
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
                profile.location(),
                profile.publicationStatus(),
                profile.ageVerificationStatus(),
                profile.storageQuota(),
                profile.storageUsed(),
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
