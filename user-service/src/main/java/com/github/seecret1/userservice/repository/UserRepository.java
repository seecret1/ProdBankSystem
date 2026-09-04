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

    @Query(value = """
        SELECT u.* FROM person_bank.users u
        WHERE u.deleted = FALSE
        AND u.birth_date IS NOT NULL
        AND EXISTS (SELECT 1 FROM person_bank.individuals i WHERE i.user_id = u.id)
        AND (
            u.birth_date + make_interval(years => :firstAge) = CURRENT_DATE
            OR
            u.birth_date + make_interval(years => :secondAge) = CURRENT_DATE
        )
        """, countQuery = """
        SELECT count(u.id) FROM person_bank.users u
        WHERE u.deleted = FALSE
        AND u.birth_date IS NOT NULL
        AND EXISTS (SELECT 1 FROM person_bank.individuals i WHERE i.user_id = u.id)
        AND (
            u.birth_date + make_interval(years => :firstAge) = CURRENT_DATE
            OR
            u.birth_date + make_interval(years => :secondAge) = CURRENT_DATE
        )
        """, nativeQuery = true)
    Page<User> findUsersUpdatePassport(Pageable pageable, int firstAge, int secondAge);

    @Query(value = """
        SELECT u.* FROM person_bank.users u
        WHERE u.deleted = FALSE
        AND u.birth_date IS NOT NULL
        AND EXISTS (SELECT 1 FROM person_bank.individuals i WHERE i.user_id = u.id)
        AND (
            u.birth_date + make_interval(years => :firstAge) <= CURRENT_DATE
            AND u.birth_date + make_interval(years => :firstAge) >= CURRENT_DATE - make_interval(days => :daysThreshold)
            OR
            u.birth_date + make_interval(years => :secondAge) <= CURRENT_DATE
            AND u.birth_date + make_interval(years => :secondAge) >= CURRENT_DATE - make_interval(days => :daysThreshold)
        )
        """, countQuery = """
        SELECT count(u.id) FROM person_bank.users u
        WHERE u.deleted = FALSE
        AND u.birth_date IS NOT NULL
        AND EXISTS (SELECT 1 FROM person_bank.individuals i WHERE i.user_id = u.id)
        AND (
            u.birth_date + make_interval(years => :firstAge) <= CURRENT_DATE
            AND u.birth_date + make_interval(years => :firstAge) >= CURRENT_DATE - make_interval(days => :daysThreshold)
            OR
            u.birth_date + make_interval(years => :secondAge) <= CURRENT_DATE
            AND u.birth_date + make_interval(years => :secondAge) >= CURRENT_DATE - make_interval(days => :daysThreshold)
        )
        """, nativeQuery = true)
    Page<User> findUsersWithExpiredPassport(Pageable pageable, int firstAge, int secondAge, int daysThreshold);

    @Query(value = """
        SELECT u.* FROM person_bank.users u
        WHERE u.deleted = FALSE
        AND u.birth_date IS NOT NULL
        AND EXISTS (SELECT 1 FROM person_bank.individuals i WHERE i.user_id = u.id)
        AND (
            u.birth_date + make_interval(years => :firstAge) <= CURRENT_DATE
            AND u.birth_date + make_interval(years => :firstAge) >= CURRENT_DATE - make_interval(days => :daysNotified)
            OR
            u.birth_date + make_interval(years => :secondAge) <= CURRENT_DATE
            AND u.birth_date + make_interval(years => :secondAge) >= CURRENT_DATE - make_interval(days => :daysNotified)
        )
        """, countQuery = """
        SELECT count(u.id) FROM person_bank.users u
        WHERE u.deleted = FALSE
        AND u.birth_date IS NOT NULL
        AND EXISTS (SELECT 1 FROM person_bank.individuals i WHERE i.user_id = u.id)
        AND (
            u.birth_date + make_interval(years => :firstAge) <= CURRENT_DATE
            AND u.birth_date + make_interval(years => :firstAge) >= CURRENT_DATE - make_interval(days => :daysNotified)
            OR
            u.birth_date + make_interval(years => :secondAge) <= CURRENT_DATE
            AND u.birth_date + make_interval(years => :secondAge) >= CURRENT_DATE - make_interval(days => :daysNotified)
        )
        """, nativeQuery = true)
    Page<User> findUsersWhoMissedPassportRenewalDeadline(Pageable pageable, int firstAge, int secondAge, int daysNotified);

    @Query("SELECT u FROM User u WHERE u.deleted = false")
    Page<User> findAllActiveUsers(Pageable pageable);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsernameOrEmail(String username, String email);
}
