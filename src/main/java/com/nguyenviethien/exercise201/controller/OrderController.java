package com.nguyenviethien.exercise201.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.DTO.CustomerPageResponse;
import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.Gallery;
import com.nguyenviethien.exercise201.entity.Order;
import com.nguyenviethien.exercise201.entity.OrderItem;
import com.nguyenviethien.exercise201.entity.OrderStatus;
import com.nguyenviethien.exercise201.entity.StaffAccount;
import com.nguyenviethien.exercise201.exception.ApiResponse;
import com.nguyenviethien.exercise201.repository.*;
import com.nguyenviethien.exercise201.service.CustomerService;
import com.nguyenviethien.exercise201.service.OrderItemService;
import com.nguyenviethien.exercise201.service.OrderService;
import com.nguyenviethien.exercise201.service.impl.OrderServiceImpl;
import com.nguyenviethien.exercise201.security.JWT.JwtService;
import com.nguyenviethien.exercise201.repository.GalleryRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderServiceImpl orderServiceImpl;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderItemService orderItemService;
    private final CustomerService customerService;
    private final StaffAccountRepository staffAccountRepository;
    private final JwtService jwtService;
    private final GalleryRepository galleryRepository;

    @Autowired
    public OrderController(
            OrderService orderService,
            OrderRepository orderRepository,
            OrderServiceImpl orderServiceImpl,
            OrderStatusRepository orderStatusRepository,
            OrderItemService orderItemService,
            CustomerService customerService,
            StaffAccountRepository staffAccountRepository,
            JwtService jwtService,
            GalleryRepository galleryRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.orderServiceImpl = orderServiceImpl;
        this.orderStatusRepository = orderStatusRepository;
        this.orderItemService = orderItemService;
        this.customerService = customerService;
        this.staffAccountRepository = staffAccountRepository;
        this.jwtService = jwtService;
        this.galleryRepository = galleryRepository;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        try {
            ResponseEntity<?> response = orderServiceImpl.checkoutWithCoupon(
                UUID.fromString(request.getCustomerId()),
                request.getCouponCode(),
                request.getShippingAddress(),
                request.getPhoneNumber(),
                request.getPaymentMethod(),
                request.getOrderDetails(),
                request.getNote()
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Object body = response.getBody();
                if (body instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) body;
                    if (result.containsKey("paymentUrl")) {
                        result.put("success", true);
                        return ResponseEntity.ok(ApiResponse.success("Checkout successful", result));
                    }
                }
                return response;
            }
            return response;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Customer ID không đúng định dạng UUID"));
        } catch (Exception e) {
            log.error("Checkout error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerId}/stats")
    public ResponseEntity<ApiResponse<?>> getCustomerOrderStats(@PathVariable String customerId) {
        try {
            if (customerId == null || customerId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Customer ID không được để trống"));
            }
            
            UUID custId = UUID.fromString(customerId);
            Optional<Customer> customerOpt = customerService.findById(custId);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Customer không tìm thấy"));
            }
            Customer customer = customerOpt.get();

            List<Order> orders = orderService.findByCustomer(customer);
            Map<String, Long> statusStats = new HashMap<>();
            for (Order order : orders) {
                String status = order.getOrderStatus().getStatusName().toLowerCase();
                statusStats.put(status, statusStats.getOrDefault(status, 0L) + 1);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("totalOrders", orders.size());
            result.put("pendingOrders", statusStats.getOrDefault("pending", 0L));
            result.put("approvedOrders", statusStats.getOrDefault("approved", 0L));
            result.put("shippedOrders", statusStats.getOrDefault("shipped", 0L));
            result.put("deliveredOrders", statusStats.getOrDefault("delivered", 0L));
            result.put("cancelledOrders", statusStats.getOrDefault("cancelled", 0L));

            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            log.error("Error getting customer order stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi thống kê đơn hàng"));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<?>> getCustomerOrders(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (customerId == null || customerId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Customer ID không được để trống"));
            }

            Optional<Customer> customerOpt;
            try {
                UUID custId = UUID.fromString(customerId);
                customerOpt = customerService.findById(custId);
            } catch (IllegalArgumentException e) {
                log.warn("Customer ID is not a valid UUID: {}", customerId);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Customer ID không đúng định dạng. Vui lòng đăng nhập lại hoặc dùng endpoint theo email."));
            }

            if (customerOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Customer không tìm thấy"));
            }

            Customer customer = customerOpt.get();
            
            if (page < 0) page = 0;
            if (size <= 0) size = 10;
            if (size > 100) size = 100;
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orderPage = orderRepository.findByCustomer(customer, pageable);
            
            List<CustomerOrderDTO> customerOrders = orderPage.getContent().stream()
                .map(this::convertToCustomerOrderDTO)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("_embedded", Map.of("orders", customerOrders));
            response.put("page", Map.of(
                "size", orderPage.getSize(),
                "totalElements", orderPage.getTotalElements(),
                "totalPages", orderPage.getTotalPages(),
                "number", orderPage.getNumber()
            ));
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            log.error("Error getting customer orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi không mong muốn xảy ra"));
        }
    }

    /** Endpoint lấy đơn hàng theo email (fallback khi customerId từ token không phải UUID). */
    @GetMapping("/customer/by-email/{email}")
    public ResponseEntity<ApiResponse<?>> getCustomerOrdersByEmail(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email không được để trống"));
            }
            String identifier = email.trim();
            Optional<Customer> customerOpt = customerService.findByEmail(identifier)
                    .or(() -> customerService.findByEmailIgnoreCase(identifier))
                    .or(() -> customerService.findByUserName(identifier));
            if (customerOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không tìm thấy khách hàng với email này"));
            }
            Customer customer = customerOpt.get();
            if (page < 0) page = 0;
            if (size <= 0) size = 10;
            if (size > 100) size = 100;
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orderPage = orderRepository.findByCustomer(customer, pageable);
            List<CustomerOrderDTO> customerOrders = orderPage.getContent().stream()
                .map(this::convertToCustomerOrderDTO)
                .collect(Collectors.toList());
            Map<String, Object> response = new HashMap<>();
            response.put("_embedded", Map.of("orders", customerOrders));
            response.put("page", Map.of(
                "size", orderPage.getSize(),
                "totalElements", orderPage.getTotalElements(),
                "totalPages", orderPage.getTotalPages(),
                "number", orderPage.getNumber()
            ));
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error getting customer orders by email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi không mong muốn xảy ra"));
        }
    }

    @GetMapping("/{orderId}/track")
    public ResponseEntity<ApiResponse<OrderTrackingDTO>> trackOrder(@PathVariable String orderId) {
        try {
            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy đơn hàng"));
            }

            Order order = orderOpt.get();
            OrderTrackingDTO tracking = convertToOrderTrackingDTO(order);
            
            return ResponseEntity.ok(ApiResponse.success(tracking));
        } catch (Exception e) {
            log.error("Error tracking order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/confirm-receipt")
    public ResponseEntity<ApiResponse<?>> confirmReceipt(
            @PathVariable String orderId,
            @RequestBody ConfirmReceiptRequest request) {
        try {
            UUID orderUUID = UUID.fromString(orderId);
            Order order = orderService.customerAcceptOrder(orderUUID);
            
            return ResponseEntity.ok(ApiResponse.success("Xác nhận nhận hàng thành công", 
                    convertToCustomerOrderDTO(order)));
        } catch (Exception e) {
            log.error("Error confirming receipt: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/customer-cancel")
    public ResponseEntity<ApiResponse<?>> customerCancelOrder(
            @PathVariable String orderId,
            @RequestBody CancelOrderRequest request) {
        try {
            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy đơn hàng"));
            }

            Order order = orderOpt.get();
            
            if (order.getOrderApprovedAt() != null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đơn hàng đã được duyệt, không thể hủy"));
            }

            Optional<OrderStatus> cancelledStatusOpt = orderStatusRepository.findAll()
                    .stream()
                    .filter(status -> "cancelled".equalsIgnoreCase(status.getStatusName()))
                    .findFirst();

            if (cancelledStatusOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không tìm thấy trạng thái Cancelled"));
            }
            
            order.setOrderStatus(cancelledStatusOpt.get());
            order.setUpdated_at(new Date());
            order = orderService.save(order);

            return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công"));

        } catch (Exception e) {
            log.error("Error cancelling order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<?>> getAllOrdersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        try {
            List<Order> orders;
            
            if ((status != null && !status.isEmpty()) || (search != null && !search.isEmpty())) {
                if (status != null && !status.isEmpty()) {
                    Optional<OrderStatus> statusOpt = orderStatusRepository.findAll()
                            .stream()
                            .filter(s -> status.equalsIgnoreCase(s.getStatusName()))
                            .findFirst();
                    if (statusOpt.isPresent()) {
                        orders = orderService.findByOrderStatus(statusOpt.get());
                    } else {
                        orders = orderService.findAll();
                    }
                } else {
                    orders = orderService.findAll();
                }
                
                if (search != null && !search.isEmpty()) {
                    String searchLower = search.toLowerCase();
                    orders = orders.stream()
                        .filter(order -> 
                            order.getId().toLowerCase().contains(searchLower) ||
                            (order.getCustomer().getEmail() != null && 
                             order.getCustomer().getEmail().toLowerCase().contains(searchLower)) ||
                            (order.getCustomer().getFirst_name() + " " + order.getCustomer().getLast_name())
                                .toLowerCase().contains(searchLower)
                        )
                        .collect(Collectors.toList());
                }
            } else {
                orders = orderService.findAll();
            }

            orders = orders.stream()
                .sorted((o1, o2) -> o2.getCreated_at().compareTo(o1.getCreated_at()))
                .collect(Collectors.toList());

            List<AdminOrderDTO> adminOrders = orders.stream()
                .map(this::convertToAdminOrderDTO)
                .collect(Collectors.toList());

            int totalElements = adminOrders.size();
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);
            
            List<AdminOrderDTO> pageContent = adminOrders.subList(startIndex, Math.min(endIndex, totalElements));
            int totalPages = totalElements > 0 ? (int) Math.ceil((double) totalElements / size) : 1;

            Map<String, Object> response = new HashMap<>();
            response.put("orders", pageContent);
            response.put("page", Map.of(
                "size", size,
                "totalElements", totalElements,
                "totalPages", totalPages,
                "number", page
            ));

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error getting all orders for admin: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<AdminOrderDTO>> getOrderById(@PathVariable String orderId) {
        try {
            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy đơn hàng"));
            }

            Order order = orderOpt.get();
            AdminOrderDTO orderDTO = convertToAdminOrderDTO(order);

            return ResponseEntity.ok(ApiResponse.success(orderDTO));
        } catch (Exception e) {
            log.error("Error getting order by ID: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/{orderId}/details")
    public ResponseEntity<ApiResponse<List<OrderItemDTO>>> getOrderDetails(@PathVariable String orderId) {
        try {
            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy đơn hàng"));
            }

            Order order = orderOpt.get();
            List<OrderItem> orderItems = orderItemService.findByOrder(order);
            
            List<OrderItemDTO> orderItemDTOs = orderItems.stream()
                .map(this::convertToOrderItemDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(orderItemDTOs));
        } catch (Exception e) {
            log.error("Error getting order details: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/approve")
    public ResponseEntity<ApiResponse<?>> approveOrder(
            @PathVariable String orderId,
            @RequestParam String staffId) {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Order ID không hợp lệ"));
            }
            if (staffId == null || staffId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Staff ID không hợp lệ"));
            }

            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không tìm thấy đơn hàng"));
            }
            
            Order order = orderOpt.get();
            if (order.getOrderStatus() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đơn hàng không có trạng thái hợp lệ"));
            }
            UUID staffUUID = UUID.fromString(staffId);
            
            if (!"Pending".equalsIgnoreCase(order.getOrderStatus().getStatusName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Chỉ có thể duyệt đơn hàng ở trạng thái Pending"));
            }
            if (order.getOrderApprovedAt() != null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đơn hàng đã được duyệt trước đó"));
            }

            StaffAccount staff = staffAccountRepository.findById(staffUUID)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            if (!staff.isActive()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tài khoản nhân viên đã bị vô hiệu hóa"));
            }

            Optional<OrderStatus> approvedStatusOpt = orderStatusRepository.findAll()
                    .stream()
                    .filter(status -> "approved".equalsIgnoreCase(status.getStatusName()))
                    .findFirst();

            if (approvedStatusOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không tìm thấy trạng thái Approved"));
            }

            OrderStatus approvedStatus = approvedStatusOpt.get();
            
            order.setOrderStatus(approvedStatus);
            order.setOrderApprovedAt(new Date());
            order.setUpdatedBy(staff);
            order.setPaymentStatus("APPROVED");
            order.setUpdated_at(new Date());
            
            order = orderService.save(order);
            log.info("Order approved successfully: {}", orderId);
            
            return ResponseEntity.ok(ApiResponse.success("Duyệt đơn hàng thành công", 
                    convertToAdminOrderDTO(order)));
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ"));
        } catch (Exception e) {
            log.error("Error approving order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/ship")
    public ResponseEntity<ApiResponse<?>> markOrderAsShipped(
            @PathVariable String orderId,
            @RequestParam String staffId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Order ID không hợp lệ"));
            }
            if (staffId == null || staffId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Staff ID không hợp lệ"));
            }

            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không tìm thấy đơn hàng"));
            }
            
            Order order = orderOpt.get();
            if (order.getOrderStatus() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đơn hàng không có trạng thái hợp lệ"));
            }
            UUID staffUUID = UUID.fromString(staffId);
            
            if (!"Approved".equalsIgnoreCase(order.getOrderStatus().getStatusName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Chỉ có thể giao đơn hàng đã được duyệt"));
            }
            if (order.getOrderApprovedAt() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đơn hàng chưa được duyệt"));
            }
            if (order.getOrderDeliveredCarrierDate() != null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đơn hàng đã được đánh dấu giao"));
            }

            StaffAccount staff = staffAccountRepository.findById(staffUUID)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            if (!staff.isActive()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tài khoản nhân viên đã bị vô hiệu hóa"));
            }

            Optional<OrderStatus> shippedStatusOpt = orderStatusRepository.findAll()
                    .stream()
                    .filter(status -> "shipped".equalsIgnoreCase(status.getStatusName()))
                    .findFirst();

            if (shippedStatusOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không tìm thấy trạng thái Shipped"));
            }

            OrderStatus shippedStatus = shippedStatusOpt.get();
            
            if (requestBody != null && requestBody.containsKey("trackingNumber")) {
                String trackingNumber = requestBody.get("trackingNumber");
                if (trackingNumber != null && !trackingNumber.trim().isEmpty()) {
                    order.setTrackingNumber(trackingNumber.trim());
                }
            }
            
            order.setOrderStatus(shippedStatus);
            order.setOrderDeliveredCarrierDate(new Date());
            order.setUpdatedBy(staff);
            order.setUpdated_at(new Date());
            
            order = orderService.save(order);
            log.info("Order shipped successfully: {}", orderId);

            try {
                return ResponseEntity.ok(ApiResponse.success("Đánh dấu đơn hàng đã giao thành công",
                        convertToAdminOrderDTO(order)));
            } catch (Exception dtoEx) {
                log.warn("Could not build full DTO after ship (order may be detached), returning minimal response: {}", dtoEx.getMessage());
                return ResponseEntity.ok(ApiResponse.success("Đánh dấu đơn hàng đã giao thành công", null));
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ"));
        } catch (Exception e) {
            log.error("Error shipping order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/confirm-delivery")
    public ResponseEntity<ApiResponse<?>> confirmOrderDelivery(
            @PathVariable String orderId,
            @RequestParam String staffId) {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Order ID không hợp lệ"));
            }
            if (staffId == null || staffId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Staff ID không hợp lệ"));
            }

            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không tìm thấy đơn hàng"));
            }
            
            Order order = orderOpt.get();
            if (order.getOrderStatus() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đơn hàng không có trạng thái hợp lệ"));
            }
            UUID staffUUID = UUID.fromString(staffId);
            
            if (!"Shipped".equalsIgnoreCase(order.getOrderStatus().getStatusName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Chỉ có thể xác nhận giao hàng cho đơn đã được ship"));
            }
            if (order.getOrderDeliveredCarrierDate() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đơn hàng chưa được đánh dấu giao cho vận chuyển"));
            }
            if (order.getOrderDeliveredCustomerDate() != null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đơn hàng đã được xác nhận giao"));
            }

            StaffAccount staff = staffAccountRepository.findById(staffUUID)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            if (!staff.isActive()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tài khoản nhân viên đã bị vô hiệu hóa"));
            }

            Optional<OrderStatus> deliveredStatusOpt = orderStatusRepository.findAll()
                    .stream()
                    .filter(status -> "delivered".equalsIgnoreCase(status.getStatusName()))
                    .findFirst();

            if (deliveredStatusOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không tìm thấy trạng thái Delivered"));
            }

            OrderStatus deliveredStatus = deliveredStatusOpt.get();
            
            order.setOrderStatus(deliveredStatus);
            order.setOrderDeliveredCustomerDate(new Date());
            order.setUpdatedBy(staff);
            order.setUpdated_at(new Date());
            
            order = orderService.save(order);
            log.info("Order delivery confirmed: {}", orderId);
            
            return ResponseEntity.ok(ApiResponse.success("Xác nhận giao hàng thành công", 
                    convertToAdminOrderDTO(order)));
                    
        } catch (Exception e) {
            log.error("Error confirming delivery: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/admin-cancel")
    public ResponseEntity<ApiResponse<?>> adminCancelOrder(
            @PathVariable String orderId,
            @RequestParam String staffId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Order ID không hợp lệ"));
            }
            if (staffId == null || staffId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Staff ID không hợp lệ"));
            }

            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không tìm thấy đơn hàng"));
            }

            Order order = orderOpt.get();
            if (order.getOrderStatus() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đơn hàng không có trạng thái hợp lệ"));
            }
            UUID staffUUID = UUID.fromString(staffId);

            if (order.getOrderDeliveredCustomerDate() != null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đơn hàng đã được giao, không thể hủy"));
            }
            if ("Cancelled".equalsIgnoreCase(order.getOrderStatus().getStatusName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đơn hàng đã bị hủy"));
            }

            String reason = (request != null && request.get("reason") != null)
                    ? request.get("reason").trim() : "";
            if (reason.isEmpty()) {
                reason = "Hủy bởi quản trị";
            }

            StaffAccount staff = staffAccountRepository.findById(staffUUID)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            if (!staff.isActive()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tài khoản nhân viên đã bị vô hiệu hóa"));
            }

            Optional<OrderStatus> cancelledStatusOpt = orderStatusRepository.findAll()
                    .stream()
                    .filter(status -> "cancelled".equalsIgnoreCase(status.getStatusName()))
                    .findFirst();

            if (cancelledStatusOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không tìm thấy trạng thái Cancelled"));
            }

            OrderStatus cancelledStatus = cancelledStatusOpt.get();

            order.setOrderStatus(cancelledStatus);
            order.setUpdated_at(new Date());
            order.setNote(order.getNote() != null ? order.getNote() + "; Lý do hủy: " + reason : "Lý do hủy: " + reason);
            order.setUpdatedBy(staff);
            
            order = orderService.save(order);
            log.info("Order cancelled: {}", orderId);

            return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công", 
                    Map.of("reason", reason, "order", convertToAdminOrderDTO(order))));
                    
        } catch (Exception e) {
            log.error("Error cancelling order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<?>> getOrderStatistics() {
        try {
            List<Order> allOrders = orderService.findAll();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", allOrders.size());
            
            Map<String, Long> statusStats = new HashMap<>();
            for (Order order : allOrders) {
                String status = order.getOrderStatus().getStatusName();
                statusStats.put(status, statusStats.getOrDefault(status, 0L) + 1);
            }
            stats.put("statusStatistics", statusStats);
            
            BigDecimal totalRevenue = allOrders.stream()
                    .map(Order::getFinalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalRevenue", totalRevenue);
            
            BigDecimal averageOrderValue = allOrders.isEmpty() ? 
                BigDecimal.ZERO : totalRevenue.divide(BigDecimal.valueOf(allOrders.size()), 2, RoundingMode.HALF_UP);
            stats.put("averageOrderValue", averageOrderValue);
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("Error calculating statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<ApiResponse<?>> getDashboardStatistics() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            List<Order> allOrders = orderService.findAll();
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", allOrders.size());
            
            Map<String, Long> statusStats = new HashMap<>();
            for (Order order : allOrders) {
                String status = order.getOrderStatus().getStatusName();
                statusStats.put(status, statusStats.getOrDefault(status, 0L) + 1);
            }
            stats.put("statusStatistics", statusStats);
            
            BigDecimal totalRevenue = allOrders.stream()
                    .map(Order::getFinalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalRevenue", totalRevenue);
            
            BigDecimal averageOrderValue = allOrders.isEmpty() ? 
                BigDecimal.ZERO : totalRevenue.divide(BigDecimal.valueOf(allOrders.size()), 2, RoundingMode.HALF_UP);
            stats.put("averageOrderValue", averageOrderValue);
            
            dashboard.put("statistics", stats);
            
            long pendingCount = allOrders.stream()
                .filter(order -> "Pending".equalsIgnoreCase(order.getOrderStatus().getStatusName()))
                .count();
            dashboard.put("pendingOrdersCount", pendingCount);
            
            long readyToShipCount = allOrders.stream()
                .filter(order -> "Approved".equalsIgnoreCase(order.getOrderStatus().getStatusName()) 
                    && order.getOrderApprovedAt() != null 
                    && order.getOrderDeliveredCarrierDate() == null)
                .count();
            dashboard.put("readyToShipCount", readyToShipCount);
            
            return ResponseEntity.ok(ApiResponse.success(dashboard));
        } catch (Exception e) {
            log.error("Error calculating dashboard statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    // ===== CONVERSION METHODS =====
    private CustomerOrderDTO convertToCustomerOrderDTO(Order order) {
        CustomerOrderDTO dto = new CustomerOrderDTO();
        dto.setId(order.getId());
        dto.setCreated_at(order.getCreated_at() != null ? order.getCreated_at().toString() : null);
        dto.setTotalPrice(order.getTotalPrice() != null ? order.getTotalPrice().doubleValue() : 0);
        dto.setStatus(order.getOrderStatus() != null ? order.getOrderStatus().getStatusName().toLowerCase() : "pending");
        dto.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");
        dto.setShippingAddress(order.getShippingAddress() != null ? order.getShippingAddress() : "");
        dto.setOrderApprovedAt(order.getOrderApprovedAt() != null ? order.getOrderApprovedAt().toString() : null);
        dto.setOrderDeliveredCarrierDate(order.getOrderDeliveredCarrierDate() != null ? order.getOrderDeliveredCarrierDate().toString() : null);
        dto.setOrderDeliveredCustomerDate(order.getOrderDeliveredCustomerDate() != null ? order.getOrderDeliveredCustomerDate().toString() : null);
        
        List<OrderItem> orderItems = orderItemService.findByOrder(order);
        dto.setOrderItems(orderItems.stream().map(this::convertToOrderItemDTO).collect(Collectors.toList()));
        
        return dto;
    }

    private AdminOrderDTO convertToAdminOrderDTO(Order order) {
        AdminOrderDTO dto = new AdminOrderDTO();
        dto.setId(order.getId());
        dto.setCreated_at(order.getCreated_at().toString());
        dto.setTotalPrice(order.getTotalPrice().doubleValue());
        
        Map<String, String> customerMap = new HashMap<>();
        if (order.getCustomer() != null) {
            customerMap.put("first_name", order.getCustomer().getFirst_name());
            customerMap.put("last_name", order.getCustomer().getLast_name());
            customerMap.put("email", order.getCustomer().getEmail());
        }
        dto.setCustomer(customerMap);
        
        Map<String, String> statusMap = new HashMap<>();
        if (order.getOrderStatus() != null) {
            statusMap.put("statusName", order.getOrderStatus().getStatusName());
        }
        dto.setOrderStatus(statusMap);
        
        dto.setOrderApprovedAt(order.getOrderApprovedAt() != null ? order.getOrderApprovedAt().toString() : null);
        dto.setOrderDeliveredCarrierDate(order.getOrderDeliveredCarrierDate() != null ? order.getOrderDeliveredCarrierDate().toString() : null);
        dto.setOrderDeliveredCustomerDate(order.getOrderDeliveredCustomerDate() != null ? order.getOrderDeliveredCustomerDate().toString() : null);
        
        dto.setShippingAddress(order.getShippingAddress());
        dto.setPhoneNumber(order.getPhoneNumber());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setTrackingNumber(order.getTrackingNumber());
        dto.setNote(order.getNote());
        
        return dto;
    }

    private OrderTrackingDTO convertToOrderTrackingDTO(Order order) {
        OrderTrackingDTO dto = new OrderTrackingDTO();
        dto.setOrderId(order.getId());
        dto.setStatus(order.getOrderStatus() != null ? order.getOrderStatus().getStatusName().toLowerCase() : "pending");
        dto.setTrackingNumber(order.getTrackingNumber() != null ? order.getTrackingNumber() : "TN" + order.getId().substring(0, 8).toUpperCase());
        
        List<OrderStatusHistoryDTO> statusHistory = List.of(
            new OrderStatusHistoryDTO("created", order.getCreated_at().toString(), "Đơn hàng đã được tạo"),
            order.getOrderApprovedAt() != null ? 
                new OrderStatusHistoryDTO("approved", order.getOrderApprovedAt().toString(), "Đơn hàng đã được duyệt") : null,
            order.getOrderDeliveredCarrierDate() != null ? 
                new OrderStatusHistoryDTO("shipping", order.getOrderDeliveredCarrierDate().toString(), "Đơn hàng đang được giao") : null,
            order.getOrderDeliveredCustomerDate() != null ? 
                new OrderStatusHistoryDTO("delivered", order.getOrderDeliveredCustomerDate().toString(), "Đơn hàng đã được giao") : null
        ).stream().filter(java.util.Objects::nonNull).collect(Collectors.toList());
        
        dto.setOrderStatuses(statusHistory);
        
        return dto;
    }

    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId().toString());
        dto.setProductId(orderItem.getProduct().getId().toString());
        dto.setProductName(orderItem.getProduct().getProductName());
        
        String productImage = "/images/items/1.jpg";
        try {
            List<Gallery> thumbnails = galleryRepository.findThumbnailByProductId(orderItem.getProduct().getId());
            if (thumbnails != null && !thumbnails.isEmpty()) {
                productImage = thumbnails.get(0).getImage();
            } else {
                List<Gallery> allImages = galleryRepository.findByProductId(orderItem.getProduct().getId());
                if (allImages != null && !allImages.isEmpty()) {
                    productImage = allImages.get(0).getImage();
                }
            }
        } catch (Exception e) {
            // Keep default fallback
        }
        dto.setProductImage(productImage);
        
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice().doubleValue());
        dto.setTotal(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())).doubleValue());
        
        return dto;
    }

    // ===== DTO CLASSES =====
    public static class CheckoutRequest {
        private String customerId;
        private List<OrderDetail> orderDetails;
        private String couponCode;
        private String shippingAddress;
        private String paymentMethod;
        private String phoneNumber;
        private String note;

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public List<OrderDetail> getOrderDetails() { return orderDetails; }
        public void setOrderDetails(List<OrderDetail> orderDetails) { this.orderDetails = orderDetails; }
        public String getCouponCode() { return couponCode; }
        public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }

        public static class OrderDetail {
            private String productId;
            private int quantity;
            private double price;

            public String getProductId() { return productId; }
            public void setProductId(String productId) { this.productId = productId; }
            public int getQuantity() { return quantity; }
            public void setQuantity(int quantity) { this.quantity = quantity; }
            public double getPrice() { return price; }
            public void setPrice(double price) { this.price = price; }
        }
    }

    public static class CustomerOrderDTO {
        private String id;
        private String created_at;
        private double totalPrice;
        private String status;
        private String paymentMethod;
        private String shippingAddress;
        private String orderApprovedAt;
        private String orderDeliveredCarrierDate;
        private String orderDeliveredCustomerDate;
        private List<OrderItemDTO> orderItems;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCreated_at() { return created_at; }
        public void setCreated_at(String created_at) { this.created_at = created_at; }
        public double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        public String getOrderApprovedAt() { return orderApprovedAt; }
        public void setOrderApprovedAt(String orderApprovedAt) { this.orderApprovedAt = orderApprovedAt; }
        public String getOrderDeliveredCarrierDate() { return orderDeliveredCarrierDate; }
        public void setOrderDeliveredCarrierDate(String orderDeliveredCarrierDate) { this.orderDeliveredCarrierDate = orderDeliveredCarrierDate; }
        public String getOrderDeliveredCustomerDate() { return orderDeliveredCustomerDate; }
        public void setOrderDeliveredCustomerDate(String orderDeliveredCustomerDate) { this.orderDeliveredCustomerDate = orderDeliveredCustomerDate; }
        public List<OrderItemDTO> getOrderItems() { return orderItems; }
        public void setOrderItems(List<OrderItemDTO> orderItems) { this.orderItems = orderItems; }
    }

    public static class AdminOrderDTO {
        private String id;
        private String created_at;
        private double totalPrice;
        private Map<String, String> customer;
        private Map<String, String> orderStatus;
        private String orderApprovedAt;
        private String orderDeliveredCarrierDate;
        private String orderDeliveredCustomerDate;
        private String shippingAddress;
        private String phoneNumber;
        private String paymentMethod;
        private String trackingNumber;
        private String note;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCreated_at() { return created_at; }
        public void setCreated_at(String created_at) { this.created_at = created_at; }
        public double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
        public Map<String, String> getCustomer() { return customer; }
        public void setCustomer(Map<String, String> customer) { this.customer = customer; }
        public Map<String, String> getOrderStatus() { return orderStatus; }
        public void setOrderStatus(Map<String, String> orderStatus) { this.orderStatus = orderStatus; }
        public String getOrderApprovedAt() { return orderApprovedAt; }
        public void setOrderApprovedAt(String orderApprovedAt) { this.orderApprovedAt = orderApprovedAt; }
        public String getOrderDeliveredCarrierDate() { return orderDeliveredCarrierDate; }
        public void setOrderDeliveredCarrierDate(String orderDeliveredCarrierDate) { this.orderDeliveredCarrierDate = orderDeliveredCarrierDate; }
        public String getOrderDeliveredCustomerDate() { return orderDeliveredCustomerDate; }
        public void setOrderDeliveredCustomerDate(String orderDeliveredCustomerDate) { this.orderDeliveredCustomerDate = orderDeliveredCustomerDate; }
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class OrderTrackingDTO {
        private String orderId;
        private String status;
        private String trackingNumber;
        private List<OrderStatusHistoryDTO> orderStatuses;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
        public List<OrderStatusHistoryDTO> getOrderStatuses() { return orderStatuses; }
        public void setOrderStatuses(List<OrderStatusHistoryDTO> orderStatuses) { this.orderStatuses = orderStatuses; }
    }

    public static class OrderStatusHistoryDTO {
        private String status;
        private String timestamp;
        private String description;

        public OrderStatusHistoryDTO(String status, String timestamp, String description) {
            this.status = status;
            this.timestamp = timestamp;
            this.description = description;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class OrderItemDTO {
        private String id;
        private String productId;
        private String productName;
        private String productImage;
        private int quantity;
        private double price;
        private double total;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getProductImage() { return productImage; }
        public void setProductImage(String productImage) { this.productImage = productImage; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
    }

    public static class ConfirmReceiptRequest {
        private String customerId;
        private int rating;
        private String review;

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }
        public String getReview() { return review; }
        public void setReview(String review) { this.review = review; }
    }

    public static class CancelOrderRequest {
        private String customerId;
        private String reason;

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
