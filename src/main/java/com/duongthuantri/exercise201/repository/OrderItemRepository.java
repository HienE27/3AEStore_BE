package com.duongthuantri.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duongthuantri.exercise201.entity.Order;
import com.duongthuantri.exercise201.entity.OrderItem;
import com.duongthuantri.exercise201.entity.Product;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByProduct(Product product);
}