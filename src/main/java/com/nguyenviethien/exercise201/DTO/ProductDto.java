package com.nguyenviethien.exercise201.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto {
    
    private UUID id;
    
    @Size(max = 200, message = "Slug không được quá 200 ký tự")
    private String slug;

    @NotBlank(message = "Tên sản phẩm là bắt buộc")
    @Size(max = 500, message = "Tên sản phẩm không được quá 500 ký tự")
    private String productName;

    @Size(max = 100, message = "SKU không được quá 100 ký tự")
    private String sku;

    @NotNull(message = "Giá bán là bắt buộc")
    @DecimalMin(value = "0.0", message = "Giá bán không được âm")
    private BigDecimal salePrice;

    @DecimalMin(value = "0.0", message = "Giá so sánh không được âm")
    private BigDecimal comparePrice;

    @DecimalMin(value = "0.0", message = "Giá mua vào không được âm")
    private BigDecimal buyingPrice;

    @Min(value = 0, message = "Số lượng không được âm")
    private Integer quantity;

    @Size(max = 1000, message = "Mô tả ngắn không được quá 1000 ký tự")
    private String shortDescription;

    private String productDescription;
    private Boolean published;
    private Boolean disableOutOfStock;
}
