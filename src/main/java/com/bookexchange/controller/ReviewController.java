package com.bookexchange.controller;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.Review;
import com.bookexchange.entity.User;
import com.bookexchange.repository.BookRepository;
import com.bookexchange.repository.ReviewRepository;
import com.bookexchange.service.JwtService;
import com.bookexchange.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ReviewController {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<List<Review>> getBookReviews(@PathVariable Long bookId) {
        List<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
        return ResponseEntity.ok(reviews);
    }
    
    @PostMapping("/books/{bookId}/reviews")
    public ResponseEntity<?> addReview(
            @PathVariable Long bookId,
            @RequestParam Integer rating,
            @RequestParam String comment,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<User> userOpt = userService.findByEmail(email);
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            
            if (userOpt.isEmpty() || bookOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User or book not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            Review review = new Review();
            review.setUser(userOpt.get());
            review.setBook(bookOpt.get());
            review.setRating(rating);
            review.setComment(comment);
            
            Review savedReview = reviewRepository.save(review);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Review added successfully");
            response.put("reviewId", savedReview.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}