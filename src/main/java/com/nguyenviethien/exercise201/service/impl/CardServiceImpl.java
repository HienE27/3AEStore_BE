package com.nguyenviethien.exercise201.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Card;
import com.nguyenviethien.exercise201.entity.CardItem;
import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.repository.CardItemRepository;
import com.nguyenviethien.exercise201.repository.CardRepository;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import com.nguyenviethien.exercise201.repository.ProductRepository;
import com.nguyenviethien.exercise201.service.CardService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class CardServiceImpl implements CardService {

    private final ObjectMapper objectMapper;

    public CardServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CardItemRepository cardItemRepository;

    @Autowired
    private CardRepository cardRepository;

    @Override
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    @Override
    public ResponseEntity<?> save(JsonNode jsonData) {
        try {
            UUID customerId = UUID.fromString(jsonData.get("idCustomer").asText());

            // 1. Kiểm tra customer tồn tại
            Optional<Customer> customerOptional = customerRepository.findById(customerId);
            if (!customerOptional.isPresent()) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            Customer customer = customerOptional.get();

            // 2. Lấy hoặc tạo mới giỏ hàng
            Card card = cardRepository.findByCustomerId(customerId)
                    .orElseGet(() -> {
                        Card newCard = new Card();
                        newCard.setCustomer(customer);
                        return cardRepository.save(newCard);
                    });

            // 3. Duyệt qua từng sản phẩm
            for (JsonNode productNode : jsonData.get("products")) {
                UUID productId = UUID.fromString(productNode.get("productId").asText());
                int quantity = productNode.get("quantity").asInt();

                // Kiểm tra sản phẩm có tồn tại không
                Optional<Product> productOptional = productRepository.findById(productId);
                if (!productOptional.isPresent()) {
                    return ResponseEntity.badRequest().body("Product not found for ID: " + productId);
                }
                Product product = productOptional.get();

                // 4. Kiểm tra sản phẩm đã có trong giỏ chưa
                Optional<CardItem> existingItemOpt = cardItemRepository.findByCardAndProduct(card, product);
                if (existingItemOpt.isPresent()) {
                    CardItem existingItem = existingItemOpt.get();
                    existingItem.setQuantity(existingItem.getQuantity() + quantity);
                    cardItemRepository.save(existingItem);
                } else {
                    CardItem newItem = new CardItem();
                    newItem.setCard(card);
                    newItem.setProduct(product);
                    newItem.setQuantity(quantity);
                    cardItemRepository.save(newItem);
                }
            }

            return ResponseEntity.ok("Products added to cart successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error processing the request: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> updateQuantity(UUID customerId, UUID productId, int newQuantity) {
        try {
            // Kiểm tra xem Customer có tồn tại không
            Optional<Customer> customerOptional = customerRepository.findById(customerId);
            if (!customerOptional.isPresent()) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            Customer customer = customerOptional.get();

            // Kiểm tra xem giỏ hàng của khách hàng có tồn tại không
            Optional<Card> cardOptional = cardRepository.findByCustomerId(customerId);
            if (!cardOptional.isPresent()) {
                return ResponseEntity.badRequest().body("Cart not found for customer");
            }
            Card card = cardOptional.get();

            // Kiểm tra xem CardItem có tồn tại không với sản phẩm và giỏ hàng hiện tại
            Optional<CardItem> cardItemOptional = cardItemRepository.findByCardIdAndProductId(card.getId(), productId);
            if (cardItemOptional.isPresent()) {
                // Nếu CardItem đã tồn tại, cập nhật số lượng
                CardItem cardItem = cardItemOptional.get();
                cardItem.setQuantity(newQuantity);
                cardItemRepository.save(cardItem);
                return ResponseEntity.ok("Product quantity updated successfully");
            } else {
                return ResponseEntity.badRequest().body("Product not found in cart");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating quantity: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> removeProductFromCart(UUID customerId, UUID productId) {
        try {
            // Kiểm tra xem Customer có tồn tại không
            Optional<Customer> customerOptional = customerRepository.findById(customerId);
            if (!customerOptional.isPresent()) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            Customer customer = customerOptional.get();

            // Kiểm tra xem giỏ hàng của khách hàng có tồn tại không
            Optional<Card> cardOptional = cardRepository.findByCustomerId(customerId);
            if (!cardOptional.isPresent()) {
                return ResponseEntity.badRequest().body("Cart not found for customer");
            }
            Card card = cardOptional.get();

            // Kiểm tra xem CardItem có tồn tại không với sản phẩm và giỏ hàng hiện tại
            Optional<CardItem> cardItemOptional = cardItemRepository.findByCardIdAndProductId(card.getId(), productId);
            if (cardItemOptional.isPresent()) {
                // Nếu CardItem đã tồn tại, xóa sản phẩm khỏi giỏ hàng
                cardItemRepository.delete(cardItemOptional.get());
                return ResponseEntity.ok("Product removed from cart successfully");
            } else {
                return ResponseEntity.badRequest().body("Product not found in cart");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error removing product from cart: " + e.getMessage());
        }
    }

    @Override
    public void clearCart(UUID customerId) {
        // Tìm giỏ hàng của khách hàng
        Optional<Card> cardOptional = cardRepository.findByCustomerId(customerId);
        if (cardOptional.isPresent()) {
            Card card = cardOptional.get();
            // Xóa tất cả CardItem liên quan
            cardItemRepository.deleteAllByCard(card);
            // Xóa giỏ hàng
            cardRepository.delete(card);
        }
    }

    @Override
    public int getCartItemCount(UUID customerId) {
        return cardItemRepository.countAllProductInCart(customerId);
    }

    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }





@Override
public ResponseEntity<?> clearSelectedItems(UUID customerId, List<UUID> cardItemIds) {
    try {
        if (cardItemIds == null || cardItemIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Danh sách sản phẩm không được để trống");
        }

        // Kiểm tra customer tồn tại
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (!customerOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Khách hàng không tồn tại");
        }

        // Kiểm tra và xóa từng item
        List<CardItem> itemsToDelete = new ArrayList<>();
        for (UUID cardItemId : cardItemIds) {
            Optional<CardItem> cardItemOpt = cardItemRepository.findById(cardItemId);
            if (cardItemOpt.isPresent()) {
                CardItem cardItem = cardItemOpt.get();
                // Kiểm tra item có thuộc về customer không
                if (cardItem.getCard().getCustomer().getId().equals(customerId)) {
                    itemsToDelete.add(cardItem);
                }
            }
        }

        if (itemsToDelete.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy sản phẩm hợp lệ để xóa");
        }

        // Xóa các items
        cardItemRepository.deleteAll(itemsToDelete);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã xóa " + itemsToDelete.size() + " sản phẩm khỏi giỏ hàng thành công");
        response.put("deletedCount", itemsToDelete.size());
        response.put("remainingCartCount", getCartItemCount(customerId));
        
        return ResponseEntity.ok(response);

    } catch (Exception e) {
        e.printStackTrace();
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Lỗi khi xóa sản phẩm: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}


    
}