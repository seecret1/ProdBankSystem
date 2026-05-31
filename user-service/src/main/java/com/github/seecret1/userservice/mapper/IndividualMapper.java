package com.github.seecret1.userservice.mapper;

import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.IndividualDto;
import com.github.seecret1.userservice.dto.response.IndividualResponse;
import com.github.seecret1.userservice.entity.Individual;
import com.github.seecret1.userservice.utils.DateTimeUtil;
import lombok.Setter;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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
        uses = {
                UserMapper.class,
                AddressMapper.class,
        }
)
@Setter(onMethod_ = @Autowired)
public abstract class IndividualMapper {

    protected DateTimeUtil dateTimeUtil;

    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "createdAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "updatedAt", expression = "java(dateTimeUtil.now())")
    public abstract Individual toEntity(IndividualRequest dto);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "middleName", source = "user.middleName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "address", source = "user.address", qualifiedByName = "fromAddress")
    public abstract IndividualDto toDto(Individual individual);

    public abstract IndividualResponse toResponseDto(Individual individual);

    public List<IndividualDto> toDto(List<Individual> individuals) {
        return isEmpty(individuals)
                ? Collections.emptyList()
                : individuals.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<IndividualResponse> toResponseDto(List<Individual> individuals) {
        return isEmpty(individuals)
                ? Collections.emptyList()
                : individuals.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }


    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "updatedAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "passportNumber", source = "passportNumber")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "user", expression = "java(userMapper.update(individual, dto))")
    public abstract void update(
            @MappingTarget
            Individual individual,
            IndividualRequest dto
    );
}

