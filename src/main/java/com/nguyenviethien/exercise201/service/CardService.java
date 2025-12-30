package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.nguyenviethien.exercise201.entity.Card;
import com.nguyenviethien.exercise201.entity.CardItem;
import com.nguyenviethien.exercise201.entity.Customer;
import com.fasterxml.jackson.databind.JsonNode;

public interface CardService {
    List<Card> getAllCards();

    public ResponseEntity<?> save(JsonNode jsonNode);

    public ResponseEntity<?> updateQuantity(UUID customerId, UUID productId, int newQuantity);

    public ResponseEntity<?> removeProductFromCart(UUID customerId, UUID productId);

    public void clearCart(UUID customerId);

        // THÊM method mới để xóa sản phẩm đã chọn
    public ResponseEntity<?> clearSelectedItems(UUID customerId, List<UUID> cardItemIds);

    int getCartItemCount(UUID customerId);

}