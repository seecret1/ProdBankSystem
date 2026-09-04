package com.github.seecret1.cardservice.repository.specification;

import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.model.CardFilterModel;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CardSpecification {

    static Specification<Card> withFilter(CardFilterModel filterModel) {
        return Specification.where(isEqual("dateActivation", filterModel.getDateActivation())
                .and(isEqual("dateExpiry", filterModel.getDateExpiry()))
                .and(isEqual("status", filterModel.getStatus()))
                .and(isEqual("balance", filterModel.getBalance())));
    }

    private static <T> Specification<Card> isEqual(String fieldName, LocalDate date) {
        return (root, query, cb) -> {

            if (date == null) return null;
            return cb.equal(
                    cb.function("date", LocalDate.class, root.get(fieldName)),
                    date
            );
        };
    }

    private static <T> Specification<Card> isEqual(String fieldName, T obj) {
        return (root, query, cb) -> {

            if (obj == null) return null;
            return cb.equal(root.get(fieldName), obj);
        };
    }

    private static <T> Specification<Card> isEqual(String fieldName, BigDecimal value) {
        return (root, query, cb) -> {

            if (value == null) return null;
            return cb.equal(root.get(fieldName), value);
        };
    }
}
