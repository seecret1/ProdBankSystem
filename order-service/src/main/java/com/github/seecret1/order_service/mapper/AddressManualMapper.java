package com.github.seecret1.order_service.mapper;

import com.github.seecret1.order_service.dto.address.AddressBaseResponse;
import com.github.seecret1.order_service.dto.address.AddressRequest;
import com.github.seecret1.order_service.entity.Address;
import com.github.seecret1.order_service.entity.Country;
import com.github.seecret1.order_service.repository.CountryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddressManualMapper {

    private final CountryRepository countryRepository;

    public Address toAddress(AddressRequest request) {
        var address = new Address();
        address.setAddress(request.address());
        address.setZipCode(request.zipCode());
        address.setCity(request.city());
        address.setCountry(toCountry(request.countryCode()));
        address.setDeleted(false);
        return address;
    }

    public Country toCountry(String countryCode) {
        return countryRepository.findByCode(countryCode)
                .orElseThrow(() -> new EntityNotFoundException("Unknow country code: " + countryCode));
    }

    public AddressRequest toAddressRequest(AddressBaseResponse addressBaseResponse) {
        return new AddressRequest(addressBaseResponse.address(), addressBaseResponse.zipCode(), addressBaseResponse.city(), addressBaseResponse.countryCode());
    }

    public AddressRequest toAddressRequestFromEntity(Address address) {
        return new AddressRequest(address.getAddress(), address.getZipCode(), address.getCity(), address.getCountry().getCode());
    }
}
