package com.github.seecret1.office_service.repository;

import com.github.seecret1.office_service.entity.Office;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OfficeRepository extends JpaRepository<Office, String> {

    @Override
    Page<Office> findAll(Pageable pageable);

    @Query("""
        SELECT o FROM Office o
        WHERE LOWER(o.address.city) LIKE LOWER(CONCAT('%', :city, '%'))
    """)
    Page<Office> findOfficeByCity(String city, Pageable pageable);

    @Query("""
        SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
        FROM Office o
        WHERE o.contactPhone = :contactPhone
    """)
    boolean checkExistsContactPhone(String contactPhone);
}
