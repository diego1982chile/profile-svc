package cl.dsoto.profile.model;

public record StoredMediaObject(
        String storageKey,
        String contentType,
        Long fileSize
) {
}
