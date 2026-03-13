package com.nguyenviethien.exercise201.controller;

import com.nguyenviethien.exercise201.entity.*;
import com.nguyenviethien.exercise201.entity.ReviewStatus;
import com.nguyenviethien.exercise201.repository.OrderItemRepository;
import com.nguyenviethien.exercise201.repository.ReviewReportRepository;
import com.nguyenviethien.exercise201.repository.ReviewRepository;
import com.nguyenviethien.exercise201.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewReportRepository reportRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReviewService reviewService;

    // Get current moderator info
    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "system";
        } catch (Exception ex) {
            return "system";
        }
    }

    // ===== REVIEW MANAGEMENT =====

    // List reviews with filters
    @GetMapping("/reviews")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> listReviews(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            List<Review> allReviews;
            
            // Filter by status if specified
            if (status != null && !status.isEmpty()) {
                try {
                    ReviewStatus reviewStatus = ReviewStatus.valueOf(status.toUpperCase());
                    allReviews = reviewService.getReviewsByStatus(reviewStatus);
                } catch (IllegalArgumentException e) {
                    allReviews = reviewService.getAllReviews();
                }
            } else {
                allReviews = reviewService.getAllReviews();
            }
            
            // Filter by product
            if (productId != null && !productId.isEmpty()) {
                UUID pid = UUID.fromString(productId);
                allReviews = allReviews.stream()
                        .filter(r -> r.getProduct() != null && r.getProduct().getId().equals(pid))
                        .toList();
            }
            
            // Filter by rating
            if (rating != null && rating >= 1 && rating <= 5) {
                allReviews = allReviews.stream()
                        .filter(r -> r.getRating() != null && r.getRating() == rating)
                        .toList();
            }
            
            // Filter by date range
            if (from != null && !from.isEmpty()) {
                Timestamp fromDate = Timestamp.valueOf(from + " 00:00:00");
                allReviews = allReviews.stream()
                        .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().after(fromDate))
                        .toList();
            }
            if (to != null && !to.isEmpty()) {
                Timestamp toDate = Timestamp.valueOf(to + " 23:59:59");
                allReviews = allReviews.stream()
                        .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().before(toDate))
                        .toList();
            }
            
            // Filter by keyword
            if (q != null && !q.isEmpty()) {
                String keyword = q.toLowerCase();
                allReviews = allReviews.stream()
                        .filter(r -> (r.getContent() != null && r.getContent().toLowerCase().contains(keyword)) ||
                                    (r.getCustomer() != null && 
                                        (r.getCustomer().getFirst_name() != null && r.getCustomer().getFirst_name().toLowerCase().contains(keyword) ||
                                         r.getCustomer().getLast_name() != null && r.getCustomer().getLast_name().toLowerCase().contains(keyword))) ||
                                    (r.getProduct() != null && r.getProduct().getProductName() != null && 
                                        r.getProduct().getProductName().toLowerCase().contains(keyword)))
                        .toList();
            }
            
            // Convert to response format
            List<Map<String, Object>> result = allReviews.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .map(this::toReviewListItem)
                    .toList();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reviews", result,
                    "total", result.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to list reviews: " + e.getMessage()
            ));
        }
    }

    // Get review details
    @GetMapping("/reviews/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> getReviewDetail(@PathVariable("id") UUID id) {
        try {
            Review review = reviewService.getReviewById(id);
            if (review == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> detail = toReviewDetail(review);
            
            // Get reports for this review
            List<ReviewReport> reports = reportRepository.findAllByReviewIdOrderByCreatedAtDesc(id);
            detail.put("reports", reports.stream().map(this::toReportItem).toList());
            
            // Get replies
            detail.put("replies", review.getReplies() != null ? 
                    review.getReplies().stream().map(this::toReplyItem).toList() : List.of());
            
            return ResponseEntity.ok(Map.of("success", true, "review", detail));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to get review: " + e.getMessage()
            ));
        }
    }

    // Update review status
    @PatchMapping("/reviews/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> updateReviewStatus(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> body) {
        try {
            String statusStr = body.get("status");
            String note = body.get("note");
            
            if (statusStr == null || statusStr.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }
            
            ReviewStatus status = ReviewStatus.valueOf(statusStr.toUpperCase());
            String moderator = getCurrentUsername();
            
            Review updated = reviewService.updateReviewStatus(id, status, note, moderator);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Review status updated",
                    "review", toReviewListItem(updated)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to update status: " + e.getMessage()
            ));
        }
    }

    // Approve review
    @PatchMapping("/reviews/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> approveReview(@PathVariable("id") UUID id) {
        try {
            String moderator = getCurrentUsername();
            reviewService.updateReviewStatus(id, ReviewStatus.APPROVED, "Approved by admin", moderator);
            return ResponseEntity.ok(Map.of("success", true, "message", "Review approved"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to approve: " + e.getMessage()
            ));
        }
    }

    // Hide review
    @PatchMapping("/reviews/{id}/hide")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> hideReview(@PathVariable("id") UUID id) {
        try {
            String moderator = getCurrentUsername();
            reviewService.updateReviewStatus(id, ReviewStatus.HIDDEN, "Hidden by admin", moderator);
            return ResponseEntity.ok(Map.of("success", true, "message", "Review hidden"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to hide: " + e.getMessage()
            ));
        }
    }

    // Lock review
    @PatchMapping("/reviews/{id}/lock")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> lockReview(@PathVariable("id") UUID id) {
        try {
            String moderator = getCurrentUsername();
            reviewService.updateReviewStatus(id, ReviewStatus.LOCKED, "Locked by admin", moderator);
            return ResponseEntity.ok(Map.of("success", true, "message", "Review locked"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to lock: " + e.getMessage()
            ));
        }
    }

    // Reply to review
    @PostMapping("/reviews/{id}/reply")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> replyToReview(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> body) {
        try {
            String reply = body.get("reply");
            if (reply == null || reply.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reply content is required"));
            }
            
            String username = getCurrentUsername();
            String role = "ADMIN";
            
            reviewService.replyToReview(id, reply, username, role);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Reply added successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to reply: " + e.getMessage()
            ));
        }
    }

    // Delete review (hard delete)
    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable("id") UUID id) {
        try {
            if (!reviewRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            reviewRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Review deleted permanently"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to delete: " + e.getMessage()
            ));
        }
    }

    // ===== REVIEW REPORTS =====

    // List review reports
    @GetMapping("/review-reports")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> listReviewReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            List<ReviewReport> reports;
            
            if (status != null && !status.isEmpty()) {
                ReviewReport.ReportStatus reportStatus = ReviewReport.ReportStatus.valueOf(status.toUpperCase());
                reports = reportRepository.findByStatus(reportStatus, PageRequest.of(page - 1, size)).getContent();
            } else {
                reports = reportRepository.findAll(PageRequest.of(page - 1, size)).getContent();
            }
            
            List<Map<String, Object>> result = reports.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .map(this::toReportItem)
                    .toList();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reports", result,
                    "total", result.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to list reports: " + e.getMessage()
            ));
        }
    }

    // Update report status
    @PatchMapping("/review-reports/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> updateReportStatus(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> body) {
        try {
            String statusStr = body.get("status");
            String note = body.get("note");
            
            if (statusStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }
            
            ReviewReport report = reportRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found"));
            
            report.setStatus(ReviewReport.ReportStatus.valueOf(statusStr.toUpperCase()));
            report.setHandledBy(getCurrentUsername());
            report.setHandledAt(new Timestamp(System.currentTimeMillis()));
            report.setHandledNote(note);
            
            reportRepository.save(report);
            
            // If resolved, also update the review
            if (report.getStatus() == ReviewReport.ReportStatus.RESOLVED) {
                // Optionally hide or lock the review
            }
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Report updated"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to update report: " + e.getMessage()
            ));
        }
    }

    // ===== REVIEW STATISTICS =====

    @GetMapping("/reviews/stats")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> getReviewStats(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        try {
            // Get overall stats
            List<Object[]> statusGroups = reviewRepository.countByStatusGroup();
            Map<String, Long> statusStats = new LinkedHashMap<>();
            for (Object[] row : statusGroups) {
                statusStats.put(((ReviewStatus) row[0]).name(), (Long) row[1]);
            }
            
            // Get rating distribution
            List<Object[]> ratingGroups = reviewRepository.countByRatingGroup();
            Map<Integer, Long> ratingStats = new LinkedHashMap<>();
            for (Object[] row : ratingGroups) {
                ratingStats.put((Integer) row[0], (Long) row[1]);
            }
            
            // Get most reported reviews
            List<Review> mostReported = reviewService.getMostReportedReviews(5);
            List<Map<String, Object>> reportedReviews = mostReported.stream()
                    .map(r -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", r.getId().toString());
                        m.put("productName", r.getProduct() != null ? r.getProduct().getProductName() : "N/A");
                        m.put("reportCount", r.getReportCount());
                        m.put("rating", r.getRating());
                        return m;
                    })
                    .toList();
            
            // Calculate totals
            long totalReviews = statusStats.values().stream().mapToLong(Long::longValue).sum();
            long approvedReviews = statusStats.getOrDefault(ReviewStatus.APPROVED, 0L);
            long pendingReviews = statusStats.getOrDefault(ReviewStatus.PENDING, 0L);
            
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalReviews", totalReviews);
            stats.put("approvedReviews", approvedReviews);
            stats.put("pendingReviews", pendingReviews);
            stats.put("approvalRate", totalReviews > 0 ? String.format("%.1f%%", approvedReviews * 100.0 / totalReviews) : "0%");
            stats.put("statusDistribution", statusStats);
            stats.put("ratingDistribution", ratingStats);
            stats.put("mostReportedReviews", reportedReviews);
            
            return ResponseEntity.ok(Map.of("success", true, "stats", stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to get stats: " + e.getMessage()
            ));
        }
    }

    // Check if a user (customerId) has purchased productId
    @GetMapping("/reviews/product/{productId}/can-review")
    public ResponseEntity<Map<String, Object>> canReview(
            @PathVariable("productId") UUID productId, 
            @RequestParam("userId") String userId) {
        try {
            Product p = new Product();
            p.setId(productId);
            List<OrderItem> items = orderItemRepository.findByProduct(p);
            boolean found = false;
            for (OrderItem oi : items) {
                if (oi.getOrder() != null && oi.getOrder().getCustomer() != null) {
                    Customer c = oi.getOrder().getCustomer();
                    if (c.getId() != null && c.getId().toString().equals(userId)) {
                        found = true;
                        break;
                    }
                }
            }
            return ResponseEntity.ok(Map.of("canReview", found));
        } catch (Exception ex) {
            return ResponseEntity.ok(Map.of("canReview", false));
        }
    }

    // ===== HELPER METHODS =====

    private Map<String, Object> toReviewListItem(Review r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("content", r.getContent());
        m.put("ratingPoint", r.getRatingPoint());
        m.put("rating", r.getRating());
        m.put("status", r.getStatus().name());
        m.put("isVerified", r.getIsVerifiedPurchase());
        m.put("isAnonymous", r.getIsAnonymous());
        m.put("reportCount", r.getReportCount());
        m.put("createdAt", r.getCreatedAt());
        m.put("updatedAt", r.getUpdatedAt());
        
        if (r.getProduct() != null) {
            m.put("productId", r.getProduct().getId());
            m.put("productName", r.getProduct().getProductName());
        } else {
            m.put("productId", null);
            m.put("productName", null);
        }
        
        if (r.getCustomer() != null) {
            m.put("customerId", r.getCustomer().getId());
            m.put("customerName", r.getIsAnonymous() ? "Ẩn danh" : 
                    (r.getCustomer().getFirst_name() + " " + r.getCustomer().getLast_name()));
        } else {
            m.put("customerId", null);
            m.put("customerName", "Unknown");
        }
        
        if (r.getImages() != null) {
            m.put("imageCount", r.getImages().size());
        } else {
            m.put("imageCount", 0);
        }
        
        return m;
    }

    private Map<String, Object> toReviewDetail(Review r) {
        Map<String, Object> detail = toReviewListItem(r);
        
        if (r.getImages() != null) {
            detail.put("images", r.getImages().stream()
                    .map(img -> Map.of(
                            "id", img.getId(),
                            "url", img.getImageUrl(),
                            "name", img.getImageName()
                    ))
                    .toList());
        }
        
        detail.put("moderatedBy", r.getModeratedBy());
        detail.put("moderatedAt", r.getModeratedAt());
        detail.put("moderationNote", r.getModerationNote());
        
        return detail;
    }

    private Map<String, Object> toReportItem(ReviewReport r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("reason", r.getReason().name());
        m.put("reasonDisplay", r.getReason().getDisplayName());
        m.put("note", r.getNote());
        m.put("status", r.getStatus().name());
        m.put("statusDisplay", r.getStatus().getDisplayName());
        m.put("createdAt", r.getCreatedAt());
        m.put("handledAt", r.getHandledAt());
        m.put("handledBy", r.getHandledBy());
        m.put("handledNote", r.getHandledNote());
        
        if (r.getReporter() != null) {
            m.put("reporterId", r.getReporter().getId());
            m.put("reporterName", r.getReporter().getFirst_name() + " " + r.getReporter().getLast_name());
        }
        
        if (r.getReview() != null) {
            m.put("reviewId", r.getReview().getId());
            m.put("reviewContent", r.getReview().getContent());
        }
        
        return m;
    }

    private Map<String, Object> toReplyItem(ReviewReply r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("content", r.getContent());
        m.put("authorName", r.getAuthorName());
        m.put("authorRole", r.getAuthorRole());
        m.put("createdAt", r.getCreatedAt());
        return m;
    }
}
