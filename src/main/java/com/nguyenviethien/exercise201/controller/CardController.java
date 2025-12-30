package com.nguyenviethien.exercise201.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.entity.Card;
import com.nguyenviethien.exercise201.entity.CardItem;
import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.service.CardItemService;
import com.nguyenviethien.exercise201.service.CardService;
import com.nguyenviethien.exercise201.service.CustomerService;
import com.nguyenviethien.exercise201.service.ProductService;
import com.nguyenviethien.exercise201.service.JWT.JwtService;

import jakarta.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardItemService cardItemService;

    @Autowired
    private JwtService jwtService;

    @GetMapping
    public List<Card> getAllCards() {
        return cardService.getAllCards();
    }

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add-product")
    public ResponseEntity<?> addProductToCart(@RequestBody JsonNode jsonData) {
        return cardService.save(jsonData);
    }

    // Sửa số lượng sản phẩm trong giỏ hàng
    @PutMapping("/update-quantity/{customerId}/{productId}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable UUID customerId,
            @PathVariable UUID productId,
            @RequestParam int quantity) {
        return cardService.updateQuantity(customerId, productId, quantity);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/remove-product/{customerId}/{productId}")
    public ResponseEntity<?> removeProductFromCart(
            @PathVariable UUID customerId,
            @PathVariable UUID productId) {
        return cardService.removeProductFromCart(customerId, productId);
    }

    // Thêm endpoint để xóa toàn bộ giỏ hàng của khách hàng
    @DeleteMapping("/clear/{customerId}")
    public ResponseEntity<Void> clearCart(@PathVariable UUID customerId) {
        try {
            cardService.clearCart(customerId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // NEW: Xóa các sản phẩm đã chọn khỏi giỏ hàng (sau khi checkout thành công)
    @DeleteMapping("/clear-selected/{customerId}")
    public ResponseEntity<?> clearSelectedItems(
            @PathVariable UUID customerId,
            @RequestBody List<UUID> cardItemIds) {
        try {
            if (cardItemIds == null || cardItemIds.isEmpty()) {
                return ResponseEntity.badRequest().body("Danh sách sản phẩm không được để trống");
            }

            cardItemService.deleteSelectedItems(cardItemIds, customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xóa " + cardItemIds.size() + " sản phẩm khỏi giỏ hàng");
            response.put("deletedCount", cardItemIds.size());
            response.put("remainingCartCount", cardItemService.getCartItemCount(customerId));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // NEW: Validate các sản phẩm đã chọn trước khi checkout
    @PostMapping("/validate-selected/{customerId}")
    public ResponseEntity<?> validateSelectedItems(
            @PathVariable UUID customerId,
            @RequestBody List<UUID> cardItemIds) {
        try {
            if (cardItemIds == null || cardItemIds.isEmpty()) {
                return ResponseEntity.badRequest().body("Danh sách sản phẩm không được để trống");
            }

            // Validate stock
            cardItemService.validateStockForSelectedItems(cardItemIds, customerId);
            
            // Get selected items info
            List<CardItem> selectedItems = cardItemService.getSelectedCardItems(cardItemIds, customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tất cả sản phẩm đã chọn có thể thanh toán");
            response.put("selectedItemsCount", selectedItems.size());
            response.put("validatedItemIds", cardItemIds);
            response.put("totalQuantity", selectedItems.stream()
                .mapToInt(CardItem::getQuantity)
                .sum());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // NEW: Lấy thông tin các sản phẩm đã chọn
    @PostMapping("/get-selected/{customerId}")
    public ResponseEntity<?> getSelectedItems(
            @PathVariable UUID customerId,
            @RequestBody List<UUID> cardItemIds) {
        try {
            if (cardItemIds == null || cardItemIds.isEmpty()) {
                return ResponseEntity.badRequest().body("Danh sách sản phẩm không được để trống");
            }

            List<CardItem> selectedItems = cardItemService.getSelectedCardItems(cardItemIds, customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("selectedItems", selectedItems);
            response.put("count", selectedItems.size());
            response.put("totalQuantity", selectedItems.stream()
                .mapToInt(CardItem::getQuantity)
                .sum());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCartItemCount(HttpServletRequest request) {
        // Lấy token từ header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("No Authorization header");
        }
        String token = authHeader.substring(7); // Bỏ "Bearer "
        UUID customerId;
        try {
            customerId = jwtService.extractId(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        int count = cardService.getCartItemCount(customerId);
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}