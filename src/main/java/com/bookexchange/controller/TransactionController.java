package com.bookexchange.controller;

import com.bookexchange.entity.Transaction;
import com.bookexchange.security.CustomUserDetails;
import com.bookexchange.service.TransactionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> createTransaction(@RequestBody Map<String, Long> request, Authentication authentication) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String email = userDetails.getEmail();
            Long bookId = request.get("bookId");
            
            Transaction transaction = transactionService.createTransaction(email, bookId);
            return ResponseEntity.ok(transaction);

    }
    
    @GetMapping("/purchases")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getUserPurchases(Authentication authentication) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String email = userDetails.getEmail();
            
            List<Transaction> purchases = transactionService.getUserPurchases(email);
            return ResponseEntity.ok(purchases);

    }
    
    @GetMapping("/sales")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getUserSales(Authentication authentication) {
        String email = ((CustomUserDetails) authentication.getPrincipal()).getEmail();
        return ResponseEntity.ok(transactionService.getUserSales(email));
    }
}