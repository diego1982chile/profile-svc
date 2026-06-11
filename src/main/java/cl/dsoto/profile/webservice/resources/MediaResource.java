package cl.dsoto.profile.webservice.resources;

import cl.dsoto.profile.model.Media;
import cl.dsoto.profile.model.MediaStatus;
import cl.dsoto.profile.model.MediaType;

import java.time.Instant;

public record MediaResource(
        String mediaId,
        String profileId,
        MediaType mediaType,
        MediaStatus mediaStatus,
        String storageKey,
        Long fileSize,
        Integer displayOrder,
        boolean primaryMedia,
        Instant uploadedAt
) {

    public static MediaResource from(Media media) {
        return new MediaResource(
                media.mediaId(),
                media.profileId(),
                media.mediaType(),
                media.mediaStatus(),
                media.storageKey(),
                media.fileSize(),
                media.displayOrder(),
                media.primaryMedia(),
                media.uploadedAt()
        );
    }
}
