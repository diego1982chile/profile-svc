package cl.dsoto.profile.webservice;

import cl.dsoto.profile.webservice.resources.MediaConfirmRequestResource;
import cl.dsoto.profile.webservice.resources.MediaUploadRequestResource;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;

public interface MediaWebService {

    Response requestUpload(String userId, @Valid MediaUploadRequestResource request);

    Response confirmUpload(String userId, @Valid MediaConfirmRequestResource request);

    Response getMyMedia(String userId);

    Response deleteMyMedia(String userId, String mediaId);
}
