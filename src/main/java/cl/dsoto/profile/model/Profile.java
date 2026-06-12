package cl.dsoto.profile.model;

import java.time.LocalDate;
import java.util.List;

public record Profile(
        String profileId,
        String userId,
        String displayName,
        String description,
        Integer age,
        LocalDate birthDate,
        Commune commune,
        PublicationStatus publicationStatus,
        AgeVerificationStatus ageVerificationStatus,
        Long storageQuota,
        Long storageUsed,
        List<OfferedService> services,
        List<Rate> rates
) {
}
