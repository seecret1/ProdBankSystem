package com.github.seecret1.userservice.mapper;

import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.IndividualResponse;
import com.github.seecret1.userservice.entity.Individual;
import com.github.seecret1.userservice.utils.DateTimeUtil;
import com.github.seecret1.userservice.utils.EncryptionUtils;
import com.github.seecret1.userservice.utils.PassportMaskUtils;
import com.github.seecret1.userservice.utils.PhoneUtils;
import lombok.Setter;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.springframework.util.CollectionUtils.isEmpty;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = CONSTRUCTOR,
        uses = AddressMapper.class
)
@Setter(onMethod_ = @Autowired)
public abstract class IndividualMapper {

    protected DateTimeUtil dateTimeUtil;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "updatedAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", source = "address", qualifiedByName = "toAddress")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "encrypt")
    @Mapping(target = "passportNumber", source = "passportNumber", qualifiedByName = "encrypt")
    public abstract Individual toEntity(IndividualRequest dto);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "middleName", source = "user.middleName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "address", source = "address", qualifiedByName = "fromAddress")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "decryptAndMaskPhone")
    @Mapping(target = "passportNumber", source = "passportNumber", qualifiedByName = "decryptAndMaskPassport")
    public abstract IndividualResponse toResponseDto(Individual individual);

    public List<IndividualResponse> toResponseDto(List<Individual> individuals) {
        return isEmpty(individuals)
                ? Collections.emptyList()
                : individuals.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "updatedAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", source = "address", qualifiedByName = "toAddress")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "encrypt")
    @Mapping(target = "passportNumber", source = "passportNumber", qualifiedByName = "encrypt")
    public abstract void update(
            @MappingTarget
            Individual individual,
            IndividualRequest dto
    );

    @Named("encrypt")
    protected String encrypt(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return EncryptionUtils.encrypt(value);
    }

    @Named("decrypt")
    protected String decrypt(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            return EncryptionUtils.decrypt(value);
        } catch (Exception e) {
            return value;
        }
    }

    @Named("decryptAndMaskPhone")
    protected String decryptAndMaskPhone(String value) {
        return PhoneUtils.maskPhoneWithPrefix(decrypt(value));
    }

    @Named("decryptAndMaskPassport")
    protected String decryptAndMaskPassport(String value) {
        return PassportMaskUtils.maskPassportFull(decrypt(value));
    }
}

