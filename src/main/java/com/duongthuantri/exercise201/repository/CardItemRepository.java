package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
}