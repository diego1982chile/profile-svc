package cl.dsoto.profile.model;

public record MediaUploadIntentRequest(
        String profileId,
        MediaType mediaType,
        String contentType,
        Long fileSize
) {
}
