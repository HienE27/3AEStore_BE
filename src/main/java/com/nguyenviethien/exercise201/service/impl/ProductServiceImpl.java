package com.nguyenviethien.exercise201.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nguyenviethien.exercise201.entity.*;
import com.nguyenviethien.exercise201.repository.*;
import com.nguyenviethien.exercise201.service.ProductService;
import com.nguyenviethien.exercise201.service.AIGenerateDescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StaffAccountRepository staffAccountRepository;
    private final GalleryRepository galleryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final AIGenerateDescriptionService aiGenerateDescriptionService;

    @Autowired
    public ProductServiceImpl(
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            StaffAccountRepository staffAccountRepository,
            GalleryRepository galleryRepository,
            ProductCategoryRepository productCategoryRepository,
            AIGenerateDescriptionService aiGenerateDescriptionService) {
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.staffAccountRepository = staffAccountRepository;
        this.galleryRepository = galleryRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.aiGenerateDescriptionService = aiGenerateDescriptionService;
    }

    @Override
    public List<Product> getAllProducts() {
        log.debug("Fetching all products");
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
        log.info("Starting product creation process for staff: {}", staffId);

        try {
            StaffAccount staff = staffAccountRepository.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));

            Product product = new Product();

            String slug = productJson.has("slug") && !productJson.get("slug").asText().trim().isEmpty()
                    ? productJson.get("slug").asText().trim()
                    : generateSlug(productJson.get("productName").asText(), null);
            product.setSlug(slug);
            product.setProductName(productJson.get("productName").asText());

            String sku = productJson.has("sku") && !productJson.get("sku").asText().trim().isEmpty()
                    ? productJson.get("sku").asText().trim()
                    : generateSku(productJson.get("productName").asText(), null);
            product.setSku(sku);

            product.setSalePrice(new BigDecimal(productJson.get("salePrice").asDouble()));
            product.setComparePrice(new BigDecimal(productJson.get("comparePrice").asDouble()));
            product.setBuyingPrice(new BigDecimal(productJson.get("buyingPrice").asDouble()));

            product.setQuantity(productJson.get("quantity").asInt());
            product.setShortDescription(
                    productJson.has("shortDescription") ? productJson.get("shortDescription").asText() : "");

            String productDescription = productJson.has("productDescription")
                    ? productJson.get("productDescription").asText()
                    : "";
            if (productDescription == null || productDescription.trim().isEmpty()) {
                log.info("Auto-generating description for product: {}", product.getProductName());
                productDescription = aiGenerateDescriptionService.generateDescription(product.getProductName());
            }
            product.setProductDescription(productDescription);

            String productTypeStr = productJson.get("productType").asText().toLowerCase();
            product.setProductType(Product.ProductType.valueOf(productTypeStr));

            product.setPublished(productJson.get("published").asBoolean());
            product.setDisableOutOfStock(productJson.get("disableOutOfStock").asBoolean());
            product.setNote(productJson.has("note") ? productJson.get("note").asText() : "");

            product.setCreatedBy(staff);
            product.setUpdatedBy(staff);
            product.setCreatedAt(new Date());
            product.setUpdatedAt(new Date());

            List<ProductCategory> productCategoryList = new ArrayList<>();
            if (productJson.has("idCategories") && productJson.get("idCategories").isArray()) {
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
                        }
                    } catch (Exception e) {
                        log.error("Error processing category: {}", e.getMessage());
                    }
                }
            }
            product.setProductCategories(productCategoryList);

            Product savedProduct = productRepository.save(product);
            log.info("Product saved with ID: {}", savedProduct.getId());

            List<Gallery> galleryList = new ArrayList<>();

            if (productJson.has("images") && productJson.get("images").isArray()) {
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
                    }
                }
            }

            if (productJson.has("imagePhus") && productJson.get("imagePhus").isArray()) {
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
                    }
                }
            }

            productRepository.save(savedProduct);

            log.info("Product creation completed successfully: {}", savedProduct.getProductName());
            return ResponseEntity.ok(Map.of(
                    "message", "Thành công!",
                    "productId", savedProduct.getId().toString(),
                    "productName", savedProduct.getProductName()));

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu không hợp lệ: " + e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error during product creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi tạo sản phẩm: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(UUID productId, JsonNode productJson, UUID staffId) {
        log.info("Starting product update for ID: {}", productId);

        try {
            Product existingProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

            StaffAccount staff = staffAccountRepository.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));

            String slug = productJson.has("slug") && !productJson.get("slug").asText().trim().isEmpty()
                    ? productJson.get("slug").asText().trim()
                    : generateSlug(productJson.get("productName").asText(), productId);
            existingProduct.setSlug(slug);
            existingProduct.setProductName(productJson.get("productName").asText());

            String sku = productJson.has("sku") && !productJson.get("sku").asText().trim().isEmpty()
                    ? productJson.get("sku").asText().trim()
                    : generateSku(productJson.get("productName").asText(), productId);
            existingProduct.setSku(sku);
            existingProduct.setSalePrice(new BigDecimal(productJson.get("salePrice").asDouble()));
            existingProduct.setComparePrice(new BigDecimal(productJson.get("comparePrice").asDouble()));
            existingProduct.setBuyingPrice(new BigDecimal(productJson.get("buyingPrice").asDouble()));
            existingProduct.setQuantity(productJson.get("quantity").asInt());
            existingProduct.setShortDescription(
                    productJson.has("shortDescription") ? productJson.get("shortDescription").asText() : "");

            String productDescription = productJson.has("productDescription")
                    ? productJson.get("productDescription").asText()
                    : "";
            if (productDescription == null || productDescription.trim().isEmpty()) {
                log.info("Auto-generating description for product: {}", existingProduct.getProductName());
                productDescription = aiGenerateDescriptionService.generateDescription(existingProduct.getProductName());
            }
            existingProduct.setProductDescription(productDescription);

            String productTypeStr = productJson.get("productType").asText().toLowerCase();
            existingProduct.setProductType(Product.ProductType.valueOf(productTypeStr));

            existingProduct.setPublished(productJson.get("published").asBoolean());
            existingProduct.setDisableOutOfStock(productJson.get("disableOutOfStock").asBoolean());
            existingProduct.setNote(productJson.has("note") ? productJson.get("note").asText() : "");
            existingProduct.setUpdatedBy(staff);
            existingProduct.setUpdatedAt(new Date());

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

            galleryRepository.deleteAllByProductId(productId);

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
            log.info("Product updated successfully: {}", productId);
            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công!"));

        } catch (Exception e) {
            log.error("Error updating product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Cập nhật thất bại: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteProduct(UUID productId) {
        log.info("Deleting product: {}", productId);

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

            List<OrderItem> orderItems = orderItemRepository.findByProduct(product);
            if (!orderItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Không thể xóa sản phẩm vì còn tồn tại trong đơn hàng."));
            }

            galleryRepository.deleteAllByProductId(productId);
            productCategoryRepository.deleteAllByProductId(productId);

            productRepository.delete(product);

            log.info("Product deleted successfully: {}", productId);
            return ResponseEntity.ok(Map.of("message", "Sản phẩm đã được xóa thành công!"));

        } catch (Exception e) {
            log.error("Error deleting product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi xóa sản phẩm: " + e.getMessage()));
        }
    }

    @Override
    public com.nguyenviethien.exercise201.DTO.ProductDetailsDTO getProductDetails(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductCategory> productCategories = productCategoryRepository.findByProduct(product);

        List<String> categoryNames = productCategories.stream()
                .map(pc -> pc.getCategory().getCategoryName())
                .collect(Collectors.toList());

        com.nguyenviethien.exercise201.DTO.ProductDetailsDTO productDTO = new com.nguyenviethien.exercise201.DTO.ProductDetailsDTO();
        productDTO.setId(product.getId());
        productDTO.setProductName(product.getProductName());
        productDTO.setDescription(product.getProductDescription() != null ? product.getProductDescription() : "");
        productDTO.setPrice(product.getSalePrice() != null ? product.getSalePrice() : BigDecimal.ZERO);
        productDTO.setQuantity(product.getQuantity() != null ? product.getQuantity() : 0);
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

    @Override
    public Long countByCategoryId(UUID categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    @Override
    public Long countAllProducts() {
        return productRepository.count();
    }

    private String generateSlug(String productName, UUID existingProductId) {
        if (productName == null || productName.trim().isEmpty()) {
            return "product-" + System.currentTimeMillis();
        }

        String normalized = normalizeVietnamese(productName.toLowerCase().trim());

        String slug = normalized.replaceAll("[^a-z0-9\\s-]", "")
                                .replaceAll("\\s+", "-")
                                .replaceAll("-+", "-")
                                .replaceAll("^-|-$", "");

        String baseSlug = slug;
        int counter = 1;
        while (isSlugExists(baseSlug, existingProductId)) {
            baseSlug = slug + "-" + counter;
            counter++;
        }

        return baseSlug;
    }

    private String generateSku(String productName, UUID existingProductId) {
        String prefix = "SKU";
        if (productName != null && !productName.trim().isEmpty()) {
            String normalized = normalizeVietnamese(productName.toUpperCase().replaceAll("[^A-Z0-9]", ""));
            prefix = normalized.length() >= 3 ? normalized.substring(0, 3) : normalized + "X";
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomSuffix = String.valueOf((int) (Math.random() * 1000));

        String baseSku = prefix + "-" + timestamp.substring(timestamp.length() - 6) + "-" + randomSuffix;

        String finalSku = baseSku;
        int counter = 1;
        while (isSkuExists(finalSku, existingProductId)) {
            finalSku = prefix + "-" + timestamp.substring(timestamp.length() - 6) + "-" + (1000 + counter);
            counter++;
        }

        return finalSku;
    }

    private boolean isSlugExists(String slug, UUID excludeProductId) {
        if (slug == null) return false;
        Optional<Product> existing = productRepository.findBySlug(slug);
        if (existing.isEmpty()) return false;
        if (excludeProductId != null && existing.get().getId().equals(excludeProductId)) return false;
        return true;
    }

    private boolean isSkuExists(String sku, UUID excludeProductId) {
        if (sku == null) return false;
        Optional<Product> existing = productRepository.findBySku(sku);
        if (existing.isEmpty()) return false;
        if (excludeProductId != null && existing.get().getId().equals(excludeProductId)) return false;
        return true;
    }

    private String normalizeVietnamese(String input) {
        if (input == null) return "";
        return input.replaceAll("[àáảãạâầấẩẫậăằắẳẵặ]", "a")
                    .replaceAll("[ÀÁẢÃẠÂẦẤẨẪẬĂẰẮẲẴẶ]", "A")
                    .replaceAll("[èéẻẽẹêềếểễệ]", "e")
                    .replaceAll("[ÈÉẺẼẸÊỀẾỂỄỆ]", "E")
                    .replaceAll("[ìíỉĩị]", "i")
                    .replaceAll("[ÌÍỈĨỊ]", "I")
                    .replaceAll("[òóỏõọôồốổỗộơờớởỡợ]", "o")
                    .replaceAll("[ÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢ]", "O")
                    .replaceAll("[ùúủũụưừứửữự]", "u")
                    .replaceAll("[ÙÚỦŨỤƯỪỨỬỮỰ]", "U")
                    .replaceAll("[ýỳỷỹỵ]", "y")
                    .replaceAll("[ÝỲỶỸỴ]", "Y")
                    .replaceAll("đ", "d")
                    .replaceAll("Đ", "D");
    }
}
