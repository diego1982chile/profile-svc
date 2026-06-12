package cl.dsoto.profile.repositories;

import cl.dsoto.profile.entities.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<RegionEntity, String> {

    @Query("""
            from RegionEntity region
            where region.countryCode = :countryCode
              and region.active = true
            order by region.displayOrder asc, region.name asc
            """)
    List<RegionEntity> findActiveByCountryCode(@Param("countryCode") String countryCode);

    Optional<RegionEntity> findByCountryCodeAndRegionCodeAndActiveTrue(String countryCode, String regionCode);
}
