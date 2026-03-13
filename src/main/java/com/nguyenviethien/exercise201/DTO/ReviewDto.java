package com.nguyenviethien.exercise201.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private Integer helpfulCount;
    private Integer reportCount;
}
