package com.bookexchange.repository;

import com.bookexchange.entity.ExchangeRequest;
import com.bookexchange.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    List<ExchangeRequest> findByOwnerOrderByCreatedAtDesc(User owner);
    List<ExchangeRequest> findByRequesterOrderByCreatedAtDesc(User requester);
    List<ExchangeRequest> findByRequestedBookId(Long bookId);
    List<ExchangeRequest> findByOfferedBookId(Long bookId);
    void deleteByRequestedBookId(Long bookId);
    void deleteByOfferedBookId(Long bookId);
}