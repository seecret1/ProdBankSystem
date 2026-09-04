package com.github.seecret1.delivery_service.service.processed;

import com.github.seecret1.delivery_service.dto.address.AddressPair;
import com.github.seecret1.delivery_service.dto.address.AddressRequest;
import com.github.seecret1.delivery_service.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressProcessed {

    private final AddressService addressService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AddressPair processOriginalAndDestinationAddresses(
            AddressRequest originAddress, AddressRequest destinationAddress
    ) {
        return new AddressPair(
                addressService.findOrCreate(originAddress),
                addressService.findOrCreate(destinationAddress)
        );
    }
}
