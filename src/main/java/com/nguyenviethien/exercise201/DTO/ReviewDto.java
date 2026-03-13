package com.nguyenviethien.exercise201.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ReviewDto {
    private UUID id;
    private String content;
    private Float ratingPoint;
    private Integer rating;
    private Timestamp createdAt;
    private ProductDto product;
    private CustomerDto customer;
    private List<ReviewImageDto> images;
    private Boolean isVerifiedPurchase;
    private Boolean isAnonymous;
    private String status;
    private List<ReviewReplyDto> replies;
    
    // Full constructor
    public ReviewDto(UUID id, String content, Float ratingPoint, Integer rating, Timestamp createdAt,
                     ProductDto product, CustomerDto customer,
                     List<ReviewImageDto> images, Boolean isVerifiedPurchase, Boolean isAnonymous, String status,
                     List<ReviewReplyDto> replies) {
        this.id = id;
        this.content = content;
        this.ratingPoint = ratingPoint;
        this.rating = rating;
        this.createdAt = createdAt;
        this.product = product;
        this.customer = customer;
        this.images = images;
        this.isVerifiedPurchase = isVerifiedPurchase;
        this.isAnonymous = isAnonymous;
        this.status = status;
        this.replies = replies;
    }
    
    // Constructor without replies (backward compatibility)
    public ReviewDto(UUID id, String content, Float ratingPoint, Integer rating, Timestamp createdAt,
                     ProductDto product, CustomerDto customer,
                     List<ReviewImageDto> images, Boolean isVerifiedPurchase, Boolean isAnonymous, String status) {
        this(id, content, ratingPoint, rating, createdAt, product, customer, images, isVerifiedPurchase, isAnonymous, status, null);
    }
}
