package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.OrderStatus;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, UUID> {
   // Optional<OrderStatus> findByStatusName(String statusName);

    //boolean existsByStatusName(String statusName);

    // Mới thêm để order
    Optional<OrderStatus> findByStatusName(String statusName);
    
    @Query("SELECT os FROM OrderStatus os ORDER BY os.created_at ASC")
    List<OrderStatus> findAllOrderedByCreatedAt();
    
    boolean existsByStatusName(String statusName);
}
