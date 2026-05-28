package com.github.seecret1.userservice.repository;

import com.github.seecret1.userservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
