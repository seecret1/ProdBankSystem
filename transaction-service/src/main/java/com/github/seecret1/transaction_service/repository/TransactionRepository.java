package com.github.seecret1.transaction_service.repository;

import com.github.seecret1.transaction_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
}
