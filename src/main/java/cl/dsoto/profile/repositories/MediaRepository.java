package cl.dsoto.profile.repositories;

import cl.dsoto.profile.entities.MediaEntity;
import cl.dsoto.profile.model.MediaStatus;
import cl.dsoto.profile.model.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<MediaEntity, String> {

    List<MediaEntity> findByProfileProfileId(String profileId);

    long countByProfileProfileIdAndMediaTypeAndMediaStatusNot(
            String profileId,
            MediaType mediaType,
            MediaStatus mediaStatus
    );
}
