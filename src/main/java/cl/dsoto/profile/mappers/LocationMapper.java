package cl.dsoto.profile.mappers;

import cl.dsoto.profile.entities.CommuneEntity;
import cl.dsoto.profile.entities.RegionEntity;
import cl.dsoto.profile.model.Commune;
import cl.dsoto.profile.model.Region;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface LocationMapper {

    Region toModel(RegionEntity region);

    @Mapping(target = "region", source = "region")
    Commune toModel(CommuneEntity commune);
}
