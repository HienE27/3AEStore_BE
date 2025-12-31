package com.nguyenviethien.exercise201.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nguyenviethien.exercise201.DTO.ProductDTO;
import com.nguyenviethien.exercise201.entity.Category;
import com.nguyenviethien.exercise201.entity.Gallery;
import com.nguyenviethien.exercise201.entity.OrderItem;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.entity.ProductCategory;
import com.nguyenviethien.exercise201.entity.StaffAccount;
import com.nguyenviethien.exercise201.repository.CategoryRepository;
import com.nguyenviethien.exercise201.repository.GalleryRepository;
import com.nguyenviethien.exercise201.repository.OrderItemRepository;
import com.nguyenviethien.exercise201.repository.ProductCategoryRepository;
import com.nguyenviethien.exercise201.repository.ProductRepository;
import com.nguyenviethien.exercise201.repository.StaffAccountRepository;
import com.nguyenviethien.exercise201.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Autowired
    private GalleryRepository galleryRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    
    private final ObjectMapper objectMapper;

    public ProductServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAllWithRelationships();
    }

    @Override
    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> getProductByIdWithCategories(UUID id) {
        return productRepository.findByIdWithCategories(id);
    }

    @Override
    @Transactional
    public ResponseEntity<?> save(JsonNode productJson, UUID staffId) {
        try {
            System.out.println("🚀 =================================");
            System.out.println("📥 Starting product creation process");
            System.out.println("👤 Staff ID: " + staffId);
            System.out.println("📦 Raw JSON: " + productJson.toString());
            System.out.println("🚀 =================================");

            // Validate staff exists
            StaffAccount staff = staffAccountRepository.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));
            System.out.println("✅ Staff found: " + staff.getId());

            // ✅ SỬA ĐỔI: Tạo Product object manually thay vì dùng objectMapper.treeToValue
            Product product = new Product();
            
            // Basic fields
            product.setSlug(productJson.get("slug").asText());
            product.setProductName(productJson.get("productName").asText());
            product.setSku(productJson.get("sku").asText());
            
            // ✅ QUAN TRỌNG: Handle BigDecimal properly
            product.setSalePrice(new BigDecimal(productJson.get("salePrice").asDouble()));
            product.setComparePrice(new BigDecimal(productJson.get("comparePrice").asDouble()));
            product.setBuyingPrice(new BigDecimal(productJson.get("buyingPrice").asDouble()));
            
            product.setQuantity(productJson.get("quantity").asInt());
            product.setShortDescription(productJson.has("shortDescription") ? productJson.get("shortDescription").asText() : "");
            product.setProductDescription(productJson.has("productDescription") ? productJson.get("productDescription").asText() : "");
            
            // ✅ QUAN TRỌNG: Handle enum properly - entity sử dụng lowercase
            String productTypeStr = productJson.get("productType").asText().toLowerCase();
            product.setProductType(Product.ProductType.valueOf(productTypeStr));
            
            product.setPublished(productJson.get("published").asBoolean());
            product.setDisableOutOfStock(productJson.get("disableOutOfStock").asBoolean());
            product.setNote(productJson.has("note") ? productJson.get("note").asText() : "");

            // Set audit fields
            product.setCreatedBy(staff);
            product.setUpdatedBy(staff);
            product.setCreatedAt(new Date());
            product.setUpdatedAt(new Date());

            System.out.println("✅ Basic product fields set");

            // ✅ SỬA ĐỔI: Handle categories properly
            List<ProductCategory> productCategoryList = new ArrayList<>();
            if (productJson.has("idCategories") && productJson.get("idCategories").isArray()) {
                System.out.println("📋 Processing categories...");
                for (JsonNode categoryIdNode : productJson.get("idCategories")) {
                    try {
                        String categoryIdStr = categoryIdNode.asText();
                        UUID categoryId = UUID.fromString(categoryIdStr);
                        
                        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
                        if (categoryOpt.isPresent()) {
                            ProductCategory productCategory = new ProductCategory();
                            productCategory.setCategory(categoryOpt.get());
                            productCategory.setProduct(product);
                            productCategoryList.add(productCategory);
                            System.out.println("✅ Added category: " + categoryOpt.get().getCategoryName());
                        } else {
                            System.out.println("⚠️ Category not found: " + categoryId);
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error processing category: " + e.getMessage());
                    }
                }
            }
            product.setProductCategories(productCategoryList);
            System.out.println("✅ Categories processed: " + productCategoryList.size());

            // Save product first to get ID
            Product savedProduct = productRepository.save(product);
            System.out.println("✅ Product saved with ID: " + savedProduct.getId());

            // ✅ SỬA ĐỔI: Handle images properly
            List<Gallery> galleryList = new ArrayList<>();
            
            // Process thumbnail images
            if (productJson.has("images") && productJson.get("images").isArray()) {
                System.out.println("🖼️ Processing thumbnail images...");
                for (JsonNode imageNode : productJson.get("images")) {
                    String imageUrl = imageNode.asText();
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        Gallery gallery = new Gallery();
                        gallery.setProduct(savedProduct);
                        gallery.setImage(imageUrl);
                        gallery.setIsThumbnail(true);
                        gallery.setPlaceholder("thumbnail");
                        gallery.setCreatedAt(new Date());
                        gallery.setUpdatedAt(new Date());
                        galleryRepository.save(gallery);
                        galleryList.add(gallery);
                        System.out.println("✅ Added thumbnail image");
                    }
                }
            }

            // Process gallery images
            if (productJson.has("imagePhus") && productJson.get("imagePhus").isArray()) {
                System.out.println("🖼️ Processing gallery images...");
                for (JsonNode imageNode : productJson.get("imagePhus")) {
                    String imageUrl = imageNode.asText();
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        Gallery gallery = new Gallery();
                        gallery.setProduct(savedProduct);
                        gallery.setImage(imageUrl);
                        gallery.setIsThumbnail(false);
                        gallery.setPlaceholder("gallery");
                        gallery.setCreatedAt(new Date());
                        gallery.setUpdatedAt(new Date());
                        galleryRepository.save(gallery);
                        galleryList.add(gallery);
                        System.out.println("✅ Added gallery image");
                    }
                }
            }

            System.out.println("✅ Images processed: " + galleryList.size());

            // Final save
            productRepository.save(savedProduct);
            
            System.out.println("🎉 Product creation completed successfully!");
            return ResponseEntity.ok().body(Map.of(
                "message", "Thành công!", 
                "productId", savedProduct.getId().toString(),
                "productName", savedProduct.getProductName()
            ));

        } catch (IllegalArgumentException e) {
            System.err.println("❌ Validation error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu không hợp lệ: " + e.getMessage()));
            
        } catch (Exception e) {
            System.err.println("💥 Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Có lỗi xảy ra khi tạo sản phẩm: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(UUID productId, JsonNode productJson, UUID staffId) {
        try {
            Product existingProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

            StaffAccount staff = staffAccountRepository.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));

            // Update basic fields manually (similar to save method)
            existingProduct.setSlug(productJson.get("slug").asText());
            existingProduct.setProductName(productJson.get("productName").asText());
            existingProduct.setSku(productJson.get("sku").asText());
            existingProduct.setSalePrice(new BigDecimal(productJson.get("salePrice").asDouble()));
            existingProduct.setComparePrice(new BigDecimal(productJson.get("comparePrice").asDouble()));
            existingProduct.setBuyingPrice(new BigDecimal(productJson.get("buyingPrice").asDouble()));
            existingProduct.setQuantity(productJson.get("quantity").asInt());
            existingProduct.setShortDescription(productJson.has("shortDescription") ? productJson.get("shortDescription").asText() : "");
            existingProduct.setProductDescription(productJson.has("productDescription") ? productJson.get("productDescription").asText() : "");
            
            String productTypeStr = productJson.get("productType").asText().toLowerCase();
            existingProduct.setProductType(Product.ProductType.valueOf(productTypeStr));
            
            existingProduct.setPublished(productJson.get("published").asBoolean());
            existingProduct.setDisableOutOfStock(productJson.get("disableOutOfStock").asBoolean());
            existingProduct.setNote(productJson.has("note") ? productJson.get("note").asText() : "");
            existingProduct.setUpdatedBy(staff);
            existingProduct.setUpdatedAt(new Date());

            // Update categories
            productCategoryRepository.deleteAllByProductId(productId);
            List<ProductCategory> newCategories = new ArrayList<>();
            if (productJson.has("idCategories") && productJson.get("idCategories").isArray()) {
                for (JsonNode categoryIdNode : productJson.get("idCategories")) {
                    UUID categoryId = UUID.fromString(categoryIdNode.asText());
                    Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
                    ProductCategory pc = new ProductCategory();
                    pc.setProduct(existingProduct);
                    pc.setCategory(category);
                    newCategories.add(pc);
                }
            }
            existingProduct.setProductCategories(newCategories);

            // Update images
            galleryRepository.deleteAllByProductId(productId);

            // Add thumbnail images
            if (productJson.has("images") && productJson.get("images").isArray()) {
                for (JsonNode imageNode : productJson.get("images")) {
                    String imageUrl = imageNode.asText();
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        Gallery gallery = new Gallery();
                        gallery.setProduct(existingProduct);
                        gallery.setImage(imageUrl);
                        gallery.setIsThumbnail(true);
                        gallery.setPlaceholder("thumbnail");
                        gallery.setCreatedAt(new Date());
                        gallery.setUpdatedAt(new Date());
                        galleryRepository.save(gallery);
                    }
                }
            }

            // Add gallery images
            if (productJson.has("imagePhus") && productJson.get("imagePhus").isArray()) {
                for (JsonNode imageNode : productJson.get("imagePhus")) {
                    String imageUrl = imageNode.asText();
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        Gallery gallery = new Gallery();
                        gallery.setProduct(existingProduct);
                        gallery.setImage(imageUrl);
                        gallery.setIsThumbnail(false);
                        gallery.setPlaceholder("gallery");
                        gallery.setCreatedAt(new Date());
                        gallery.setUpdatedAt(new Date());
                        galleryRepository.save(gallery);
                    }
                }
            }

            productRepository.save(existingProduct);
            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công!"));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Cập nhật thất bại: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteProduct(UUID productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

            // Check if product exists in orders
            List<OrderItem> orderItems = orderItemRepository.findByProduct(product);
            if (!orderItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Không thể xóa sản phẩm vì còn tồn tại trong đơn hàng."));
            }

            // Delete related data
            galleryRepository.deleteAllByProductId(productId);
            productCategoryRepository.deleteAllByProductId(productId);

            // Delete product
            productRepository.delete(product);

            return ResponseEntity.ok(Map.of("message", "Sản phẩm đã được xóa thành công!"));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi xóa sản phẩm: " + e.getMessage()));
        }
    }
    
    public ProductDTO getProductDetails(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductCategory> productCategories = productCategoryRepository.findByProduct(product);

        List<String> categoryNames = productCategories.stream()
                .map(pc -> pc.getCategory().getCategoryName())
                .collect(Collectors.toList());

        ProductDTO productDTO = new ProductDTO();
        // Map full product fields into ProductDTO
        productDTO.setId(product.getId());
        productDTO.setSlug(product.getSlug());
        productDTO.setProductName(product.getProductName());
        productDTO.setSku(product.getSku() != null ? product.getSku() : "");
        productDTO.setSalePrice(product.getSalePrice() != null ? product.getSalePrice() : BigDecimal.ZERO);
        productDTO.setComparePrice(product.getComparePrice() != null ? product.getComparePrice() : BigDecimal.ZERO);
        productDTO.setBuyingPrice(product.getBuyingPrice() != null ? product.getBuyingPrice() : BigDecimal.ZERO);
        productDTO.setQuantity(product.getQuantity() != null ? product.getQuantity() : 0);
        productDTO.setShortDescription(product.getShortDescription() != null ? product.getShortDescription() : "");
        productDTO.setCategoryNames(categoryNames);

        return productDTO;
    }

    @Override
    public List<Product> getProductsByCategoryId(UUID categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> getTop3LatestProducts() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 3);
        List<Product> products = productRepository.findTop3ByOrderByCreatedAtDescWithRelationships(pageable);
        return products.stream().limit(3).collect(java.util.stream.Collectors.toList());
    }
}