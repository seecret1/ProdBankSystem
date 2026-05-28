package com.github.seecret1.userservice.mapper;

import com.github.seecret1.userservice.dto.request.IndividualWriteDto;
import com.github.seecret1.userservice.entity.Individual;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.utils.DateTimeUtil;
import lombok.Setter;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = CONSTRUCTOR,
        uses = {
                AddressMapper.class
        }
)
@Setter(onMethod_ = @Autowired)
public abstract class UserMapper {

        protected DateTimeUtil dateTimeUtil;

        @Named("toUser")
        @Mapping(target = "deleted", constant = "false")
        @Mapping(target = "createdAt", expression = "java(dateTimeUtil.now())")
        @Mapping(target = "updatedAt", expression = "java(dateTimeUtil.now())")
        @Mapping(target = "address", source = ".", qualifiedByName = "toAddress")
        public abstract User to(IndividualWriteDto dto);

        @BeanMapping(ignoreByDefault = true)
        @Mapping(target = "updatedAt", expression = "java(dateTimeUtil.now())")
        @Mapping(target = "firstName", source = "firstName")
        @Mapping(target = "lastName", source = "lastName")
        @Mapping(target = "address", expression = "java(addressMapper.update(user, dto))")
        public abstract User update(
                @MappingTarget
                User user,
                IndividualWriteDto dto
        );

        public User update(Individual individual, IndividualWriteDto dto) {
                return update(individual.getUser(), dto);
        }
}
