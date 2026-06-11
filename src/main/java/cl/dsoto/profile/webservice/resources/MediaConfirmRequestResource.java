package cl.dsoto.profile.webservice.resources;

import cl.dsoto.profile.model.MediaType;
import cl.dsoto.profile.model.MediaUploadConfirmation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MediaConfirmRequestResource(
        @NotBlank String storageKey,
        @NotNull MediaType mediaType,
        Integer displayOrder,
        Boolean primaryMedia
) {

    public MediaUploadConfirmation toConfirmation() {
        return new MediaUploadConfirmation(
                storageKey,
                mediaType,
                displayOrder,
                primaryMedia != null && primaryMedia
        );
    }
}
