// package com.nguyenviethien.exercise201.controller;

// import com.nguyenviethien.exercise201.config.VNPayConfig;
// import com.nguyenviethien.exercise201.DTO.VnpayRequest;
// import com.nguyenviethien.exercise201.entity.Order;
// import com.nguyenviethien.exercise201.service.OrderService;
// import jakarta.servlet.http.HttpServletRequest;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.io.UnsupportedEncodingException;
// import java.net.URLEncoder;
// import java.nio.charset.StandardCharsets;
// import java.text.SimpleDateFormat;
// import java.util.*;

// @RestController
// @RequestMapping("/api/payments")
// @CrossOrigin("*")
// public class PaymentController {

//     private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

//     @Autowired
//     private OrderService orderService;

//     @PostMapping("/vnpay")
//     public ResponseEntity<?> createVNPayPayment(
//             HttpServletRequest request,
//             @RequestBody VnpayRequest dto
//     ) throws UnsupportedEncodingException {
//         // 1. Lấy dữ liệu từ DTO
//         String orderId = dto.getOrderId();
//         double amountInput = dto.getAmount();

//         // 2. Kiểm tra order tồn tại
//         Order order = orderService.findById(orderId).orElse(null);
//         if (order == null) {
//             return ResponseEntity.badRequest().body("Order not found");
//         }

//         // 3. Chuẩn bị tham số VNPay
//         String vnp_TxnRef = orderId;
//         String vnp_IpAddr = VNPayConfig.getIpAddress(request);
//         String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

//         Map<String, String> vnp_Params = new HashMap<>();
//         vnp_Params.put("vnp_Version", VNPayConfig.vnp_Version);
//         vnp_Params.put("vnp_Command", VNPayConfig.vnp_Command);
//         vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
//         // VNPay tính theo đơn vị 100 VND
//         long vnp_Amount = (long) (amountInput * 100);
//         vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
//         vnp_Params.put("vnp_CurrCode", "VND");
//         vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
//         vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + vnp_TxnRef);
//         vnp_Params.put("vnp_OrderType", "other");
//         vnp_Params.put("vnp_Locale", "vn");
//         vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
//         vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

//         // 4. Tạo ngày tạo và ngày hết hạn
//         Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
//         SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//         String vnp_CreateDate = formatter.format(cld.getTime());
//         vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

//         cld.add(Calendar.MINUTE, 15);
//         String vnp_ExpireDate = formatter.format(cld.getTime());
//         vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

//         // 5. Build chuỗi hashData và query
//         List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
//         Collections.sort(fieldNames);
//         StringBuilder hashData = new StringBuilder();
//         StringBuilder query = new StringBuilder();
//         for (Iterator<String> it = fieldNames.iterator(); it.hasNext(); ) {
//             String fieldName = it.next();
//             String fieldValue = vnp_Params.get(fieldName);
//             if (fieldValue != null && !fieldValue.isEmpty()) {
//                 hashData.append(fieldName)
//                         .append('=')
//                         .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
//                 query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
//                      .append('=')
//                      .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
//                 if (it.hasNext()) {
//                     hashData.append('&');
//                     query.append('&');
//                 }
//             }
//         }

//         // 6. Sinh secure hash và tạo URL hoàn chỉnh
//         String queryUrl = query.toString();
//         String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
//         queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
//         String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;

//         logger.info("Generated VNPay URL for orderId {}: {}", orderId, paymentUrl);
//         return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
//     }

//     @GetMapping("/vnpay/callback")
//     public ResponseEntity<?> handleVNPayCallback(HttpServletRequest request) {
//         Map<String, String[]> params = request.getParameterMap();
//         logger.info("VNPay callback parameters: {}", params);

//         String responseCode = request.getParameter("vnp_ResponseCode");
//         String txnRef      = request.getParameter("vnp_TxnRef");
//         if (responseCode == null || txnRef == null) {
//             return ResponseEntity.badRequest().body("Missing required parameters");
//         }

//         Order order = orderService.findById(txnRef).orElse(null);
//         if (order == null) {
//             return ResponseEntity.badRequest().body("Order not found");
//         }

//         if ("00".equals(responseCode)) {
//             order.setPaymentStatus("COMPLETED");
//             orderService.save(order);
//             return ResponseEntity.ok("redirect:http://localhost:3000/check-out?success=true&orderId=" + txnRef);
//         } else {
//             order.setPaymentStatus("FAILED");
//             orderService.save(order);
//             return ResponseEntity.ok("redirect:http://localhost:3000/check-out?success=false&orderId=" + txnRef);
//         }
//     }
// }




package com.nguyenviethien.exercise201.controller;

import com.nguyenviethien.exercise201.config.VNPayConfig;
import com.nguyenviethien.exercise201.DTO.VnpayRequest;
import com.nguyenviethien.exercise201.entity.Order;
import com.nguyenviethien.exercise201.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin("*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping("/vnpay")
    public Map<String, Object> createVNPayPayment(
            HttpServletRequest request,
            @RequestBody VnpayRequest dto
    ) throws UnsupportedEncodingException {
        String orderId = dto.getOrderId();
        double amountInput = dto.getAmount();

        Order order = orderService.findById(orderId).orElse(null);
        if (order == null) {
            return Map.of("error", "Order not found");
        }

        String vnp_TxnRef = orderId;
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VNPayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        long vnp_Amount = (long) (amountInput * 100);
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
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
        for (Iterator<String> it = fieldNames.iterator(); it.hasNext(); ) {
            String fieldName = it.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName)
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
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
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;

        logger.info("Generated VNPay URL for orderId {}: {}", orderId, paymentUrl);
        return Map.of("paymentUrl", paymentUrl);
    }

    // === CHỈNH SỬA PHẦN NÀY ===
@GetMapping("/vnpay/callback")
public void handleVNPayCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String responseCode = request.getParameter("vnp_ResponseCode");
    String txnRef = request.getParameter("vnp_TxnRef");

    if (txnRef == null) {
        response.sendRedirect("http://localhost:3001/checkout?success=false");
        return;
    }

    Order order = orderService.findById(txnRef).orElse(null);

    if (order == null) {
        response.sendRedirect("http://localhost:3001/checkout?success=false&orderId=" + txnRef);
        return;
    }

    if ("00".equals(responseCode)) {
        order.setPaymentStatus("COMPLETED");
        orderService.save(order);
        response.sendRedirect("http://localhost:3001/checkout?success=true&orderId=" + txnRef);
    } else {
        order.setPaymentStatus("FAILED");
        orderService.save(order);
        response.sendRedirect("http://localhost:3001/checkout?success=false&orderId=" + txnRef);
    }
}

}
