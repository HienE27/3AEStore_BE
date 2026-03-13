package com.nguyenviethien.exercise201.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.DTO.ProductDetailsDTO;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.exception.ApiResponse;
import com.nguyenviethien.exercise201.service.ProductService;
import com.nguyenviethien.exercise201.service.AIGenerateDescriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@CrossOrigin(origins = { "http://localhost:3000", "http://127.0.0.1:3000" }, allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS })
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final AIGenerateDescriptionService aiGenerateDescriptionService;

    @Autowired
    public ProductController(
            ProductService productService,
            ObjectMapper objectMapper,
            AIGenerateDescriptionService aiGenerateDescriptionService) {
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.aiGenerateDescriptionService = aiGenerateDescriptionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            List<Product> products = productService.getAllProducts();

            if (page != null && size != null) {
                int totalElements = products.size();
                int totalPages = (int) Math.ceil((double) totalElements / size);
                int start = page * size;
                int end = Math.min(start + size, totalElements);
                List<Product> pagedProducts = start < totalElements ? products.subList(start, end) : List.of();

                return ResponseEntity.ok(ApiResponse.<List<Product>>builder()
                        .success(true)
                        .data(pagedProducts)
                        .message("Page " + page + " of " + totalPages)
                        .build());
            }

            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            log.error("Error getting all products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách sản phẩm"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable UUID id) {
        try {
            Optional<Product> product = productService.getProductByIdWithCategories(id);
            return product.map(p -> ResponseEntity.ok(ApiResponse.success(p)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Không tìm thấy sản phẩm")));
        } catch (Exception e) {
            log.error("Error getting product by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy thông tin sản phẩm"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createProduct(@RequestBody JsonNode productJson, @RequestParam UUID staffId) {
        try {
            log.info("Creating product with staffId: {}", staffId);

            if (staffId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Staff ID không được để trống"));
            }

            if (productJson == null || productJson.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Dữ liệu sản phẩm không được để trống"));
            }

            if (!productJson.has("productName") || productJson.get("productName").asText().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tên sản phẩm không được để trống"));
            }

            ResponseEntity<?> result = productService.save(productJson, staffId);
            return result;

        } catch (IllegalArgumentException e) {
            log.warn("Validation error creating product: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi tạo sản phẩm"));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createProductAlternative(@RequestBody JsonNode jsonData, @RequestParam UUID staffId) {
        try {
            log.debug("Alternative create endpoint called with staffId: {}", staffId);

            if (staffId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Staff ID không được để trống"));
            }

            if (jsonData == null || jsonData.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Dữ liệu sản phẩm không được để trống"));
            }

            return productService.save(jsonData, staffId);

        } catch (Exception e) {
            log.error("Error in alternative create endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra"));
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<?>> updateProduct(
            @PathVariable UUID productId,
            @RequestParam("staffId") UUID staffId,
            @RequestBody JsonNode productJson) {
        try {
            log.info("Updating product ID: {} by staff: {}", productId, staffId);

            if (productId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Product ID không hợp lệ"));
            }

            if (staffId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Staff ID không được để trống"));
            }

            return productService.update(productId, productJson, staffId);

        } catch (Exception e) {
            log.error("Error updating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi cập nhật sản phẩm"));
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable UUID productId) {
        try {
            log.info("Deleting product ID: {}", productId);

            if (productId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Product ID không hợp lệ"));
            }

            return productService.deleteProduct(productId);

        } catch (Exception e) {
            log.error("Error deleting product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Có lỗi xảy ra khi xóa sản phẩm"));
        }
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailsDTO>> getProductDetails(@PathVariable UUID productId) {
        try {
            if (productId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Product ID không hợp lệ"));
            }

            ProductDetailsDTO productDTO = productService.getProductDetails(productId);
            if (productDTO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy sản phẩm"));
            }

            return ResponseEntity.ok(ApiResponse.success(productDTO));

        } catch (Exception e) {
            log.error("Error getting product details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy chi tiết sản phẩm"));
        }
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsByCategory(@PathVariable UUID categoryId) {
        try {
            if (categoryId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Category ID không hợp lệ"));
            }

            List<Product> products = productService.getProductsByCategoryId(categoryId);
            return ResponseEntity.ok(ApiResponse.success(products));

        } catch (Exception e) {
            log.error("Error getting products by category: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy sản phẩm theo danh mục"));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<?>> countProductsByCategory(@RequestParam(required = false) String categoryId) {
        try {
            Long count;
            if (categoryId != null && !categoryId.isEmpty()) {
                UUID catId = UUID.fromString(categoryId);
                count = productService.countByCategoryId(catId);
            } else {
                count = productService.countAllProducts();
            }

            return ResponseEntity.ok(ApiResponse.success("count", count));

        } catch (Exception e) {
            log.error("Error counting products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi đếm sản phẩm"));
        }
    }

    @GetMapping("/search/salePriceGreaterThanZero")
    public ResponseEntity<ApiResponse<?>> getProductsWithSalePrice(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        try {
            List<Product> allProducts = productService.getAllProducts();
            List<Product> saleProducts = allProducts.stream()
                    .filter(p -> p.getSalePrice() != null && p.getSalePrice().compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());

            int totalElements = saleProducts.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int start = page * size;
            int end = Math.min(start + size, totalElements);
            List<Product> pagedProducts = start < totalElements ? saleProducts.subList(start, end) : List.of();

            return ResponseEntity.ok(ApiResponse.<List<Product>>builder()
                    .success(true)
                    .data(pagedProducts)
                    .message("Page " + page + " of " + totalPages)
                    .build());

        } catch (Exception e) {
            log.error("Error getting products with sale price: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tìm kiếm sản phẩm"));
        }
    }

    @GetMapping("/search/findByProductNameContaining")
    public ResponseEntity<ApiResponse<?>> searchProductsByName(
            @RequestParam String productName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            List<Product> allProducts = productService.getAllProducts();
            List<Product> filteredProducts = allProducts.stream()
                    .filter(p -> p.getProductName() != null &&
                            p.getProductName().toLowerCase().contains(productName.toLowerCase()))
                    .collect(Collectors.toList());

            int totalElements = filteredProducts.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int start = page * size;
            int end = Math.min(start + size, totalElements);
            List<Product> pagedProducts = start < totalElements ? filteredProducts.subList(start, end) : List.of();

            return ResponseEntity.ok(ApiResponse.<List<Product>>builder()
                    .success(true)
                    .data(pagedProducts)
                    .message("Found " + totalElements + " products")
                    .build());

        } catch (Exception e) {
            log.error("Error searching products by name: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tìm kiếm sản phẩm"));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<?>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Product service is running"));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<?>> testEndpoint(@RequestBody(required = false) JsonNode data) {
        return ResponseEntity.ok(ApiResponse.success("Test endpoint works!", data));
    }

    @PostMapping("/debug")
    public ResponseEntity<ApiResponse<?>> debugEndpoint(@RequestBody JsonNode data) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Debug endpoint", data));
        } catch (Exception e) {
            log.error("Error in debug endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Debug failed"));
        }
    }

    @PostMapping("/generate-description")
    public ResponseEntity<ApiResponse<?>> generateDescription(@RequestBody JsonNode request) {
        try {
            String productName = request.has("productName") ? request.get("productName").asText() : null;

            if (productName == null || productName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tên sản phẩm không được để trống"));
            }

            log.info("Generating description for product: {}", productName);
            String description = aiGenerateDescriptionService.generateDescription(productName);

            return ResponseEntity.ok(ApiResponse.success("description", description));

        } catch (Exception e) {
            log.error("Error generating description: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi tạo mô tả"));
        }
    }
}
