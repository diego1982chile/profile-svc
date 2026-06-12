package cl.dsoto.profile.services;

import cl.dsoto.profile.model.Commune;
import cl.dsoto.profile.model.Region;

import java.util.List;

public interface LocationService {

    List<Region> getRegions(String countryCode);

    List<Commune> getCommunes(String countryCode, String regionCode);
}
