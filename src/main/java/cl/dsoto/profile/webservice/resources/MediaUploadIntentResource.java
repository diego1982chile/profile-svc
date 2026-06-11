package cl.dsoto.profile.webservice.resources;

import cl.dsoto.profile.model.MediaUploadIntent;

import java.time.Instant;
import java.util.Map;

public record MediaUploadIntentResource(
        String storageKey,
        String uploadUrl,
        String method,
        Map<String, String> headers,
        Instant expiresAt
) {

    public static MediaUploadIntentResource from(MediaUploadIntent intent) {
        return new MediaUploadIntentResource(
                intent.storageKey(),
                intent.uploadUrl(),
                intent.method(),
                intent.headers(),
                intent.expiresAt()
        );
    }
}
