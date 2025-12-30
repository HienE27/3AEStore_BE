package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nguyenviethien.exercise201.entity.Card;
import com.nguyenviethien.exercise201.entity.CardItem;

public interface CardItemService {

    CardItem save(CardItem cardItem);
    
    Optional<CardItem> findById(UUID cardItemId);
    
    List<CardItem> findByCard(Card card);
    
    List<CardItem> getCardItemsByCustomerId(UUID customerId);

    // Single item operations
    boolean deleteById(UUID cardItemId, UUID customerId);
    
    boolean customerOwnsCardItem(UUID cardItemId, UUID customerId);

    // NEW: Selected items operations 
    void deleteSelectedItems(List<UUID> cardItemIds, UUID customerId);
    
    List<CardItem> getSelectedCardItems(List<UUID> cardItemIds, UUID customerId);
    
    void validateStockForSelectedItems(List<UUID> cardItemIds, UUID customerId);

    // Cart management
    void clearCart(UUID customerId);
    
    int getCartItemCount(UUID customerId);

    // Validation methods
    void validateItemOwnership(UUID cardItemId, UUID customerId);
    
    void validateItemsStock(List<CardItem> items);
}