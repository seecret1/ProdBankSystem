package com.github.seecret1.userservice.repository;

import com.github.seecret1.userservice.entity.Individual;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.repository.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    @Override
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @Override
    Page<User> findAll(Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE u.deleted = false
        AND u.birthDate IS NOT NULL
        AND u.individual IS NOT NULL
        AND (
            FUNCTION('DATE_ADD', u.birthDate, 20, 'YEAR') = CURRENT_DATE
            OR 
            FUNCTION('DATE_ADD', u.birthDate, 45, 'YEAR') = CURRENT_DATE
        )
        """)
    Page<User> findUsersUpdatePassport(Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE u.deleted = false
        AND u.birthDate IS NOT NULL
        AND u.individual IS NOT NULL
        AND (
            FUNCTION('DATE_ADD', u.birthDate, 20, 'YEAR') <= CURRENT_DATE
            AND FUNCTION('DATE_ADD', u.birthDate, 20, 'YEAR') >= FUNCTION('DATE_ADD', CURRENT_DATE, -90, 'DAY')
            OR 
            FUNCTION('DATE_ADD', u.birthDate, 45, 'YEAR') <= CURRENT_DATE
            AND FUNCTION('DATE_ADD', u.birthDate, 45, 'YEAR') >= FUNCTION('DATE_ADD', CURRENT_DATE, -90, 'DAY')
        )
        """)
    Page<User> findUsersWhoMissedPassportRenewalDeadline(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deleted = false")
    Page<User> findAllActiveUsers(Pageable pageable);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsernameOrEmail(String username, String email);
}
