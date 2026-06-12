package cl.dsoto.profile.webservice.impl;

import cl.dsoto.profile.services.LocationService;
import cl.dsoto.profile.webservice.resources.CommuneResource;
import cl.dsoto.profile.webservice.resources.RegionResource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Path("locations")
public class DefaultLocationWebService {

    private final LocationService locationService;

    public DefaultLocationWebService(LocationService locationService) {
        this.locationService = locationService;
    }

    @GET
    @Path("regions")
    public Response getRegions(@QueryParam("countryCode") @DefaultValue("CL") String countryCode) {
        return Response.ok(locationService.getRegions(countryCode).stream()
                .map(RegionResource::from)
                .toList()).build();
    }

    @GET
    @Path("regions/{regionCode}/communes")
    public Response getCommunes(
            @QueryParam("countryCode") @DefaultValue("CL") String countryCode,
            @PathParam("regionCode") String regionCode
    ) {
        try {
            return Response.ok(locationService.getCommunes(countryCode, regionCode).stream()
                    .map(CommuneResource::from)
                    .toList()).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", exception.getMessage()))
                    .build();
        }
    }
}
