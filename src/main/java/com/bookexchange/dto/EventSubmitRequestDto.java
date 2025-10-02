package com.bookexchange.dto;

import com.bookexchange.entity.Event;
import lombok.*;

import org.antlr.v4.runtime.misc.NotNull;


import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class EventSubmitRequestDto {
    @NotNull
    private String title;
    @NotNull
    private String description;
    @NotNull
    private String location;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    @NotNull
    private Event.EventType type;

    /**
     * Convert DTO to Event entity
     */
    public Event toEvent() {
        Event event = new Event();
        event.setTitle(this.title);
        event.setDescription(this.description);
        event.setLocation(this.location);
        event.setStartDate(this.startDate);
        event.setEndDate(this.endDate);
        event.setType(this.type != null ? this.type : Event.EventType.BOOK_FAIR);
        return event;
    }
}
