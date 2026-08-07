package com.github.seecret1.delivery_service.dto.address;

import java.time.Instant;

public record AddressResponse(

        Boolean deleted,

        Instant createdAt,

        Instant updatedAt,

        Instant deletedAt,

        String deletedBy,

        String address,

        String zipCode,

        String city

) {
}
