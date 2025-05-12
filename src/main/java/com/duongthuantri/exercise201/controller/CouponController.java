package com.duongthuantri.exercise201.controller;

import java.util.List;
import java.util.UUID;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.duongthuantri.exercise201.entity.Coupon;
import com.duongthuantri.exercise201.service.CouponService;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable UUID id) {
        return couponService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Coupon> getCouponByCode(@PathVariable String code) {
        return couponService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Coupon>> getActiveCoupons() {
        return ResponseEntity.ok(
                couponService.findByMaxUsageGreaterThanTimesUsed());
    }

    @GetMapping("/valid")
    public ResponseEntity<List<Coupon>> getValidCoupons() {
        return ResponseEntity.ok(
                couponService.findValidCoupons(new Date()));
    }

    @PostMapping
    public ResponseEntity<?> createCoupon(@RequestBody Coupon coupon) {
        if (couponService.existsByCode(coupon.getCode())) {
            return ResponseEntity.badRequest()
                    .body("Coupon code already exists");
        }
        return ResponseEntity.ok(couponService.save(coupon));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCoupon(
            @PathVariable UUID id,
            @RequestBody Coupon coupon) {
        if (!couponService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (!coupon.getCode().equals(couponService.findById(id).get().getCode()) &&
                couponService.existsByCode(coupon.getCode())) {
            return ResponseEntity.badRequest()
                    .body("Coupon code already exists");
        }
        coupon.setId(id);
        return ResponseEntity.ok(couponService.save(coupon));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable UUID id) {
        if (!couponService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        couponService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/increment-usage")
    public ResponseEntity<Void> incrementCouponUsage(@PathVariable UUID id) {
        Coupon coupon = couponService.findById(id).orElse(null);
        if (coupon == null) {
            return ResponseEntity.notFound().build();
        }
        couponService.incrementTimesUsed(coupon);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/validate")
    public ResponseEntity<Boolean> validateCoupon(@PathVariable UUID id) {
        return couponService.findById(id)
                .map(coupon -> ResponseEntity.ok(couponService.isValidCoupon(coupon)))
                .orElse(ResponseEntity.notFound().build());
    }
}