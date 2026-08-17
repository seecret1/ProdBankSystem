package com.github.seecret1.office_service.dto.response;

public record OfficeMainResponse(

    String id,

    String name,

    String contactPhone,

    String scheduleJson,

    boolean active,

    AddressBaseResponse address

) { }
