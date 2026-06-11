package cl.dsoto.profile.webservice.impl;

import cl.dsoto.profile.services.MediaService;
import cl.dsoto.profile.webservice.MediaWebService;
import cl.dsoto.profile.webservice.resources.MediaConfirmRequestResource;
import cl.dsoto.profile.webservice.resources.MediaResource;
import cl.dsoto.profile.webservice.resources.MediaUploadIntentResource;
import cl.dsoto.profile.webservice.resources.MediaUploadRequestResource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
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
public class DefaultMediaWebService implements MediaWebService {

    private final MediaService mediaService;

    public DefaultMediaWebService(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @POST
    @Path("media/upload-url")
    @Override
    public Response requestUpload(
            @HeaderParam("X-User-Id") String userId,
            @Valid MediaUploadRequestResource request
    ) {
        try {
            return Response.ok(MediaUploadIntentResource.from(
                    mediaService.requestUpload(userId, request.toUploadRequest())
            )).build();
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

    @POST
    @Path("media/confirm")
    @Override
    public Response confirmUpload(
            @HeaderParam("X-User-Id") String userId,
            @Valid MediaConfirmRequestResource request
    ) {
        try {
            return Response.status(Response.Status.CREATED)
                    .entity(MediaResource.from(mediaService.confirmUpload(userId, request.toConfirmation())))
                    .build();
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
    @Path("profiles/me/media")
    @Override
    public Response getMyMedia(@HeaderParam("X-User-Id") String userId) {
        try {
            return Response.ok(mediaService.getMedia(userId).stream()
                    .map(MediaResource::from)
                    .toList()).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", exception.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("profiles/me/media/{mediaId}")
    @Override
    public Response deleteMyMedia(
            @HeaderParam("X-User-Id") String userId,
            @PathParam("mediaId") String mediaId
    ) {
        try {
            mediaService.deleteMedia(userId, mediaId);
            return Response.noContent().build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", exception.getMessage()))
                    .build();
        }
    }
}
