package com.bookexchange.controller;

import com.bookexchange.dto.EventSubmitRequestDto;
import com.bookexchange.entity.Event;
import com.bookexchange.entity.User;
import com.bookexchange.security.CustomUserDetails;
import com.bookexchange.service.UserEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class UserEventController {

    @Autowired
    private UserEventService userEventService;

    /**
     * Submit an event (ROLE_USER required)
     */
    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> submitEvent( @RequestBody EventSubmitRequestDto request,
                                         Authentication authentication) {
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        Event savedEvent = userEventService.submitEvent(user, request);
        return ResponseEntity.ok(Map.of("message", "Event submitted for approval", "event", savedEvent));
    }

    /**
     * List upcoming events (public)
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingEvents() {
        return ResponseEntity.ok(userEventService.getUpcomingEvents());
    }

    /**
     * List events by type (public)
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getEventsByType(@PathVariable Event.EventType type) {
        return ResponseEntity.ok(userEventService.getEventsByType(type));
    }
}
