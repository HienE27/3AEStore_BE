package com.nguyenviethien.exercise201.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VnpayRequest {
    
    @NotBlank(message = "Order ID là bắt buộc")
    private String orderId;

    @NotNull(message = "Số tiền là bắt buộc")
    @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0")
    @DecimalMax(value = "999999999.99", message = "Số tiền không hợp lệ")
    private Double amount;

    @NotBlank(message = "Customer ID là bắt buộc")
    private String customerId;
}
