package com.github.seecret1.userservice.mapper;

import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.entity.Individual;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.utils.DateTimeUtil;
import lombok.Setter;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = CONSTRUCTOR,
        uses = AddressMapper.class
)
@Setter(onMethod_ = @Autowired)
public abstract class UserMapper {

    protected DateTimeUtil dateTimeUtil;
    protected AddressMapper addressMapper;

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "updatedAt", expression = "java(dateTimeUtil.now())")
    public abstract void update(
            @MappingTarget User user,
            IndividualRequest dto
    );

    @AfterMapping
    protected void updateAddress(@MappingTarget User user, IndividualRequest dto) {
        addressMapper.update(user, dto);
    }

    public User update(Individual individual, IndividualRequest dto) {
        update(individual.getUser(), dto);
        return individual.getUser();
    }
}
