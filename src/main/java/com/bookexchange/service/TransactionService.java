package com.bookexchange.service;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.BookStatus;
import com.bookexchange.entity.Transaction;
import com.bookexchange.entity.User;
import com.bookexchange.exception.BookNotFoundException;
import com.bookexchange.repository.BookRepository;
import com.bookexchange.repository.TransactionRepository;
import com.bookexchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    public Transaction createTransaction(String buyerEmail, Long bookId) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Buyer not found with email: " + buyerEmail));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));
        
        if (book.getSeller().getId().equals(buyer.getId())) {
            throw new RuntimeException("Cannot buy your own book");
        }
        
        Transaction transaction = new Transaction();
        transaction.setBook(book);
        transaction.setBuyer(buyer);
        transaction.setSeller(book.getSeller());
        transaction.setAmount(book.getPrice());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        
        // Mark book as sold
        book.setStatus(BookStatus.SOLD);
        bookRepository.save(book);
        
        return transactionRepository.save(transaction);
    }
    
    public List<Transaction> getUserPurchases(String buyerEmail) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with buyerEmail: " + buyerEmail));

        return transactionRepository.findByBuyerOrderByCreatedAtDesc(buyer);
    }
    
    public List<Transaction> getUserSales(String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with sellerEmail: " + sellerEmail));

        return transactionRepository.findBySellerOrderByCreatedAtDesc(seller);
    }
}