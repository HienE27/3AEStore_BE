package com.nguyenviethien.exercise201.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class ProductDetailsDTO {
    private UUID id;
    private String productName;
    private String description; 
    private BigDecimal price;
    private Integer quantity;
    private List<String> categoryNames;
    private List<String> images; // for thumbnails
    private List<String> imagePhus; // for gallery images
}

