package com.nguyenviethien.exercise201.service.impl;

import com.nguyenviethien.exercise201.entity.*;
import com.nguyenviethien.exercise201.repository.*;
import com.nguyenviethien.exercise201.service.ReviewService;
import com.nguyenviethien.exercise201.service.ReviewImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageService reviewImageService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public Review createReview(Review review, List<MultipartFile> images) throws Exception {
        // Validate dữ liệu
        if (review.getRatingPoint() == null || review.getRatingPoint() < 1 || review.getRatingPoint() > 5) {
            throw new IllegalArgumentException("Rating point must be between 1 and 5");
        }

        // Validate required relations
        if (review.getCustomer() == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (review.getProduct() == null) {
            throw new IllegalArgumentException("Product is required");
        }

        // Kiểm tra customer đã review sản phẩm này trong order item này chưa (nếu orderItem được cung cấp)
        if (review.getOrderItem() != null) {
            try {
                if (hasCustomerReviewedProduct(review.getCustomer().getId(), review.getProduct().getId(), review.getOrderItem().getId())) {
                    throw new IllegalArgumentException("Customer has already reviewed this product in this order");
                }
            } catch (Exception ex) {
                // ignore check failures, proceed to save (do not block user for unexpected reasons)
            }
        }

        // Set timestamp
        review.setCreatedAt(Timestamp.from(Instant.now()));
        // Set legacy integer rating (DB constraint requires `rating` not null)
        if (review.getRatingPoint() != null) {
            review.setRating(Integer.valueOf(Math.round(review.getRatingPoint())));
        } else {
            review.setRating(0);
        }

        // Lưu review
        Review savedReview = reviewRepository.save(review);

        // Upload ảnh nếu có
        if (images != null && !images.isEmpty()) {
            reviewImageService.uploadImages(savedReview, images);
        }

        // Cập nhật điểm đánh giá trung bình của sản phẩm
        if (review.getProduct() != null && review.getProduct().getId() != null) {
            updateProductAverageRating(review.getProduct().getId());
        }

        return savedReview;
    }

    @Override
    public Review updateReview(UUID reviewId, Review reviewData, List<MultipartFile> newImages) throws Exception {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Cập nhật dữ liệu
        if (reviewData.getContent() != null) {
            existingReview.setContent(reviewData.getContent());
        }
        if (reviewData.getRatingPoint() != null) {
            if (reviewData.getRatingPoint() < 1 || reviewData.getRatingPoint() > 5) {
                throw new IllegalArgumentException("Rating point must be between 1 and 5");
            }
            existingReview.setRatingPoint(reviewData.getRatingPoint());
            // Update legacy integer rating as well
            existingReview.setRating(Integer.valueOf(Math.round(reviewData.getRatingPoint())));
        }

        // Upload ảnh mới nếu có
        if (newImages != null && !newImages.isEmpty()) {
            reviewImageService.uploadImages(existingReview, newImages);
        }

        Review updatedReview = reviewRepository.save(existingReview);

        // Cập nhật điểm đánh giá trung bình của sản phẩm
        updateProductAverageRating(existingReview.getProduct().getId());

        return updatedReview;
    }

    @Override
    public void deleteReview(UUID reviewId) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        UUID productId = review.getProduct().getId();

        // Xóa review (cascade sẽ xóa luôn ảnh)
        reviewRepository.delete(review);

        // Cập nhật điểm đánh giá trung bình của sản phẩm
        updateProductAverageRating(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Review getReviewById(UUID reviewId) {
        return reviewRepository.findById(reviewId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByProduct(UUID productId) {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_13\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewServiceImpl.java:132\",\"message\":\"getReviewsByProduct entry\",\"data\":{\"productId\":\"" + productId + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
            fw.close();
        } catch (Exception ex) {}
        // #endregion
        Product product = productRepository.findById(productId).orElse(null);
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_14\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewServiceImpl.java:134\",\"message\":\"Product lookup\",\"data\":{\"productFound\":" + (product != null) + "},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
            fw.close();
        } catch (Exception ex) {}
        // #endregion
        if (product == null) return List.of();

        List<Review> reviews = reviewRepository.findByProductOrderByCreatedAtDesc(product);
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_15\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewServiceImpl.java:137\",\"message\":\"Reviews fetched from DB\",\"data\":{\"reviewCount\":\"" + (reviews != null ? reviews.size() : 0) + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
            fw.close();
        } catch (Exception ex) {}
        // #endregion
        return reviews;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) return List.of();

        return reviewRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Review getReviewByOrderItem(UUID orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElse(null);
        if (orderItem == null) return null;

        return reviewRepository.findByOrderItem(orderItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(UUID productId) {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_16\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewServiceImpl.java:159\",\"message\":\"getAverageRating entry\",\"data\":{\"productId\":\"" + productId + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"F\"}\n");
            fw.close();
        } catch (Exception ex) {}
        // #endregion
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return 0.0;

        Double avg = reviewRepository.findAverageRatingByProduct(product);
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_17\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewServiceImpl.java:164\",\"message\":\"Average rating calculated\",\"data\":{\"averageRating\":\"" + (avg != null ? avg : "null") + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"F\"}\n");
            fw.close();
        } catch (Exception ex) {}
        // #endregion
        return avg != null ? avg : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countReviews(UUID productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return 0L;

        return reviewRepository.countByProduct(product);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasCustomerReviewedProduct(UUID customerId, UUID productId, UUID orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElse(null);
        if (orderItem == null) return false;

        Review existingReview = reviewRepository.findByOrderItem(orderItem);
        return existingReview != null;
    }

    private void updateProductAverageRating(UUID productId) {
        // Có thể implement logic cập nhật average rating trong Product entity
        // Hoặc có thể để trigger tự động khi cần
    }
}
