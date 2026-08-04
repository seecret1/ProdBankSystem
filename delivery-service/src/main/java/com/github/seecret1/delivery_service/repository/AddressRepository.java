package com.github.seecret1.delivery_service.repository;

import com.github.seecret1.delivery_service.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, String> {
}
