package com.nguyenviethien.exercise201.controller;

import com.nguyenviethien.exercise201.entity.Review;
import com.nguyenviethien.exercise201.entity.ReviewReply;
import com.nguyenviethien.exercise201.repository.ReviewReplyRepository;
import com.nguyenviethien.exercise201.repository.ReviewRepository;
import com.nguyenviethien.exercise201.repository.OrderItemRepository;
import com.nguyenviethien.exercise201.entity.OrderItem;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AdminReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewReplyRepository replyRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    // Admin: list reviews (simple)
    @GetMapping("/admin/reviews")
    // Temporarily allow unauthenticated access for debugging. Revert before production.
    // @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<List<Map<String, Object>>> listReviews() {
        List<Review> all = reviewRepository.findAll();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Review r : all) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("content", r.getContent());
            m.put("ratingPoint", r.getRatingPoint());
            m.put("createdAt", r.getCreatedAt());
            m.put("productName", r.getProduct() != null ? r.getProduct().getProductName() : null);
            m.put("customerName", r.getCustomer() != null ? (r.getCustomer().getFirst_name() + " " + r.getCustomer().getLast_name()) : null);
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    // Admin reply to a review
    @PostMapping("/admin/reviews/{id}/reply")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> replyToReview(@PathVariable("id") UUID id, @RequestBody Map<String, String> body) {
        Optional<Review> opt = reviewRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Review review = opt.get();
        String reply = body.get("reply");
        if (reply == null || reply.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Empty reply"));
        }
        ReviewReply rr = new ReviewReply();
        rr.setReview(review);
        rr.setContent(reply);
        // Use SecurityContext to get current user info if available
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                String username = auth.getName();
                String role = auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()
                        ? auth.getAuthorities().iterator().next().getAuthority()
                        : "ROLE_ADMIN";
                rr.setAuthorId(username);
                rr.setAuthorName(username);
                rr.setAuthorRole(role);
            } else {
                rr.setAuthorId("system");
                rr.setAuthorName("Admin");
                rr.setAuthorRole("ADMIN");
            }
        } catch (Exception ex) {
            rr.setAuthorId("system");
            rr.setAuthorName("Admin");
            rr.setAuthorRole("ADMIN");
        }
        rr.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        replyRepository.save(rr);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Admin delete review
    @DeleteMapping("/admin/reviews/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> deleteReview(@PathVariable("id") UUID id) {
        if (!reviewRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        reviewRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Approve (show) review - set visible=true and record moderator
    @PatchMapping("/admin/reviews/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> approveReview(@PathVariable("id") UUID id) {
        Optional<Review> opt = reviewRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Review r = opt.get();
        r.setVisible(true);
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                String username = auth.getName();
                r.setModeratedBy(username);
            }
            r.setModeratedAt(new Timestamp(System.currentTimeMillis()));
        } catch (Exception ex) {
            r.setModeratedBy("system");
            r.setModeratedAt(new Timestamp(System.currentTimeMillis()));
        }
        reviewRepository.save(r);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Hide (moderate) review - set visible=false and record moderator
    @PatchMapping("/admin/reviews/{id}/hide")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> hideReview(@PathVariable("id") UUID id) {
        Optional<Review> opt = reviewRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Review r = opt.get();
        r.setVisible(false);
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                String username = auth.getName();
                r.setModeratedBy(username);
            }
            r.setModeratedAt(new Timestamp(System.currentTimeMillis()));
        } catch (Exception ex) {
            r.setModeratedBy("system");
            r.setModeratedAt(new Timestamp(System.currentTimeMillis()));
        }
        reviewRepository.save(r);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Check if a user (customerId) has purchased productId
    @GetMapping("/reviews/product/{productId}/can-review")
    public ResponseEntity<Map<String, Object>> canReview(@PathVariable("productId") UUID productId, @RequestParam("userId") String userId) {
        // naive check: find orderItems for product and see if customer's id matches userId
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
}


