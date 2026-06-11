package cl.dsoto.profile.repositories;

import cl.dsoto.profile.entities.OfferedServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferedServiceRepository extends JpaRepository<OfferedServiceEntity, String> {

    List<OfferedServiceEntity> findByProfileProfileId(String profileId);
}
