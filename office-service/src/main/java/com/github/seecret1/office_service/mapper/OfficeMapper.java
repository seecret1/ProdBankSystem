package com.github.seecret1.office_service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.office_service.dto.request.OfficeCreateRequest;
import com.github.seecret1.office_service.dto.request.OfficeUpdateRequest;
import com.github.seecret1.office_service.dto.request.ScheduleRequest;
import com.github.seecret1.office_service.dto.response.OfficeFullResponse;
import com.github.seecret1.office_service.dto.response.OfficeMainResponse;
import com.github.seecret1.office_service.dto.response.OfficeResponse;
import com.github.seecret1.office_service.entity.Office;
import com.github.seecret1.office_service.utils.DateTimeUtil;
import com.github.seecret1.office_service.utils.PhoneUtils;
import lombok.RequiredArgsConstructor;
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

    private ObjectMapper objectMapper;

    protected DateTimeUtil dateTimeUtil;

    @Mapping(target = "active", constant = "true")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "updatedAt", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "contactPhone", source = "contactPhone", qualifiedByName = "maskPhone")
    @Mapping(target = "address", source = "address", qualifiedByName = "toAddress")
    @Mapping(target = "scheduleJson", source = "scheduleJson", qualifiedByName = "toScheduleJson")
    public abstract Office toEntity(OfficeCreateRequest request);

    public abstract OfficeResponse toDto(Office office);

    public abstract List<OfficeResponse> toDto(List<Office> offices);

    public abstract OfficeFullResponse toFullDto(Office office);

    public abstract List<OfficeFullResponse> toFullDto(List<Office> offices);

    @Mapping(target = "address", qualifiedByName = "fromBaseAddress")
    public abstract OfficeMainResponse toMainResponse(Office office);

    @Mapping(target = "address", qualifiedByName = "fromBaseAddress")
    public abstract List<OfficeMainResponse> toMainResponseList(List<Office> offices);

    public Office toEntity(Office office, OfficeUpdateRequest request) {
        office.setName(request.name());
        office.setContactPhone(maskPhone(request.contactPhone()));
        office.setScheduleJson(toScheduleJson(request.scheduleJson()));
        return office;
    }

    @Named("maskPhone")
    protected String maskPhone(String value) {
        return PhoneUtils.formatInternationalPhone(value);
    }

    @Named("toScheduleJson")
    protected String toScheduleJson(List<ScheduleRequest> request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize schedule", e);
        }
    }
}
