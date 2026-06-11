package cl.dsoto.profile.model;

import java.math.BigDecimal;

public record Rate(
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
