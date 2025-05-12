package com.duongthuantri.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.duongthuantri.exercise201.entity.Product;
import com.fasterxml.jackson.databind.JsonNode;

public interface ProductService {
    List<Product> getAllProducts();

    Optional<Product> getProductById(UUID id);

    public ResponseEntity<?> save(JsonNode productJson, UUID staffId);

    // public ResponseEntity<?> update(JsonNode bookJson);
    public ResponseEntity<?> update(UUID productId, JsonNode productJson, UUID staffId);
    // Product updateProduct(UUID id, Product product);

    // void deleteProduct(UUID id);
    public ResponseEntity<?> deleteProduct(UUID productId);
}