package com.github.seecret1.delivery_service.service;

import com.github.seecret1.delivery_service.dto.address.AddressRequest;
import com.github.seecret1.delivery_service.entity.Address;

public interface AddressService {

    Address findOrCreate(AddressRequest addressRequest);
}
