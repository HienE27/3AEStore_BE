package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Order;
import com.nguyenviethien.exercise201.entity.OrderItem;
import com.nguyenviethien.exercise201.entity.Product;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByProduct(Product product);

    // Cải thiện method xóa với @Modifying
    @Modifying
    @Query("DELETE FROM OrderItem oi WHERE oi.order = :order")
    void deleteAllByOrder(@Param("order") Order order);

    // Tìm kiếm OrderItem theo Order và Product
    
}