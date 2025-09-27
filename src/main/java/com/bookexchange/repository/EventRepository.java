package com.bookexchange.repository;

import com.bookexchange.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStartDateAfterOrderByStartDateAsc(LocalDateTime date);
    List<Event> findByTypeOrderByStartDateAsc(Event.EventType type);
}