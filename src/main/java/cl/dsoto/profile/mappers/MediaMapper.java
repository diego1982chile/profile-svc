package cl.dsoto.profile.mappers;

import cl.dsoto.profile.entities.MediaEntity;
import cl.dsoto.profile.model.Media;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface MediaMapper {

    @Mapping(target = "profileId", source = "profile.profileId")
    @Mapping(target = "storageKey", source = "objectKey")
    Media toModel(MediaEntity media);
}
