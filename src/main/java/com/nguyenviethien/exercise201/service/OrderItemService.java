package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nguyenviethien.exercise201.entity.Order;
import com.nguyenviethien.exercise201.entity.OrderItem;
import com.nguyenviethien.exercise201.entity.Product;

public interface OrderItemService {
    List<OrderItem> findAll();

    Optional<OrderItem> findById(UUID id);

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByProduct(Product product);

    OrderItem save(OrderItem orderItem);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}