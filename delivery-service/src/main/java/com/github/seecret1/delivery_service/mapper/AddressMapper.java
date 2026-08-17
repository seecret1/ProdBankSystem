package com.github.seecret1.delivery_service.mapper;

import com.github.seecret1.delivery_service.dto.address.AddressBaseResponse;
import com.github.seecret1.delivery_service.dto.address.AddressRequest;
import com.github.seecret1.delivery_service.dto.address.AddressResponse;
import com.github.seecret1.delivery_service.entity.Address;
import com.github.seecret1.delivery_service.entity.Country;
import com.github.seecret1.delivery_service.exception.DeliveryException;
import com.github.seecret1.delivery_service.repository.CountryRepository;
import com.github.seecret1.delivery_service.utils.DateTimeUtil;
import lombok.Setter;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
@Setter(onMethod_ = @Autowired)
public abstract class AddressMapper {

    protected CountryRepository countryRepository;
    protected DateTimeUtil dateTimeUtil;

    @Named("toAddress")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "updatedAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "country", source = "countryCode", qualifiedByName = "toCountry")
    public abstract Address toAddress(AddressRequest dto);

    @Named("fromAddress")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "deletedAt", source = "deletedAt")
    @Mapping(target = "deletedBy", source = "deletedBy")
    public abstract AddressResponse fromAddress(Address address);

    @Named("fromBaseAddress")
    @Mapping(target = "deleted", source = "deleted")
    public abstract AddressBaseResponse fromBaseAddress(Address address);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "updatedAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "zipCode", source = "zipCode")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "country", source = "countryCode", qualifiedByName = "toCountry")
    public abstract Address updateAddress(
            @MappingTarget Address address,
            AddressRequest dto
    );

    @Named("toCountry")
    public Country toCountry(String countryCode) {
        return countryRepository.findByCode(countryCode)
                .orElseThrow(() -> new DeliveryException("Unknow country code: [%s]", countryCode));
    }
}
