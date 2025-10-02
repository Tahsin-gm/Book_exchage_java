package com.bookexchange.service;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.User;
import com.bookexchange.entity.Wishlist;
import com.bookexchange.exception.BookNotFoundException;
import com.bookexchange.exception.DuplicateWishlistItemException;
import com.bookexchange.repository.BookRepository;
import com.bookexchange.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishListService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private BookRepository bookRepository;

    /**
     * Get all wishlist items for a user
     */
    public List<Wishlist> getWishListByUser(User user) {
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    /**
     * Add a book to the user's wishlist
     */
    @Transactional
    public void addBookToWishlist(User user, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new BookNotFoundException("Book not found with id: " + bookId));

        boolean exists = wishlistRepository.
                findByUserIdAndBookId(user.getId(), bookId).isPresent();
        if (exists) {
            throw new DuplicateWishlistItemException("Book is already in wishlist");
        }

        Wishlist wishlistItem = new Wishlist();
        wishlistItem.setUser(user);
        wishlistItem.setBook(book);

        wishlistRepository.save(wishlistItem);
    }

    /**
     * Remove a book from the user's wishlist
     */
    @Transactional
    public void removeBookFromWishlist(User user, Long bookId) {
        Wishlist wishlistItem = wishlistRepository.
                findByUserIdAndBookId(user.getId(), bookId)
                .orElseThrow(() -> new BookNotFoundException
                            ("Book not found in wishlist"));

        wishlistRepository.delete(wishlistItem);
    }
}
