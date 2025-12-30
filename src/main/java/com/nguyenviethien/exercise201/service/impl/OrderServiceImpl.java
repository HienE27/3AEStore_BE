package com.nguyenviethien.exercise201.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nguyenviethien.exercise201.controller.OrderController.CheckoutRequest;
import com.nguyenviethien.exercise201.entity.*;
import com.nguyenviethien.exercise201.repository.*;
import com.nguyenviethien.exercise201.service.OrderService;
import com.nguyenviethien.exercise201.config.VNPayConfig;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CardRepository cartRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CardItemRepository cartItemRepository;

    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> findById(String id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> findByCustomer(Customer customer) {
        return orderRepository.findByCustomer(customer);
    }

    @Override
    public Page<Order> findByCustomer(Customer customer, Pageable pageable) {
        return orderRepository.findByCustomer(customer, pageable);
    }

    @Override
    public List<Order> findByOrderStatus(OrderStatus orderStatus) {
        return orderRepository.findByOrderStatus(orderStatus);
    }

    @Override
    public List<Order> findByCustomerAndOrderStatus(Customer customer, OrderStatus orderStatus) {
        return orderRepository.findByCustomerAndOrderStatus(customer, orderStatus);
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public void deleteById(String id) {
        orderRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return orderRepository.existsById(id);
    }


    @Override
public Page<Order> findOrdersWithFilters(String search, String status, Pageable pageable) {
    return orderRepository.findOrdersWithFilters(search, status, pageable);
}

@Override
public List<Order> findPendingOrders() {
    return orderRepository.findPendingOrders();
}

@Override
public long countPendingOrders() {
    return orderRepository.countPendingOrders();
}

@Override
public List<Order> findOrdersReadyToShip() {
    return orderRepository.findOrdersReadyToShip();
}

@Override
public List<Object[]> getMonthlyOrderStatistics() {
    return orderRepository.getMonthlyOrderStatistics();
}



    /**
     * Checkout với khả năng áp dụng coupon, địa chỉ, số điện thoại và phương thức thanh toán
     */
    @Override
    public ResponseEntity<?> checkout(UUID customerId) {
        return checkoutWithCoupon(customerId, null, null, null, "COD", null, null);
    }

    public ResponseEntity<?> checkoutWithCoupon(UUID customerId, String couponCode, 
                                              String shippingAddress, String phoneNumber, 
                                              String paymentMethod, List<CheckoutRequest.OrderDetail> orderDetails,
                                              String note) {
        try {
            // 1. Kiểm tra customer
            if (customerId == null) {
                return ResponseEntity.badRequest().body("Customer ID không hợp lệ");
            }
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Khách hàng không tồn tại");
            }
            Customer customer = customerOpt.get();

            // 2. Kiểm tra orderDetails
            if (orderDetails == null || orderDetails.isEmpty()) {
                return ResponseEntity.badRequest().body("Danh sách sản phẩm trống");
            }

            // 3. Kiểm tra tồn kho và tính tổng tiền
            BigDecimal totalPrice = BigDecimal.ZERO;
            for (CheckoutRequest.OrderDetail detail : orderDetails) {
                Product product = productRepository.findById(UUID.fromString(detail.getProductId()))
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại với ID: " + detail.getProductId()));
                if (product.getQuantity() < detail.getQuantity()) {
                    return ResponseEntity.badRequest()
                        .body("Sản phẩm " + product.getProductName() + " không đủ hàng trong kho. " +
                              "Còn lại: " + product.getQuantity() + ", yêu cầu: " + detail.getQuantity());
                }
                totalPrice = totalPrice.add(new BigDecimal(String.valueOf(detail.getPrice())).multiply(BigDecimal.valueOf(detail.getQuantity())));
            }

            // 4. Áp dụng coupon (nếu có)
            BigDecimal discountAmount = BigDecimal.ZERO;
            Coupon appliedCoupon = null;
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                Optional<Coupon> couponOpt = couponRepository.findValidCouponByCode(couponCode, new Date());
                if (couponOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body("Mã giảm giá không hợp lệ hoặc đã hết hạn");
                }
                appliedCoupon = couponOpt.get();
                if (appliedCoupon.getOrderAmountLimit() != null && totalPrice.compareTo(appliedCoupon.getOrderAmountLimit()) < 0) {
                    return ResponseEntity.badRequest()
                        .body("Đơn hàng phải có giá trị tối thiểu " + appliedCoupon.getOrderAmountLimit() + " để áp dụng mã giảm giá");
                }
                discountAmount = calculateDiscountAmount(totalPrice, appliedCoupon);
            }

            // 5. Tạo đơn hàng
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setCustomer(customer);
            order.setTotalPrice(totalPrice);
            order.setDiscountAmount(discountAmount);
            order.setCoupon(appliedCoupon);
            order.setShippingAddress(shippingAddress);
            order.setPhoneNumber(phoneNumber); // Sử dụng phoneNumber từ request
            order.setNote(note); // thêm dòng này để lưu note từ request

            order.setPaymentMethod(paymentMethod);
            order.setPaymentStatus("PENDING");
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            order.setCreated_at(Date.from(vietnamTime.toInstant()));
            order.setUpdated_at(Date.from(vietnamTime.toInstant()));

            OrderStatus defaultStatus = orderStatusRepository.findByStatusName("Pending")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái Pending"));
            order.setOrderStatus(defaultStatus);

            // 6. Xử lý phương thức thanh toán
            String paymentUrl = null;
            if ("BANK_TRANSFER".equals(paymentMethod)) {
                Payment payment = new Payment();
                payment.setNamePayment("VNPay");
                payment.setAmount(totalPrice.doubleValue());
                payment.setTransactionId(order.getId());
                payment.setPaymentStatus("PENDING");
                payment = paymentRepository.save(payment);
                order.setPayment(payment);

                // Tạo URL thanh toán VNPay
                paymentUrl = generateVNPayUrl(order.getId(), totalPrice.doubleValue());
            } else if ("COD".equals(paymentMethod)) {
                order.setPaymentStatus("PENDING");
            } else {
                return ResponseEntity.badRequest().body("Phương thức thanh toán không hỗ trợ");
            }

            order.setFinalPrice(); // Thêm dòng này sau khi tính discountAmount
            order = orderRepository.save(order);

            // 7. Tạo order items và cập nhật tồn kho
            for (CheckoutRequest.OrderDetail detail : orderDetails) {
                Product product = productRepository.findById(UUID.fromString(detail.getProductId()))
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại với ID: " + detail.getProductId()));
                product.setQuantity(product.getQuantity() - detail.getQuantity());
                productRepository.save(product);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setPrice(new BigDecimal(String.valueOf(detail.getPrice())));
                orderItem.setQuantity(detail.getQuantity());
                orderItemRepository.save(orderItem);
            }

            // 8. Cập nhật usage coupon
            if (appliedCoupon != null) {
                appliedCoupon.setTimesUsed(appliedCoupon.getTimesUsed().add(BigDecimal.ONE));
                couponRepository.save(appliedCoupon);
            }

            // 9. Xóa giỏ hàng (nếu có)
            // Card cart = cartRepository.findByCustomerId(customerId).orElse(null);
            // if (cart != null) {
            //     cartItemRepository.deleteAll(cart.getCardItems());
            //     cartRepository.delete(cart);
            // }

            // 10. Trả về kết quả
            CheckoutResponse response = new CheckoutResponse(
                order.getId(),
                "Đặt hàng thành công!",
                totalPrice,
                discountAmount,
                order.getFinalPrice(),
                couponCode
            );
            if ("BANK_TRANSFER".equals(paymentMethod)) {
                response.setMessage("Vui lòng thanh toán qua VNPay để hoàn tất!");
                Map<String, Object> result = new HashMap<>();
                result.put("orderId", order.getId());
                result.put("message", response.getMessage());
                result.put("paymentUrl", paymentUrl);
                result.put("originalTotal", response.getOriginalTotal().doubleValue());
                result.put("discountAmount", response.getDiscountAmount().doubleValue());
                result.put("finalTotal", response.getFinalTotal().doubleValue());
                result.put("couponUsed", response.getCouponUsed());
                result.put("note", note);

                return ResponseEntity.ok(result);
            }
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi trong quá trình thanh toán: " + e.getMessage());
        }
    }

    private String generateVNPayUrl(String orderId, double amount) throws UnsupportedEncodingException {
        String vnp_TxnRef = orderId;
        String vnp_IpAddr = "127.0.0.1"; // Cần lấy từ HttpServletRequest trong thực tế
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VNPayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((long) (amount * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (Iterator<String> it = fieldNames.iterator(); it.hasNext();) {
            String fieldName = it.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                     .append('=')
                     .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (it.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return VNPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    private BigDecimal calculateCartTotal(Card cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CardItem item : cart.getCardItems()) {
            BigDecimal itemPrice = calculateProductPrice(item.getProduct());
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }
        return total;
    }

    private BigDecimal calculateProductPrice(Product product) {
        BigDecimal buyingPrice = product.getBuyingPrice();
        BigDecimal salePercent = product.getSalePrice();
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                salePercent.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        return buyingPrice.multiply(discountMultiplier);
    }

    private BigDecimal calculateDiscountAmount(BigDecimal totalPrice, Coupon coupon) {
        if ("PERCENTAGE".equals(coupon.getDiscountType())) {
            return totalPrice.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if ("FIXED".equals(coupon.getDiscountType())) {
            return coupon.getDiscountValue().min(totalPrice);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Order approveOrder(UUID orderId, UUID staffId) {
        Order order = orderRepository.findById(orderId.toString())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        StaffAccount staff = staffAccountRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        OrderStatus approvedStatus = orderStatusRepository.findByStatusName("Approved")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái Approved"));
        
        order.setOrderStatus(approvedStatus);
        order.setOrderApprovedAt(new Date());
        order.setUpdatedBy(staff);
        order.setPaymentStatus("APPROVED");
        
        return orderRepository.save(order);
    }

    @Override
    public Order markOrderAsShipped(UUID orderId, UUID staffId) {
        Order order = orderRepository.findById(orderId.toString())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        StaffAccount staff = staffAccountRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        OrderStatus shippedStatus = orderStatusRepository.findByStatusName("Shipped")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái Shipped"));
        
        order.setOrderStatus(shippedStatus);
        order.setOrderDeliveredCarrierDate(new Date());
        order.setUpdatedBy(staff);
        
        return orderRepository.save(order);
    }

    @Override
    public Order customerAcceptOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId.toString())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        OrderStatus deliveredStatus = orderStatusRepository.findByStatusName("Delivered")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái Delivered"));
        
        order.setOrderStatus(deliveredStatus);
        order.setOrderDeliveredCustomerDate(new Date());
        order.setPaymentStatus("COMPLETED");
        
        return orderRepository.save(order);
    }

    @Override
    public Optional<OrderStatus> findOrderStatusById(UUID id) {
        return orderStatusRepository.findById(id);
    }

    @Override
    public void deleteOrdersByCustomer(Customer customer) {
        List<Order> orders = orderRepository.findByCustomer(customer);
        for (Order order : orders) {
            orderItemRepository.deleteAllByOrder(order);
            orderRepository.delete(order);
        }
    }

    // DTO cho response checkout
    public static class CheckoutResponse {
        private String orderId;
        private String message;
        private BigDecimal originalTotal;
        private BigDecimal discountAmount;
        private BigDecimal finalTotal;
        private String couponUsed;

        public CheckoutResponse(String orderId, String message, BigDecimal originalTotal, 
                              BigDecimal discountAmount, BigDecimal finalTotal, String couponUsed) {
            this.orderId = orderId;
            this.message = message;
            this.originalTotal = originalTotal;
            this.discountAmount = discountAmount;
            this.finalTotal = finalTotal;
            this.couponUsed = couponUsed;
        }

        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public BigDecimal getOriginalTotal() { return originalTotal; }
        public void setOriginalTotal(BigDecimal originalTotal) { this.originalTotal = originalTotal; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
        public BigDecimal getFinalTotal() { return finalTotal; }
        public void setFinalTotal(BigDecimal finalTotal) { this.finalTotal = finalTotal; }
        public String getCouponUsed() { return couponUsed; }
        public void setCouponUsed(String couponUsed) { this.couponUsed = couponUsed; }
    }
}