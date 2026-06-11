package cl.dsoto.profile.repositories;

import cl.dsoto.profile.entities.RateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RateRepository extends JpaRepository<RateEntity, String> {

    List<RateEntity> findByProfileProfileId(String profileId);
}
