package cl.dsoto.profile.services.impl;

import cl.dsoto.profile.entities.MediaEntity;
import cl.dsoto.profile.entities.ProfileEntity;
import cl.dsoto.profile.mappers.MediaMapper;
import cl.dsoto.profile.model.Media;
import cl.dsoto.profile.model.MediaStatus;
import cl.dsoto.profile.model.MediaType;
import cl.dsoto.profile.model.MediaUploadConfirmation;
import cl.dsoto.profile.model.MediaUploadIntent;
import cl.dsoto.profile.model.MediaUploadIntentRequest;
import cl.dsoto.profile.model.MediaUploadRequest;
import cl.dsoto.profile.model.StoredMediaObject;
import cl.dsoto.profile.repositories.MediaRepository;
import cl.dsoto.profile.repositories.ProfileRepository;
import cl.dsoto.profile.services.MediaService;
import cl.dsoto.profile.services.MediaStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class DefaultMediaService implements MediaService {

    private final ProfileRepository profileRepository;
    private final MediaRepository mediaRepository;
    private final MediaStorageService mediaStorageService;
    private final MediaMapper mediaMapper;

    @ConfigProperty(name = "profile.media.max-photos")
    Long maxPhotos;

    @ConfigProperty(name = "profile.media.max-videos")
    Long maxVideos;

    public DefaultMediaService(
            ProfileRepository profileRepository,
            MediaRepository mediaRepository,
            MediaStorageService mediaStorageService,
            MediaMapper mediaMapper
    ) {
        this.profileRepository = profileRepository;
        this.mediaRepository = mediaRepository;
        this.mediaStorageService = mediaStorageService;
        this.mediaMapper = mediaMapper;
    }

    @Override
    @Transactional
    public MediaUploadIntent requestUpload(String userId, MediaUploadRequest request) {
        validateUserId(userId);
        validateUploadRequest(request);

        ProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("profile not found"));

        validateLimits(profile, request.mediaType(), request.fileSize());

        return mediaStorageService.createUploadIntent(new MediaUploadIntentRequest(
                profile.getProfileId(),
                request.mediaType(),
                request.contentType(),
                request.fileSize()
        ));
    }

    @Override
    @Transactional
    public Media confirmUpload(String userId, MediaUploadConfirmation confirmation) {
        validateUserId(userId);
        validateConfirmation(confirmation);

        ProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("profile not found"));

        if (!confirmation.storageKey().startsWith(storageKeyPrefix(profile))) {
            throw new IllegalArgumentException("storage key does not belong to profile");
        }

        StoredMediaObject storedObject = mediaStorageService.getObjectMetadata(confirmation.storageKey())
                .orElseThrow(() -> new IllegalArgumentException("uploaded object not found"));

        validateLimits(profile, confirmation.mediaType(), storedObject.fileSize());

        if (confirmation.primaryMedia()) {
            clearPrimaryMedia(profile.getProfileId());
        }

        MediaEntity media = new MediaEntity();
        media.setProfile(profile);
        media.setMediaType(confirmation.mediaType());
        media.setMediaStatus(MediaStatus.AVAILABLE);
        media.setObjectKey(storedObject.storageKey());
        media.setFileSize(storedObject.fileSize());
        media.setDisplayOrder(confirmation.displayOrder());
        media.setPrimaryMedia(confirmation.primaryMedia());

        profile.setStorageUsed(profile.getStorageUsed() + storedObject.fileSize());
        profileRepository.save(profile);

        return mediaMapper.toModel(mediaRepository.save(media));
    }

    @Override
    @Transactional
    public List<Media> getMedia(String userId) {
        validateUserId(userId);
        ProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("profile not found"));

        return mediaRepository.findByProfileProfileId(profile.getProfileId()).stream()
                .filter(media -> media.getMediaStatus() != MediaStatus.DELETED)
                .sorted(Comparator.comparing(
                        MediaEntity::getDisplayOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .map(mediaMapper::toModel)
                .toList();
    }

    @Override
    @Transactional
    public void deleteMedia(String userId, String mediaId) {
        validateUserId(userId);
        if (mediaId == null || mediaId.isBlank()) {
            throw new IllegalArgumentException("mediaId is required");
        }

        ProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("profile not found"));

        MediaEntity media = mediaRepository.findById(mediaId)
                .filter(item -> item.getProfile().getProfileId().equals(profile.getProfileId()))
                .orElseThrow(() -> new IllegalArgumentException("media not found"));

        if (media.getMediaStatus() == MediaStatus.DELETED) {
            return;
        }

        mediaStorageService.deleteObject(media.getObjectKey());
        media.setMediaStatus(MediaStatus.DELETED);
        profile.setStorageUsed(Math.max(0L, profile.getStorageUsed() - media.getFileSize()));
        profileRepository.save(profile);
        mediaRepository.save(media);
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
    }

    private void validateUploadRequest(MediaUploadRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("media upload request is required");
        }
        if (request.mediaType() == null) {
            throw new IllegalArgumentException("mediaType is required");
        }
        if (request.contentType() == null || request.contentType().isBlank()) {
            throw new IllegalArgumentException("contentType is required");
        }
        if (request.fileSize() == null || request.fileSize() <= 0) {
            throw new IllegalArgumentException("fileSize must be positive");
        }
    }

    private void validateConfirmation(MediaUploadConfirmation confirmation) {
        if (confirmation == null) {
            throw new IllegalArgumentException("media upload confirmation is required");
        }
        if (confirmation.storageKey() == null || confirmation.storageKey().isBlank()) {
            throw new IllegalArgumentException("storageKey is required");
        }
        if (confirmation.mediaType() == null) {
            throw new IllegalArgumentException("mediaType is required");
        }
    }

    private void validateLimits(ProfileEntity profile, MediaType mediaType, Long fileSize) {
        long currentCount = mediaRepository.countByProfileProfileIdAndMediaTypeAndMediaStatusNot(
                profile.getProfileId(),
                mediaType,
                MediaStatus.DELETED
        );
        long maxCount = mediaType == MediaType.PHOTO ? maxPhotos : maxVideos;
        if (currentCount >= maxCount) {
            throw new IllegalStateException("media type limit exceeded");
        }
        if (profile.getStorageUsed() + fileSize > profile.getStorageQuota()) {
            throw new IllegalStateException("storage quota exceeded");
        }
    }

    private void clearPrimaryMedia(String profileId) {
        mediaRepository.findByProfileProfileId(profileId).stream()
                .filter(media -> media.getMediaStatus() != MediaStatus.DELETED)
                .forEach(media -> media.setPrimaryMedia(false));
    }

    private String storageKeyPrefix(ProfileEntity profile) {
        return "profiles/" + profile.getProfileId() + "/";
    }

}
