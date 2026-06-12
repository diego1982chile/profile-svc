package cl.dsoto.profile.webservice.resources;

import cl.dsoto.profile.model.Commune;

public record CommuneResource(
        String countryCode,
        String regionCode,
        String communeCode,
        String name,
        Integer displayOrder,
        boolean active,
        RegionResource region
) {

    public static CommuneResource from(Commune commune) {
        return new CommuneResource(
                commune.countryCode(),
                commune.region().regionCode(),
                commune.communeCode(),
                commune.name(),
                commune.displayOrder(),
                commune.active(),
                RegionResource.from(commune.region())
        );
    }
}
