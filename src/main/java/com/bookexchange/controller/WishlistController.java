package com.bookexchange.controller;

import com.bookexchange.entity.User;
import com.bookexchange.security.CustomUserDetails;
import com.bookexchange.service.WishListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
public class WishlistController {

    @Autowired
    private WishListService wishListService;

    /**
     * Get all wishlist items for the authenticated user
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getWishlist(Authentication authentication) {
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        return ResponseEntity.ok(wishListService.getWishListByUser(user));
    }

    /**
     * Add a book to the wishlist
     */
    @PostMapping("/{bookId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> addToWishlist(@PathVariable Long bookId, Authentication authentication) {
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        wishListService.addBookToWishlist(user, bookId);
        return ResponseEntity.ok(Map.of("message", "Book added to wishlist"));
    }

    /**
     * Remove a book from the wishlist
     */
    @DeleteMapping("/{bookId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> removeFromWishlist(@PathVariable Long bookId, Authentication authentication) {
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        wishListService.removeBookFromWishlist(user, bookId);
        return ResponseEntity.ok(Map.of("message", "Book removed from wishlist"));
    }
}
