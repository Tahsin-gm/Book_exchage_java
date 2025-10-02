package com.bookexchange.controller;

import com.bookexchange.entity.ExchangeRequest;
import com.bookexchange.security.CustomUserDetails;
import com.bookexchange.service.ExchangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange-requests")
@CrossOrigin(origins = "*")
public class ExchangeRequestController {

    @Autowired
    private ExchangeRequestService exchangeRequestService;

    @GetMapping("/received")
    public ResponseEntity<List<ExchangeRequest>> getReceivedRequests(Authentication authentication) {
        String email = ((CustomUserDetails) authentication.getPrincipal()).getEmail();
        return ResponseEntity.ok(exchangeRequestService.getReceivedRequests(email));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<ExchangeRequest>> getSentRequests(Authentication authentication) {
        String email = ((CustomUserDetails) authentication.getPrincipal()).getEmail();
        return ResponseEntity.ok(exchangeRequestService.getSentRequests(email));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createExchangeRequest(
            @RequestParam Long requestedBookId,
            @RequestParam Long offeredBookId,
            @RequestParam(required = false) String message,
            Authentication authentication) {

        String email = ((CustomUserDetails) authentication.getPrincipal()).getEmail();
        ExchangeRequest savedRequest = exchangeRequestService.createExchangeRequest(email, requestedBookId, offeredBookId, message);

        return ResponseEntity.ok(Map.of(
                "message", "Exchange request sent successfully",
                "requestId", savedRequest.getId()
        ));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Map<String, String>> acceptRequest(@PathVariable Long id, Authentication authentication) {
        String email = ((CustomUserDetails) authentication.getPrincipal()).getEmail();
        exchangeRequestService.acceptExchangeRequest(email, id);
        return ResponseEntity.ok(Map.of("message", "Exchange request accepted"));
    }

    @PutMapping("/{id}/decline")
    public ResponseEntity<Map<String, String>> declineRequest(@PathVariable Long id, Authentication authentication) {
        String email = ((CustomUserDetails) authentication.getPrincipal()).getEmail();
        exchangeRequestService.declineExchangeRequest(email, id);
        return ResponseEntity.ok(Map.of("message", "Exchange request declined"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelRequest(@PathVariable Long id, Authentication authentication) {
        String email = ((CustomUserDetails) authentication.getPrincipal()).getEmail();
        exchangeRequestService.cancelExchangeRequest(email, id);
        return ResponseEntity.ok(Map.of("message", "Exchange request cancelled"));
    }
}
