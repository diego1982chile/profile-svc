package cl.dsoto.profile.config;

import cl.dsoto.profile.entities.CommuneEntity;
import cl.dsoto.profile.entities.RegionEntity;
import cl.dsoto.profile.repositories.CommuneRepository;
import cl.dsoto.profile.repositories.RegionRepository;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Startup
@Singleton
public class LocationCatalogInitializer {

    private static final String CATALOG_RESOURCE = "/data/chile-communes.csv";

    private final RegionRepository regionRepository;
    private final CommuneRepository communeRepository;

    public LocationCatalogInitializer(
            RegionRepository regionRepository,
            CommuneRepository communeRepository
    ) {
        this.regionRepository = regionRepository;
        this.communeRepository = communeRepository;
    }

    @PostConstruct
    @Transactional
    void init() {
        if (communeRepository.count() > 0) {
            return;
        }

        List<LocationCatalogRow> rows = readCatalog();
        Map<String, RegionEntity> regionsById = new LinkedHashMap<>();

        for (LocationCatalogRow row : rows) {
            String regionId = regionId(row.countryCode(), row.regionCode());
            regionsById.computeIfAbsent(regionId, ignored -> region(row, regionsById.size() + 1));
        }

        regionRepository.saveAll(regionsById.values());

        List<CommuneEntity> communes = rows.stream()
                .map(row -> commune(row, regionsById.get(regionId(row.countryCode(), row.regionCode()))))
                .toList();
        communeRepository.saveAll(communes);
    }

    private List<LocationCatalogRow> readCatalog() {
        InputStream inputStream = LocationCatalogInitializer.class.getResourceAsStream(CATALOG_RESOURCE);
        if (inputStream == null) {
            throw new IllegalStateException("location catalog resource not found");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(this::parseRow)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("could not read location catalog", exception);
        }
    }

    private LocationCatalogRow parseRow(String line) {
        List<String> fields = parseCsvLine(line);
        if (fields.size() != 6) {
            throw new IllegalStateException("invalid location catalog row");
        }

        return new LocationCatalogRow(
                fields.get(0),
                fields.get(1),
                fields.get(2),
                fields.get(3),
                fields.get(4),
                Integer.valueOf(fields.get(5))
        );
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }

        fields.add(field.toString());
        return fields;
    }

    private RegionEntity region(LocationCatalogRow row, int displayOrder) {
        RegionEntity region = new RegionEntity();
        region.setRegionId(regionId(row.countryCode(), row.regionCode()));
        region.setCountryCode(row.countryCode());
        region.setRegionCode(row.regionCode());
        region.setName(row.regionName());
        region.setDisplayOrder(displayOrder);
        region.setActive(true);
        return region;
    }

    private CommuneEntity commune(LocationCatalogRow row, RegionEntity region) {
        CommuneEntity commune = new CommuneEntity();
        commune.setCommuneId(communeId(row.countryCode(), row.communeCode()));
        commune.setCountryCode(row.countryCode());
        commune.setCommuneCode(row.communeCode());
        commune.setName(row.communeName());
        commune.setDisplayOrder(row.displayOrder());
        commune.setActive(true);
        commune.setRegion(region);
        return commune;
    }

    private String regionId(String countryCode, String regionCode) {
        return countryCode + "-" + regionCode;
    }

    private String communeId(String countryCode, String communeCode) {
        return countryCode + "-" + communeCode;
    }

    private record LocationCatalogRow(
            String countryCode,
            String regionCode,
            String regionName,
            String communeCode,
            String communeName,
            Integer displayOrder
    ) {
    }
}
