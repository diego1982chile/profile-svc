package cl.dsoto.profile.webservice;

import cl.dsoto.profile.webservice.resources.ProfileRequestResource;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;

public interface ProfileWebService {

    Response createProfile(String userId, @Valid ProfileRequestResource request);

    Response getMyProfile(String userId);

    Response updateMyProfile(String userId, @Valid ProfileRequestResource request);

    Response getPublicProfile(String profileId);
}
