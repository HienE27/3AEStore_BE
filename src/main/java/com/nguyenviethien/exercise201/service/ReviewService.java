package com.nguyenviethien.exercise201.service;

import com.nguyenviethien.exercise201.entity.Review;
import com.nguyenviethien.exercise201.entity.ReviewReport;
import com.nguyenviethien.exercise201.entity.ReviewStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReviewService {

    // Create review with validation
    Review createReview(Review review, List<MultipartFile> images) throws Exception;

    // Update review
    Review updateReview(UUID reviewId, Review reviewData, List<MultipartFile> newImages) throws Exception;

    // Soft delete review
    void deleteReview(UUID reviewId) throws Exception;

    // Get review by ID
    Review getReviewById(UUID reviewId);

    // Get reviews by product (approved only for public)
    List<Review> getApprovedReviewsByProduct(UUID productId);

    // Get all reviews by product (admin)
    List<Review> getReviewsByProduct(UUID productId);

    // Get reviews by customer
    List<Review> getReviewsByCustomer(UUID customerId);

    // Get review by order item
    Review getReviewByOrderItem(UUID orderItemId);

    // Check if user can review (verified purchase)
    Map<String, Object> checkEligibleForReview(UUID customerId, UUID productId);

    // Calculate average rating
    Double getAverageRating(UUID productId);

    // Count approved reviews
    Long countApprovedReviews(UUID productId);

    // Get star distribution
    Map<Integer, Long> getStarDistribution(UUID productId);

    // Get review summary
    Map<String, Object> getReviewSummary(UUID productId);

    // Report review
    ReviewReport reportReview(UUID reviewId, UUID reporterId, ReviewReport.ReportReason reason, String note) throws Exception;

    // Update review status (admin)
    Review updateReviewStatus(UUID reviewId, ReviewStatus status, String note, String moderator) throws Exception;

    // Reply to review (admin)
    void replyToReview(UUID reviewId, String content, String authorName, String authorRole) throws Exception;

    // Increment report count
    void incrementReportCount(UUID reviewId);

    // Increment helpful count
    void incrementHelpfulCount(UUID reviewId);

    // Get reviews by status (admin)
    List<Review> getReviewsByStatus(ReviewStatus status);

    // Get most reported reviews
    List<Review> getMostReportedReviews(int limit);

    // Get ALL reviews (admin) - FIX for empty filters
    List<Review> getAllReviews();
}
