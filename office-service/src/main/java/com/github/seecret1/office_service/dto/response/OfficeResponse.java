package com.github.seecret1.office_service.dto.response;

import com.github.seecret1.office_service.dto.request.AddressRequest;
import com.github.seecret1.office_service.dto.request.ScheduleRequest;

import java.util.List;

public record OfficeResponse(

        String name,

        String contactPhone,

        List<ScheduleRequest> scheduleJson,

        boolean active,

        AddressRequest address
) {
}
