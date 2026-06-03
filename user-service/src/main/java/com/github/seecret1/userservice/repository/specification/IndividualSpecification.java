package com.github.seecret1.userservice.repository.specification;

import com.github.seecret1.userservice.entity.Individual;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public interface IndividualSpecification {

    static Specification<Individual> searchByCriterial(String searchCriterial) {
        return (root, query, cb) -> {

            if (!StringUtils.hasText(searchCriterial))  return cb.conjunction();

            return cb.or(
                    cb.equal(root.get("id"), searchCriterial),
                    cb.equal(root.get("phoneNumber"), searchCriterial)
            );
        };
    }
}
