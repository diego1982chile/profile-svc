package cl.dsoto.profile.model;

public record Region(
        String countryCode,
        String regionCode,
        String name,
        Integer displayOrder,
        boolean active
) {
}
