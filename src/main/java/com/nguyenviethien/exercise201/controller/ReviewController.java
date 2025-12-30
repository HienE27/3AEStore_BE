package com.nguyenviethien.exercise201.controller;

import com.nguyenviethien.exercise201.entity.Review;
import com.nguyenviethien.exercise201.entity.ReviewImage;
import com.nguyenviethien.exercise201.service.ReviewService;
import com.nguyenviethien.exercise201.service.ReviewImageService;
import com.nguyenviethien.exercise201.repository.ProductRepository;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import com.nguyenviethien.exercise201.repository.OrderItemRepository;
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
        try {
            List<Review> reviews = reviewService.getReviewsByProduct(productId);

            List<com.nguyenviethien.exercise201.DTO.ReviewDto> dtoList = reviews.stream().map(r -> {
                com.nguyenviethien.exercise201.DTO.ProductDto p = null;
                if (r.getProduct() != null) {
                    p = new com.nguyenviethien.exercise201.DTO.ProductDto(
                            r.getProduct().getId(),
                            r.getProduct().getSlug(),
                            r.getProduct().getProductName(),
                            r.getProduct().getSku(),
                            r.getProduct().getSalePrice(),
                            r.getProduct().getComparePrice(),
                            r.getProduct().getBuyingPrice(),
                            r.getProduct().getQuantity()
                    );
                }

                com.nguyenviethien.exercise201.DTO.CustomerDto c = null;
                if (r.getCustomer() != null) {
                    c = new com.nguyenviethien.exercise201.DTO.CustomerDto(
                            r.getCustomer().getId(),
                            r.getCustomer().getFirst_name(),
                            r.getCustomer().getLast_name()
                    );
                }

                List<com.nguyenviethien.exercise201.DTO.ReviewImageDto> imgs = null;
                if (r.getImages() != null) {
                    imgs = r.getImages().stream().map(img -> new com.nguyenviethien.exercise201.DTO.ReviewImageDto(
                            img.getId(),
                            img.getImageUrl(),
                            img.getImageName(),
                            img.getImageSize(),
                            img.getSortOrder()
                    )).toList();
                }

                com.nguyenviethien.exercise201.DTO.ReviewDto dto = new com.nguyenviethien.exercise201.DTO.ReviewDto(
                        r.getId(),
                        r.getContent(),
                        r.getRatingPoint(),
                        r.getRating(),
                        r.getCreatedAt(),
                        p,
                        c,
                        imgs
                );
                return dto;
            }).toList();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reviews", dtoList,
                    "total", dtoList.size(),
                    "averageRating", reviewService.getAverageRating(productId)
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to get reviews: " + e.getMessage()
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
