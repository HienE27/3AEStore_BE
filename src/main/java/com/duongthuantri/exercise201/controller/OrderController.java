package com.duongthuantri.exercise201.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.duongthuantri.exercise201.entity.Customer;
import com.duongthuantri.exercise201.entity.Order;
import com.duongthuantri.exercise201.entity.OrderItem;
import com.duongthuantri.exercise201.entity.OrderStatus;
import com.duongthuantri.exercise201.service.CustomerService;
import com.duongthuantri.exercise201.service.OrderItemService;
import com.duongthuantri.exercise201.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable String customerId) {
        return customerService.findById(java.util.UUID.fromString(customerId))
                .map(customer -> ResponseEntity.ok(orderService.findByCustomer(customer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{statusId}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String statusId) {
        return orderService.findById(statusId)
                .map(order -> ResponseEntity.ok(
                        orderService.findByOrderStatus(order.getOrderStatus())))
                .orElse(ResponseEntity.notFound().build());
    }

    //checkout
     @PostMapping("/checkout/{customerId}")
    public ResponseEntity<?> checkout(@PathVariable UUID customerId) {
        return orderService.checkout(customerId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable String id,
            @RequestBody Order order) {
        if (!orderService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        order.setId(id);
        return ResponseEntity.ok(orderService.save(order));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable UUID orderId) {
        try {
            orderService.deleteById(orderId.toString());
            return ResponseEntity.ok("Xóa đơn hàng thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Xóa đơn hàng thất bại.");
        }
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItem>> getOrderItems(@PathVariable String orderId) {
        return orderService.findById(orderId)
                .map(order -> ResponseEntity.ok(orderItemService.findByOrder(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderItem> addOrderItem(
            @PathVariable String orderId,
            @RequestBody OrderItem orderItem) {
        return orderService.findById(orderId)
                .map(order -> {
                    orderItem.setOrder(order);
                    return ResponseEntity.ok(orderItemService.save(orderItem));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Void> deleteOrderItem(
            @PathVariable String orderId,
            @PathVariable String itemId) {
        if (!orderService.existsById(orderId)) {
            return ResponseEntity.notFound().build();
        }
        orderItemService.deleteById(java.util.UUID.fromString(itemId));
        return ResponseEntity.ok().build();
    }
    //chập nhận đơn hàng
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveOrder(@PathVariable("id") UUID orderId,
                                        @RequestParam("staffId") UUID staffId) {
        try {
            Order approvedOrder = orderService.approveOrder(orderId, staffId);
            return ResponseEntity.ok(approvedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    //chấp nhận chuyển giao đơn hàng
    @PutMapping("/{orderId}/ship")
    public ResponseEntity<Order> markOrderAsShipped(
            @PathVariable UUID orderId,
            @RequestParam UUID staffId
    ) {
        Order order = orderService.markOrderAsShipped(orderId, staffId);
        return ResponseEntity.ok(order);
    }
    //khách hàng chấp nhận đơn hàng
    @PutMapping("/{orderId}/accept")
    public ResponseEntity<Order> customerAcceptOrder(@PathVariable UUID orderId) {
        Order order = orderService.customerAcceptOrder(orderId);
        return ResponseEntity.ok(order);
    }
}