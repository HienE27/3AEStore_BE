package com.nguyenviethien.exercise201.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.Order;
import com.nguyenviethien.exercise201.entity.OrderItem;
import com.nguyenviethien.exercise201.entity.OrderStatus;
import com.nguyenviethien.exercise201.entity.StaffAccount;
import com.nguyenviethien.exercise201.repository.OrderRepository;
import com.nguyenviethien.exercise201.repository.OrderStatusRepository;
import com.nguyenviethien.exercise201.repository.StaffAccountRepository;
import com.nguyenviethien.exercise201.service.CustomerService;
import com.nguyenviethien.exercise201.service.OrderItemService;
import com.nguyenviethien.exercise201.service.OrderService;
import com.nguyenviethien.exercise201.repository.*;
import com.nguyenviethien.exercise201.entity.*;
import com.nguyenviethien.exercise201.service.impl.OrderServiceImpl;
import com.nguyenviethien.exercise201.service.JWT.JwtService;
import com.nguyenviethien.exercise201.repository.GalleryRepository;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

     @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderServiceImpl orderServiceImpl;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private StaffAccountRepository staffAccountRepository;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private GalleryRepository galleryRepository;

    // ===== CHECKOUT ENDPOINT =====
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
                        return ResponseEntity.ok(result);
                    }
                }
                return response;
            }
            return response;
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Customer ID không đúng định dạng UUID: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    // ===== CUSTOMER ORDER ENDPOINTS =====
    // @GetMapping("/customer/{customerId}")
    // public ResponseEntity<?> getCustomerOrders(
    //         @PathVariable String customerId,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size) {
    //     try {
    //         UUID custId = UUID.fromString(customerId);
    //         Optional<Customer> customerOpt = customerService.findById(custId);
    //         if (customerOpt.isEmpty()) {
    //             return ResponseEntity.badRequest().body("Customer not found");
    //         }

    //         Customer customer = customerOpt.get();
    //         Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
    //         Page<Order> orderPage = orderService.findByCustomer(customer, pageable);

    //         List<CustomerOrderDTO> customerOrders = orderPage.getContent().stream()
    //             .map(this::convertToCustomerOrderDTO)
    //             .toList();

    //         Map<String, Object> response = new HashMap<>();
    //         response.put("_embedded", Map.of("orders", customerOrders));
    //         response.put("page", Map.of(
    //             "size", orderPage.getSize(),
    //             "totalElements", orderPage.getTotalElements(),
    //             "totalPages", orderPage.getTotalPages(),
    //             "number", orderPage.getNumber()
    //         ));

    //         return ResponseEntity.ok(response);
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    //     }
    // }


    @GetMapping("/customer/{customerId}/stats")
public ResponseEntity<?> getCustomerOrderStats(@PathVariable String customerId) {
    try {
        // Validate
        if (customerId == null || customerId.trim().isEmpty())
            return ResponseEntity.badRequest().body("Customer ID không được để trống");
        UUID custId;
        try {
            custId = UUID.fromString(customerId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Customer ID không đúng định dạng UUID: " + customerId);
        }

        Optional<Customer> customerOpt = customerService.findById(custId);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Customer không tìm thấy với ID: " + customerId);
        }
        Customer customer = customerOpt.get();

        // Lấy tất cả đơn hàng của khách
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
        // Có thể bổ sung thêm các trạng thái khác nếu cần

        return ResponseEntity.ok(result);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi thống kê đơn hàng: " + e.getMessage());
    }
}



