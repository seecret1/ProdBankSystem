package com.github.seecret1.delivery_service.dto.address;

import com.github.seecret1.delivery_service.entity.Address;

public record AddressPair(

        Address origin,

        Address destination

) { }
