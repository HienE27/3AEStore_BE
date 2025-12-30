package com.nguyenviethien.exercise201.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_addresses")
public class CustomerAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_customer_address_customer"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Customer customer;
    
    // Tên người nhận
    @Column(nullable = false)
    private String recipient_name;
    
    // Địa chỉ cụ thể (số nhà, tên đường) - THAY ĐỔI: không ghép với ward, district
    @Column(nullable = false, length = 500)
    private String address_line1;
    
    // Phường/Xã - THÊM MỚI
    @Column(nullable = false)
    private String ward;
    
    // Quận/Huyện - THÊM MỚI  
    @Column(nullable = false)
    private String district;
    
    // Ghi chú bổ sung (landmark, hướng dẫn giao hàng, etc.)
    @Column(length = 500)
    private String address_line2;
    
    // Số điện thoại
    @Column(nullable = false)
    private String phone_number;
    
    // Mã vùng (+84, +1, etc.)
    @Column(nullable = false)
    private String dial_code;
    
    // Quốc gia
    @Column(nullable = false)
    private String country;
    
    // Mã bưu điện
    @Column(nullable = false)
    private String postal_code;
    
    // Thành phố/Tỉnh (province)
    @Column(nullable = false)
    private String city;
    
    // Địa chỉ mặc định
    @Column(name = "is_default")
    private Boolean isDefault;
    
    // Thời gian tạo
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Thời gian cập nhật
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isDefault == null) {
            isDefault = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper method để lấy địa chỉ đầy đủ theo format cũ (để tương thích)
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        fullAddress.append(address_line1);
        if (ward != null && !ward.trim().isEmpty()) {
            fullAddress.append(", ").append(ward);
        }
        if (district != null && !district.trim().isEmpty()) {
            fullAddress.append(", ").append(district);
        }
        fullAddress.append(", ").append(city);
        if (address_line2 != null && !address_line2.trim().isEmpty()) {
            fullAddress.append(" (").append(address_line2).append(")");
        }
        return fullAddress.toString();
    }
    
    // Helper method để lấy địa chỉ shipping format
    public String getShippingAddress() {
        return String.format("%s, %s, %s, %s", 
            address_line1, ward, district, city);
    }
    
    // Helper method để lấy số điện thoại đầy đủ
    public String getFullPhoneNumber() {
        return dial_code + phone_number;
    }
    
    @Override
    public String toString() {
        return "CustomerAddress{" +
                "id=" + id +
                ", recipient_name='" + recipient_name + '\'' +
                ", address_line1='" + address_line1 + '\'' +
                ", ward='" + ward + '\'' +
                ", district='" + district + '\'' +
                ", city='" + city + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", isDefault=" + isDefault +
                ", createdAt=" + createdAt +
                '}';
    }
}