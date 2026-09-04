package com.github.seecret1.delivery_service.repository;

import com.github.seecret1.delivery_service.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Integer> {

    Optional<Country> findByCode(String code);
}
