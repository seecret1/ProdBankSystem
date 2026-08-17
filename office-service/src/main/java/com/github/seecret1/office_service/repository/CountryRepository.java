package com.github.seecret1.office_service.repository;

import com.github.seecret1.office_service.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Integer> {

    Optional<Country> findByCode(String code);
}
