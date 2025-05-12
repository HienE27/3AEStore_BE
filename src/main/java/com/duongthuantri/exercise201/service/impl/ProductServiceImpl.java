package com.duongthuantri.exercise201.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.duongthuantri.exercise201.entity.Category;
import com.duongthuantri.exercise201.entity.Gallery;
import com.duongthuantri.exercise201.entity.Product;
import com.duongthuantri.exercise201.entity.ProductCategory;
import com.duongthuantri.exercise201.entity.StaffAccount;
import com.duongthuantri.exercise201.repository.CategoryRepository;
import com.duongthuantri.exercise201.repository.GalleryRepository;
import com.duongthuantri.exercise201.repository.ProductCategoryRepository;
import com.duongthuantri.exercise201.repository.ProductRepository;
import com.duongthuantri.exercise201.repository.StaffAccountRepository;
import com.duongthuantri.exercise201.service.ProductService;
import com.duongthuantri.exercise201.service.util.Base64ToMultipartFileConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProductServiceImpl implements ProductService {
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
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id);
    }
    //
    @Override
    public ResponseEntity<?> save(JsonNode productJson, UUID staffId) {
        try {
            Product product = objectMapper.treeToValue(productJson, Product.class);

            StaffAccount staff = staffAccountRepository.findById(staffId)
            .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));
            product.setCreatedBy(staff);
            product.setUpdatedBy(staff);

            //lưu thể loại của sản phẩm
            List<UUID> idCategoryList = objectMapper.readValue(productJson.get("idCategories").traverse(), new TypeReference<List<UUID>>() {
            });
            List<Category> categoryList = new ArrayList<>();
            for (UUID idCategory : idCategoryList) {
                Optional<Category> category = categoryRepository.findById(idCategory);
                categoryList.add(category.get());
            }
            List<ProductCategory> productCategoryList = new ArrayList<>();
            for (Category category : categoryList) {
                ProductCategory productCategory = new ProductCategory();
                productCategory.setCategory(category);
                productCategory.setProduct(product);
                productCategoryList.add(productCategory);
            }
            product.setProductCategories(productCategoryList);
            //lưu trước sản phẩm để lấy id đặt tên cho ảnh
            Product newProduct = productRepository.save(product);
            //Lưu ảnh của sản phẩm
            List<Gallery> galleryList = new ArrayList<>();
            List<String> imageList = objectMapper.readValue(productJson.get("images").traverse(), new TypeReference<List<String>>() {
            });
            for (String image : imageList) {
                Gallery gallery = new Gallery();
                gallery.setProduct(newProduct);
                gallery.setImage(image); // giữ nguyên base64
                // gallery.setIsThumbnail(null);
                gallery.setPlaceholder("placeholder-value"); // phải có hoặc cho nullable
                gallery.setCreatedAt(new Date());
                gallery.setUpdatedAt(new Date());
                galleryRepository.save(gallery);
            }   
            //cập nhật lại ảnh cho sản phẩm
            productRepository.save(newProduct);
            

            return ResponseEntity.ok("Thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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

            // Cập nhật thông tin cơ bản
            Product updatedProduct = objectMapper.readerForUpdating(existingProduct).readValue(productJson);
            updatedProduct.setUpdatedBy(staff);
            updatedProduct.setUpdatedAt(new Date());

            // Cập nhật thể loại sản phẩm
            List<UUID> idCategoryList = objectMapper.readValue(productJson.get("idCategories").traverse(), new TypeReference<List<UUID>>() {});
            // Xoá các liên kết cũ
            productCategoryRepository.deleteAllByProductId(productId);
            // Thêm lại mới
            List<ProductCategory> newCategories = new ArrayList<>();
            for (UUID idCategory : idCategoryList) {
                Category category = categoryRepository.findById(idCategory)
                        .orElseThrow(() -> new RuntimeException("Category not found: " + idCategory));
                ProductCategory pc = new ProductCategory();
                pc.setProduct(updatedProduct);
                pc.setCategory(category);
                newCategories.add(pc);
            }
            updatedProduct.setProductCategories(newCategories);

            // Cập nhật ảnh
            List<String> imageList = objectMapper.readValue(productJson.get("images").traverse(), new TypeReference<List<String>>() {});
            // Xoá ảnh cũ
            galleryRepository.deleteAllByProductId(productId);
            // Thêm lại ảnh mới
            for (String image : imageList) {
                Gallery gallery = new Gallery();
                gallery.setProduct(updatedProduct);
                gallery.setImage(image);
                gallery.setPlaceholder("placeholder-value"); // bạn có thể set theo logic
                gallery.setCreatedAt(new Date());
                gallery.setUpdatedAt(new Date());
                galleryRepository.save(gallery);
            }

            productRepository.save(updatedProduct);
            return ResponseEntity.ok("Cập nhật thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Cập nhật thất bại: " + e.getMessage());
        }
    }


    @Override
    public ResponseEntity<?> deleteProduct(UUID productId) {
        try {
            // Tìm sản phẩm theo ID
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

            // Xóa các ảnh của sản phẩm trong bảng Gallery
            galleryRepository.deleteAllByProductId(productId);

            // Xóa sản phẩm trong bảng Product
            productRepository.delete(product);

            return ResponseEntity.ok("Sản phẩm và ảnh đã được xóa thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

}