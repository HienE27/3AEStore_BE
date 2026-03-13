package com.nguyenviethien.exercise201.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailsDTO {
    private UUID id;
    private String productName;
    private String description; 
    private BigDecimal price;
    private Integer quantity;
    private List<String> categoryNames;
    private List<String> images;
    private List<String> imagePhus;
}
