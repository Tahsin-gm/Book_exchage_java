package com.bookexchange.repository;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.BookStatus;
import com.bookexchange.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByStatusOrderByCreatedAtDesc(BookStatus status);
    List<Book> findBySellerOrderByCreatedAtDesc(User seller);
    Optional<Book> findByTitleIgnoreCase(String title);
    List<Book> findByTitleContainingIgnoreCase(String title);
}