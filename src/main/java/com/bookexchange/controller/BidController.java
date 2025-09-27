package com.bookexchange.controller;

import com.bookexchange.entity.Bid;
import com.bookexchange.entity.Book;
import com.bookexchange.entity.User;
import com.bookexchange.repository.BidRepository;
import com.bookexchange.repository.BookRepository;
import com.bookexchange.repository.UserRepository;
import com.bookexchange.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bids")
@CrossOrigin(origins = "*")
public class BidController {
    
    @Autowired
    private BidRepository bidRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping
    public ResponseEntity<?> placeBid(@RequestBody Map<String, Object> request, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Long bookId = Long.valueOf(request.get("bookId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            
            Optional<User> bidderOpt = userRepository.findByEmail(email);
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            
            if (bidderOpt.isEmpty() || bookOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User or book not found"));
            }
            
            User bidder = bidderOpt.get();
            Book book = bookOpt.get();
            
            if (book.getSeller().getId().equals(bidder.getId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot bid on your own book"));
            }
            
            Bid bid = new Bid();
            bid.setBook(book);
            bid.setBidder(bidder);
            bid.setAmount(amount);
            
            return ResponseEntity.ok(bidRepository.save(bid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/book/{bookId}")
    public ResponseEntity<?> getBookBids(@PathVariable Long bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Book not found"));
        }
        
        List<Bid> bids = bidRepository.findByBookOrderByAmountDesc(bookOpt.get());
        return ResponseEntity.ok(bids);
    }
    
    @PutMapping("/{bidId}/accept")
    public ResponseEntity<?> acceptBid(@PathVariable Long bidId, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<Bid> bidOpt = bidRepository.findById(bidId);
            if (bidOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bid not found"));
            }
            
            Bid bid = bidOpt.get();
            if (!bid.getBook().getSeller().getEmail().equals(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Not authorized"));
            }
            
            bid.setStatus(Bid.BidStatus.ACCEPTED);
            bid.getBook().setStatus(Book.BookStatus.SOLD);
            
            bidRepository.save(bid);
            bookRepository.save(bid.getBook());
            
            return ResponseEntity.ok(bid);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}