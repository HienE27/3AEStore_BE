package com.nguyenviethien.exercise201.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payment")
    private int idPayment; // Mã thanh toán

    @Column(name = "name_payment")
    private String namePayment; // Tên thanh toán (ví dụ: "VNPay", "COD")

    @Column(name = "description")
    private String description; // Mô tả

    @Column(name = "fee_payment")
    private double feePayment; // Chi phí thanh toán

    @Column(name = "transaction_id")
    private String transactionId; // Mã giao dịch VNPay

    @Column(name = "payment_status")
    private String paymentStatus; // Trạng thái (PENDING, COMPLETED, FAILED)

    @Column(name = "amount")
    private double amount; // Số tiền thanh toán

    @OneToMany(mappedBy = "payment", fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Order> listOrders; // Danh sách đơn hàng
}