@GetMapping("/customer/{customerId}")
public ResponseEntity<?> getCustomerOrders(
        @PathVariable String customerId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    try {
        System.out.println("=== GET CUSTOMER ORDERS ===");
        System.out.println("Customer ID: " + customerId);
        System.out.println("Page: " + page + ", Size: " + size);
        
        // Validate customer ID format
        if (customerId == null || customerId.trim().isEmpty()) {
            System.out.println("ERROR: Customer ID is null or empty");
            return ResponseEntity.badRequest().body("Customer ID không được để trống");
        }
        
        UUID custId;
        try {
            custId = UUID.fromString(customerId);
            System.out.println("Parsed Customer UUID: " + custId);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: Invalid UUID format: " + customerId);
            return ResponseEntity.badRequest().body("Customer ID không đúng định dạng UUID: " + customerId);
        }
        
        // Check if customer exists
        Optional<Customer> customerOpt = customerService.findById(custId);
        if (customerOpt.isEmpty()) {
            System.out.println("ERROR: Customer not found with ID: " + custId);
            return ResponseEntity.badRequest().body("Customer không tìm thấy với ID: " + customerId);
        }

        Customer customer = customerOpt.get();
        System.out.println("Found customer: " + customer.getEmail());
        
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100; // Limit max size
        
        // FIXED: Use custom query instead of default findByCustomer to avoid 'created' field issue
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findByCustomer(customer, pageable);
        
        
        System.out.println("Found " + orderPage.getTotalElements() + " orders for customer");

        List<CustomerOrderDTO> customerOrders = orderPage.getContent().stream()
            .map(this::convertToCustomerOrderDTO)
            .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("_embedded", Map.of("orders", customerOrders));
        response.put("page", Map.of(
            "size", orderPage.getSize(),
            "totalElements", orderPage.getTotalElements(),
            "totalPages", orderPage.getTotalPages(),
            "number", orderPage.getNumber()
        ));
        
        System.out.println("Returning response with " + customerOrders.size() + " orders");
        return ResponseEntity.ok(response);
        
    } catch (IllegalArgumentException e) {
        System.out.println("ERROR: IllegalArgumentException - " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ: " + e.getMessage());
    } catch (Exception e) {
        System.out.println("ERROR: Unexpected exception - " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Lỗi server: " + e.getMessage());
    }
}




// ===== ALTERNATIVE ENDPOINT: GET ORDERS BY EMAIL =====
@GetMapping("/customer/by-email/{email}")
public ResponseEntity<?> getCustomerOrdersByEmail(
        @PathVariable String email,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    try {
        System.out.println("=== GET CUSTOMER ORDERS BY EMAIL ===");
        System.out.println("Email: " + email);
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email không được để trống");
        }
        
        // Find customer by email using Optional
        Optional<Customer> customerOpt = customerService.findByEmail(email);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy customer với email: " + email);
        }
        
        Customer customer = customerOpt.get();
        System.out.println("Found customer: " + customer.getId());
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
        Page<Order> orderPage = orderService.findByCustomer(customer, pageable);

        List<CustomerOrderDTO> customerOrders = orderPage.getContent().stream()
            .map(this::convertToCustomerOrderDTO)
            .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("_embedded", Map.of("orders", customerOrders));
        response.put("page", Map.of(
            "size", orderPage.getSize(),
            "totalElements", orderPage.getTotalElements(),
            "totalPages", orderPage.getTotalPages(),
            "number", orderPage.getNumber()
        ));
        response.put("customerInfo", Map.of(
            "id", customer.getId().toString(),
            "email", customer.getEmail(),
            "name", customer.getFirst_name() + " " + customer.getLast_name()
        ));
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        System.out.println("ERROR in getCustomerOrdersByEmail: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Lỗi server: " + e.getMessage());
    }
}

