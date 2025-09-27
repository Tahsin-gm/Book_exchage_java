package com.bookexchange.repository;

import com.bookexchange.entity.Transaction;
import com.bookexchange.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<Transaction> findBySellerOrderByCreatedAtDesc(User seller);
}