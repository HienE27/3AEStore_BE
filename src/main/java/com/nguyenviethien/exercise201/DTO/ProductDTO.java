package com.nguyenviethien.exercise201.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private java.util.UUID id;
    private String slug;
    private String productName;
    private String sku;
    private Double salePrice;
    private Double comparePrice;
    private Double buyingPrice;
    private Integer quantity;
    private String shortDescription;
}

package com.nguyenviethien.exercise201.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class ProductDTO {
    private UUID id;
    private String productName;
    private String description; 
    private BigDecimal price;
    private Integer quantity;
    private List<String> categoryNames;
    private List<String> images; // for thumbnails
    private List<String> imagePhus; // for gallery images
}
