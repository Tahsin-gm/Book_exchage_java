package com.bookexchange.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private BookCondition conditionType;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String image;
    
    private String isbn;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnoreProperties({"password", "books"})
    private User seller;
    
    @Enumerated(EnumType.STRING)
    private BookStatus status = BookStatus.AVAILABLE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "listing_type")
    private ListingType listingType = ListingType.SALE;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum BookCondition {
        NEW, LIKE_NEW, GOOD, FAIR, POOR
    }
    
    public enum BookStatus {
        AVAILABLE, SOLD
    }
    
    public enum ListingType {
        SALE, EXCHANGE, AUCTION
    }
    

}