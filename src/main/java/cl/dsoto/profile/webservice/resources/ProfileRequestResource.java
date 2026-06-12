package cl.dsoto.profile.webservice.resources;

import cl.dsoto.profile.model.DurationUnit;
import cl.dsoto.profile.model.OfferedService;
import cl.dsoto.profile.model.ProfileUpdate;
import cl.dsoto.profile.model.Rate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProfileRequestResource(
        @NotBlank String displayName,
        String description,
        @Min(18) Integer age,
        LocalDate birthDate,
        String countryCode,
        String regionCode,
        String communeCode,
        List<@Valid OfferedServiceResource> services,
        List<@Valid RateResource> rates
) {

    public ProfileUpdate toProfileUpdate() {
        return new ProfileUpdate(
                displayName,
                description,
                age,
                birthDate,
                countryCode,
                regionCode,
                communeCode,
                services == null
                        ? List.of()
                        : services.stream().map(OfferedServiceResource::toOfferedService).toList(),
                rates == null
                        ? List.of()
                        : rates.stream().map(RateResource::toRate).toList()
        );
    }

    public record OfferedServiceResource(
            String serviceId,
            @NotBlank String name,
            String description,
            Boolean active
    ) {

        OfferedService toOfferedService() {
            return new OfferedService(
                    serviceId,
                    name,
                    description,
                    active == null || active
            );
        }
    }

    public record RateResource(
            String rateId,
            String serviceId,
            @NotBlank String label,
            BigDecimal amount,
            String currency,
            Integer durationAmount,
            DurationUnit durationUnit,
            Integer displayOrder,
            Boolean active
    ) {

        Rate toRate() {
            return new Rate(
                    rateId,
                    serviceId,
                    label,
                    amount,
                    currency,
                    durationAmount,
                    durationUnit,
                    displayOrder,
                    active == null || active
            );
        }
    }
}
