package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Coupon;
import com.duongthuantri.exercise201.entity.Product;
import com.duongthuantri.exercise201.entity.ProductCoupon;

@Repository
public interface ProductCouponRepository extends JpaRepository<ProductCoupon, UUID> {
    List<ProductCoupon> findByProduct(Product product);

    List<ProductCoupon> findByCoupon(Coupon coupon);

    void deleteByProductAndCoupon(Product product, Coupon coupon);
}