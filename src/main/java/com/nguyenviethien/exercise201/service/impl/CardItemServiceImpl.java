package com.nguyenviethien.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Card;
import com.nguyenviethien.exercise201.entity.CardItem;
import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.repository.CardItemRepository;
import com.nguyenviethien.exercise201.repository.CardRepository;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import com.nguyenviethien.exercise201.service.CardItemService;

@Service
@Transactional
public class CardItemServiceImpl implements CardItemService {

    @Autowired
    private CardItemRepository cardItemRepository;

    @Autowired
    private CardRepository cardRepository;
    
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public CardItem save(CardItem cardItem) {
        return cardItemRepository.save(cardItem);
    }

    @Override
    public Optional<CardItem> findById(UUID cardItemId) {
        return cardItemRepository.findById(cardItemId);
    }

    @Override
    public List<CardItem> findByCard(Card card) {
        return cardItemRepository.findByCard(card);
    }

    @Override
    public List<CardItem> getCardItemsByCustomerId(UUID customerId) {
        return cardItemRepository.findByCard_Customer_Id(customerId);
    }

    @Override
    public boolean deleteById(UUID cardItemId, UUID customerId) {
        try {
            Optional<CardItem> cardItemOpt = cardItemRepository.findById(cardItemId);
            if (cardItemOpt.isEmpty()) {
                throw new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng");
            }

            CardItem cardItem = cardItemOpt.get();
            
            // Validate customer ownership
            validateItemOwnership(cardItemId, customerId);

            cardItemRepository.delete(cardItem);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
    }

    // NEW: Delete selected items
    @Override
    @Transactional
    public void deleteSelectedItems(List<UUID> cardItemIds, UUID customerId) {
        try {
            if (cardItemIds == null || cardItemIds.isEmpty()) {
                throw new RuntimeException("Danh sách sản phẩm không được để trống");
            }

            // Validate customer ownership for all items
            List<CardItem> itemsToDelete = cardItemRepository.findAllById(cardItemIds);
            
            if (itemsToDelete.isEmpty()) {
                throw new RuntimeException("Không tìm thấy sản phẩm nào để xóa");
            }

            for (CardItem item : itemsToDelete) {
                if (!item.getCard().getCustomer().getId().equals(customerId)) {
                    throw new RuntimeException("Bạn không có quyền xóa một số sản phẩm trong giỏ hàng");
                }
            }
            
            // Delete all validated items
            cardItemRepository.deleteAll(itemsToDelete);
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa các sản phẩm đã chọn: " + e.getMessage());
        }
    }

    // NEW: Get selected cart items
    @Override
    public List<CardItem> getSelectedCardItems(List<UUID> cardItemIds, UUID customerId) {
        if (cardItemIds == null || cardItemIds.isEmpty()) {
            throw new RuntimeException("Danh sách ID sản phẩm không được để trống");
        }

        List<CardItem> allItems = cardItemRepository.findAllById(cardItemIds);
        
        // Filter only items belonging to the customer
        List<CardItem> customerItems = allItems.stream()
                .filter(item -> item.getCard().getCustomer().getId().equals(customerId))
                .collect(Collectors.toList());

        if (customerItems.size() != cardItemIds.size()) {
            throw new RuntimeException("Một số sản phẩm không thuộc về giỏ hàng của bạn");
        }

        return customerItems;
    }

    // NEW: Validate stock for selected items
    @Override
    public void validateStockForSelectedItems(List<UUID> cardItemIds, UUID customerId) {
        List<CardItem> selectedItems = getSelectedCardItems(cardItemIds, customerId);
        validateItemsStock(selectedItems);
    }

    @Override
    public void validateItemsStock(List<CardItem> items) {
        for (CardItem item : items) {
            if (item.getProduct().getQuantity() < item.getQuantity()) {
                throw new RuntimeException(
                    String.format("Sản phẩm '%s' chỉ còn %d trong kho, không đủ cho số lượng %d bạn đã chọn",
                        item.getProduct().getProductName(),
                        item.getProduct().getQuantity(),
                        item.getQuantity())
                );
            }
        }
    }

    @Override
    @Transactional
    public void clearCart(UUID customerId) {
        try {
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isEmpty()) {
                throw new RuntimeException("Không tìm thấy khách hàng");
            }

            Customer customer = customerOpt.get();
            Optional<Card> cardOpt = cardRepository.findByCustomer(customer);
            
            if (cardOpt.isPresent()) {
                Card card = cardOpt.get();
                cardItemRepository.deleteAllByCard(card);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa giỏ hàng: " + e.getMessage());
        }
    }

    @Override
    public int getCartItemCount(UUID customerId) {
        return cardItemRepository.countAllProductInCart(customerId);
    }

    @Override
    public boolean customerOwnsCardItem(UUID cardItemId, UUID customerId) {
        Optional<CardItem> cardItemOpt = cardItemRepository.findById(cardItemId);
        return cardItemOpt.isPresent() && 
               cardItemOpt.get().getCard().getCustomer().getId().equals(customerId);
    }

    @Override
    public void validateItemOwnership(UUID cardItemId, UUID customerId) {
        if (!customerOwnsCardItem(cardItemId, customerId)) {
            throw new RuntimeException("Bạn không có quyền thao tác với sản phẩm này");
        }
    }
}