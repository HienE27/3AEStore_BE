package com.nguyenviethien.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Order;
import com.nguyenviethien.exercise201.entity.OrderItem;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.repository.OrderItemRepository;
import com.nguyenviethien.exercise201.service.OrderItemService;

@Service
@Transactional
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public List<OrderItem> findAll() {
        return orderItemRepository.findAll();
    }

    @Override
    public Optional<OrderItem> findById(UUID id) {
        return orderItemRepository.findById(id);
    }

    @Override
    public List<OrderItem> findByOrder(Order order) {
        return orderItemRepository.findByOrder(order);
    }

    @Override
    public List<OrderItem> findByProduct(Product product) {
        return orderItemRepository.findByProduct(product);
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public void deleteById(UUID id) {
        orderItemRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return orderItemRepository.existsById(id);
    }
}