package com.github.seecret1.office_service.mapper;

import com.github.seecret1.office_service.dto.request.OfficeCreateRequest;
import com.github.seecret1.office_service.dto.response.OfficeResponse;
import com.github.seecret1.office_service.entity.Office;
import com.github.seecret1.office_service.utils.DateTimeUtil;
import com.github.seecret1.office_service.utils.PhoneUtils;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = CONSTRUCTOR,
        uses = AddressMapper.class
)
@Setter(onMethod_ = @Autowired)
public abstract class OfficeMapper {

    protected DateTimeUtil dateTimeUtil;

    @Mapping(target = "active", constant = "true")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "updatedAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "contactPhone", source = "contactPhone", qualifiedByName = "maskPhone")
    @Mapping(target = "address", source = "address", qualifiedByName = "toAddress")
    public abstract Office toEntity(OfficeCreateRequest request);

    public abstract OfficeResponse toDto(Office office);

    public abstract List<OfficeResponse> toDto(List<Office> offices);

    @Named("maskPhone")
    protected String maskPhone(String value) {
        return PhoneUtils.maskPhoneWithPrefix(value);
    }
}
