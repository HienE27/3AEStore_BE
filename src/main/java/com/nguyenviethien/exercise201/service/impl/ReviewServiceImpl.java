package com.nguyenviethien.exercise201.service.impl;

import com.nguyenviethien.exercise201.entity.*;
import com.nguyenviethien.exercise201.repository.*;
import com.nguyenviethien.exercise201.service.ReviewService;
import com.nguyenviethien.exercise201.service.ReviewImageService;
import com.nguyenviethien.exercise201.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final ReviewImageService reviewImageService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public Review createReview(Review review, List<MultipartFile> images) throws Exception {
        System.out.println("=== [DEBUG] ReviewServiceImpl.createReview START ===");
        System.out.println("[DEBUG] ratingPoint: " + review.getRatingPoint());
        System.out.println("[DEBUG] customerId: " + (review.getCustomer() != null ? review.getCustomer().getId() : "NULL"));
        System.out.println("[DEBUG] productId: " + (review.getProduct() != null ? review.getProduct().getId() : "NULL"));
        System.out.println("[DEBUG] orderItemId: " + (review.getOrderItem() != null ? review.getOrderItem().getId() : "NULL"));
        
        // Validate rating
        if (review.getRatingPoint() == null || review.getRatingPoint() < 1 || review.getRatingPoint() > 5) {
            System.out.println("[DEBUG] ERROR: Invalid rating point");
            throw new IllegalArgumentException("Rating point must be between 1 and 5");
        }

        // Validate required relations
        if (review.getCustomer() == null) {
            System.out.println("[DEBUG] ERROR: Customer is null");
            throw new IllegalArgumentException("Customer is required");
        }
        if (review.getProduct() == null) {
            System.out.println("[DEBUG] ERROR: Product is null");
            throw new IllegalArgumentException("Product is required");
        }
        if (review.getOrderItem() == null) {
            System.out.println("[DEBUG] ERROR: OrderItem is null");
            throw new IllegalArgumentException("Order item is required to submit a review");
        }

        // 1. Verify order item and status
        System.out.println("[DEBUG] Checking order item and status...");
        OrderItem orderItem = review.getOrderItem();
        
        try {
            System.out.println("[DEBUG] orderItem.getOrder() = " + (orderItem.getOrder() != null ? "NOT NULL" : "NULL"));
            
            if (orderItem.getOrder() == null) {
                System.out.println("[DEBUG] ERROR: orderItem.getOrder() is NULL");
                throw new IllegalArgumentException("Invalid order item: order is null");
            }
            
            System.out.println("[DEBUG] orderId: " + orderItem.getOrder().getId());
            System.out.println("[DEBUG] orderItem.getOrder().getOrderStatus() = " + (orderItem.getOrder().getOrderStatus() != null ? "NOT NULL" : "NULL"));
            
            if (orderItem.getOrder().getOrderStatus() == null) {
                System.out.println("[DEBUG] ERROR: orderItem.getOrder().getOrderStatus() is NULL");
                throw new IllegalArgumentException("Invalid order item status: order status is null");
            }
            
            String statusName = orderItem.getOrder().getOrderStatus().getStatusName();
            System.out.println("[DEBUG] Order status name: " + statusName);
            
            boolean isDelivered = "Delivered".equalsIgnoreCase(statusName) || "Completed".equalsIgnoreCase(statusName);
            System.out.println("[DEBUG] isDelivered: " + isDelivered);
            
            if (!isDelivered) {
                System.out.println("[DEBUG] ERROR: Order not delivered/completed");
                throw new IllegalArgumentException("You can only review products from delivered orders. Current status: " + statusName);
            }
        } catch (NullPointerException e) {
            System.out.println("[DEBUG] NULLPOINTER EXCEPTION at order status check: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // 2. Verify customer owns this order
        System.out.println("[DEBUG] Checking customer ownership...");
        try {
            if (orderItem.getOrder().getCustomer() == null) {
                System.out.println("[DEBUG] ERROR: orderItem.getOrder().getCustomer() is NULL");
                throw new IllegalArgumentException("Invalid order: customer is null");
            }
            
            UUID orderCustomerId = orderItem.getOrder().getCustomer().getId();
            UUID reviewCustomerId = review.getCustomer().getId();
            System.out.println("[DEBUG] orderCustomerId: " + orderCustomerId);
            System.out.println("[DEBUG] reviewCustomerId: " + reviewCustomerId);
            System.out.println("[DEBUG] customer match: " + orderCustomerId.equals(reviewCustomerId));
            
            if (!orderCustomerId.equals(reviewCustomerId)) {
                System.out.println("[DEBUG] ERROR: Customer doesn't own this order");
                throw new IllegalArgumentException("You can only review products from your own orders");
            }
        } catch (NullPointerException e) {
            System.out.println("[DEBUG] NULLPOINTER EXCEPTION at customer check: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // 3. Check if already reviewed this order item (NOT by product - allows re-review on new purchase)
        System.out.println("[DEBUG] Checking for existing review by order item...");
        Review existingByOrderItem = reviewRepository.findByOrderItem(orderItem);
        System.out.println("[DEBUG] existingByOrderItem: " + (existingByOrderItem != null ? existingByOrderItem.getId() : "NULL"));
        if (existingByOrderItem != null) {
            System.out.println("[DEBUG] ERROR: Already reviewed this order item");
            throw new IllegalArgumentException("You have already reviewed this product for this order");
        }
        
        // NOTE: No product-level check - allows re-reviewing same product on new purchase

        // Set review properties
        System.out.println("[DEBUG] Setting review properties...");
        try {
            review.setIsVerifiedPurchase(true);
            review.setStatus(ReviewStatus.PENDING);
            review.setVisible(true);
            review.setCreatedAt(Timestamp.from(Instant.now()));
            review.setUpdatedAt(review.getCreatedAt());
            
            if (review.getRatingPoint() != null) {
                review.setRating(Math.round(review.getRatingPoint()));
                System.out.println("[DEBUG] Set rating: " + review.getRating());
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] EXCEPTION when setting properties: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Save review
        System.out.println("[DEBUG] About to save review...");
        try {
            Review savedReview = reviewRepository.save(review);
            System.out.println("[DEBUG] Review saved successfully! ID: " + savedReview.getId());
            
            // Send email notification to admin
            try {
                emailService.sendReviewNotification(savedReview);
            } catch (Exception emailEx) {
                System.err.println("[WARN] Failed to send review notification email: " + emailEx.getMessage());
            }
            
            // Upload images if any
            if (images != null && !images.isEmpty()) {
                System.out.println("[DEBUG] Uploading " + images.size() + " images...");
                reviewImageService.uploadImages(savedReview, images);
                System.out.println("[DEBUG] Images uploaded successfully");
            }
            
            System.out.println("[DEBUG] === ReviewServiceImpl.createReview END ===");
            return savedReview;
        } catch (Exception e) {
            System.out.println("[DEBUG] EXCEPTION during save: " + e.getClass().getName());
            System.out.println("[DEBUG] Exception message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Review updateReview(UUID reviewId, Review reviewData, List<MultipartFile> newImages) throws Exception {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Check if review can be edited (not locked)
        if (existingReview.getStatus() == ReviewStatus.LOCKED) {
            throw new IllegalArgumentException("This review is locked and cannot be edited");
        }

        // Update content
        if (reviewData.getContent() != null) {
            // Sanitize content to prevent XSS
            String sanitized = reviewData.getContent()
                    .replaceAll("<[^>]*>", "")
                    .trim();
            existingReview.setContent(sanitized.isEmpty() ? null : sanitized);
        }

        // Update rating
        if (reviewData.getRatingPoint() != null) {
            if (reviewData.getRatingPoint() < 1 || reviewData.getRatingPoint() > 5) {
                throw new IllegalArgumentException("Rating point must be between 1 and 5");
            }
            existingReview.setRatingPoint(reviewData.getRatingPoint());
            existingReview.setRating(Math.round(reviewData.getRatingPoint()));
        }

        // Update anonymous flag
        if (reviewData.getIsAnonymous() != null) {
            existingReview.setIsAnonymous(reviewData.getIsAnonymous());
        }

        // Upload new images if any
        if (newImages != null && !newImages.isEmpty()) {
            reviewImageService.uploadImages(existingReview, newImages);
        }

        // Reset to PENDING after edit (requires re-approval)
        existingReview.setStatus(ReviewStatus.PENDING);
        existingReview.setUpdatedAt(Timestamp.from(Instant.now()));

        return reviewRepository.save(existingReview);
    }

    @Override
    public void deleteReview(UUID reviewId) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Soft delete - hide instead of hard delete
        review.setStatus(ReviewStatus.HIDDEN);
        review.setVisible(false);
        review.setUpdatedAt(Timestamp.from(Instant.now()));
        reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Review getReviewById(UUID reviewId) {
        return reviewRepository.findById(reviewId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getApprovedReviewsByProduct(UUID productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return List.of();
        return reviewRepository.findByProductAndStatusOrderByCreatedAtDesc(product, ReviewStatus.APPROVED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByProduct(UUID productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return List.of();
        return reviewRepository.findByProductOrderByCreatedAtDesc(product);
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
    public Map<String, Object> checkEligibleForReview(UUID customerId, UUID productId) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        Customer customer = customerRepository.findById(customerId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);
        
        result.put("eligible", false);
        result.put("reason", "");
        result.put("orderItemId", null);
        result.put("hasReviewed", false);

        if (customer == null || product == null) {
            result.put("reason", "Invalid customer or product");
            return result;
        }

        // NOTE: We no longer check by product - allows re-reviewing on new purchases
        // Only check for verified purchase (completed order with this product)
        List<OrderItem> eligibleItems = getEligibleOrderItems(customer.getId(), product.getId());
        if (eligibleItems.isEmpty()) {
            result.put("reason", "You need to purchase this product before reviewing");
            return result;
        }

        // Get the most recent eligible order item
        OrderItem latestItem = eligibleItems.get(0);
        result.put("eligible", true);
        result.put("orderItemId", latestItem.getId().toString());
        result.put("reason", "You can review this product");
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(UUID productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return 0.0;
        Double avg = reviewRepository.findAverageRatingByProduct(product);
        return avg != null ? Math.round(avg * 10) / 10.0 : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countApprovedReviews(UUID productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return 0L;
        return reviewRepository.countByProductAndStatusAndVisibleTrue(product, ReviewStatus.APPROVED);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Long> getStarDistribution(UUID productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return new LinkedHashMap<>();

        List<Object[]> results = reviewRepository.countByRatingGroupByProduct(product);
        Map<Integer, Long> distribution = new LinkedHashMap<>();
        
        // Initialize all stars with 0
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        
        // Fill actual values
        for (Object[] row : results) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            if (rating != null && rating >= 1 && rating <= 5) {
                distribution.put(rating, count);
            }
        }
        
        return distribution;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getReviewSummary(UUID productId) {
        Map<String, Object> summary = new LinkedHashMap<>();
        
        Double averageRating = getAverageRating(productId);
        Long totalReviews = countApprovedReviews(productId);
        Map<Integer, Long> distribution = getStarDistribution(productId);
        
        summary.put("averageRating", averageRating);
        summary.put("totalReviews", totalReviews);
        summary.put("starDistribution", distribution);
        summary.put("percentage5Star", calculatePercentage(distribution.get(5), totalReviews));
        summary.put("percentage4Star", calculatePercentage(distribution.get(4), totalReviews));
        summary.put("percentage3Star", calculatePercentage(distribution.get(3), totalReviews));
        summary.put("percentage2Star", calculatePercentage(distribution.get(2), totalReviews));
        summary.put("percentage1Star", calculatePercentage(distribution.get(1), totalReviews));
        
        return summary;
    }

    @Override
    public ReviewReport reportReview(UUID reviewId, UUID reporterId, ReviewReport.ReportReason reason, String note) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        Customer reporter = customerRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));
        
        // Check if already reported
        Optional<ReviewReport> existing = reviewReportRepository.findByReviewIdAndReporterId(reviewId, reporterId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("You have already reported this review");
        }
        
        ReviewReport report = new ReviewReport();
        report.setReview(review);
        report.setReporter(reporter);
        report.setReason(reason);
        report.setNote(note);
        report.setStatus(ReviewReport.ReportStatus.PENDING);
        
        ReviewReport saved = reviewReportRepository.save(report);
        
        // Increment report count
        incrementReportCount(reviewId);
        
        return saved;
    }

    @Override
    public Review updateReviewStatus(UUID reviewId, ReviewStatus status, String note, String moderator) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        review.setStatus(status);
        review.setModeratedBy(moderator);
        review.setModeratedAt(new Timestamp(System.currentTimeMillis()));
        review.setModerationNote(note);
        
        // Set visible based on status
        review.setVisible(status == ReviewStatus.APPROVED);
        
        return reviewRepository.save(review);
    }

    @Override
    public void replyToReview(UUID reviewId, String content, String authorName, String authorRole) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        ReviewReply reply = new ReviewReply();
        reply.setReview(review);
        reply.setContent(content);
        reply.setAuthorName(authorName);
        reply.setAuthorRole(authorRole);
        reply.setAuthorId(authorName);
        reply.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Save through entity relationship
        review.getReplies().add(reply);
        reviewRepository.save(review);
    }

    @Override
    public void incrementReportCount(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review != null) {
            review.setReportCount(review.getReportCount() + 1);
            reviewRepository.save(review);
        }
    }

    @Override
    public void incrementHelpfulCount(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review != null) {
            review.setHelpfulCount(review.getHelpfulCount() + 1);
            reviewRepository.save(review);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByStatus(ReviewStatus status) {
        return reviewRepository.findByStatus(status, org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getMostReportedReviews(int limit) {
        return reviewRepository.findMostReportedReviews(
            org.springframework.data.domain.PageRequest.of(0, limit)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // Private helper method
    private List<OrderItem> getEligibleOrderItems(UUID customerId, UUID productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return List.of();
        
        List<OrderItem> items = orderItemRepository.findByProduct(product);
        
        return items.stream()
                .filter(item -> item.getOrder() != null)
                .filter(item -> item.getOrder().getOrderStatus() != null)
                .filter(item -> {
                    String statusName = item.getOrder().getOrderStatus().getStatusName();
                    return "Delivered".equalsIgnoreCase(statusName) || 
                           "Completed".equalsIgnoreCase(statusName);
                })
                .filter(item -> item.getOrder().getCustomer() != null)
                .filter(item -> item.getOrder().getCustomer().getId().equals(customerId))
                .sorted((a, b) -> b.getOrder().getCreated_at().compareTo(a.getOrder().getCreated_at()))
                .collect(Collectors.toList());
    }

    private String calculatePercentage(Long count, Long total) {
        if (total == 0 || count == 0) return "0%";
        return String.format("%.1f%%", (count * 100.0 / total));
    }
}
