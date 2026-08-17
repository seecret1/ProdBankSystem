package com.github.seecret1.office_service.repository;

import com.github.seecret1.office_service.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
