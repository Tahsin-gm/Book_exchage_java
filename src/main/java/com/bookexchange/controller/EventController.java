package com.bookexchange.controller;

import com.bookexchange.entity.Event;
import com.bookexchange.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {
    
    @Autowired
    private EventRepository eventRepository;
    
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventRepository.findByStartDateAfterOrderByStartDateAsc(LocalDateTime.now());
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/book-fairs")
    public ResponseEntity<List<Event>> getBookFairs() {
        List<Event> bookFairs = eventRepository.findByTypeOrderByStartDateAsc(Event.EventType.BOOK_FAIR);
        return ResponseEntity.ok(bookFairs);
    }
}