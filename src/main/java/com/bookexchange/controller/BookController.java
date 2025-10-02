package com.bookexchange.controller;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.User;
import com.bookexchange.security.CustomUserDetails;
import com.bookexchange.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    

    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {

        List<Book> books = bookService.getAllAvailableBooks();
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }
    
    @GetMapping("/books/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        Optional<Book> book = null;
        try {
            book = bookService.getBookById(id);
        } catch (Exception e) {
            e.getStackTrace();
        }

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
    @PreAuthorize("hasAuthority('ROLE_USER') or" +
            " hasAuthority('ROLE_ADMIN') or" +
            " hasAuthority('ROLE_SUPER_ADMIN')")
    
    public ResponseEntity<?> addBook(
            @ModelAttribute Book  book,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication authentication) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User seller = userDetails.getUser();

            Book addedBook = bookService.saveBook(book,image,seller);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedBook);

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