package com.bookexchange.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "exchange_requests")
public class ExchangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", nullable = false)
    @JsonIgnoreProperties({"password", "books"})
    private User requester;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"password", "books"})
    private User owner;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_book_id", nullable = false)
    @JsonIgnoreProperties({"seller"})
    private Book requestedBook;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offered_book_id", nullable = false)
    @JsonIgnoreProperties({"seller"})
    private Book offeredBook;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    private ExchangeStatus status = ExchangeStatus.PENDING;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum ExchangeStatus {
        PENDING, ACCEPTED, DECLINED, CANCELLED
    }

}