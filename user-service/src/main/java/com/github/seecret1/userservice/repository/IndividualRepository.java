package com.github.seecret1.userservice.repository;

import com.github.seecret1.userservice.entity.Individual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IndividualRepository extends JpaRepository<Individual, String>, JpaSpecificationExecutor<Individual> {

    boolean existsByUserId(String userId);

    @Override
    Page<Individual> findAll(Pageable pageable);

    @Query("""
        SELECT EXISTS (
             SELECT 1 FROM Individual i
             WHERE i.passportNumber = :passportNumber
         )
        """)
    boolean existsIndividualByPassportNumber(String passportNumber);

    @Query("""
        SELECT EXISTS (
             SELECT 1 FROM Individual i
             WHERE i.phoneNumber = :phoneNumber
         )
        """)
    boolean existsIndividualByPhoneNumber(String phoneNumber);

    @Query("""
         SELECT i FROM Individual i
         WHERE i.phoneNumber = :phoneNumber
        """)
    Optional<Individual> findByPhoneNumber(String phoneNumber);

    @Modifying
    @Query("""
        UPDATE Individual i SET i.deleted = true WHERE i.id = :id
        """)
    void softDelete(@Param("id") String id);
}
