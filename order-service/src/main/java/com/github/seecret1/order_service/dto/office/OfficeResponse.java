package com.github.seecret1.order_service.dto.office;

import com.github.seecret1.order_service.dto.address.AddressResponse;

public record OfficeResponse(

        String name,

        String contactPhone,

        String scheduleJson,

        boolean active,

        AddressResponse address
) {
}
