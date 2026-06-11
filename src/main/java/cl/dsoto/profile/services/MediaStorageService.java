package cl.dsoto.profile.services;

import cl.dsoto.profile.model.MediaUploadIntent;
import cl.dsoto.profile.model.MediaUploadIntentRequest;
import cl.dsoto.profile.model.StoredMediaObject;

import java.util.Optional;

public interface MediaStorageService {

    MediaUploadIntent createUploadIntent(MediaUploadIntentRequest request);

    Optional<StoredMediaObject> getObjectMetadata(String storageKey);

    void deleteObject(String storageKey);
}
