package com.github.seecret1.delivery_service.mapper;

import com.github.seecret1.delivery_service.dto.user.FullNameDto;
import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import com.github.seecret1.delivery_service.entity.FullName;
import com.github.seecret1.delivery_service.entity.Recipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface RecipientMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "fullName", source = "fullName", qualifiedByName = "toFullName")
    Recipient toEntity(RecipientDto dto);

    @Mapping(target = "fullName", source = "fullName", qualifiedByName = "toFullNameDto")
    RecipientDto toDto(Recipient entity);

    @Named("toFullName")
    FullName toFullName(FullNameDto dto);

    @Named("toFullNameDto")
    FullNameDto toFullNameDto(FullName entity);
}
