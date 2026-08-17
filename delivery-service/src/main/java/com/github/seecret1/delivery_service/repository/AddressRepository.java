package com.github.seecret1.delivery_service.repository;

import com.github.seecret1.delivery_service.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, String> {

    Optional<Address> findByCityAndAddressAndCountryCode(String city, String address, String countryCode);
}
