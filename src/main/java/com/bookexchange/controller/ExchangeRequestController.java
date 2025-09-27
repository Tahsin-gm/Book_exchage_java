package com.bookexchange.controller;

import com.bookexchange.entity.Book;
import com.bookexchange.entity.ExchangeRequest;
import com.bookexchange.entity.User;
import com.bookexchange.repository.BookRepository;
import com.bookexchange.repository.ExchangeRequestRepository;
import com.bookexchange.service.JwtService;
import com.bookexchange.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExchangeRequestController {
    
    @Autowired
    private ExchangeRequestRepository exchangeRequestRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @GetMapping("/exchange-requests/received")
    public ResponseEntity<?> getReceivedRequests(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<ExchangeRequest> requests = exchangeRequestRepository.findByOwnerOrderByCreatedAtDesc(userOpt.get());
            return ResponseEntity.ok(requests);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/exchange-requests/sent")
    public ResponseEntity<?> getSentRequests(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<ExchangeRequest> requests = exchangeRequestRepository.findByRequesterOrderByCreatedAtDesc(userOpt.get());
            return ResponseEntity.ok(requests);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/exchange-requests")
    public ResponseEntity<?> createExchangeRequest(
            @RequestParam Long requestedBookId,
            @RequestParam Long offeredBookId,
            @RequestParam(required = false) String message,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<User> requesterOpt = userService.findByEmail(email);
            Optional<Book> requestedBookOpt = bookRepository.findById(requestedBookId);
            Optional<Book> offeredBookOpt = bookRepository.findById(offeredBookId);
            
            if (requesterOpt.isEmpty() || requestedBookOpt.isEmpty() || offeredBookOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User or books not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            Book requestedBook = requestedBookOpt.get();
            User owner = requestedBook.getSeller();
            
            ExchangeRequest exchangeRequest = new ExchangeRequest();
            exchangeRequest.setRequester(requesterOpt.get());
            exchangeRequest.setOwner(owner);
            exchangeRequest.setRequestedBook(requestedBook);
            exchangeRequest.setOfferedBook(offeredBookOpt.get());
            exchangeRequest.setMessage(message);
            
            ExchangeRequest savedRequest = exchangeRequestRepository.save(exchangeRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Exchange request sent successfully");
            response.put("requestId", savedRequest.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/exchange-requests/{id}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<User> userOpt = userService.findByEmail(email);
            Optional<ExchangeRequest> requestOpt = exchangeRequestRepository.findById(id);
            
            if (userOpt.isEmpty() || requestOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User or request not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            ExchangeRequest request = requestOpt.get();
            if (!request.getOwner().getId().equals(userOpt.get().getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.badRequest().body(error);
            }
            
            request.setStatus(ExchangeRequest.ExchangeStatus.ACCEPTED);
            exchangeRequestRepository.save(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Exchange request accepted");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/exchange-requests/{id}/decline")
    public ResponseEntity<?> declineRequest(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<User> userOpt = userService.findByEmail(email);
            Optional<ExchangeRequest> requestOpt = exchangeRequestRepository.findById(id);
            
            if (userOpt.isEmpty() || requestOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User or request not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            ExchangeRequest request = requestOpt.get();
            if (!request.getOwner().getId().equals(userOpt.get().getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.badRequest().body(error);
            }
            
            request.setStatus(ExchangeRequest.ExchangeStatus.DECLINED);
            exchangeRequestRepository.save(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Exchange request declined");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/exchange-requests/{id}")
    public ResponseEntity<?> cancelRequest(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<User> userOpt = userService.findByEmail(email);
            Optional<ExchangeRequest> requestOpt = exchangeRequestRepository.findById(id);
            
            if (userOpt.isEmpty() || requestOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User or request not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            ExchangeRequest request = requestOpt.get();
            if (!request.getRequester().getId().equals(userOpt.get().getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.badRequest().body(error);
            }
            
            exchangeRequestRepository.delete(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Exchange request cancelled");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}