package com.github.seecret1.office_service.dto.response;

public record OfficeResponse(

        String name,

        String contactPhone,

        String scheduleJson,

        boolean active,

        AddressResponse address

) {
}
