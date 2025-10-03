package com.bookexchange.service;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.Event;
import com.bookexchange.entity.Role;
import com.bookexchange.entity.User;
import com.bookexchange.exception.BookNotFoundException;
import com.bookexchange.exception.EventNotFoundException;
import com.bookexchange.exception.UnauthorizedActionException;
import com.bookexchange.repository.BookRepository;
import com.bookexchange.repository.EventRepository;
import com.bookexchange.security.CustomUserDetails;
import com.bookexchange.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final BookRepository bookRepository;
    private final EventRepository eventRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticate admin and return JWT token
     */
    public Map<String, Object> adminLogin(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedActionException("Admin access required");
        }

        String token = jwtService.generateToken(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("admin", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));

        return response;
    }

    /**
     * Delete a book by id (only admin)
     */
    public void deleteBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException("Book not found with ID: " + bookId);
        }
        bookRepository.deleteById(bookId);
    }

    /**
     * Get all books (admin view)
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * Approve an event
     */
    public String approveEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow( () -> new EventNotFoundException("Event not found with ID: " + eventId));
        //event.setApproved(true); // assuming you have a boolean field `approved`
        eventRepository.save(event);

        return "Event approved successfully";
    }
}
