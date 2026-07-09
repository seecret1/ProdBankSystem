package com.github.seecret1.userservice.repository.specification;

import com.github.seecret1.userservice.entity.enums.RoleType;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.model.UserFilterModel;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

public interface UserSpecification {

    static Specification<User> withFilter(UserFilterModel filterModel) {
        Specification<User> spec = Specification.where(isEquals("status", filterModel.getStatus()))
                        .and(isEquals("firstName", filterModel.getFirstName())
                        .and(isEquals("lastName", filterModel.getLastName()))
                        .and(isEquals("middleName", filterModel.getMiddleName()))
                        .and(isEquals("birthDate", filterModel.getBirthDate()))
                        .and(isEquals("role", filterModel.getRole()))
                );

        if (filterModel.isDeleted()) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("deleted")));
        } else {
            spec = spec.and((root, query, cb) -> cb.isFalse(root.get("deleted")));
        }

        return spec;
    }

    private static <T> Specification<User> isEquals(String fieldName, T object) {
        return (root, query, cb) -> {
            if (object == null) {
                return null;
            }
            return cb.equal(root.get(fieldName), object);
        };
    }

    private static <T> Specification<User> isEquals(String fieldName, LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) {
                return null;
            }

            return cb.equal(
                    cb.function(
                        "date", LocalDate.class, root.get(fieldName)),
                        date
            );
        };
    }

    private static <T> Specification<User> isEquals(String fieldName, RoleType roleType) {
        return (root, query, cb) -> {
            return cb.equal(root.get(fieldName), roleType);
        };
    }
}
