package com.github.seecret1.userservice.repository;

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

    @Query("SELECT u FROM User u WHERE u.deleted = false")
    Page<User> findAllActiveUsers(Pageable pageable);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsernameOrEmail(String username, String email);

    default Optional<User> findByCriterial(String searchCriterial) {
        return findOne(UserSpecification.searchByCriterial(searchCriterial));
    }
}
