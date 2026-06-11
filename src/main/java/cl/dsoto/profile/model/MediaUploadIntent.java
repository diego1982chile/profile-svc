package cl.dsoto.profile.model;

import java.time.Instant;
import java.util.Map;

public record MediaUploadIntent(
        String storageKey,
        String uploadUrl,
        String method,
        Map<String, String> headers,
        Instant expiresAt
) {
}
