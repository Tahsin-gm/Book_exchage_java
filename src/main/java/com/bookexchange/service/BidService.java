package com.bookexchange.service;

import com.bookexchange.entity.*;
import com.bookexchange.exception.BookNotFoundException;
import com.bookexchange.exception.SelfBiddingNotAllowedException;
import com.bookexchange.exception.UnauthorizedBidAcceptanceException;
import com.bookexchange.repository.BidRepository;
import com.bookexchange.repository.BookRepository;
import com.bookexchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BidService {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    public Bid placeBid(Map<String, Object> request, String email) {
        Long bookId = Long.valueOf(request.get("bookId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        User bidder = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId));

        if (book.getSeller().getId().equals(bidder.getId())) {
            throw new SelfBiddingNotAllowedException("You cannot bid on your own book");
        }

        // Optional: prevent duplicate active bids by same user on same book
        boolean alreadyBid = bidRepository.findByBidderOrderByCreatedAtDesc(bidder).stream()
                .anyMatch(b -> b.getBook().getId().equals(bookId) && b.getStatus() == Bid.BidStatus.ACTIVE);
        if (alreadyBid) {
            throw new RuntimeException("You already have an active bid for this book");
        }

        Bid bid = new Bid();
        bid.setBook(book);
        bid.setBidder(bidder);
        bid.setAmount(amount);
        bid.setStatus(Bid.BidStatus.ACTIVE);

        return bidRepository.save(bid);
    }

    public List<Bid> getBookBids(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId));

        return bidRepository.findByBookOrderByAmountDesc(book);
    }

    public Bid acceptBid(Long bidId, String email) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found with ID: " + bidId));

        if (!bid.getBook().getSeller().getEmail().equals(email)) {
            throw new UnauthorizedBidAcceptanceException("You are not authorized to accept this bid");
        }

        // Mark selected bid as accepted
        bid.setStatus(Bid.BidStatus.ACCEPTED);
        bid.getBook().setStatus(BookStatus.SOLD);

        // Reject other active bids for the same book
        List<Bid> otherBids = bidRepository.findByBookOrderByAmountDesc(bid.getBook());
        otherBids.stream()
                .filter(b -> !b.getId().equals(bidId) && b.getStatus() == Bid.BidStatus.ACTIVE)
                .forEach(b -> b.setStatus(Bid.BidStatus.REJECTED));

        bidRepository.saveAll(otherBids);
        bookRepository.save(bid.getBook());

        return bidRepository.save(bid);
    }
}
