package com.nguyenviethien.exercise201.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "discount_type", nullable = false, length = 50)
    private String discountType;

    @Column(name = "times_used", nullable = false)
    private BigDecimal timesUsed = BigDecimal.ZERO;

    @Column(name = "max_usage")
    private BigDecimal maxUsage;

    @Column(name = "order_amount_limit")
    private BigDecimal orderAmountLimit;

    @Column(name = "coupon_start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date couponStartDate;

    @Column(name = "coupon_end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date couponEndDate;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL)
    private List<ProductCoupon> productCoupons;
}