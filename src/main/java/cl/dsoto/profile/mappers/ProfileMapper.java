package cl.dsoto.profile.mappers;

import cl.dsoto.profile.entities.OfferedServiceEntity;
import cl.dsoto.profile.entities.ProfileEntity;
import cl.dsoto.profile.entities.RateEntity;
import cl.dsoto.profile.model.OfferedService;
import cl.dsoto.profile.model.Profile;
import cl.dsoto.profile.model.ProfileUpdate;
import cl.dsoto.profile.model.Rate;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi", uses = LocationMapper.class)
public interface ProfileMapper {

    Profile toModel(ProfileEntity profile);

    OfferedService toModel(OfferedServiceEntity service);

    @Mapping(target = "serviceId", source = "service.serviceId")
    Rate toModel(RateEntity rate);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "age", source = "age")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "commune", ignore = true)
    void updateEntity(ProfileUpdate update, @MappingTarget ProfileEntity profile);

    @Mapping(target = "profile", ignore = true)
    OfferedServiceEntity toEntity(OfferedService service);

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "service", ignore = true)
    RateEntity toEntity(Rate rate);
}
