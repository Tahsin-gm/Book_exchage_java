package com.bookexchange.controller;

import com.bookexchange.entity.Review;
import com.bookexchange.entity.User;
import com.bookexchange.security.CustomUserDetails;
import com.bookexchange.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * Get all reviews for a book (public)
     */
    @GetMapping("/{bookId}/reviews")
    public ResponseEntity<List<Review>> getBookReviews(@PathVariable Long bookId) {
        List<Review> reviews = reviewService.getReviewsByBookId(bookId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Add a review to a book (ROLE_USER required)
     */
    @PostMapping("/{bookId}/reviews")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> addReview(@PathVariable Long bookId,
                                       @RequestParam Integer rating,
                                       @RequestParam String comment,
                                       Authentication authentication) {
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        Review savedReview = reviewService.addReview(user, bookId, rating, comment);

        return ResponseEntity.ok(Map.of(
                "message", "Review added successfully",
                "reviewId", savedReview.getId()
        ));
    }
}
