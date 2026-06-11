package cl.dsoto.profile.webservice.impl;

import cl.dsoto.profile.services.ProfileService;
import cl.dsoto.profile.webservice.ProfileWebService;
import cl.dsoto.profile.webservice.resources.ProfileRequestResource;
import cl.dsoto.profile.webservice.resources.ProfileResource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/")
public class DefaultProfileWebService implements ProfileWebService {

    private final ProfileService profileService;

    public DefaultProfileWebService(ProfileService profileService) {
        this.profileService = profileService;
    }

    @POST
    @Path("profiles")
    @Override
    public Response createProfile(
            @HeaderParam("X-User-Id") String userId,
            @Valid ProfileRequestResource request
    ) {
        try {
            ProfileResource profile = ProfileResource.from(
                    profileService.createProfile(userId, request.toProfileUpdate())
            );
            return Response.status(Response.Status.CREATED).entity(profile).build();
        } catch (IllegalStateException exception) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("message", exception.getMessage()))
                    .build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", exception.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("profiles/me")
    @Override
    public Response getMyProfile(@HeaderParam("X-User-Id") String userId) {
        try {
            return profileService.getProfileByUserId(userId)
                    .map(ProfileResource::from)
                    .map(profile -> Response.ok(profile).build())
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", exception.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("profiles/me")
    @Override
    public Response updateMyProfile(
            @HeaderParam("X-User-Id") String userId,
            @Valid ProfileRequestResource request
    ) {
        try {
            return Response.ok(ProfileResource.from(
                    profileService.updateProfile(userId, request.toProfileUpdate())
            )).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", exception.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("public/profiles/{profileId}")
    @Override
    public Response getPublicProfile(@PathParam("profileId") String profileId) {
        return profileService.getPublishedProfile(profileId)
                .map(ProfileResource::from)
                .map(profile -> Response.ok(profile).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }
}
