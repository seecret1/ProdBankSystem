package com.github.seecret1.order_service.dto.user;

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
