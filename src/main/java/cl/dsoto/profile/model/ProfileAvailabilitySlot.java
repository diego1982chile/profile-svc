package cl.dsoto.profile.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ProfileAvailabilitySlot(
        String availabilitySlotId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Boolean available,
        Integer displayOrder
) {
}
