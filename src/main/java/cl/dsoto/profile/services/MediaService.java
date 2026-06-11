package cl.dsoto.profile.services;

import cl.dsoto.profile.model.Media;
import cl.dsoto.profile.model.MediaUploadConfirmation;
import cl.dsoto.profile.model.MediaUploadIntent;
import cl.dsoto.profile.model.MediaUploadRequest;

import java.util.List;

public interface MediaService {

    MediaUploadIntent requestUpload(String userId, MediaUploadRequest request);

    Media confirmUpload(String userId, MediaUploadConfirmation confirmation);

    List<Media> getMedia(String userId);

    void deleteMedia(String userId, String mediaId);
}
