package cl.dsoto.profile.services.impl;

import cl.dsoto.profile.model.MediaUploadIntent;
import cl.dsoto.profile.model.MediaUploadIntentRequest;
import cl.dsoto.profile.model.StoredMediaObject;
import cl.dsoto.profile.services.MediaStorageService;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@IfBuildProperty(name = "profile.media.storage.provider", stringValue = "fake", enableIfMissing = true)
public class FakeMediaStorageService implements MediaStorageService {

    private final Map<String, StoredMediaObject> objects = new ConcurrentHashMap<>();

    @ConfigProperty(name = "profile.media.fake-upload-base-url")
    String fakeUploadBaseUrl;

    @ConfigProperty(name = "profile.media.upload-url-ttl")
    Duration uploadUrlTtl;

    @Override
    public MediaUploadIntent createUploadIntent(MediaUploadIntentRequest request) {
        String storageKey = storageKey(request);
        objects.put(storageKey, new StoredMediaObject(
                storageKey,
                request.contentType(),
                request.fileSize()
        ));

        return new MediaUploadIntent(
                storageKey,
                fakeUploadBaseUrl + "/" + storageKey,
                "PUT",
                Map.of("Content-Type", request.contentType()),
                Instant.now().plus(uploadUrlTtl)
        );
    }

    @Override
    public Optional<StoredMediaObject> getObjectMetadata(String storageKey) {
        return Optional.ofNullable(objects.get(storageKey));
    }

    @Override
    public void deleteObject(String storageKey) {
        objects.remove(storageKey);
    }

    private String storageKey(MediaUploadIntentRequest request) {
        return "profiles/%s/%s/%s".formatted(
                request.profileId(),
                request.mediaType().name().toLowerCase(),
                UUID.randomUUID()
        );
    }
}
