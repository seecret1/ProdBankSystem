package com.github.seecret1.delivery_service.service;

import com.github.seecret1.delivery_service.dto.address.AddressRequest;
import com.github.seecret1.delivery_service.entity.Address;
import com.github.seecret1.delivery_service.mapper.AddressMapper;
import com.github.seecret1.delivery_service.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    private final AddressMapper addressMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Address findOrCreate(AddressRequest addressRequest) {
        return addressRepository
                .findByCityAndAddressAndCountryCode(
                        addressRequest.city(),
                        addressRequest.address(),
                        addressRequest.countryCode()
                )
                .orElseGet(() -> {
                    Address newAddress = addressMapper.toAddress(addressRequest);
                    return addressRepository.save(newAddress);
                });
    }
}
