package com.github.seecret1.userservice.repository;

import com.github.seecret1.userservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, String> {
}
