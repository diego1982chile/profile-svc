package cl.dsoto.profile.model;

import java.time.LocalDate;
import java.util.List;

public record ProfileUpdate(
        String displayName,
        String description,
        Integer age,
        LocalDate birthDate,
        String countryCode,
        String regionCode,
        String communeCode,
        List<OfferedService> services,
        List<Rate> rates
) {
    public ProfileUpdate {
        countryCode = blankToNull(countryCode);
        regionCode = blankToNull(regionCode);
        communeCode = blankToNull(communeCode);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
