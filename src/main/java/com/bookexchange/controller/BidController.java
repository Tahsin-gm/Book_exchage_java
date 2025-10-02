package com.bookexchange.controller;

import com.bookexchange.entity.Bid;
import com.bookexchange.security.CustomUserDetails;
import com.bookexchange.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bids")
@CrossOrigin(origins = "*")
public class BidController {

    @Autowired
    private BidService bidService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Bid> placeBid(@RequestBody Map<String, Object> request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getEmail();
        return ResponseEntity.ok(bidService.placeBid(request, email));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<?> getBookBids(@PathVariable Long bookId) {
        return ResponseEntity.ok(bidService.getBookBids(bookId));
    }

    @PutMapping("/{bidId}/accept")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Bid> acceptBid(@PathVariable Long bidId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getEmail();
        return ResponseEntity.ok(bidService.acceptBid(bidId, email));
    }
}
