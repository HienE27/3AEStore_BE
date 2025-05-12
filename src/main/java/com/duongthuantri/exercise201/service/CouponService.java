package com.duongthuantri.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.duongthuantri.exercise201.entity.Coupon;

import java.util.Date;

public interface CouponService {
    List<Coupon> findAll();

    Optional<Coupon> findById(UUID id);

    Optional<Coupon> findByCode(String code);

    List<Coupon> findByMaxUsageGreaterThanTimesUsed();

    List<Coupon> findValidCoupons(Date currentDate);

    Coupon save(Coupon coupon);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsByCode(String code);

    boolean isValidCoupon(Coupon coupon);

    void incrementTimesUsed(Coupon coupon);
}