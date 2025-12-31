package com.nguyenviethien.exercise201.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.DTO.ProductDTO;
import com.nguyenviethien.exercise201.entity.Category;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.service.ProductService;
import com.nguyenviethien.exercise201.service.AIGenerateDescriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AIGenerateDescriptionService aiGenerateDescriptionService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            System.err.println("💥 Error getting all products: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        try {
            Optional<Product> product = productService.getProductByIdWithCategories(id);
            return product.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("💥 Error getting product by ID: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ SỬA ĐỔI: Better JSON handling và validation
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> productData, @RequestParam UUID staffId) {
        try {
            System.out.println("🚀 =================================");
            System.out.println("📥 Received product creation request");
            System.out.println("👤 Staff ID: " + staffId);
            System.out.println("📦 Product data: " + productData);
            System.out.println("🚀 =================================");

            // Validate staffId
            if (staffId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Staff ID không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate productData
            if (productData == null || productData.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Dữ liệu sản phẩm không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate required fields
            String[] requiredFields = {"productName", "sku", "slug"};
            for (String field : requiredFields) {
                if (!productData.containsKey(field) || 
                    productData.get(field) == null || 
                    productData.get(field).toString().trim().isEmpty()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Trường " + field + " không được để trống");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // Convert Map to JsonNode
            JsonNode jsonData = objectMapper.valueToTree(productData);
            
            System.out.println("🔄 Converted to JsonNode: " + jsonData.toString());

            // Call service
            ResponseEntity<?> result = productService.save(jsonData, staffId);
            
            System.out.println("✅ Service call completed with status: " + result.getStatusCode());
            
            return result;

        } catch (IllegalArgumentException e) {
            System.err.println("❌ Validation error: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            System.err.println("💥 Unexpected error creating product: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra khi tạo sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ THÊM: Alternative endpoint with different mapping
    @PostMapping("/create")
    public ResponseEntity<?> createProductAlternative(@RequestBody JsonNode jsonData, @RequestParam UUID staffId) {
        try {
            System.out.println("🚀 Alternative endpoint called");
            System.out.println("👤 Staff ID: " + staffId);
            System.out.println("📦 JSON data: " + jsonData.toString());

            if (staffId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Staff ID không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            if (jsonData == null || jsonData.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Dữ liệu sản phẩm không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            return productService.save(jsonData, staffId);

        } catch (Exception e) {
            System.err.println("💥 Error in alternative endpoint: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable UUID productId,
            @RequestParam("staffId") UUID staffId,
            @RequestBody JsonNode productJson) {
        try {
            if (productId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Product ID không hợp lệ");
                return ResponseEntity.badRequest().body(error);
            }

            if (staffId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Staff ID không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            System.out.println("🔄 Updating product ID: " + productId + " by staff: " + staffId);
            
            return productService.update(productId, productJson, staffId);
            
        } catch (Exception e) {
            System.err.println("💥 Error updating product: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra khi cập nhật sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID productId) {
        try {
            if (productId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Product ID không hợp lệ");
                return ResponseEntity.badRequest().body(error);
            }

            System.out.println("🗑️ Deleting product ID: " + productId);
            
            return productService.deleteProduct(productId);
            
        } catch (Exception e) {
            System.err.println("💥 Error deleting product: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra khi xóa sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDTO> getProductDetails(@PathVariable UUID productId) {
        try {
            if (productId == null) {
                return ResponseEntity.badRequest().build();
            }

            ProductDTO productDTO = productService.getProductDetails(productId);
            if (productDTO == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(productDTO);
            
        } catch (Exception e) {
            System.err.println("💥 Error getting product details: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable UUID categoryId) {
        try {
            if (categoryId == null) {
                return ResponseEntity.badRequest().build();
            }

            List<Product> products = productService.getProductsByCategoryId(categoryId);
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            System.err.println("💥 Error getting products by category: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Product service is running");
        response.put("timestamp", java.time.Instant.now().toString());
        response.put("service", "ProductController");
        return ResponseEntity.ok(response);
    }

    // ✅ Test endpoint
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint(@RequestBody(required = false) Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Test endpoint works!");
        response.put("receivedData", data);
        response.put("timestamp", java.time.Instant.now().toString());
        response.put("dataType", data != null ? data.getClass().getSimpleName() : "null");
        
        System.out.println("🧪 Test endpoint called with data: " + data);
        
        return ResponseEntity.ok(response);
    }

    // ✅ Debug endpoint để test JSON parsing
    @PostMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugEndpoint(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Debug endpoint");
        response.put("receivedData", data);
        response.put("dataKeys", data != null ? data.keySet() : null);
        
        // Convert to JsonNode
        try {
            JsonNode jsonNode = objectMapper.valueToTree(data);
            response.put("jsonNodeData", jsonNode.toString());
            response.put("conversionSuccess", true);
        } catch (Exception e) {
            response.put("conversionError", e.getMessage());
            response.put("conversionSuccess", false);
        }
        
        System.out.println("🐛 Debug endpoint called with data: " + data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Generate product description using AI based on product name
     * POST /api/products/generate-description
     * Body: { "productName": "Tên sách" }
     */
    @PostMapping("/generate-description")
    public ResponseEntity<Map<String, Object>> generateDescription(@RequestBody Map<String, String> request) {
        try {
            String productName = request != null ? request.get("productName") : null;
            
            if (productName == null || productName.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Tên sản phẩm không được để trống");
                return ResponseEntity.badRequest().body(error);
            }
            
            System.out.println("🤖 Generating description for product: " + productName);
            
            String description = aiGenerateDescriptionService.generateDescription(productName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("description", description);
            response.put("productName", productName);
            response.put("success", true);
            
            System.out.println("✅ Description generated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("💥 Error generating description: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra khi tạo mô tả: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}