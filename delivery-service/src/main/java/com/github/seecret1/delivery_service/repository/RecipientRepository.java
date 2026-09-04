package com.github.seecret1.delivery_service.repository;

import com.github.seecret1.delivery_service.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    Optional<Recipient> findByUserId(String userId);

    @Modifying
    @Query("""
        UPDATE Recipient r
        SET r.fullName = :#{#entity.fullName},
            r.contactPhone = :#{#entity.contactPhone},
            r.officeId = :#{#entity.officeId},
            r.personType = :#{#entity.personType},
            r.updatedAt = CURRENT_INSTANT
        WHERE r.userId = :#{#entity.userId}
    """)
    void update(@Param("entity") Recipient recipient);
}
