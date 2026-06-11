package cl.dsoto.profile.webservice.resources;

import cl.dsoto.profile.model.MediaType;
import cl.dsoto.profile.model.MediaUploadRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MediaUploadRequestResource(
        @NotNull MediaType mediaType,
        @NotBlank String contentType,
        @NotNull @Min(1) Long fileSize
) {

    public MediaUploadRequest toUploadRequest() {
        return new MediaUploadRequest(mediaType, contentType, fileSize);
    }
}
