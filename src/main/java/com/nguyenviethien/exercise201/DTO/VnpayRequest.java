package com.nguyenviethien.exercise201.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VnpayRequest {
    private String orderId;
    private double amount;
    private String customerId;  // nếu bạn cần lưu lại

    public VnpayRequest() {}

    public VnpayRequest(String orderId, double amount, String customerId) {
        this.orderId = orderId;
        this.amount = amount;
        this.customerId = customerId;
    }

    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCustomerId() {
        return customerId;
    }
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}

