package com.bookexchange.controller;

import com.bookexchange.entity.Event;
import com.bookexchange.entity.User;
import com.bookexchange.repository.EventRepository;
import com.bookexchange.repository.UserRepository;
import com.bookexchange.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class UserEventController {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/submit")
    public ResponseEntity<?> submitEvent(@RequestBody Map<String, Object> request, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            
            Event event = new Event();
            event.setTitle(request.get("title").toString());
            event.setDescription(request.get("description").toString());
            event.setLocation(request.get("location").toString());
            event.setStartDate(LocalDateTime.parse(request.get("startDate").toString()));
            event.setEndDate(LocalDateTime.parse(request.get("endDate").toString()));
            event.setType(Event.EventType.valueOf(request.get("type").toString()));
            // event.setStatus(Event.EventStatus.PENDING); // Requires approval
            // event.setSubmittedBy(userOpt.get());
            
            Event savedEvent = eventRepository.save(event);
            return ResponseEntity.ok(Map.of("message", "Event submitted for approval", "event", savedEvent));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}