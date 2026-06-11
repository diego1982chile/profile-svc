package cl.dsoto.profile.model;

public record OfferedService(
        String serviceId,
        String name,
        String description,
        boolean active
) {
}
