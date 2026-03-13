package com.nguyenviethien.exercise201.repository;

import com.nguyenviethien.exercise201.entity.OrderItem;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.entity.Review;
import com.nguyenviethien.exercise201.entity.ReviewStatus;
import com.nguyenviethien.exercise201.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Tìm review theo order item
    Review findByOrderItem(OrderItem orderItem);

    // Tìm review theo order item và customer
    Optional<Review> findByOrderItemAndCustomer(OrderItem orderItem, Customer customer);

    // Tìm tất cả review của một sản phẩm
    List<Review> findByProductOrderByCreatedAtDesc(Product product);

    // Tìm review đã duyệt của một sản phẩm
    List<Review> findByProductAndStatusOrderByCreatedAtDesc(Product product, ReviewStatus status);

    // Tìm tất cả review của một khách hàng
    List<Review> findByCustomerOrderByCreatedAtDesc(Customer customer);

    // Tính điểm đánh giá trung bình của sản phẩm (chỉ APPROVED)
    @Query("SELECT AVG(r.ratingPoint) FROM Review r WHERE r.product = :product AND r.status = 'APPROVED' AND r.visible = true")
    Double findAverageRatingByProduct(@Param("product") Product product);

    // Đếm số lượng review của sản phẩm (chỉ APPROVED)
    Long countByProductAndStatusAndVisibleTrue(Product product, ReviewStatus status);

    // Tìm review có hình ảnh
    @Query("SELECT r FROM Review r WHERE r.product = :product AND SIZE(r.images) > 0")
    List<Review> findReviewsWithImagesByProduct(@Param("product") Product product);

    // Tìm reviews theo status với phân trang (admin)
    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    // Tìm reviews theo product và status với phân trang (admin)
    Page<Review> findByProductAndStatus(Product product, ReviewStatus status, Pageable pageable);

    // Tìm reviews theo customer và status (user's reviews)
    List<Review> findByCustomerAndStatus(Customer customer, ReviewStatus status);

    // Đếm report count
    @Query("SELECT r.reportCount FROM Review r WHERE r.id = :reviewId")
    Integer getReportCountById(@Param("reviewId") UUID reviewId);

    // Tìm reviews đã báo cáo nhiều
    @Query("SELECT r FROM Review r WHERE r.reportCount > 0 ORDER BY r.reportCount DESC")
    List<Review> findMostReportedReviews(Pageable pageable);

    // Thống kê theo status
    @Query("SELECT r.status, COUNT(r) FROM Review r GROUP BY r.status")
    List<Object[]> countByStatusGroup();

    // Thống kê theo rating
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.status = 'APPROVED' GROUP BY r.rating")
    List<Object[]> countByRatingGroup();

    // Thống kê theo rating cho một sản phẩm cụ thể
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product = :product AND r.status = 'APPROVED' AND r.visible = true GROUP BY r.rating")
    List<Object[]> countByRatingGroupByProduct(@Param("product") Product product);

    // Tìm review theo customer và product
    Optional<Review> findByCustomerAndProduct(Customer customer, Product product);

    // Kiểm tra user đã review sản phẩm chưa
    boolean existsByCustomerAndProduct(Customer customer, Product product);
}
