package com.nguyenviethien.exercise201.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_status_id")
    @JsonIgnore
    private OrderStatus orderStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date created_at;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated_at;

    // Tracking fields
    @Column(name = "order_approved_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderApprovedAt;

    @Column(name = "order_delivered_carrier_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDeliveredCarrierDate;

    @Column(name = "order_delivered_customer_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDeliveredCustomerDate;

    // Staff accounts
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private StaffAccount createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @JsonIgnore
    private StaffAccount updatedBy;

    // Coupon information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    @JsonIgnore
    private Coupon coupon;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // Address and contact information
    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    // Payment information
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_status", length = 20, nullable = false)
    private String paymentStatus = "PENDING";

    // Additional tracking fields
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "note", length = 1000)
    private String note;

    // Final price
    @Column(name = "final_price")
    private BigDecimal finalPrice;

    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    @JsonIgnore
    private Payment payment;

    // Helper methods
    public BigDecimal getFinalPrice() {
        if (finalPrice != null) {
            return finalPrice;
        }
        return totalPrice.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }

    public void setFinalPrice() {
        this.finalPrice = totalPrice.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }

    // Utility methods for checking order state
    public boolean isPending() {
        return orderStatus != null && "Pending".equalsIgnoreCase(orderStatus.getStatusName());
    }

    public boolean isApproved() {
        return orderStatus != null && "Approved".equalsIgnoreCase(orderStatus.getStatusName());
    }

    public boolean isShipped() {
        return orderStatus != null && "Shipped".equalsIgnoreCase(orderStatus.getStatusName());
    }

    public boolean isDelivered() {
        return orderStatus != null && "Delivered".equalsIgnoreCase(orderStatus.getStatusName());
    }

    public boolean isCancelled() {
        return orderStatus != null && "Cancelled".equalsIgnoreCase(orderStatus.getStatusName());
    }

    public boolean canBeApproved() {
        return isPending() && orderApprovedAt == null;
    }

    public boolean canBeShipped() {
        return isApproved() && orderApprovedAt != null && orderDeliveredCarrierDate == null;
    }

    public boolean canBeCancelled() {
        return orderDeliveredCustomerDate == null && !isCancelled();
    }
}