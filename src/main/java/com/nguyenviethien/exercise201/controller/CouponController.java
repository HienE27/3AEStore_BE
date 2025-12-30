package com.nguyenviethien.exercise201.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.entity.Coupon;
import com.nguyenviethien.exercise201.service.CouponService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * Validate coupon cho frontend checkout
     */
    @GetMapping("/validate/{couponCode}")
    public ResponseEntity<?> validateCoupon(
            @PathVariable String couponCode,
            @RequestParam double orderAmount) {
        try {
            Optional<Coupon> couponOpt = couponService.findByCode(couponCode);
            
            if (couponOpt.isEmpty()) {
                return ResponseEntity.ok(createValidationResponse(false, null, "Mã giảm giá không tồn tại"));
            }

            Coupon coupon = couponOpt.get();
            
            // Kiểm tra tính hợp lệ
            String validationMessage = validateCouponConditions(coupon, BigDecimal.valueOf(orderAmount));
            
            if (validationMessage != null) {
                return ResponseEntity.ok(createValidationResponse(false, null, validationMessage));
            }

            // Coupon hợp lệ
            return ResponseEntity.ok(createValidationResponse(true, coupon, "Mã giảm giá hợp lệ"));

        } catch (Exception e) {
            return ResponseEntity.ok(createValidationResponse(false, null, "Có lỗi xảy ra khi kiểm tra mã giảm giá"));
        }
    }

    /**
     * Lấy danh sách coupon có thể sử dụng
     */
    @GetMapping("/available")
    public ResponseEntity<List<Coupon>> getAvailableCoupons() {
        try {
            List<Coupon> validCoupons = couponService.findValidCoupons(new Date());
            return ResponseEntity.ok(validCoupons);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Áp dụng coupon và tính toán discount
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyCoupon(@RequestBody ApplyCouponRequest request) {
        try {
            Optional<Coupon> couponOpt = couponService.findByCode(request.getCouponCode());
            
            if (couponOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Mã giảm giá không tồn tại");
            }

            Coupon coupon = couponOpt.get();
            BigDecimal orderAmount = BigDecimal.valueOf(request.getOrderAmount());
            
            // Validate coupon
            String validationMessage = validateCouponConditions(coupon, orderAmount);
            if (validationMessage != null) {
                return ResponseEntity.badRequest().body(validationMessage);
            }

            // Tính discount amount
            BigDecimal discountAmount = calculateDiscountAmount(coupon, orderAmount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("coupon", createCouponDTO(coupon));
            response.put("discountAmount", discountAmount);
            response.put("finalAmount", orderAmount.subtract(discountAmount));
            response.put("message", "Áp dụng mã giảm giá thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Có lỗi xảy ra khi áp dụng mã giảm giá");
        }
    }

    // ========== HELPER METHODS ==========

    private Map<String, Object> createValidationResponse(boolean valid, Coupon coupon, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("valid", valid);
        response.put("message", message);
        
        if (valid && coupon != null) {
            response.put("coupon", createCouponDTO(coupon));
        }
        
        return response;
    }

    private Map<String, Object> createCouponDTO(Coupon coupon) {
        Map<String, Object> couponDTO = new HashMap<>();
        couponDTO.put("id", coupon.getId().toString());
        couponDTO.put("code", coupon.getCode());
        couponDTO.put("discountValue", coupon.getDiscountValue());
        couponDTO.put("discountType", coupon.getDiscountType());
        couponDTO.put("orderAmountLimit", coupon.getOrderAmountLimit());
        couponDTO.put("maxUsage", coupon.getMaxUsage());
        couponDTO.put("timesUsed", coupon.getTimesUsed());
        couponDTO.put("couponStartDate", coupon.getCouponStartDate());
        couponDTO.put("couponEndDate", coupon.getCouponEndDate());
        
        return couponDTO;
    }

    private String validateCouponConditions(Coupon coupon, BigDecimal orderAmount) {
        Date currentDate = new Date();
        
        // Kiểm tra thời gian hiệu lực
        if (coupon.getCouponStartDate() != null && currentDate.before(coupon.getCouponStartDate())) {
            return "Mã giảm giá chưa có hiệu lực";
        }
        
        if (coupon.getCouponEndDate() != null && currentDate.after(coupon.getCouponEndDate())) {
            return "Mã giảm giá đã hết hạn";
        }
        
        // Kiểm tra số lần sử dụng
        if (coupon.getMaxUsage() != null && 
            coupon.getTimesUsed().compareTo(coupon.getMaxUsage()) >= 0) {
            return "Mã giảm giá đã hết lượt sử dụng";
        }
        
        // Kiểm tra giá trị đơn hàng tối thiểu
        if (coupon.getOrderAmountLimit() != null && 
            orderAmount.compareTo(coupon.getOrderAmountLimit()) < 0) {
            return String.format("Đơn hàng phải có giá trị tối thiểu %,.0f VND để sử dụng mã này", 
                    coupon.getOrderAmountLimit().doubleValue());
        }
        
        return null; // Coupon hợp lệ
    }

    private BigDecimal calculateDiscountAmount(Coupon coupon, BigDecimal orderAmount) {
        if ("PERCENTAGE".equals(coupon.getDiscountType())) {
            return orderAmount.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        } else if ("FIXED".equals(coupon.getDiscountType())) {
            return coupon.getDiscountValue().min(orderAmount);
        }
        return BigDecimal.ZERO;
    }

    // ========== DTOs ==========

    public static class ApplyCouponRequest {
        private String couponCode;
        private double orderAmount;

        // Getters and setters
        public String getCouponCode() { return couponCode; }
        public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

        public double getOrderAmount() { return orderAmount; }
        public void setOrderAmount(double orderAmount) { this.orderAmount = orderAmount; }
    }
}