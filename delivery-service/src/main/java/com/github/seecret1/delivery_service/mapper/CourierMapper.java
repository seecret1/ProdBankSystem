package com.github.seecret1.delivery_service.mapper;

import com.github.seecret1.delivery_service.dto.CourierDto;
import com.github.seecret1.delivery_service.entity.Courier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface CourierMapper {

    CourierDto toDto(Courier courier);

    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "busy", constant = "false")
    Courier toEntity(CourierDto courierDto);
}
