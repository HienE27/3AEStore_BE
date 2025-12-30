package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Card;
import com.nguyenviethien.exercise201.entity.CardItem;
import com.nguyenviethien.exercise201.entity.Product;

@Repository
@RepositoryRestResource(path = "cardItems")
public interface CardItemRepository extends JpaRepository<CardItem, UUID> {

    // Basic queries
    Optional<CardItem> findByCard_IdAndProduct_Id(UUID cardId, UUID productId);

    Optional<CardItem> findByCardIdAndProductId(UUID cardId, UUID productId);

    Optional<CardItem> findByCardAndProduct(Card card, Product product);

    List<CardItem> findByCard_Customer_Id(UUID customerId);

    List<CardItem> findByCard(Card card);

    // Enhanced queries
    @Query("SELECT ci FROM CardItem ci JOIN FETCH ci.product WHERE ci.card = :card")
    List<CardItem> findByCardWithProduct(@Param("card") Card card);

    @Query("SELECT ci FROM CardItem ci JOIN FETCH ci.product WHERE ci.card.customer.id = :customerId")
    List<CardItem> findByCustomerIdWithProduct(@Param("customerId") UUID customerId);

    // Product lookup by cart item
    @Query("SELECT ci.product FROM CardItem ci WHERE ci.id = :cardItemId")
    Optional<Product> findProductByCardItemId(@Param("cardItemId") UUID cardItemId);

    // Batch operations
    @Modifying
    @Query("DELETE FROM CardItem ci WHERE ci.card = :card")
    void deleteAllByCard(@Param("card") Card card);

    @Modifying
    @Query("DELETE FROM CardItem ci WHERE ci.card.customer.id = :customerId")
    void deleteAllByCustomerId(@Param("customerId") UUID customerId);

    // Statistics
    @Query("SELECT SUM(ci.quantity) FROM CardItem ci WHERE ci.card.customer.id = :customerId")
    Integer sumQuantityByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COUNT(ci) FROM CardItem ci WHERE ci.card.customer.id = :customerId")
    int countByCustomerId(@Param("customerId") UUID customerId);

    // Stock validation
    @Query("SELECT ci FROM CardItem ci JOIN ci.product p WHERE ci.card.customer.id = :customerId AND p.quantity < ci.quantity")
    List<CardItem> findItemsWithInsufficientStock(@Param("customerId") UUID customerId);

    // Find duplicate items
    @Query("SELECT ci FROM CardItem ci WHERE ci.card = :card AND ci.product = :product")
    List<CardItem> findDuplicateItems(@Param("card") Card card, @Param("product") Product product);

    // Đếm tổng số lượng sản phẩm trong giỏ hàng của user
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CardItem ci WHERE ci.card.customer.id = :customerId")
    int countAllProductInCart(@Param("customerId") UUID customerId);

    void deleteById(UUID id);

}