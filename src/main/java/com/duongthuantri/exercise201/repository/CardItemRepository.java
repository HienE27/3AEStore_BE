package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Card;
import com.duongthuantri.exercise201.entity.CardItem;
import com.duongthuantri.exercise201.entity.Product;

@Repository
@RepositoryRestResource(path = "cardItems")
public interface CardItemRepository extends JpaRepository<CardItem, UUID> {
    Optional<CardItem> findByCard_IdAndProduct_Id(UUID cardId, UUID productId);

    // Phương thức tìm kiếm CardItem theo cardId và productId
    Optional<CardItem> findByCardIdAndProductId(UUID cardId, UUID productId);

    Optional<CardItem> findByCardAndProduct(Card card, Product product);

    //tìm tất cã sản phẩm trong giỏ hàng dựa trên customerId
    List<CardItem> findByCard_Customer_Id(UUID customerId);

    // Tìm kiếm Product theo CardItem Id
    @Query("SELECT ci.product FROM CardItem ci WHERE ci.id = :cardItemId")
    Optional<Product> findProductByCardItemId(@Param("cardItemId") UUID cardItemId);
    

}