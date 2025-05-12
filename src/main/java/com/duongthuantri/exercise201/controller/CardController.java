package com.duongthuantri.exercise201.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.duongthuantri.exercise201.entity.Card;
import com.duongthuantri.exercise201.entity.CardItem;
import com.duongthuantri.exercise201.entity.Customer;
import com.duongthuantri.exercise201.entity.Product;
import com.duongthuantri.exercise201.service.CardItemService;
import com.duongthuantri.exercise201.service.CardService;
import com.duongthuantri.exercise201.service.CustomerService;
import com.duongthuantri.exercise201.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    @Autowired
    private CardService cardService;

    @GetMapping
    public List<Card> getAllCards() {
        return cardService.getAllCards();
    }
    //thêm sản phẩm vào giỏ hàng
    @PostMapping("/add-product")
    public ResponseEntity<?> addProductToCart(@RequestBody JsonNode jsonData) {
        return cardService.save(jsonData);
    }
    //sửa số lượng sản phẩm trong giỏ hàng
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
}