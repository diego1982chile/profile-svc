package cl.dsoto.profile.model;

public record MediaUploadRequest(
        MediaType mediaType,
        String contentType,
        Long fileSize
) {
}
