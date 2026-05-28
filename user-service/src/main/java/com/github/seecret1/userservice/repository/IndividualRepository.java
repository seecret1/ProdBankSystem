package com.github.seecret1.userservice.repository;

import com.github.seecret1.userservice.entity.Individual;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface IndividualRepository extends JpaRepository<Individual, String> {

    @Query("""
        FROM Individual i WHERE (:emails) IS NULL OR i.user.email IN :emails
        """)
    Page<Individual> findAllByEmails(@Param("emails") Set<String> emails);

    @Modifying
    @Query("""
        UPDATE Individual i SET i.deleted = true WHERE i.id = :id
        """)
    void softDelete(@Param("id") String id);
}
