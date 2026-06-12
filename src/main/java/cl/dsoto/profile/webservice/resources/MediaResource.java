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
        String url,
        Long fileSize,
        Integer displayOrder,
        boolean primaryMedia,
        Instant uploadedAt
) {

    public static MediaResource from(Media media, String mediaBaseUrl) {
        return new MediaResource(
                media.mediaId(),
                media.profileId(),
                media.mediaType(),
                media.mediaStatus(),
                media.storageKey(),
                mediaUrl(mediaBaseUrl, media.storageKey()),
                media.fileSize(),
                media.displayOrder(),
                media.primaryMedia(),
                media.uploadedAt()
        );
    }

    private static String mediaUrl(String mediaBaseUrl, String storageKey) {
        if (mediaBaseUrl == null || mediaBaseUrl.isBlank() || storageKey == null || storageKey.isBlank()) {
            return null;
        }

        return mediaBaseUrl.endsWith("/")
                ? mediaBaseUrl + storageKey
                : mediaBaseUrl + "/" + storageKey;
    }
}
