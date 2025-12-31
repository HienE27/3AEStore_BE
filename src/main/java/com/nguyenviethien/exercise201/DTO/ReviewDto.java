package com.nguyenviethien.exercise201.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private UUID id;
    private String content;
    private Float ratingPoint;
    private Integer rating;
    private Timestamp createdAt;
    private com.nguyenviethien.exercise201.DTO.ProductDto product;
    private CustomerDto customer;
    private List<ReviewImageDto> images;
}