// ===== DEBUG ENDPOINT =====
@GetMapping("/debug/customer/{customerId}")
public ResponseEntity<?> debugCustomerInfo(@PathVariable String customerId) {
    try {
        System.out.println("=== DEBUG CUSTOMER INFO ===");
        
        Map<String, Object> debug = new HashMap<>();
        debug.put("inputCustomerId", customerId);
        debug.put("inputLength", customerId.length());
        
        // Try to parse as UUID
        try {
            UUID custId = UUID.fromString(customerId);
            debug.put("validUUID", true);
            debug.put("parsedUUID", custId.toString());
            
            // Check if customer exists
            Optional<Customer> customerOpt = customerService.findById(custId);
            debug.put("customerExists", customerOpt.isPresent());
            
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                debug.put("customerEmail", customer.getEmail());
                debug.put("customerFirstName", customer.getFirst_name());
                debug.put("customerLastName", customer.getLast_name());
                
                // Count orders
                List<Order> orders = orderService.findByCustomer(customer);
                debug.put("orderCount", orders.size());
            }
            
        } catch (IllegalArgumentException e) {
            debug.put("validUUID", false);
            debug.put("uuidError", e.getMessage());
        }
        
        return ResponseEntity.ok(debug);
        
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Debug error: " + e.getMessage());
    }
}


    @GetMapping("/{orderId}/track")
    public ResponseEntity<?> trackOrder(@PathVariable String orderId) {
        try {
            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            OrderTrackingDTO tracking = convertToOrderTrackingDTO(order);
            
            return ResponseEntity.ok(tracking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{orderId}/confirm-receipt")
    public ResponseEntity<?> confirmReceipt(
            @PathVariable String orderId,
            @RequestBody ConfirmReceiptRequest request) {
        try {
            UUID orderUUID = UUID.fromString(orderId);
            Order order = orderService.customerAcceptOrder(orderUUID);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xác nhận nhận hàng thành công");
            response.put("order", convertToCustomerOrderDTO(order));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }






    @PutMapping("/{orderId}/customer-cancel")
    public ResponseEntity<?> customerCancelOrder(
            @PathVariable String orderId,
            @RequestBody CancelOrderRequest request) {
        try {
            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            
            if (order.getOrderApprovedAt() != null) {
                return ResponseEntity.badRequest().body("Đơn hàng đã được duyệt, không thể hủy");
            }

            // FIXED: Use case-insensitive search for Cancelled status
            Optional<OrderStatus> cancelledStatusOpt = orderStatusRepository.findAll()
                    .stream()
                    .filter(status -> "cancelled".equalsIgnoreCase(status.getStatusName()))
                    .findFirst();

            if (cancelledStatusOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy trạng thái Cancelled trong hệ thống");
            }
            
            order.setOrderStatus(cancelledStatusOpt.get());
            order.setUpdated_at(new Date());
            order = orderService.save(order);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Hủy đơn hàng thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ===== ADMIN ORDER MANAGEMENT ENDPOINTS =====
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllOrdersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        try {
            System.out.println("=== GET ALL ORDERS FOR ADMIN ===");
            System.out.println("Page: " + page + ", Size: " + size);
            System.out.println("Status filter: " + status);
            System.out.println("Search: " + search);
            
            List<Order> orders;
            
            // Apply filters
            if ((status != null && !status.isEmpty()) || (search != null && !search.isEmpty())) {
                if (status != null && !status.isEmpty()) {
                    // FIXED: Case-insensitive status search
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
                
                // Apply search filter if provided
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
                        .toList();
                }
            } else {
                orders = orderService.findAll();
            }

            // Sort orders by created date descending
            orders = orders.stream()
                .sorted((o1, o2) -> o2.getCreated_at().compareTo(o1.getCreated_at()))
                .toList();

            List<AdminOrderDTO> adminOrders = orders.stream()
                .map(this::convertToAdminOrderDTO)
                .toList();

            // Calculate pagination manually since we filtered in memory
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

            System.out.println("Returning " + pageContent.size() + " orders out of " + totalElements + " total");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("ERROR in getAllOrdersForAdmin: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Get single order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable String orderId) {
        try {
            System.out.println("=== GET ORDER BY ID ===");
            System.out.println("Order ID: " + orderId);

            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                System.out.println("Order not found: " + orderId);
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            AdminOrderDTO orderDTO = convertToAdminOrderDTO(order);

            System.out.println("Found order: " + orderId);
            return ResponseEntity.ok(orderDTO);
        } catch (Exception e) {
            System.out.println("ERROR in getOrderById: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error loading order: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}/details")
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId) {
        try {
            System.out.println("=== GET ORDER DETAILS ===");
            System.out.println("Order ID: " + orderId);
            
            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                System.out.println("Order not found: " + orderId);
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            List<OrderItem> orderItems = orderItemService.findByOrder(order);
            
            List<OrderItemDTO> orderItemDTOs = orderItems.stream()
                .map(this::convertToOrderItemDTO)
                .toList();

            System.out.println("Found " + orderItemDTOs.size() + " items for order: " + orderId);
            return ResponseEntity.ok(orderItemDTOs);
        } catch (Exception e) {
            System.out.println("ERROR in getOrderDetails: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error loading order details: " + e.getMessage());
        }
    }

    // FIXED: Simplified approve order method
    @PutMapping("/{orderId}/approve")
    public ResponseEntity<?> approveOrder(
            @PathVariable String orderId,
            @RequestParam String staffId) {
        try {
            System.out.println("=== APPROVE ORDER ===");
            System.out.println("Order ID: " + orderId);
            System.out.println("Staff ID: " + staffId);
            
            // Validate inputs
            if (orderId == null || orderId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Order ID không hợp lệ");
            }
            if (staffId == null || staffId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Staff ID không hợp lệ");
            }

            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng");
            }
            
            Order order = orderOpt.get();
            UUID staffUUID = UUID.fromString(staffId);
            
            System.out.println("Order current status: " + order.getOrderStatus().getStatusName());
            System.out.println("Order approved at: " + order.getOrderApprovedAt());
            
            // Simple validation
            if (!"Pending".equalsIgnoreCase(order.getOrderStatus().getStatusName())) {
                return ResponseEntity.badRequest().body("Chỉ có thể duyệt đơn hàng ở trạng thái Pending");
            }
            if (order.getOrderApprovedAt() != null) {
                return ResponseEntity.badRequest().body("Đơn hàng đã được duyệt trước đó");
            }

            // Verify staff
            StaffAccount staff = staffAccountRepository.findById(staffUUID)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            if (!staff.isActive()) {
                return ResponseEntity.badRequest().body("Tài khoản nhân viên đã bị vô hiệu hóa");
            }

            // Find Approved status
            Optional<OrderStatus> approvedStatusOpt = orderStatusRepository.findAll()
                    .stream()
                    .filter(status -> "approved".equalsIgnoreCase(status.getStatusName()))
                    .findFirst();

            if (approvedStatusOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy trạng thái Approved trong hệ thống");
            }

            OrderStatus approvedStatus = approvedStatusOpt.get();
            
            // Update order directly
            order.setOrderStatus(approvedStatus);
            order.setOrderApprovedAt(new Date());
            order.setUpdatedBy(staff);
            order.setPaymentStatus("APPROVED");
            order.setUpdated_at(new Date());
            
            order = orderService.save(order);
            System.out.println("Order approved successfully");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Duyệt đơn hàng thành công");
            response.put("order", convertToAdminOrderDTO(order));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: IllegalArgumentException - " + e.getMessage());
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: Exception - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // FIXED: Simplified ship order method
    @PutMapping("/{orderId}/ship")
    public ResponseEntity<?> markOrderAsShipped(
            @PathVariable String orderId,
            @RequestParam String staffId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            System.out.println("=== SHIP ORDER ===");
            System.out.println("Order ID: " + orderId);
            System.out.println("Staff ID: " + staffId);
            
            // Validate inputs
            if (orderId == null || orderId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Order ID không hợp lệ");
            }
            if (staffId == null || staffId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Staff ID không hợp lệ");
            }

            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng");
            }
            
            Order order = orderOpt.get();
            UUID staffUUID = UUID.fromString(staffId);
            
            System.out.println("Order current status: " + order.getOrderStatus().getStatusName());
            
            // Simple validation
            if (!"Approved".equalsIgnoreCase(order.getOrderStatus().getStatusName())) {
                return ResponseEntity.badRequest().body("Chỉ có thể giao đơn hàng đã được duyệt");
            }
            if (order.getOrderApprovedAt() == null) {
                return ResponseEntity.badRequest().body("Đơn hàng chưa được duyệt");
            }
            if (order.getOrderDeliveredCarrierDate() != null) {
                return ResponseEntity.badRequest().body("Đơn hàng đã được đánh dấu giao");
            }

            // Verify staff
            StaffAccount staff = staffAccountRepository.findById(staffUUID)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            if (!staff.isActive()) {
                return ResponseEntity.badRequest().body("Tài khoản nhân viên đã bị vô hiệu hóa");
            }

            // Find Shipped status
            Optional<OrderStatus> shippedStatusOpt = orderStatusRepository.findAll()
                    .stream()
                    .filter(status -> "shipped".equalsIgnoreCase(status.getStatusName()))
                    .findFirst();

            if (shippedStatusOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy trạng thái Shipped trong hệ thống");
            }

            OrderStatus shippedStatus = shippedStatusOpt.get();
            
            // Set tracking number if provided
            if (requestBody != null && requestBody.containsKey("trackingNumber")) {
                String trackingNumber = requestBody.get("trackingNumber");
                if (trackingNumber != null && !trackingNumber.trim().isEmpty()) {
                    order.setTrackingNumber(trackingNumber.trim());
                    System.out.println("Setting tracking number: " + trackingNumber);
                }
            }
            
            // Update order directly
            order.setOrderStatus(shippedStatus);
            order.setOrderDeliveredCarrierDate(new Date());
            order.setUpdatedBy(staff);
            order.setUpdated_at(new Date());
            
            order = orderService.save(order);
            System.out.println("Order shipped successfully");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đánh dấu đơn hàng đã giao thành công");
            response.put("order", convertToAdminOrderDTO(order));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: IllegalArgumentException - " + e.getMessage());
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: Exception - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }


// ENDPOINT: Confirm delivery (xác nhận giao hàng)
@PutMapping("/{orderId}/confirm-delivery")
public ResponseEntity<?> confirmOrderDelivery(
        @PathVariable String orderId,
        @RequestParam String staffId) {
    try {
        System.out.println("=== CONFIRM DELIVERY ===");
        System.out.println("Order ID: " + orderId);
        System.out.println("Staff ID: " + staffId);
        
        // Validate inputs
        if (orderId == null || orderId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Order ID không hợp lệ");
        }
        if (staffId == null || staffId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Staff ID không hợp lệ");
        }

        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng");
        }
        
        Order order = orderOpt.get();
        UUID staffUUID = UUID.fromString(staffId);
        
        System.out.println("Order current status: " + order.getOrderStatus().getStatusName());
        
        // Validation
        if (!"Shipped".equalsIgnoreCase(order.getOrderStatus().getStatusName())) {
            return ResponseEntity.badRequest().body("Chỉ có thể xác nhận giao hàng cho đơn đã được ship");
        }
        if (order.getOrderDeliveredCarrierDate() == null) {
            return ResponseEntity.badRequest().body("Đơn hàng chưa được đánh dấu giao cho vận chuyển");
        }
        if (order.getOrderDeliveredCustomerDate() != null) {
            return ResponseEntity.badRequest().body("Đơn hàng đã được xác nhận giao");
        }

        // Verify staff
        StaffAccount staff = staffAccountRepository.findById(staffUUID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        if (!staff.isActive()) {
            return ResponseEntity.badRequest().body("Tài khoản nhân viên đã bị vô hiệu hóa");
        }

        // Find Delivered status
        Optional<OrderStatus> deliveredStatusOpt = orderStatusRepository.findAll()
                .stream()
                .filter(status -> "delivered".equalsIgnoreCase(status.getStatusName()))
                .findFirst();

        if (deliveredStatusOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy trạng thái Delivered trong hệ thống");
        }

        OrderStatus deliveredStatus = deliveredStatusOpt.get();
        
        // Update order
        order.setOrderStatus(deliveredStatus);
        order.setOrderDeliveredCustomerDate(new Date());
        order.setUpdatedBy(staff);
        order.setUpdated_at(new Date());
        
        order = orderService.save(order);
        System.out.println("Order delivery confirmed successfully");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xác nhận giao hàng thành công");
        response.put("order", convertToAdminOrderDTO(order));
        
        return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
        System.out.println("ERROR: IllegalArgumentException - " + e.getMessage());
        return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ: " + e.getMessage());
    } catch (Exception e) {
        System.out.println("ERROR: Exception - " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
    }
}

// ENDPOINT: Update shipping status (cập nhật trạng thái vận chuyển)
@PutMapping("/{orderId}/update-shipping")
public ResponseEntity<?> updateShippingStatus(
        @PathVariable String orderId,
        @RequestParam(required = false) String staffId,
        @RequestBody UpdateShippingRequest request,
        HttpServletRequest httpRequest) {
    try {
        System.out.println("=== UPDATE SHIPPING STATUS ===");
        System.out.println("Order ID: " + orderId);
        System.out.println("Staff ID (param): " + staffId);
        System.out.println("Request body: status=" + request.getStatus() + ", trackingNumber=" + request.getTrackingNumber() + ", note=" + request.getNote() + ", staffId=" + request.getStaffId());
        String authHeader = httpRequest.getHeader("Authorization");
        System.out.println("Authorization header: " + (authHeader != null ? authHeader.replaceFirst("Bearer ", "Bearer <token>") : "none"));
        
        // Validate inputs
        if (orderId == null || orderId.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Order ID không hợp lệ");
            return ResponseEntity.badRequest().body(err);
        }
        if (staffId == null || staffId.trim().isEmpty()) {
            // allow staffId from body
            if (request.getStaffId() == null || request.getStaffId().trim().isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Staff ID không hợp lệ");
                return ResponseEntity.badRequest().body(err);
            }
        }

        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Không tìm thấy đơn hàng");
            return ResponseEntity.badRequest().body(err);
        }
        
        Order order = orderOpt.get();
        // Allow staffId to be provided either as request param or inside the request body
        if ((staffId == null || staffId.trim().isEmpty()) && request.getStaffId() != null) {
            staffId = request.getStaffId();
        }

        // Fallback resolution sequence when staffId missing:
        // 1) Try SecurityContext username -> findByUser_name
        // 2) Try SecurityContext username -> findByEmail
        // 3) Try id claim in JWT token
        if (staffId == null || staffId.trim().isEmpty()) {
            try {
                String username = SecurityContextHolder.getContext().getAuthentication() != null ?
                        SecurityContextHolder.getContext().getAuthentication().getName() : null;
                if (username != null && !username.trim().isEmpty()) {
                    try {
                        var staffAcc = staffAccountRepository.findByUser_name(username);
                        if (staffAcc == null) {
                            staffAcc = staffAccountRepository.findByEmail(username).orElse(null);
                        }
                        if (staffAcc != null) {
                            staffId = staffAcc.getId().toString();
                            System.out.println("Resolved staffId from security username/email lookup: " + staffId);
                        } else {
                            System.out.println("No staff account found for username/email: " + username);
                        }
                    } catch (Exception e) {
                        System.out.println("Error looking up staff by username/email: " + e.getMessage());
                    }
                }
                // If still missing, try extract id claim from token
                if (staffId == null || staffId.trim().isEmpty()) {
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            UUID idFromToken = jwtService.extractId(token);
                            if (idFromToken != null) {
                                staffId = idFromToken.toString();
                                System.out.println("Resolved staffId from token id claim: " + staffId);
                            }
                        } catch (Exception ex) {
                            System.out.println("Could not extract id from token: " + ex.getMessage());
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("Could not resolve staffId from security/token: " + ex.getMessage());
            }
        }

        UUID staffUUID = null;
        if (staffId != null && !staffId.trim().isEmpty()) {
            try {
                staffUUID = UUID.fromString(staffId);
            } catch (IllegalArgumentException iae) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Staff ID không đúng định dạng UUID: " + staffId);
                return ResponseEntity.badRequest().body(err);
            }
        } else {
            // staffId not provided and could not be resolved from SecurityContext earlier
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Unauthorized: staff authentication missing. Please login as staff and try again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
        
        // Verify staff
        StaffAccount staff = staffAccountRepository.findById(staffUUID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        if (!staff.isActive()) {
            return ResponseEntity.badRequest().body("Tài khoản nhân viên đã bị vô hiệu hóa");
        }

        // Update based on status
        String newStatus = request.getStatus();
        if (newStatus == null || newStatus.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Trạng thái mới không được để trống");
            return ResponseEntity.badRequest().body(err);
        }
        switch (newStatus.toLowerCase()) {
            case "shipped":
                // Find Shipped status
                Optional<OrderStatus> shippedStatusOpt = orderStatusRepository.findAll()
                        .stream()
                        .filter(status -> "shipped".equalsIgnoreCase(status.getStatusName()))
                        .findFirst();

                if (shippedStatusOpt.isEmpty()) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("success", false);
                    err.put("message", "Không tìm thấy trạng thái Shipped");
                    return ResponseEntity.badRequest().body(err);
                }

                order.setOrderStatus(shippedStatusOpt.get());
                if (order.getOrderDeliveredCarrierDate() == null) {
                    order.setOrderDeliveredCarrierDate(new Date());
                }
                break;
                
            case "in_transit":
                // Keep current status but update tracking
                break;
                
            case "delivered":
                // Find Delivered status
                Optional<OrderStatus> deliveredStatusOpt = orderStatusRepository.findAll()
                        .stream()
                        .filter(status -> "delivered".equalsIgnoreCase(status.getStatusName()))
                        .findFirst();

                if (deliveredStatusOpt.isEmpty()) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("success", false);
                    err.put("message", "Không tìm thấy trạng thái Delivered");
                    return ResponseEntity.badRequest().body(err);
                }

                order.setOrderStatus(deliveredStatusOpt.get());
                order.setOrderDeliveredCustomerDate(new Date());
                break;
                
            default:
                return ResponseEntity.badRequest().body("Trạng thái không hợp lệ: " + newStatus);
        }
        
        // Update tracking number and note if provided
        if (request.getTrackingNumber() != null && !request.getTrackingNumber().trim().isEmpty()) {
            order.setTrackingNumber(request.getTrackingNumber().trim());
        }
        
        if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
            String existingNote = order.getNote() != null ? order.getNote() : "";
            order.setNote(existingNote + "; Vận chuyển: " + request.getNote());
        }
        
        order.setUpdatedBy(staff);
        order.setUpdated_at(new Date());
        
        order = orderService.save(order);
        System.out.println("Shipping status updated successfully");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật trạng thái vận chuyển thành công");
        response.put("order", convertToAdminOrderDTO(order));
        
        return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
        System.out.println("ERROR: IllegalArgumentException - " + e.getMessage());
        Map<String, Object> err = new HashMap<>();
        err.put("success", false);
        err.put("message", "Dữ liệu không hợp lệ: " + e.getMessage());
        return ResponseEntity.badRequest().body(err);
    } catch (Exception e) {
        System.out.println("ERROR: Exception - " + e.getMessage());
        e.printStackTrace();
        Map<String, Object> err = new HashMap<>();
        err.put("success", false);
        err.put("message", "Lỗi: " + e.getMessage());
        return ResponseEntity.badRequest().body(err);
    }
}


    // FIXED: Simplified cancel order method
    @PutMapping("/{orderId}/admin-cancel")
    public ResponseEntity<?> adminCancelOrder(
            @PathVariable String orderId,
            @RequestParam String staffId,
            @RequestBody Map<String, String> request) {
        try {
            System.out.println("=== ADMIN CANCEL ORDER ===");
            System.out.println("Order ID: " + orderId);
            System.out.println("Staff ID: " + staffId);
            
            // Validate inputs
            if (orderId == null || orderId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Order ID không hợp lệ");
            }
            if (staffId == null || staffId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Staff ID không hợp lệ");
            }

            Optional<Order> orderOpt = orderService.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng");
            }

            Order order = orderOpt.get();
            UUID staffUUID = UUID.fromString(staffId);

            System.out.println("Order current status: " + order.getOrderStatus().getStatusName());

            // Simple validation
            if (order.getOrderDeliveredCustomerDate() != null) {
                return ResponseEntity.badRequest().body("Đơn hàng đã được giao, không thể hủy");
            }
            if ("Cancelled".equalsIgnoreCase(order.getOrderStatus().getStatusName())) {
                return ResponseEntity.badRequest().body("Đơn hàng đã bị hủy");
            }

            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Lý do hủy không được để trống");
            }

            // Verify staff exists
            StaffAccount staff = staffAccountRepository.findById(staffUUID)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            if (!staff.isActive()) {
                return ResponseEntity.badRequest().body("Tài khoản nhân viên đã bị vô hiệu hóa");
            }

            // FIXED: Find Cancelled status with case-insensitive search
            Optional<OrderStatus> cancelledStatusOpt = orderStatusRepository.findAll()
                    .stream()
                    .filter(status -> "cancelled".equalsIgnoreCase(status.getStatusName()))
                    .findFirst();

            if (cancelledStatusOpt.isEmpty()) {
                System.out.println("ERROR: Cancelled status not found in database");
                System.out.println("Available statuses:");
                orderStatusRepository.findAll().forEach(status -> 
                    System.out.println("- " + status.getStatusName())
                );
                return ResponseEntity.badRequest().body("Không tìm thấy trạng thái Cancelled trong hệ thống");
            }

            OrderStatus cancelledStatus = cancelledStatusOpt.get();
            System.out.println("Using status: " + cancelledStatus.getStatusName());

            // Update order directly
            order.setOrderStatus(cancelledStatus);
            order.setUpdated_at(new Date());
            order.setNote(order.getNote() != null ? order.getNote() + "; Lý do hủy: " + reason : "Lý do hủy: " + reason);
            order.setUpdatedBy(staff);
            
            order = orderService.save(order);
            System.out.println("Order cancelled successfully");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Hủy đơn hàng thành công");
            response.put("reason", reason);
            response.put("order", convertToAdminOrderDTO(order));

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: IllegalArgumentException - " + e.getMessage());
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: Exception - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // ===== STATISTICS ENDPOINTS =====
    @GetMapping("/statistics")
    public ResponseEntity<?> getOrderStatistics() {
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
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating statistics: " + e.getMessage());
        }
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> getDashboardStatistics() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // Basic statistics
            List<Order> allOrders = orderService.findAll();
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", allOrders.size());
            
            // Status statistics
            Map<String, Long> statusStats = new HashMap<>();
            for (Order order : allOrders) {
                String status = order.getOrderStatus().getStatusName();
                statusStats.put(status, statusStats.getOrDefault(status, 0L) + 1);
            }
            stats.put("statusStatistics", statusStats);
            
            // Revenue calculation
            BigDecimal totalRevenue = allOrders.stream()
                    .map(Order::getFinalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalRevenue", totalRevenue);
            
            BigDecimal averageOrderValue = allOrders.isEmpty() ? 
                BigDecimal.ZERO : totalRevenue.divide(BigDecimal.valueOf(allOrders.size()), 2, RoundingMode.HALF_UP);
            stats.put("averageOrderValue", averageOrderValue);
            
            dashboard.put("statistics", stats);
            
            // Count orders by status using basic Java methods
            long pendingCount = allOrders.stream()
                .filter(order -> "Pending".equalsIgnoreCase(order.getOrderStatus().getStatusName()))
                .count();
            dashboard.put("pendingOrdersCount", pendingCount);
            
            // Orders ready to ship (Approved status with approval date set)
            long readyToShipCount = allOrders.stream()
                .filter(order -> "Approved".equalsIgnoreCase(order.getOrderStatus().getStatusName()) 
                    && order.getOrderApprovedAt() != null 
                    && order.getOrderDeliveredCarrierDate() == null)
                .count();
            dashboard.put("readyToShipCount", readyToShipCount);
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating dashboard statistics: " + e.getMessage());
        }
    }

    @GetMapping("/monthly-statistics")
    public ResponseEntity<?> getMonthlyStatistics() {
        try {
            List<Object[]> monthlyStats = orderService.getMonthlyOrderStatistics();
            
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (Object[] stat : monthlyStats) {
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("year", stat[0]);
                monthData.put("month", stat[1]);
                monthData.put("orders", stat[2]);
                monthData.put("revenue", stat[3]);
                result.add(monthData);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating monthly statistics: " + e.getMessage());
        }
    }

    // ===== CONVERSION METHODS =====
    private CustomerOrderDTO convertToCustomerOrderDTO(Order order) {
        CustomerOrderDTO dto = new CustomerOrderDTO();
        dto.setId(order.getId());
        dto.setCreated_at(order.getCreated_at().toString());
        dto.setTotalPrice(order.getTotalPrice().doubleValue());
        dto.setStatus(order.getOrderStatus().getStatusName().toLowerCase());
        dto.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");
        dto.setShippingAddress(order.getShippingAddress() != null ? order.getShippingAddress() : "");
        dto.setOrderApprovedAt(order.getOrderApprovedAt() != null ? order.getOrderApprovedAt().toString() : null);
        dto.setOrderDeliveredCarrierDate(order.getOrderDeliveredCarrierDate() != null ? order.getOrderDeliveredCarrierDate().toString() : null);
        dto.setOrderDeliveredCustomerDate(order.getOrderDeliveredCustomerDate() != null ? order.getOrderDeliveredCustomerDate().toString() : null);
        
        List<OrderItem> orderItems = orderItemService.findByOrder(order);
        dto.setOrderItems(orderItems.stream().map(this::convertToOrderItemDTO).toList());
        
        return dto;
    }

    private AdminOrderDTO convertToAdminOrderDTO(Order order) {
        AdminOrderDTO dto = new AdminOrderDTO();
        dto.setId(order.getId());
        dto.setCreated_at(order.getCreated_at().toString());
        dto.setTotalPrice(order.getTotalPrice().doubleValue());
        
        // Customer info
        Map<String, String> customerMap = new HashMap<>();
        if (order.getCustomer() != null) {
            customerMap.put("first_name", order.getCustomer().getFirst_name());
            customerMap.put("last_name", order.getCustomer().getLast_name());
            customerMap.put("email", order.getCustomer().getEmail());
        }
        dto.setCustomer(customerMap);
        
        // Order status
        Map<String, String> statusMap = new HashMap<>();
        if (order.getOrderStatus() != null) {
            statusMap.put("statusName", order.getOrderStatus().getStatusName());
        }
        dto.setOrderStatus(statusMap);
        
        // Timestamps
        dto.setOrderApprovedAt(order.getOrderApprovedAt() != null ? order.getOrderApprovedAt().toString() : null);
        dto.setOrderDeliveredCarrierDate(order.getOrderDeliveredCarrierDate() != null ? order.getOrderDeliveredCarrierDate().toString() : null);
        dto.setOrderDeliveredCustomerDate(order.getOrderDeliveredCustomerDate() != null ? order.getOrderDeliveredCustomerDate().toString() : null);
        
        // Additional info
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
        dto.setStatus(order.getOrderStatus().getStatusName().toLowerCase());
        dto.setTrackingNumber(order.getTrackingNumber() != null ? order.getTrackingNumber() : "TN" + order.getId().substring(0, 8).toUpperCase());
        
        List<OrderStatusHistoryDTO> statusHistory = List.of(
            new OrderStatusHistoryDTO("created", order.getCreated_at().toString(), "Đơn hàng đã được tạo"),
            order.getOrderApprovedAt() != null ? 
                new OrderStatusHistoryDTO("approved", order.getOrderApprovedAt().toString(), "Đơn hàng đã được duyệt") : null,
            order.getOrderDeliveredCarrierDate() != null ? 
                new OrderStatusHistoryDTO("shipping", order.getOrderDeliveredCarrierDate().toString(), "Đơn hàng đang được giao") : null,
            order.getOrderDeliveredCustomerDate() != null ? 
                new OrderStatusHistoryDTO("delivered", order.getOrderDeliveredCustomerDate().toString(), "Đơn hàng đã được giao") : null
        ).stream().filter(java.util.Objects::nonNull).toList();
        
        dto.setOrderStatuses(statusHistory);
        
        return dto;
    }

    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId().toString());
        dto.setProductId(orderItem.getProduct().getId().toString());
        dto.setProductName(orderItem.getProduct().getProductName());
        
        // Get actual product image from Gallery
        String productImage = "/images/items/1.jpg"; // Default fallback
        try {
            List<Gallery> thumbnails = galleryRepository.findThumbnailByProductId(orderItem.getProduct().getId());
            if (thumbnails != null && !thumbnails.isEmpty()) {
                productImage = thumbnails.get(0).getImage();
            } else {
                // Try to get any image for this product
                List<Gallery> allImages = galleryRepository.findByProductId(orderItem.getProduct().getId());
                if (allImages != null && !allImages.isEmpty()) {
                    productImage = allImages.get(0).getImage();
                }
            }
        } catch (Exception e) {
            // Keep default fallback if error
        }
        dto.setProductImage(productImage);
        
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice().doubleValue());
        dto.setTotal(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())).doubleValue());
        
        return dto;
    }

    // ===== EXCEPTION HANDLERS =====
    // @ExceptionHandler(RuntimeException.class)
    // public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
    //     Map<String, Object> error = new HashMap<>();
    //     error.put("success", false);
    //     error.put("message", e.getMessage());
    //     error.put("timestamp", new Date());
    //     return ResponseEntity.badRequest().body(error);
    // }

    @ExceptionHandler(RuntimeException.class)
public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
    System.out.println("EXCEPTION HANDLER: RuntimeException - " + e.getMessage());
    e.printStackTrace();
    
    Map<String, Object> error = new HashMap<>();
    error.put("success", false);
    error.put("error", "RuntimeException");
    error.put("message", e.getMessage());
    error.put("timestamp", new Date());
    
    return ResponseEntity.badRequest().body(error);
}

    // @ExceptionHandler(IllegalArgumentException.class)
    // public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
    //     Map<String, Object> error = new HashMap<>();
    //     error.put("success", false);
    //     error.put("message", "Invalid input: " + e.getMessage());
    //     error.put("timestamp", new Date());
    //     return ResponseEntity.badRequest().body(error);
    // }

    @ExceptionHandler(Exception.class)
public ResponseEntity<?> handleGenericException(Exception e) {
    System.out.println("EXCEPTION HANDLER: Generic Exception - " + e.getMessage());
    e.printStackTrace();
    
    Map<String, Object> error = new HashMap<>();
    error.put("success", false);
    error.put("error", "InternalServerError");
    error.put("message", "Lỗi server không xác định: " + e.getMessage());
    error.put("timestamp", new Date());
    
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
}

    @ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
    System.out.println("EXCEPTION HANDLER: IllegalArgumentException - " + e.getMessage());
    e.printStackTrace();
    
    Map<String, Object> error = new HashMap<>();
    error.put("success", false);
    error.put("error", "IllegalArgumentException");
    error.put("message", e.getMessage());
    error.put("timestamp", new Date());
    
    return ResponseEntity.badRequest().body(error);
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
public String getNote() { return note; }
public void setNote(String note) { this.note = note; }


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

    public static class UpdateShippingRequest {
    private String status;
    private String trackingNumber;
    private String note;
    private String staffId;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    
    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    }
}