package com.github.seecret1.userservice.mapper;

import com.github.seecret1.userservice.dto.AddressWriteDto;
import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.AddressDto;
import com.github.seecret1.userservice.entity.Address;
import com.github.seecret1.userservice.entity.Country;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.exception.PersonException;
import com.github.seecret1.userservice.repository.CountryRepository;
import com.github.seecret1.userservice.utils.DateTimeUtil;
import lombok.Setter;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
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
    public abstract Address toAddress(AddressWriteDto dto);

    @Named("fromAddress")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "deletedAt", source = "deletedAt")
    @Mapping(target = "deletedBy", source = "deletedBy")
    public abstract AddressDto fromAddress(Address address);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "updatedAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "zipCode", source = "zipCode")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "country", source = "countryCode", qualifiedByName = "toCountry")
    public abstract Address updateAddress(
            @MappingTarget Address address,
            AddressWriteDto dto
    );

    @Named("toCountry")
    public Country toCountry(String countryCode) {
        return countryRepository.findByCode(countryCode)
                .orElseThrow(() -> new PersonException("Unknow country code: [%s]", countryCode));
    }

    public Address update(User user, IndividualRequest dto) {
        if (dto.address() == null) {
            return user.getAddress();
        }
        return updateAddress(user.getAddress(), dto.address());
    }
}
