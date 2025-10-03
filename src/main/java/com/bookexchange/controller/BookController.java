package com.bookexchange.controller;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.BookCondition;
import com.bookexchange.entity.ListingType;
import com.bookexchange.entity.User;
import com.bookexchange.security.CustomUserDetails;
import com.bookexchange.security.JwtService;
import com.bookexchange.service.BookService;
import com.bookexchange.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BookController {

    
    @Autowired
    private BookService bookService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;

    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllAvailableBooks();

        log.info("=== FETCHING ALL BOOKS ===");

        if (books == null || books.isEmpty()) {
            log.warn("No books found.");
        } else {
            for (Book book : books) {
                log.info("---- Book ----");
                log.info("ID: {}", book.getId());
                log.info("Title: {}", book.getTitle());
                log.info("Author: {}", book.getAuthor());
                log.info("ISBN: {}", book.getIsbn());
                log.info("Price: {}", book.getPrice());
                log.info("Condition: {}", book.getConditionType());
                log.info("Description: {}", book.getDescription());
                log.info("Listing Type: {}", book.getListingType());

            }
        }

        return ResponseEntity.ok(books);
    }

    
    @GetMapping("/books/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {

        Optional<Book> book = bookService.getBookById(id);


        return ResponseEntity.status(HttpStatus.OK).body(book);

    }
    /*@GetMapping("/books/title/{title}")
    public ResponseEntity<?> getBookById(@PathVariable String title) {
        Book book = bookService.getBookByTitle(title);

        return ResponseEntity.status(HttpStatus.OK).body(book);

    }
    @GetMapping("/books/{search}")
    public ResponseEntity<?> searchBooks(@RequestParam("q") String keyword) {
        List<Book> books = bookService.searchBooksByTitle(keyword);
        return ResponseEntity.ok(books);
    }
*/




    @PostMapping("/books")
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
            book.setConditionType(BookCondition.valueOf(condition.toUpperCase()));
            book.setDescription(description);
            book.setListingType(ListingType.valueOf(listingType.toUpperCase()));
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
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getMyBooks(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        List<Book> books = bookService.getBooksBySeller(user);
        return ResponseEntity.ok(books);
    }
    
    @DeleteMapping("/books/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        // Service handles all business logic and returns message
        String message = bookService.deleteBook(id, user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

}