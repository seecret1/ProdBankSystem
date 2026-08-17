package com.github.seecret1.order_service.mapper;

import com.github.seecret1.order_service.dto.address.AddressBaseResponse;
import com.github.seecret1.order_service.dto.address.AddressRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = CONSTRUCTOR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface AddressMapper {

    @Mapping(target = "address", source = "address")
    @Mapping(target = "zipCode", source = "zipCode")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "countryCode", source = "countryCode")
    AddressRequest toAddressRequest(AddressBaseResponse addressBaseResponse);
}
