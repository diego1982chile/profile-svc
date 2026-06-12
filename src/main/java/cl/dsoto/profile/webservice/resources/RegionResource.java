package cl.dsoto.profile.webservice.resources;

import cl.dsoto.profile.model.Region;

public record RegionResource(
        String countryCode,
        String regionCode,
        String name,
        Integer displayOrder,
        boolean active
) {

    public static RegionResource from(Region region) {
        return new RegionResource(
                region.countryCode(),
                region.regionCode(),
                region.name(),
                region.displayOrder(),
                region.active()
        );
    }
}
