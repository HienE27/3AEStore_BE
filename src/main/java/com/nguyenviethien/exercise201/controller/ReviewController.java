package com.nguyenviethien.exercise201.controller;

import com.nguyenviethien.exercise201.entity.Review;
import com.nguyenviethien.exercise201.entity.ReviewImage;
import com.nguyenviethien.exercise201.service.ReviewService;
import com.nguyenviethien.exercise201.service.ReviewImageService;
import com.nguyenviethien.exercise201.repository.ProductRepository;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import com.nguyenviethien.exercise201.repository.OrderItemRepository;
import com.nguyenviethien.exercise201.entity.OrderItem;
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
        try {
            List<Review> reviews = reviewService.getReviewsByProduct(productId);

            List<com.nguyenviethien.exercise201.DTO.ReviewDto> dtoList = reviews.stream().map(r -> {
                com.nguyenviethien.exercise201.DTO.ProductDTO p = null;
                if (r.getProduct() != null) {
                    p = new com.nguyenviethien.exercise201.DTO.ProductDTO();
                    p.setId(r.getProduct().getId());
                    p.setSlug(r.getProduct().getSlug());
                    p.setProductName(r.getProduct().getProductName());
                    p.setSku(r.getProduct().getSku());
                    p.setSalePrice(r.getProduct().getSalePrice());
                    p.setComparePrice(r.getProduct().getComparePrice());
                    p.setBuyingPrice(r.getProduct().getBuyingPrice());
                    p.setQuantity(r.getProduct().getQuantity());
                    p.setShortDescription(r.getProduct().getShortDescription());
                    p.setCategoryNames(r.getProduct().getProductCategories() != null ?
                            r.getProduct().getProductCategories().stream()
                                    .map(pc -> pc.getCategory().getCategoryName())
                                    .collect(java.util.stream.Collectors.toList())
                            : java.util.Collections.emptyList());
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
