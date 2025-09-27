package com.bookexchange.repository;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByStatusOrderByCreatedAtDesc(Book.BookStatus status);
    List<Book> findBySellerOrderByCreatedAtDesc(User seller);
}