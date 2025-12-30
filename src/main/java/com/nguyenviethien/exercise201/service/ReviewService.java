package com.nguyenviethien.exercise201.service;

import com.nguyenviethien.exercise201.entity.Review;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.OrderItem;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    // Tạo review mới với ảnh
    Review createReview(Review review, List<MultipartFile> images) throws Exception;

    // Cập nhật review
    Review updateReview(UUID reviewId, Review reviewData, List<MultipartFile> newImages) throws Exception;

    // Xóa review
    void deleteReview(UUID reviewId) throws Exception;

    // Lấy review theo ID
    Review getReviewById(UUID reviewId);

    // Lấy tất cả review của một sản phẩm
    List<Review> getReviewsByProduct(UUID productId);

    // Lấy tất cả review của một khách hàng
    List<Review> getReviewsByCustomer(UUID customerId);

    // Lấy review theo order item
    Review getReviewByOrderItem(UUID orderItemId);

    // Tính điểm đánh giá trung bình của sản phẩm
    Double getAverageRating(UUID productId);

    // Đếm số lượng review của sản phẩm
    Long countReviews(UUID productId);

    // Kiểm tra khách hàng đã review sản phẩm trong đơn hàng này chưa
    boolean hasCustomerReviewedProduct(UUID customerId, UUID productId, UUID orderItemId);
}
