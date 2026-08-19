package com.github.seecret1.office_service.repository;

import com.github.seecret1.office_service.entity.Office;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OfficeRepository extends JpaRepository<Office, String> {

    @Override
    Page<Office> findAll(Pageable pageable);

    @Query("""
        SELECT o FROM Office o
        WHERE LOWER(o.address.city) LIKE LOWER(CONCAT('%', :city, '%'))
            AND o.active = true AND o.deleted = false
    """)
    List<Office> findOfficeByCity(String city);

    @Query("""
    SELECT o FROM Office o WHERE o.address.city = 'Moscow' AND o.main = true
    """)
    @EntityGraph(attributePaths = {"address", "address.country"})
    Office findMainOffice();

    @Query("""
        SELECT o FROM Office o
        WHERE LOWER(o.address.city) LIKE LOWER(CONCAT('%', :city, '%'))
            AND o.active = true AND o.deleted = false
    """)
    Page<Office> findOfficeByCity(String city, Pageable pageable);
}
