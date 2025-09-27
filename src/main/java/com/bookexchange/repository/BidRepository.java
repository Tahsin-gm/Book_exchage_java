package com.bookexchange.repository;

import com.bookexchange.entity.Bid;
import com.bookexchange.entity.Book;
import com.bookexchange.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByBookOrderByAmountDesc(Book book);
    List<Bid> findByBidderOrderByCreatedAtDesc(User bidder);
    List<Bid> findByBookSellerOrderByCreatedAtDesc(User seller);
}