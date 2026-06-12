package cl.dsoto.profile.services.impl;

import cl.dsoto.profile.model.LocalMediaObject;
import cl.dsoto.profile.model.MediaUploadIntent;
import cl.dsoto.profile.model.MediaUploadIntentRequest;
import cl.dsoto.profile.model.StoredMediaObject;
import cl.dsoto.profile.services.MediaStorageService;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@ApplicationScoped
@IfBuildProperty(name = "profile.media.storage.provider", stringValue = "local")
public class LocalMediaStorageService implements MediaStorageService {

    private static final String CONTENT_TYPE_PROPERTY = "contentType";

    @ConfigProperty(name = "profile.media.local-storage-path")
    Path storagePath;

    @ConfigProperty(name = "profile.media.local-public-base-url")
    URI localPublicBaseUrl;

    @ConfigProperty(name = "profile.media.upload-url-ttl")
    Duration uploadUrlTtl;

    @Override
    public MediaUploadIntent createUploadIntent(MediaUploadIntentRequest request) {
        String storageKey = storageKey(request);

        return new MediaUploadIntent(
                storageKey,
                localPublicBaseUrl.resolve(localPublicBaseUrl.getPath().endsWith("/")
                        ? storageKey
                        : localPublicBaseUrl.getPath() + "/" + storageKey).toString(),
                "PUT",
                Map.of("Content-Type", request.contentType()),
                Instant.now().plus(uploadUrlTtl)
        );
    }

    @Override
    public Optional<StoredMediaObject> getObjectMetadata(String storageKey) {
        return readObject(storageKey)
                .map(object -> new StoredMediaObject(
                        storageKey,
                        object.contentType(),
                        object.fileSize()
                ));
    }

    @Override
    public void deleteObject(String storageKey) {
        Path objectPath = objectPath(storageKey);
        try {
            Files.deleteIfExists(objectPath);
            Files.deleteIfExists(metadataPath(objectPath));
        } catch (IOException exception) {
            throw new IllegalStateException("could not delete local media object", exception);
        }
    }

    public void writeObject(String storageKey, String contentType, InputStream inputStream) {
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content-Type is required");
        }

        Path objectPath = objectPath(storageKey);
        try {
            Files.createDirectories(objectPath.getParent());
            Files.copy(inputStream, objectPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            writeMetadata(objectPath, contentType);
        } catch (IOException exception) {
            throw new IllegalStateException("could not write local media object", exception);
        }
    }

    public Optional<LocalMediaObject> readObject(String storageKey) {
        Path objectPath = objectPath(storageKey);
        Path metadataPath = metadataPath(objectPath);
        if (!Files.isRegularFile(objectPath) || !Files.isRegularFile(metadataPath)) {
            return Optional.empty();
        }

        try (InputStream inputStream = Files.newInputStream(metadataPath)) {
            Properties metadata = new Properties();
            metadata.load(inputStream);
            String contentType = metadata.getProperty(CONTENT_TYPE_PROPERTY);
            return Optional.of(new LocalMediaObject(
                    objectPath,
                    contentType,
                    Files.size(objectPath)
            ));
        } catch (IOException exception) {
            throw new IllegalStateException("could not read local media object", exception);
        }
    }

    private void writeMetadata(Path objectPath, String contentType) throws IOException {
        Properties metadata = new Properties();
        metadata.setProperty(CONTENT_TYPE_PROPERTY, contentType);

        try (OutputStream outputStream = Files.newOutputStream(metadataPath(objectPath))) {
            metadata.store(outputStream, null);
        }
    }

    private Path objectPath(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("storageKey is required");
        }

        Path rootPath = storagePath.toAbsolutePath().normalize();
        Path objectPath = rootPath.resolve(storageKey).normalize();
        if (!objectPath.startsWith(rootPath)) {
            throw new IllegalArgumentException("invalid storageKey");
        }
        return objectPath;
    }

    private Path metadataPath(Path objectPath) {
        return objectPath.resolveSibling(objectPath.getFileName() + ".metadata");
    }

    private String storageKey(MediaUploadIntentRequest request) {
        return "profiles/%s/%s/%s".formatted(
                request.profileId(),
                request.mediaType().name().toLowerCase(),
                UUID.randomUUID()
        );
    }
}
