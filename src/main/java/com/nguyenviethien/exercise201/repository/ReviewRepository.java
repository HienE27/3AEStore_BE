package com.nguyenviethien.exercise201.repository;

import com.nguyenviethien.exercise201.entity.OrderItem;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.entity.Review;
import com.nguyenviethien.exercise201.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Tìm review theo order item
    Review findByOrderItem(OrderItem orderItem);

    // Tìm tất cả review của một sản phẩm
    List<Review> findByProductOrderByCreatedAtDesc(Product product);

    // Tìm tất cả review của một khách hàng
    List<Review> findByCustomerOrderByCreatedAtDesc(Customer customer);

    // Tính điểm đánh giá trung bình của sản phẩm
    @Query("SELECT AVG(r.ratingPoint) FROM Review r WHERE r.product = :product")
    Double findAverageRatingByProduct(@Param("product") Product product);

    // Đếm số lượng review của sản phẩm
    Long countByProduct(Product product);

    // Tìm review có hình ảnh
    @Query("SELECT r FROM Review r WHERE r.product = :product AND SIZE(r.images) > 0")
    List<Review> findReviewsWithImagesByProduct(@Param("product") Product product);
}
