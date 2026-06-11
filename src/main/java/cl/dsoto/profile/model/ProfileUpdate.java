package cl.dsoto.profile.model;

import java.time.LocalDate;
import java.util.List;

public record ProfileUpdate(
        String displayName,
        String description,
        Integer age,
        LocalDate birthDate,
        String location,
        List<OfferedService> services,
        List<Rate> rates
) {
}
