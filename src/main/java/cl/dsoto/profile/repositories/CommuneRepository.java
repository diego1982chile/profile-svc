package cl.dsoto.profile.repositories;

import cl.dsoto.profile.entities.CommuneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommuneRepository extends JpaRepository<CommuneEntity, String> {

    @Query("""
            from CommuneEntity commune
            where commune.countryCode = :countryCode
              and commune.region.regionCode = :regionCode
              and commune.active = true
            order by commune.displayOrder asc, commune.name asc
            """)
    List<CommuneEntity> findActiveByCountryCodeAndRegionCode(
            @Param("countryCode") String countryCode,
            @Param("regionCode") String regionCode
    );

    @Query("""
            from CommuneEntity commune
            where commune.countryCode = :countryCode
              and commune.region.regionCode = :regionCode
              and commune.communeCode = :communeCode
              and commune.active = true
            """)
    Optional<CommuneEntity> findActiveByCountryCodeAndRegionCodeAndCommuneCode(
            @Param("countryCode") String countryCode,
            @Param("regionCode") String regionCode,
            @Param("communeCode") String communeCode
    );
}
