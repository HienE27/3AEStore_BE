package com.duongthuantri.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duongthuantri.exercise201.entity.Coupon;
import com.duongthuantri.exercise201.repository.CouponRepository;
import com.duongthuantri.exercise201.service.CouponService;

@Service
@Transactional
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Override
    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }

    @Override
    public Optional<Coupon> findById(UUID id) {
        return couponRepository.findById(id);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return couponRepository.findByCode(code);
    }

    @Override
    public List<Coupon> findByMaxUsageGreaterThanTimesUsed() {
        return couponRepository.findByMaxUsageGreaterThanTimesUsed();
    }

    @Override
    public List<Coupon> findValidCoupons(Date currentDate) {
        return couponRepository.findByCouponStartDateBeforeAndCouponEndDateAfter(currentDate, currentDate);
    }

    @Override
    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    public void deleteById(UUID id) {
        couponRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return couponRepository.existsById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return couponRepository.existsByCode(code);
    }

    @Override
    public boolean isValidCoupon(Coupon coupon) {
        Date currentDate = new Date();
        return (coupon.getMaxUsage() == null || coupon.getTimesUsed().compareTo(coupon.getMaxUsage()) < 0) &&
                (coupon.getCouponStartDate() == null || coupon.getCouponStartDate().before(currentDate)) &&
                (coupon.getCouponEndDate() == null || coupon.getCouponEndDate().after(currentDate));
    }

    @Override
    @Transactional
    public void incrementTimesUsed(Coupon coupon) {
        coupon.setTimesUsed(coupon.getTimesUsed().add(java.math.BigDecimal.ONE));
        save(coupon);
    }
}