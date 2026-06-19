package cl.dsoto.profile.webservice.resources;

import cl.dsoto.profile.model.DurationUnit;
import cl.dsoto.profile.model.OfferedService;
import cl.dsoto.profile.model.ProfileAvailabilitySlot;
import cl.dsoto.profile.model.ProfileDetails;
import cl.dsoto.profile.model.ProfileUpdate;
import cl.dsoto.profile.model.Rate;
import cl.dsoto.profile.model.ServiceModality;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ProfileRequestResource(
        @NotBlank String displayName,
        String description,
        @Min(18) Integer age,
        LocalDate birthDate,
        String countryCode,
        String regionCode,
        String communeCode,
        @Valid ProfileDetailsResource details,
        List<@Valid AvailabilitySlotResource> availability,
        List<ServiceModality> modalities,
        List<String> tags,
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
                details == null ? null : details.toProfileDetails(),
                availability == null
                        ? List.of()
                        : availability.stream().map(AvailabilitySlotResource::toProfileAvailabilitySlot).toList(),
                modalities == null ? List.of() : modalities,
                tags == null ? List.of() : tags,
                services == null
                        ? List.of()
                        : services.stream().map(OfferedServiceResource::toOfferedService).toList(),
                rates == null
                        ? List.of()
                        : rates.stream().map(RateResource::toRate).toList()
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

        ProfileDetails toProfileDetails() {
            return new ProfileDetails(
                    contactPhone,
                    whatsappEnabled,
                    shortTitle,
                    experience,
                    rules,
                    heightCm,
                    weightKg,
                    measurements,
                    bodyType,
                    hairColor,
                    eyeColor,
                    smokes,
                    tattoos,
                    piercings,
                    grooming,
                    languages
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

        ProfileAvailabilitySlot toProfileAvailabilitySlot() {
            return new ProfileAvailabilitySlot(
                    availabilitySlotId,
                    dayOfWeek,
                    startTime,
                    endTime,
                    available,
                    displayOrder
            );
        }
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
