package com.nguyenviethien.exercise201.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutRequest {
    
    @NotBlank(message = "Customer ID là bắt buộc")
    private String customerId;

    @Size(max = 50, message = "Mã giảm giá không được quá 50 ký tự")
    private String couponCode;

    @NotBlank(message = "Địa chỉ giao hàng là bắt buộc")
    @Size(max = 500, message = "Địa chỉ không được quá 500 ký tự")
    private String shippingAddress;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Pattern(regexp = "^[0-9+\\-\\s()]{10,20}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotBlank(message = "Phương thức thanh toán là bắt buộc")
    @Pattern(regexp = "^(COD|VNPAY|PayPal)$", message = "Phương thức thanh toán không hợp lệ")
    private String paymentMethod;

    @Size(max = 1000, message = "Ghi chú không được quá 1000 ký tự")
    private String note;

    @NotNull(message = "Danh sách sản phẩm là bắt buộc")
    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    @Valid
    private List<OrderDetail> orderDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderDetail {
        
        @NotBlank(message = "Product ID là bắt buộc")
        private String productId;

        @NotNull(message = "Số lượng là bắt buộc")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        @Max(value = 999, message = "Số lượng không được quá 999")
        private Integer quantity;

        @NotNull(message = "Giá là bắt buộc")
        @DecimalMin(value = "0.0", message = "Giá không được âm")
        @DecimalMax(value = "999999999.99", message = "Giá không hợp lệ")
        private BigDecimal price;
    }
}
