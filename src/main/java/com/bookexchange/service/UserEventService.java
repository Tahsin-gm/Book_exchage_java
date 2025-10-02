package com.bookexchange.service;


import com.bookexchange.dto.EventSubmitRequestDto;
import com.bookexchange.entity.Event;
import com.bookexchange.entity.User;

import com.bookexchange.repository.EventRepository;
import com.bookexchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserEventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Submit a new event for approval
     */
    public Event submitEvent(User user, EventSubmitRequestDto request) {
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        Event event = request.toEvent();
        event.setCreatedAt(LocalDateTime.now());
        // Optional: event.setStatus(Event.EventStatus.PENDING);
        // Optional: event.setSubmittedBy(user);

        return eventRepository.save(event);
    }

    /**
     * Get upcoming events
     */
    public List<Event> getUpcomingEvents() {
        return eventRepository.findByStartDateAfterOrderByStartDateAsc(LocalDateTime.now());
    }

    /**
     * Get events by type
     */
    public List<Event> getEventsByType(Event.EventType type) {
        return eventRepository.findByTypeOrderByStartDateAsc(type);
    }
}
