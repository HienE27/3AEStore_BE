package com.nguyenviethien.exercise201.controller;

import com.nguyenviethien.exercise201.DTO.*;
import com.nguyenviethien.exercise201.entity.*;
import com.nguyenviethien.exercise201.repository.*;
import com.nguyenviethien.exercise201.service.ReviewService;
import com.nguyenviethien.exercise201.service.ReviewImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewImageService reviewImageService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;

    // ===== PUBLIC ENDPOINTS =====

    // Get reviews for a product (approved only)
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductReviews(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) Integer star,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Review> reviews = reviewService.getApprovedReviewsByProduct(productId);
            
            // Filter by star if specified
            if (star != null && star >= 1 && star <= 5) {
                reviews = reviews.stream()
                        .filter(r -> r.getRating() != null && r.getRating() == star)
                        .toList();
            }
            
            // Sort
            reviews = switch (sort) {
                case "highest" -> reviews.stream()
                        .sorted((a, b) -> Integer.compare(b.getRating(), a.getRating()))
                        .toList();
                case "lowest" -> reviews.stream()
                        .sorted((a, b) -> Integer.compare(a.getRating(), b.getRating()))
                        .toList();
                default -> reviews; // latest
            };
            
            // Get summary
            Map<String, Object> summary = reviewService.getReviewSummary(productId);
            
            List<ReviewDto> dtoList = reviews.stream()
                    .filter(r -> r.getVisible() == null || r.getVisible())
                    .map(this::toDto)
                    .toList();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reviews", dtoList,
                    "total", dtoList.size(),
                    "summary", summary
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to get reviews: " + e.getMessage()
            ));
        }
    }

    // Get review summary for a product
    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<?> getProductReviewSummary(@PathVariable UUID productId) {
        try {
            Map<String, Object> summary = reviewService.getReviewSummary(productId);
            return ResponseEntity.ok(Map.of("success", true, "data", summary));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to get review summary: " + e.getMessage()
            ));
        }
    }

    // Get review stats for a product (for frontend compatibility)
    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<?> getProductReviewStats(@PathVariable UUID productId) {
        try {
            Map<String, Object> summary = reviewService.getReviewSummary(productId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "averageRating", summary.getOrDefault("averageRating", 0),
                    "totalReviews", summary.getOrDefault("totalReviews", 0),
                    "starDistribution", summary.getOrDefault("starDistribution", Map.of(1, 0, 2, 0, 3, 0, 4, 0, 5, 0))
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to get review stats: " + e.getMessage(),
                    "averageRating", 0,
                    "totalReviews", 0,
                    "starDistribution", Map.of(1, 0, 2, 0, 3, 0, 4, 0, 5, 0)
            ));
        }
    }

    // Get review stats for multiple products (batch)
    @GetMapping("/products/stats")
    public ResponseEntity<?> getProductsReviewStats(@RequestParam List<UUID> productIds) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            
            Map<String, Object> stats = new LinkedHashMap<>();
            for (UUID productId : productIds) {
                Map<String, Object> summary = reviewService.getReviewSummary(productId);
                stats.put(productId.toString(), Map.of(
                        "averageRating", summary.getOrDefault("averageRating", 0),
                        "totalReviews", summary.getOrDefault("totalReviews", 0)
                ));
            }
            result.put("stats", stats);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to get review stats: " + e.getMessage()
            ));
        }
    }

    // Get reviews for a product (simple list format for backward compatibility)
    // TEMP FIX: Show all non-rejected reviews for testing (remove APPROVED filter)
    @GetMapping("/product/{productId}/list")
    public ResponseEntity<?> getProductReviewsSimple(@PathVariable UUID productId) {
        try {
            // Show all reviews except REJECTED for testing
            List<Review> allReviews = reviewService.getReviewsByProduct(productId);
            List<Review> reviews = allReviews.stream()
                    .filter(r -> r.getStatus() != ReviewStatus.REJECTED)
                    .filter(r -> r.getVisible() == null || r.getVisible())
                    .toList();
            
            // Convert to simple map format
            var simple = reviews.stream()
                    .filter(r -> r.getVisible() == null || r.getVisible())
                    .map(r -> {
                var m = new LinkedHashMap<String, Object>();
                m.put("id", r.getId());
                m.put("content", r.getContent());
                m.put("ratingPoint", r.getRatingPoint());
                m.put("createdAt", r.getCreatedAt());
                // customer minimal
                if (r.getCustomer() != null && !r.getIsAnonymous()) {
                    var c = new LinkedHashMap<String, Object>();
                    c.put("id", r.getCustomer().getId());
                    c.put("first_name", r.getCustomer().getFirst_name());
                    c.put("last_name", r.getCustomer().getLast_name());
                    m.put("customer", c);
                } else {
                    m.put("customer", null);
                }
                // images as URLs only
                if (r.getImages() != null && !r.getImages().isEmpty()) {
                    var imgs = r.getImages().stream()
                            .map(img -> img.getImageUrl())
                            .toList();
                    m.put("images", imgs);
                } else {
                    m.put("images", java.util.List.of());
                }
                // replies
                if (r.getReplies() != null && !r.getReplies().isEmpty()) {
                    var replyList = r.getReplies().stream()
                            .map(reply -> {
                                var replyMap = new LinkedHashMap<String, Object>();
                                replyMap.put("id", reply.getId());
                                replyMap.put("content", reply.getContent());
                                replyMap.put("authorName", reply.getAuthorName());
                                replyMap.put("authorRole", reply.getAuthorRole());
                                replyMap.put("createdAt", reply.getCreatedAt());
                                return replyMap;
                            })
                            .toList();
                    m.put("replies", replyList);
                } else {
                    m.put("replies", java.util.List.of());
                }
                return m;
            }).toList();

            return ResponseEntity.ok(Map.of("success", true, "reviews", simple));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to get reviews (simple): " + e.getMessage()
            ));
        }
    }

    // Check if user can review a product
    @GetMapping("/eligible")
    public ResponseEntity<?> checkEligible(
            @RequestParam String customerId,
            @RequestParam String productId) {
        try {
            UUID customerUuid = UUID.fromString(customerId);
            UUID productUuid = UUID.fromString(productId);
            Map<String, Object> result = reviewService.checkEligibleForReview(customerUuid, productUuid);
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to check eligibility: " + e.getMessage()
            ));
        }
    }

    // ===== USER AUTHENTICATED ENDPOINTS =====

    // Create a new review (from order page)
    @PostMapping("/from-order")
    public ResponseEntity<?> createReviewFromOrder(
            @RequestParam String customerId,
            @RequestParam String orderItemId,
            @RequestParam String ratingPoint,
            @RequestParam(required = false) String content,
            @RequestParam(required = false, defaultValue = "false") Boolean isAnonymous,
            @RequestParam(required = false) List<MultipartFile> images) {
        try {
            // Validate customer
            Customer customer = customerRepository.findById(UUID.fromString(customerId))
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            
            // Validate order item
            OrderItem orderItem = orderItemRepository.findById(UUID.fromString(orderItemId))
                    .orElseThrow(() -> new IllegalArgumentException("Order item not found"));
            
            // Validate order belongs to customer
            if (orderItem.getOrder() == null || orderItem.getOrder().getCustomer() == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid order item"));
            }
            
            if (!orderItem.getOrder().getCustomer().getId().equals(customer.getId())) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "You can only review products from your own orders"));
            }
            
            // Validate order is delivered
            String statusName = orderItem.getOrder().getOrderStatus().getStatusName();
            boolean isDelivered = "Delivered".equalsIgnoreCase(statusName) || "Completed".equalsIgnoreCase(statusName);
            if (!isDelivered) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "You can only review products from delivered orders. Current status: " + statusName));
            }
            
            // Check if already reviewed this specific order item (NOT by product)
            Review existingReview = reviewRepository.findByOrderItem(orderItem);
            if (existingReview != null) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Bạn đã đánh giá sản phẩm này cho đơn hàng này"
                ));
            }
            
            Product product = orderItem.getProduct();
            
            Review review = new Review();
            review.setContent(content);
            review.setRatingPoint(Float.parseFloat(ratingPoint));
            review.setIsAnonymous(isAnonymous != null && isAnonymous);
            review.setIsVerifiedPurchase(true);
            review.setProduct(product);
            review.setCustomer(customer);
            review.setOrderItem(orderItem);
            
            Review saved = reviewService.createReview(review, images);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Review submitted successfully",
                    "review", toDto(saved)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to create review: " + e.getMessage()));
        }
    }

    // Original create review (for backward compatibility)
    @PostMapping
    public ResponseEntity<?> createReview(
            @RequestParam String customerId,
            @RequestParam String productId,
            @RequestParam(required = false) String orderItemId,
            @RequestParam String ratingPoint,
            @RequestParam(required = false) String content,
            @RequestParam(required = false, defaultValue = "false") Boolean isAnonymous,
            @RequestParam(required = false) List<MultipartFile> images) {
        try {
            Customer customer = customerRepository.findById(UUID.fromString(customerId))
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            
            Product product;
            try {
                product = productRepository.findById(UUID.fromString(productId)).orElse(null);
            } catch (IllegalArgumentException ex) {
                product = productRepository.findBySlug(productId).orElse(null);
            }
            if (product == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Product not found"));
            }
            
            OrderItem orderItem = null;
            if (orderItemId != null) {
                try {
                    orderItem = orderItemRepository.findById(UUID.fromString(orderItemId)).orElse(null);
                } catch (IllegalArgumentException ignored) {}
            }
            
            // Check if already reviewed (only for the same order item)
            if (orderItem != null) {
                Review existingReview = reviewRepository.findByOrderItem(orderItem);
                if (existingReview != null) {
                    return ResponseEntity.status(403).body(Map.of(
                            "success", false,
                            "message", "Bạn đã đánh giá sản phẩm này cho đơn hàng này"
                    ));
                }
            }
            
            // Check if already reviewed this product (from any order)
            Optional<Review> existingProductReview = reviewRepository.findByCustomerAndProduct(customer, product);
            if (existingProductReview.isPresent()) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Bạn đã đánh giá sản phẩm này"
                ));
            }
            
            // Verify purchase - check if customer has completed order with this product
            boolean isVerifiedPurchase = verifyPurchase(customer.getId(), product.getId());
            
            Review review = new Review();
            review.setContent(content);
            review.setRatingPoint(Float.parseFloat(ratingPoint));
            review.setIsAnonymous(isAnonymous != null && isAnonymous);
            review.setIsVerifiedPurchase(isVerifiedPurchase);
            review.setProduct(product);
            review.setCustomer(customer);
            review.setOrderItem(orderItem);
            
            Review saved = reviewService.createReview(review, images);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Review submitted successfully",
                    "review", toDto(saved)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to create review: " + e.getMessage()
            ));
        }
    }
    
    // Helper method to verify purchase
    private boolean verifyPurchase(UUID customerId, UUID productId) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) return false;
            
            List<OrderItem> items = orderItemRepository.findByProduct(product);
            
            for (OrderItem item : items) {
                if (item.getOrder() != null && 
                    item.getOrder().getOrderStatus() != null &&
                    item.getOrder().getCustomer() != null &&
                    item.getOrder().getCustomer().getId().equals(customerId)) {
                    // Check by statusName string
                    String statusName = item.getOrder().getOrderStatus().getStatusName();
                    if ("Delivered".equalsIgnoreCase(statusName) || 
                        "Completed".equalsIgnoreCase(statusName)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Update a review
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable UUID reviewId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String ratingPoint,
            @RequestParam(required = false) List<MultipartFile> newImages) {
        try {
            Review reviewData = new Review();
            if (content != null) reviewData.setContent(content);
            if (ratingPoint != null) reviewData.setRatingPoint(Float.parseFloat(ratingPoint));
            
            Review updated = reviewService.updateReview(reviewId, reviewData, newImages);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Review updated successfully",
                    "review", toDto(updated)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to update review: " + e.getMessage()
            ));
        }
    }

    // Delete a review (soft delete)
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable UUID reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Review deleted successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to delete review: " + e.getMessage()
            ));
        }
    }

    // Report a review
    @PostMapping("/{reviewId}/report")
    public ResponseEntity<?> reportReview(
            @PathVariable UUID reviewId,
            @RequestParam String reporterId,
            @RequestParam String reason,
            @RequestParam(required = false) String note) {
        try {
            ReviewReport.ReportReason reportReason = ReviewReport.ReportReason.valueOf(reason);
            ReviewReport report = reviewService.reportReview(
                    reviewId,
                    UUID.fromString(reporterId),
                    reportReason,
                    note
            );
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Report submitted successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to report review: " + e.getMessage()
            ));
        }
    }

    // Get user's reviews
    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews(@RequestParam String customerId) {
        try {
            List<Review> reviews = reviewService.getReviewsByCustomer(UUID.fromString(customerId));
            List<ReviewDto> dtoList = reviews.stream().map(this::toDto).toList();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reviews", dtoList,
                    "total", dtoList.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to get reviews: " + e.getMessage()
            ));
        }
    }

    // Get single review
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReview(@PathVariable UUID reviewId) {
        try {
            Review review = reviewService.getReviewById(reviewId);
            if (review == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Map.of("success", true, "review", toDto(review)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to get review: " + e.getMessage()
            ));
        }
    }

    // Get review by order item
    @GetMapping("/order-item/{orderItemId}")
    public ResponseEntity<?> getReviewByOrderItem(@PathVariable UUID orderItemId) {
        try {
            OrderItem orderItem = orderItemRepository.findById(orderItemId).orElse(null);
            if (orderItem == null) {
                return ResponseEntity.ok(Map.of("success", false, "message", "Order item not found"));
            }
            Review review = reviewRepository.findByOrderItem(orderItem);
            if (review == null) {
                return ResponseEntity.ok(Map.of("success", false, "message", "Review not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "review", toDto(review)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to get review: " + e.getMessage()
            ));
        }
    }

    // Delete review image
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteReviewImage(@PathVariable UUID imageId) {
        try {
            reviewImageService.deleteImage(imageId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Image deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to delete image: " + e.getMessage()
            ));
        }
    }

    // Helper method to convert Review to DTO
    private ReviewDto toDto(Review r) {
        CustomerDto c = null;
        if (r.getCustomer() != null && !r.getIsAnonymous()) {
            c = new CustomerDto(
                    r.getCustomer().getId(),
                    r.getCustomer().getFirst_name(),
                    r.getCustomer().getLast_name()
            );
        }
        
        List<ReviewImageDto> imgs = null;
        if (r.getImages() != null) {
            imgs = r.getImages().stream()
                    .map(img -> new ReviewImageDto(
                            img.getId(),
                            img.getImageUrl(),
                            img.getImageName(),
                            img.getImageSize(),
                            img.getSortOrder()
                    ))
                    .toList();
        }
        
        // Include replies
        List<ReviewReplyDto> replies = null;
        if (r.getReplies() != null) {
            replies = r.getReplies().stream()
                    .map(reply -> new ReviewReplyDto(
                            reply.getId(),
                            reply.getContent(),
                            reply.getAuthorName(),
                            reply.getAuthorRole(),
                            reply.getCreatedAt()
                    ))
                    .toList();
        }
        
        return new ReviewDto(
                r.getId(),
                r.getContent(),
                r.getRatingPoint(),
                r.getRating(),
                r.getCreatedAt(),
                null, // product
                c,
                imgs,
                r.getIsVerifiedPurchase(),
                r.getIsAnonymous(),
                r.getStatus().name(),
                replies
        );
    }

    // Mark review as helpful
    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<?> markHelpful(@PathVariable UUID reviewId) {
        try {
            reviewService.incrementHelpfulCount(reviewId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Review marked as helpful"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to mark helpful: " + e.getMessage()
            ));
        }
    }
}
