package cl.dsoto.profile.model;

public record Commune(
        String countryCode,
        String communeCode,
        String name,
        Integer displayOrder,
        boolean active,
        Region region
) {
}
