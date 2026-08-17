package com.github.seecret1.order_service.dto.office;

import com.github.seecret1.order_service.dto.address.AddressBaseResponse;

public record OfficeMainResponse(

    String id,

    String name,

    String contactPhone,

    String scheduleJson,

    boolean active,

    AddressBaseResponse address

) { }
