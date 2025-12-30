package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.Order;
import com.nguyenviethien.exercise201.entity.OrderStatus;

public interface OrderService {

    // Lấy tất cả đơn hàng
    List<Order> findAll();

    // Tìm đơn hàng theo id
    Optional<Order> findById(String id);

    // Tìm đơn hàng theo khách hàng (danh sách)
    List<Order> findByCustomer(Customer customer);

    // Tìm đơn hàng theo khách hàng (phân trang)
    Page<Order> findByCustomer(Customer customer, Pageable pageable);

    // Tìm đơn hàng theo trạng thái
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    // Tìm đơn hàng theo khách hàng và trạng thái
    List<Order> findByCustomerAndOrderStatus(Customer customer, OrderStatus orderStatus);

    // Lưu hoặc cập nhật đơn hàng
    Order save(Order order);

    // Xóa đơn hàng theo id
    void deleteById(String id);

    // Kiểm tra tồn tại đơn hàng theo id
    boolean existsById(String id);

    // Checkout đơn hàng cho khách hàng
    ResponseEntity<?> checkout(UUID customerId);

    // Duyệt đơn hàng
    Order approveOrder(UUID orderId, UUID staffId);

    // Đánh dấu đơn hàng đã giao
    Order markOrderAsShipped(UUID orderId, UUID staffId);

    // Khách hàng chấp nhận đơn hàng
    Order customerAcceptOrder(UUID orderId);

    // Tìm trạng thái đơn hàng theo id
    Optional<OrderStatus> findOrderStatusById(UUID id);

    void deleteOrdersByCustomer(Customer customer);

    // Tìm kiếm orders với filters
    Page<Order> findOrdersWithFilters(String search, String status, Pageable pageable);

    // Lấy pending orders
    List<Order> findPendingOrders();

    // Đếm pending orders
    long countPendingOrders();

    // Lấy orders sẵn sàng ship
    List<Order> findOrdersReadyToShip();

    // Thống kê theo tháng
    List<Object[]> getMonthlyOrderStatistics();


}
