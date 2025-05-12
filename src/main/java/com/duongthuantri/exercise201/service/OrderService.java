package com.duongthuantri.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.duongthuantri.exercise201.entity.Customer;
import com.duongthuantri.exercise201.entity.Order;
import com.duongthuantri.exercise201.entity.OrderStatus;

public interface OrderService {
    List<Order> findAll();

    Optional<Order> findById(String id);

    List<Order> findByCustomer(Customer customer);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findByCustomerAndOrderStatus(Customer customer, OrderStatus orderStatus);

    Order save(Order order);

    void deleteById(String id);

    boolean existsById(String id);
    //
    ResponseEntity<?> checkout(UUID customerId);

    public Order approveOrder(UUID orderId, UUID staffId);

    public Order markOrderAsShipped(UUID orderId, UUID staffId);

    public Order customerAcceptOrder(UUID orderId);
}