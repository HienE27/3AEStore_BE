package com.nguyenviethien.exercise201.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // Nội dung đánh giá

    @Column(name = "rating_point", nullable = false)
    private Float ratingPoint; // Điểm xếp hạng (1-5)

    @Column(name = "rating", nullable = false)
    private Integer rating; // Legacy integer rating (1-5) kept for DB constraint compatibility

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt; // Thời gian tạo review

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Sản phẩm được đánh giá

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer; // Người đánh giá

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = true)
    private OrderItem orderItem; // Item trong đơn hàng

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewImage> images; // Ảnh của review
}
