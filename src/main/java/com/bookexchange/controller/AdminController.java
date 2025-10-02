package com.bookexchange.controller;

import com.bookexchange.entity.Book;
import com.bookexchange.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> request) {
        Map<String, Object> response = adminService.adminLogin(
                request.get("email"),
                request.get("password")
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/books/{bookId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long bookId) {
        adminService.deleteBook(bookId);
        return ResponseEntity.ok(Map.of("message", "Book deleted successfully"));
    }

    @GetMapping("/books")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(adminService.getAllBooks());
    }

    @PutMapping("/events/{eventId}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> approveEvent(@PathVariable Long eventId) {
        String message = adminService.approveEvent(eventId);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
