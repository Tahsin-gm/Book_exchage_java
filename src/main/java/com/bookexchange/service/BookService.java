package com.bookexchange.service;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.User;
import com.bookexchange.repository.BookRepository;
import com.bookexchange.repository.ExchangeRequestRepository;
import com.bookexchange.repository.ReviewRepository;
import com.bookexchange.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private ExchangeRequestRepository exchangeRequestRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private WishlistRepository wishlistRepository;
    
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";
    
    public List<Book> getAllAvailableBooks() {
        return bookRepository.findByStatusOrderByCreatedAtDesc(Book.BookStatus.AVAILABLE);
    }
    
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }
    
    public List<Book> getBooksBySeller(User seller) {
        return bookRepository.findBySellerOrderByCreatedAtDesc(seller);
    }
    
    public Book saveBook(Book book, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath);
                book.setImage(fileName);
                System.out.println("Image saved: " + filePath.toString());
            } catch (Exception e) {
                System.err.println("Failed to save image: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return bookRepository.save(book);
    }
    
    @Transactional
    public void deleteBook(Long id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            
            // Delete related records first to avoid foreign key constraint violations
            exchangeRequestRepository.deleteByRequestedBookId(id);
            exchangeRequestRepository.deleteByOfferedBookId(id);
            reviewRepository.deleteByBookId(id);
            wishlistRepository.deleteByBookId(id);
            
            // Delete associated image file if exists
            if (book.getImage() != null && !book.getImage().isEmpty()) {
                try {
                    Path imagePath = Paths.get(uploadDir + book.getImage());
                    Files.deleteIfExists(imagePath);
                    System.out.println("Deleted image: " + imagePath.toString());
                } catch (Exception e) {
                    System.err.println("Failed to delete image: " + e.getMessage());
                }
            }
            
            // Finally delete the book
            bookRepository.deleteById(id);
        }
    }
}