package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.nguyenviethien.exercise201.DTO.ProductDTO;
import com.nguyenviethien.exercise201.entity.Product;
import com.fasterxml.jackson.databind.JsonNode;

public interface ProductService {
    List<Product> getAllProducts();

    Optional<Product> getProductById(UUID id);
    
    // ✅ THÊM METHOD NÀY
    Optional<Product> getProductByIdWithCategories(UUID id);

    public ResponseEntity<?> save(JsonNode productJson, UUID staffId);

    public ResponseEntity<?> update(UUID productId, JsonNode productJson, UUID staffId);

    public ResponseEntity<?> deleteProduct(UUID productId);

    public ProductDTO getProductDetails(UUID productId);

    public List<Product> getProductsByCategoryId(UUID categoryId);

    public List<Product> getTop3LatestProducts();
}