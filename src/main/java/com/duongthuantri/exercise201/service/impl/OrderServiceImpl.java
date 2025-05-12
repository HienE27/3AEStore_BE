package com.duongthuantri.exercise201.service.impl;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duongthuantri.exercise201.entity.Card;
import com.duongthuantri.exercise201.entity.CardItem;
import com.duongthuantri.exercise201.entity.Customer;
import com.duongthuantri.exercise201.entity.Order;
import com.duongthuantri.exercise201.entity.OrderItem;
import com.duongthuantri.exercise201.entity.OrderStatus;
import com.duongthuantri.exercise201.entity.Product;
import com.duongthuantri.exercise201.entity.StaffAccount;
import com.duongthuantri.exercise201.repository.CardItemRepository;
import com.duongthuantri.exercise201.repository.CardRepository;
import com.duongthuantri.exercise201.repository.CustomerRepository;
import com.duongthuantri.exercise201.repository.OrderItemRepository;
import com.duongthuantri.exercise201.repository.OrderRepository;
import com.duongthuantri.exercise201.repository.OrderStatusRepository;
import com.duongthuantri.exercise201.repository.ProductRepository;
import com.duongthuantri.exercise201.repository.StaffAccountRepository;
import com.duongthuantri.exercise201.service.OrderService;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    //
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
    //
    @Autowired
    private OrderRepository orderRepository;

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
    //checkout
    @Override
    public ResponseEntity<?> checkout(UUID customerId) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) return ResponseEntity.badRequest().body("Khách hàng không tồn tại");

        Card cart = cartRepository.findByCustomerId(customerId).orElse(null);
        if (cart == null || cart.getCardItems().isEmpty()) {
            return ResponseEntity.badRequest().body("Giỏ hàng trống");
        }
        // Tính tổng tiền trước
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CardItem item : cart.getCardItems()) {
            BigDecimal buyingPrice = item.getProduct().getBuyingPrice();
            BigDecimal salePercent = item.getProduct().getSalePrice(); // Giả sử đây là phần trăm giảm giá (VD: 20 cho 20%)

            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(salePercent.divide(BigDecimal.valueOf(100)));
            BigDecimal discountedPrice = buyingPrice.multiply(discountMultiplier);

            BigDecimal itemTotal = discountedPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);
        }

        // Tạo đơn hàng mới
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setCustomer(customerOpt.get());
        //LẤY GIỜ VIỆT NAM
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Date createdAt = Date.from(vietnamTime.toInstant());
        order.setCreated_at(createdAt);

        order.setTotalPrice(totalPrice);
        // Gán trạng thái mặc định
        order.setOrderStatus(orderStatusRepository.findByStatusName("Pending").orElse(null));
        order = orderRepository.save(order);

        // Tạo các item
        for (CardItem item : cart.getCardItems()) {
            Product product = item.getProduct();
        int orderedQuantity = item.getQuantity();

        if (product.getQuantity() < orderedQuantity) {
            throw new RuntimeException("Sản phẩm " + product.getProductName() + " không đủ hàng trong kho");
        }

        product.setQuantity(product.getQuantity() - orderedQuantity); // Trừ số lượng
        productRepository.save(product); // Lưu lại

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setPrice(item.getProduct().getBuyingPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItemRepository.save(orderItem);
        }

        // Xóa giỏ hàng sau khi đặt
        cartItemRepository.deleteAll(cart.getCardItems());
        cartRepository.delete(cart);

        return ResponseEntity.ok("Thanh toán thành công");
    }
    //Chấp nhận order bới nhân viên
    @Override
    public Order approveOrder(UUID orderId, UUID staffId) {
        Order order = orderRepository.findById(orderId.toString())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        StaffAccount staff = staffAccountRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        order.setOrderApprovedAt(new Date());
        order.setUpdatedBy(staff);

        return orderRepository.save(order);
    }
    //chấp nhận chuyển phát cho nhà vận chuyển
    @Override
    public Order markOrderAsShipped(UUID orderId, UUID staffId) {
        Order order = orderRepository.findById(orderId.toString())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        StaffAccount staff = staffAccountRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        order.setOrderDeliveredCarrierDate(new Date()); // Cập nhật ngày gửi hàng
        order.setUpdatedBy(staff);

        return orderRepository.save(order);
    }
    //chập nhận giao cho khách hàng
    @Override
    public Order customerAcceptOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId.toString())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Cập nhật ngày khách xác nhận đã nhận hàng
        order.setOrderDeliveredCustomerDate(new Date());

        // Cập nhật trạng thái đơn hàng nếu muốn (tuỳ hệ thống bạn)
        // Ví dụ: set status là "Đã giao thành công"
        OrderStatus completedStatus = orderStatusRepository.findByStatusName("Delivered")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái Completed"));
        order.setOrderStatus(completedStatus);

        return orderRepository.save(order);
    }

}