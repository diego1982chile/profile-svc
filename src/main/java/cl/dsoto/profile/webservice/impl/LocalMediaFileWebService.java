package cl.dsoto.profile.webservice.impl;

import cl.dsoto.profile.model.LocalMediaObject;
import cl.dsoto.profile.services.impl.LocalMediaStorageService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;

@RequestScoped
@Path("media/files")
public class LocalMediaFileWebService {

    private final Instance<LocalMediaStorageService> localMediaStorageService;

    public LocalMediaFileWebService(Instance<LocalMediaStorageService> localMediaStorageService) {
        this.localMediaStorageService = localMediaStorageService;
    }

    @PUT
    @Path("{storageKey:.+}")
    @Consumes("*/*")
    public Response upload(
            @PathParam("storageKey") String storageKey,
            @HeaderParam("Content-Type") String contentType,
            InputStream inputStream
    ) {
        LocalMediaStorageService storageService = storageService();
        storageService.writeObject(storageKey, contentType, inputStream);
        return Response.noContent().build();
    }

    @GET
    @Path("{storageKey:.+}")
    @Produces("*/*")
    public Response download(@PathParam("storageKey") String storageKey) {
        LocalMediaObject object = storageService().readObject(storageKey)
                .orElseThrow(NotFoundException::new);

        return Response.ok(object.path().toFile(), object.contentType())
                .header("Content-Length", object.fileSize())
                .build();
    }

    private LocalMediaStorageService storageService() {
        if (localMediaStorageService.isUnsatisfied()) {
            throw new NotFoundException();
        }
        return localMediaStorageService.get();
    }
}
