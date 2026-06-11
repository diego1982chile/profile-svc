package cl.dsoto.profile.repositories;

import cl.dsoto.profile.entities.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity, String> {

    Optional<ProfileEntity> findByUserId(String userId);
}
