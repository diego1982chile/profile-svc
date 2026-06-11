package cl.dsoto.profile.model;

public record MediaUploadConfirmation(
        String storageKey,
        MediaType mediaType,
        Integer displayOrder,
        boolean primaryMedia
) {
}
