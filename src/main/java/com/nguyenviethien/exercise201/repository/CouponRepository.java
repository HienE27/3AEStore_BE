// package com.nguyenviethien.exercise201.repository;

// import java.util.UUID;
// import java.util.Optional;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.data.rest.core.annotation.RepositoryRestResource;
// import org.springframework.stereotype.Repository;

// import com.nguyenviethien.exercise201.entity.Coupon;

// import java.util.Date;
// import java.util.List;

// @Repository
// @RepositoryRestResource(path = "coupons")
// public interface CouponRepository extends JpaRepository<Coupon, UUID> {
//     //Optional<Coupon> findByCode(String code);

//     // @Query("SELECT c FROM Coupon c WHERE c.maxUsage > c.timesUsed")
//     // List<Coupon> findByMaxUsageGreaterThanTimesUsed();

//     // List<Coupon> findByCouponStartDateBeforeAndCouponEndDateAfter(Date now, Date now2);

//     // boolean existsByCode(String code);

//     // Mới thêm để order
//     Optional<Coupon> findByCode(String code);
    
//     boolean existsByCode(String code);
    
//     @Query("SELECT c FROM Coupon c WHERE c.maxUsage IS NULL OR c.timesUsed < c.maxUsage")
//     List<Coupon> findByMaxUsageGreaterThanTimesUsed();
    
//     @Query("SELECT c FROM Coupon c WHERE (c.couponStartDate IS NULL OR c.couponStartDate <= :date) AND (c.couponEndDate IS NULL OR c.couponEndDate >= :date)")
//     List<Coupon> findByCouponStartDateBeforeAndCouponEndDateAfter(@Param("date") Date date1, @Param("date") Date date2);
    
//     @Query("SELECT c FROM Coupon c WHERE c.code = :code AND (c.maxUsage IS NULL OR c.timesUsed < c.maxUsage) AND (c.couponStartDate IS NULL OR c.couponStartDate <= :date) AND (c.couponEndDate IS NULL OR c.couponEndDate >= :date)")
//     Optional<Coupon> findValidCouponByCode(@Param("code") String code, @Param("date") Date date);
// }



package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Coupon;

@Repository
@RepositoryRestResource(path = "coupons")
public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    
    // Basic queries
    Optional<Coupon> findByCode(String code);
    boolean existsByCode(String code);
    
    // Enhanced coupon validation
    @Query("SELECT c FROM Coupon c WHERE c.code = :code AND " +
           "(c.maxUsage IS NULL OR c.timesUsed < c.maxUsage) AND " +
           "(c.couponStartDate IS NULL OR c.couponStartDate <= :date) AND " +
           "(c.couponEndDate IS NULL OR c.couponEndDate >= :date)")
    Optional<Coupon> findValidCouponByCode(@Param("code") String code, @Param("date") Date date);
    
    // Available coupons
    @Query("SELECT c FROM Coupon c WHERE " +
           "(c.maxUsage IS NULL OR c.timesUsed < c.maxUsage) AND " +
           "(c.couponStartDate IS NULL OR c.couponStartDate <= :date) AND " +
           "(c.couponEndDate IS NULL OR c.couponEndDate >= :date)")
    List<Coupon> findAvailableCoupons(@Param("date") Date date);
    
    // Usage tracking
    @Query("SELECT c FROM Coupon c WHERE c.maxUsage IS NULL OR c.timesUsed < c.maxUsage")
    List<Coupon> findByMaxUsageGreaterThanTimesUsed();
    
    // Date range queries
    @Query("SELECT c FROM Coupon c WHERE " +
           "(c.couponStartDate IS NULL OR c.couponStartDate <= :date) AND " +
           "(c.couponEndDate IS NULL OR c.couponEndDate >= :date)")
    List<Coupon> findByCouponStartDateBeforeAndCouponEndDateAfter(@Param("date") Date date1, @Param("date") Date date2);
    
    // Expired coupons
    @Query("SELECT c FROM Coupon c WHERE c.couponEndDate IS NOT NULL AND c.couponEndDate < :date")
    List<Coupon> findExpiredCoupons(@Param("date") Date date);
    
    // Active coupons
    @Query("SELECT c FROM Coupon c WHERE " +
           "(c.couponStartDate IS NULL OR c.couponStartDate <= :date) AND " +
           "(c.couponEndDate IS NULL OR c.couponEndDate >= :date)")
    List<Coupon> findActiveCoupons(@Param("date") Date date);
    
    // Coupon statistics
    @Query("SELECT c.discountType, COUNT(c), AVG(c.discountValue) FROM Coupon c GROUP BY c.discountType")
    List<Object[]> getCouponStatistics();
    
    // Most used coupons
    @Query("SELECT c FROM Coupon c ORDER BY c.timesUsed DESC")
    List<Coupon> findMostUsedCoupons();
    
    // Coupons by value range
    @Query("SELECT c FROM Coupon c WHERE c.discountValue BETWEEN :minValue AND :maxValue")
    List<Coupon> findByDiscountValueRange(@Param("minValue") Double minValue, @Param("maxValue") Double maxValue);
}