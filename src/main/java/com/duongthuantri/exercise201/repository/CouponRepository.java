package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Coupon;

import java.util.Date;
import java.util.List;

@Repository
@RepositoryRestResource(path = "coupons")
public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    Optional<Coupon> findByCode(String code);

    @Query("SELECT c FROM Coupon c WHERE c.maxUsage > c.timesUsed")
    List<Coupon> findByMaxUsageGreaterThanTimesUsed();

    List<Coupon> findByCouponStartDateBeforeAndCouponEndDateAfter(Date now, Date now2);

    boolean existsByCode(String code);
}