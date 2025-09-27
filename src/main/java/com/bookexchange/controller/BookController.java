package com.bookexchange.controller;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.User;
import com.bookexchange.service.BookService;
import com.bookexchange.service.JwtService;
import com.bookexchange.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BookController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        System.out.println("=== GET ALL BOOKS REQUEST ===");
        List<Book> books = bookService.getAllAvailableBooks();
        System.out.println("Found " + books.size() + " books");
        for (Book book : books) {
            System.out.println("Book: " + book.getTitle() + " by " + book.getAuthor() + " (ID: " + book.getId() + ")");
        }
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/books/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        Optional<Book> book = bookService.getBookById(id);
        if (book.isPresent()) {
            return ResponseEntity.ok(book.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Book not found");
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/books")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> addBook(
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam(value = "isbn", required = false, defaultValue = "") String isbn,
            @RequestParam("price") BigDecimal price,
            @RequestParam("condition") String condition,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "listingType", defaultValue = "sale") String listingType,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            System.out.println("=== ADD BOOK REQUEST ===");
            System.out.println("Title: " + title);
            System.out.println("Author: " + author);
            System.out.println("ISBN: " + isbn);
            System.out.println("Price: " + price);
            System.out.println("Condition: " + condition);
            System.out.println("Description: " + description);
            System.out.println("Listing Type: " + listingType);
            System.out.println("Image file: " + (image != null ? image.getOriginalFilename() : "none"));
            
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            Long userId = jwtService.extractUserId(token);
            System.out.println("User ID from token: " + userId);
            
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                System.out.println("User not found: " + email);
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            User seller = userOpt.get();
            System.out.println("Seller found: " + seller.getUsername() + " (ID: " + seller.getId() + ")");
            
            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            book.setIsbn(isbn);
            book.setPrice(price);
            book.setConditionType(Book.BookCondition.valueOf(condition.toUpperCase()));
            book.setDescription(description);
            book.setListingType(Book.ListingType.valueOf(listingType.toUpperCase()));
            book.setSeller(seller);
            
            System.out.println("Saving book...");
            Book savedBook = bookService.saveBook(book, image);
            System.out.println("Book saved with ID: " + savedBook.getId());
            System.out.println("Image filename: " + savedBook.getImage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Book added successfully");
            response.put("bookId", savedBook.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Add book error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/my-books")
    public ResponseEntity<?> getMyBooks(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<Book> books = bookService.getBooksBySeller(userOpt.get());
            return ResponseEntity.ok(books);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<User> userOpt = userService.findByEmail(email);
            Optional<Book> bookOpt = bookService.getBookById(id);
            
            if (userOpt.isEmpty() || bookOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User or book not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            Book book = bookOpt.get();
            User user = userOpt.get();
            
            // Check if user owns the book
            if (!book.getSeller().getId().equals(user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "You can only delete your own books");
                return ResponseEntity.badRequest().body(error);
            }
            
            bookService.deleteBook(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Book deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}