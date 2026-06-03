package com.github.seecret1.userservice.repository;

import com.github.seecret1.userservice.entity.Individual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndividualRepository extends JpaRepository<Individual, String> {

    boolean existsByUserId(String userId);

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

    @Modifying
    @Query("""
        UPDATE Individual i SET i.deleted = true WHERE i.id = :id
        """)
    void softDelete(@Param("id") String id);
}
