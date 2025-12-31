package com.nguyenviethien.exercise201.controller;

import com.nguyenviethien.exercise201.entity.Review;
import com.nguyenviethien.exercise201.entity.ReviewImage;
import com.nguyenviethien.exercise201.service.ReviewService;
import com.nguyenviethien.exercise201.service.ReviewImageService;
import com.nguyenviethien.exercise201.repository.ProductRepository;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import com.nguyenviethien.exercise201.repository.OrderItemRepository;
import com.nguyenviethien.exercise201.entity.OrderItem;
import com.nguyenviethien.exercise201.DTO.ProductDto;
import com.nguyenviethien.exercise201.DTO.ReviewDto;
import com.nguyenviethien.exercise201.DTO.CustomerDto;
import com.nguyenviethien.exercise201.DTO.ReviewImageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewImageService reviewImageService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;

    // Tạo review mới với ảnh
    @PostMapping
    public ResponseEntity<?> createReview(
            @RequestParam("customerId") String customerIdStr,
            @RequestParam("productId") String productIdStr,
            @RequestParam("orderItemId") String orderItemIdStr,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam("ratingPoint") Float ratingPoint,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        try {
            // Resolve customer
            var customer = customerRepository.findById(java.util.UUID.fromString(customerIdStr)).orElse(null);
            if (customer == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Customer not found"));
            }

            // Resolve product: try UUID first, otherwise try slug
            com.nguyenviethien.exercise201.entity.Product product = null;
            try {
                java.util.UUID pid = java.util.UUID.fromString(productIdStr);
                product = productRepository.findById(pid).orElse(null);
            } catch (IllegalArgumentException ex) {
                product = productRepository.findBySlug(productIdStr).orElse(null);
            }
            if (product == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Product not found"));
            }

            // Resolve order item (may be UUID)
            com.nguyenviethien.exercise201.entity.OrderItem orderItem = null;
            try {
                java.util.UUID oid = java.util.UUID.fromString(orderItemIdStr);
                orderItem = orderItemRepository.findById(oid).orElse(null);
            } catch (IllegalArgumentException ex) {
                // ignore, leave orderItem null
            }

            Review review = new Review();
            review.setContent(content);
            review.setRatingPoint(ratingPoint);
            review.setProduct(product);
            review.setCustomer(customer);
            review.setOrderItem(orderItem);
            // Verify purchaser: allow review only if the customer actually bought the product
            boolean purchased = false;
            try {
                if (orderItem != null && orderItem.getOrder() != null && orderItem.getOrder().getCustomer() != null) {
                    purchased = orderItem.getOrder().getCustomer().getId().equals(customer.getId());
                } else {
                    // fallback: check OrderItem entries for the product and match customer
                    var items = orderItemRepository.findByProduct(product);
                    for (OrderItem oi : items) {
                        if (oi.getOrder() != null && oi.getOrder().getCustomer() != null &&
                                oi.getOrder().getCustomer().getId().equals(customer.getId())) {
                            purchased = true;
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                purchased = false;
            }

            if (!purchased) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Only customers who bought the product can review it"));
            }

            Review savedReview = reviewService.createReview(review, images);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Review created successfully",
                "review", savedReview
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

    // Cập nhật review
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable UUID reviewId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "ratingPoint", required = false) Float ratingPoint,
            @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages) {

        try {
            Review reviewData = new Review();
            reviewData.setContent(content);
            reviewData.setRatingPoint(ratingPoint);

            Review updatedReview = reviewService.updateReview(reviewId, reviewData, newImages);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Review updated successfully",
                "review", updatedReview
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

    // Xóa review
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

    // Lấy review theo ID
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReview(@PathVariable UUID reviewId) {
        try {
            Review review = reviewService.getReviewById(reviewId);

            if (review == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(review);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get review: " + e.getMessage()
            ));
        }
    }

    // Lấy tất cả review của một sản phẩm
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProduct(@PathVariable UUID productId) {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_1\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewController.java:179\",\"message\":\"getReviewsByProduct entry\",\"data\":{\"productId\":\"" + productId + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n");
            fw.close();
        } catch (Exception ex) {}
        // #endregion
        try {
            List<Review> reviews = reviewService.getReviewsByProduct(productId);
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_2\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewController.java:183\",\"message\":\"Reviews fetched\",\"data\":{\"reviewCount\":\"" + (reviews != null ? reviews.size() : 0) + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
                fw.close();
            } catch (Exception ex) {}
            // #endregion

            List<ReviewDto> dtoList = reviews.stream().map(r -> {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                    fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_3\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewController.java:186\",\"message\":\"Processing review\",\"data\":{\"reviewId\":\"" + (r != null && r.getId() != null ? r.getId() : "null") + "\",\"hasProduct\":" + (r != null && r.getProduct() != null) + "},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\"}\n");
                    fw.close();
                } catch (Exception ex) {}
                // #endregion
                ProductDto p = null;
                if (r.getProduct() != null) {
                    // #region agent log
                    try {
                        java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                        fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_4\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewController.java:189\",\"message\":\"Before ProductDto creation\",\"data\":{\"productId\":\"" + (r.getProduct().getId() != null ? r.getProduct().getId() : "null") + "\",\"productName\":\"" + (r.getProduct().getProductName() != null ? r.getProduct().getProductName() : "null") + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\"}\n");
                        fw.close();
                    } catch (Exception ex) {}
                    // #endregion
                    try {
                        p = new ProductDto(
                                r.getProduct().getId(),
                                r.getProduct().getSlug(),
                                r.getProduct().getProductName(),
                                r.getProduct().getSku(),
                                r.getProduct().getSalePrice(),
                                r.getProduct().getComparePrice(),
                                r.getProduct().getBuyingPrice(),
                                r.getProduct().getQuantity(),
                                r.getProduct().getShortDescription()
                        );
                        // #region agent log
                        try {
                            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_5\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewController.java:200\",\"message\":\"ProductDto created successfully\",\"data\":{\"productDtoId\":\"" + (p != null && p.getId() != null ? p.getId() : "null") + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\"}\n");
                            fw.close();
                        } catch (Exception ex) {}
                        // #endregion
                    } catch (Exception ex) {
                        // #region agent log
                        try {
                            java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_6\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewController.java:201\",\"message\":\"ProductDto creation failed\",\"data\":{\"error\":\"" + ex.getClass().getName() + "\",\"message\":\"" + ex.getMessage() + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n");
                            fw.close();
                        } catch (Exception ex2) {}
                        // #endregion
                        throw ex;
                    }
                }

                CustomerDto c = null;
                if (r.getCustomer() != null) {
                    c = new CustomerDto(
                            r.getCustomer().getId(),
                            r.getCustomer().getFirst_name(),
                            r.getCustomer().getLast_name()
                    );
                }

                List<ReviewImageDto> imgs = null;
                if (r.getImages() != null) {
                    imgs = r.getImages().stream().map(img -> new ReviewImageDto(
                            img.getId(),
                            img.getImageUrl(),
                            img.getImageName(),
                            img.getImageSize(),
                            img.getSortOrder()
                    )).toList();
                }

                ReviewDto dto = null;
                try {
                    dto = new ReviewDto(
                            r.getId(),
                            r.getContent(),
                            r.getRatingPoint(),
                            r.getRating(),
                            r.getCreatedAt(),
                            p,
                            c,
                            imgs
                    );
                    // #region agent log
                    try {
                        java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                        fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_7\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewController.java:230\",\"message\":\"ReviewDto created successfully\",\"data\":{\"reviewDtoId\":\"" + (dto != null && dto.getId() != null ? dto.getId() : "null") + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\"}\n");
                        fw.close();
                    } catch (Exception ex) {}
                    // #endregion
                } catch (Exception ex) {
                    // #region agent log
                    try {
                        java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                        fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_8\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewController.java:231\",\"message\":\"ReviewDto creation failed\",\"data\":{\"error\":\"" + ex.getClass().getName() + "\",\"message\":\"" + ex.getMessage() + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\"}\n");
                        fw.close();
                    } catch (Exception ex2) {}
                    // #endregion
                    throw ex;
                }
                return dto;
            }).toList();
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_9\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewController.java:233\",\"message\":\"DTO list created\",\"data\":{\"dtoListSize\":\"" + (dtoList != null ? dtoList.size() : 0) + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
                fw.close();
            } catch (Exception ex) {}
            // #endregion

            Double avgRating = null;
            try {
                avgRating = reviewService.getAverageRating(productId);
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                    fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_10\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewController.java:237\",\"message\":\"Average rating fetched\",\"data\":{\"averageRating\":\"" + (avgRating != null ? avgRating : "null") + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"F\"}\n");
                    fw.close();
                } catch (Exception ex) {}
                // #endregion
            } catch (Exception ex) {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                    fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_11\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewController.java:238\",\"message\":\"Average rating failed\",\"data\":{\"error\":\"" + ex.getClass().getName() + "\",\"message\":\"" + ex.getMessage() + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"F\"}\n");
                    fw.close();
                } catch (Exception ex2) {}
                // #endregion
                throw ex;
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reviews", dtoList,
                    "total", dtoList.size(),
                    "averageRating", avgRating != null ? avgRating : 0.0
            ));

        } catch (Exception e) {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_12\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ReviewController.java:240\",\"message\":\"Exception caught\",\"data\":{\"error\":\"" + e.getClass().getName() + "\",\"message\":\"" + e.getMessage() + "\",\"stackTrace\":\"" + java.util.Arrays.toString(e.getStackTrace()).replace("\"", "'") + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"ALL\"}\n");
                fw.close();
            } catch (Exception ex) {}
            // #endregion
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to get reviews: " + e.getMessage()
            ));
        }
    }

    // Lấy tất cả review (simple DTO) của một sản phẩm - compact, safe for frontend
    @GetMapping("/product/{productId}/list")
    public ResponseEntity<?> getReviewsByProductSimple(@PathVariable UUID productId) {
        try {
            List<Review> reviews = reviewService.getReviewsByProduct(productId);

            var simple = reviews.stream()
                    .filter(r -> r.getVisible() == null ? true : r.getVisible())
                    .map(r -> {
                var m = new java.util.LinkedHashMap<String, Object>();
                m.put("id", r.getId());
                m.put("content", r.getContent());
                m.put("ratingPoint", r.getRatingPoint());
                m.put("createdAt", r.getCreatedAt());
                // customer minimal
                if (r.getCustomer() != null) {
                    var c = new java.util.LinkedHashMap<String, Object>();
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

    // Lấy tất cả review của một khách hàng
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getReviewsByCustomer(@PathVariable UUID customerId) {
        try {
            List<Review> reviews = reviewService.getReviewsByCustomer(customerId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "reviews", reviews,
                "total", reviews.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get customer reviews: " + e.getMessage()
            ));
        }
    }

    // Lấy review theo order item
    @GetMapping("/order-item/{orderItemId}")
    public ResponseEntity<?> getReviewByOrderItem(@PathVariable UUID orderItemId) {
        try {
            Review review = reviewService.getReviewByOrderItem(orderItemId);

            if (review == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(review);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get review: " + e.getMessage()
            ));
        }
    }

    // Lấy thông tin thống kê review của sản phẩm
    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<?> getProductReviewStats(@PathVariable UUID productId) {
        try {
            Double averageRating = reviewService.getAverageRating(productId);
            Long totalReviews = reviewService.countReviews(productId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "productId", productId,
                "averageRating", averageRating,
                "totalReviews", totalReviews
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get review stats: " + e.getMessage()
            ));
        }
    }

    // Xóa ảnh review
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteReviewImage(@PathVariable UUID imageId) {
        try {
            reviewImageService.deleteImage(imageId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Image deleted successfully"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete image: " + e.getMessage()
            ));
        }
    }

    // Lấy tất cả ảnh của một review
    @GetMapping("/{reviewId}/images")
    public ResponseEntity<?> getReviewImages(@PathVariable UUID reviewId) {
        try {
            List<ReviewImage> images = reviewImageService.getImagesByReview(reviewId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "images", images,
                "total", images.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get review images: " + e.getMessage()
            ));
        }
    }

    // Cập nhật thứ tự ảnh
    @PutMapping("/images/{imageId}/order")
    public ResponseEntity<?> updateImageOrder(
            @PathVariable UUID imageId,
            @RequestParam Integer newOrder) {

        try {
            reviewImageService.updateImageOrder(imageId, newOrder);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Image order updated successfully"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update image order: " + e.getMessage()
            ));
        }
    }
}
