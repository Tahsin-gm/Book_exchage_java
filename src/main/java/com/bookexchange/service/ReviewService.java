package com.bookexchange.service;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.Review;
import com.bookexchange.entity.User;
import com.bookexchange.exception.BookNotFoundException;
import com.bookexchange.repository.BookRepository;
import com.bookexchange.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    /**
     * Get all reviews for a specific book
     */
    public List<Review> getReviewsByBookId(Long bookId) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }

    /**
     * Add a review to a book
     */
    public Review addReview(User user, Long bookId, Integer rating, String comment) {
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    /**
     * Get all reviews by a specific user
     */
    public List<Review> getReviewsByUser(User user) {
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
}
