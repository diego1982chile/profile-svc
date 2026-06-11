package cl.dsoto.profile.model;

import java.time.Instant;

public record Media(
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
}
