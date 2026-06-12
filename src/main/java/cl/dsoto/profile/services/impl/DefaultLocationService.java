package cl.dsoto.profile.services.impl;

import cl.dsoto.profile.mappers.LocationMapper;
import cl.dsoto.profile.model.Commune;
import cl.dsoto.profile.model.Region;
import cl.dsoto.profile.repositories.CommuneRepository;
import cl.dsoto.profile.repositories.RegionRepository;
import cl.dsoto.profile.services.LocationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class DefaultLocationService implements LocationService {

    private static final String DEFAULT_COUNTRY_CODE = "CL";

    private final RegionRepository regionRepository;
    private final CommuneRepository communeRepository;
    private final LocationMapper locationMapper;

    public DefaultLocationService(
            RegionRepository regionRepository,
            CommuneRepository communeRepository,
            LocationMapper locationMapper
    ) {
        this.regionRepository = regionRepository;
        this.communeRepository = communeRepository;
        this.locationMapper = locationMapper;
    }

    @Override
    @Transactional
    public List<Region> getRegions(String countryCode) {
        return regionRepository.findActiveByCountryCode(
                        normalizeCountryCode(countryCode)
                ).stream()
                .map(locationMapper::toModel)
                .toList();
    }

    @Override
    @Transactional
    public List<Commune> getCommunes(String countryCode, String regionCode) {
        if (regionCode == null || regionCode.isBlank()) {
            throw new IllegalArgumentException("regionCode is required");
        }

        String normalizedCountryCode = normalizeCountryCode(countryCode);
        if (regionRepository.findByCountryCodeAndRegionCodeAndActiveTrue(
                normalizedCountryCode,
                regionCode
        ).isEmpty()) {
            throw new IllegalArgumentException("region not found");
        }

        return communeRepository.findActiveByCountryCodeAndRegionCode(
                        normalizedCountryCode,
                        regionCode
                ).stream()
                .map(locationMapper::toModel)
                .toList();
    }

    private String normalizeCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return DEFAULT_COUNTRY_CODE;
        }
        return countryCode.trim().toUpperCase();
    }
}
