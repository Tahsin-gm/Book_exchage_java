package com.bookexchange.controller;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.User;
import com.bookexchange.entity.Wishlist;
import com.bookexchange.repository.BookRepository;
import com.bookexchange.repository.WishlistRepository;
import com.bookexchange.service.JwtService;
import com.bookexchange.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class WishlistController {
    
    @Autowired
    private WishlistRepository wishlistRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @GetMapping("/wishlist")
    public ResponseEntity<?> getWishlist(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<Wishlist> wishlistItems = wishlistRepository.findByUserIdOrderByCreatedAtDesc(userOpt.get().getId());
            return ResponseEntity.ok(wishlistItems);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/wishlist/{bookId}")
    public ResponseEntity<?> addToWishlist(
            @PathVariable Long bookId,
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
            
            // Check if already in wishlist
            Optional<Wishlist> existing = wishlistRepository.findByUserIdAndBookId(userOpt.get().getId(), bookId);
            if (existing.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Book already in wishlist");
                return ResponseEntity.badRequest().body(error);
            }
            
            Wishlist wishlistItem = new Wishlist();
            wishlistItem.setUser(userOpt.get());
            wishlistItem.setBook(bookOpt.get());
            
            wishlistRepository.save(wishlistItem);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Book added to wishlist");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/wishlist/{bookId}")
    @Transactional
    public ResponseEntity<?> removeFromWishlist(
            @PathVariable Long bookId,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            wishlistRepository.deleteByUserIdAndBookId(userOpt.get().getId(), bookId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Book removed from wishlist");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}