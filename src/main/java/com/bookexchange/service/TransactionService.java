package com.bookexchange.service;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.Transaction;
import com.bookexchange.entity.User;
import com.bookexchange.repository.BookRepository;
import com.bookexchange.repository.TransactionRepository;
import com.bookexchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        Optional<User> buyerOpt = userRepository.findByEmail(buyerEmail);
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        
        if (buyerOpt.isEmpty()) {
            throw new RuntimeException("Buyer not found");
        }
        
        if (bookOpt.isEmpty()) {
            throw new RuntimeException("Book not found");
        }
        
        User buyer = buyerOpt.get();
        Book book = bookOpt.get();
        
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
        book.setStatus(Book.BookStatus.SOLD);
        bookRepository.save(book);
        
        return transactionRepository.save(transaction);
    }
    
    public List<Transaction> getUserPurchases(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        return transactionRepository.findByBuyerOrderByCreatedAtDesc(userOpt.get());
    }
    
    public List<Transaction> getUserSales(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        return transactionRepository.findBySellerOrderByCreatedAtDesc(userOpt.get());
    }
}