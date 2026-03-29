package com.nguyenviethien.exercise201.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String first_name;

    @Column(nullable = false, length = 100)
    private String last_name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String user_name;

    @Column(nullable = false)
    private String password_hash;

    @Column
    private Boolean active = true;

    @Column(name = "gender")
    private String gender;

    /** Không map xuống DB nếu bảng customers chưa có cột phone_number. Thêm cột sau: ALTER TABLE customers ADD COLUMN phone_number VARCHAR(15) NULL; rồi đổi thành @Column(name = "phone_number", length = 15) */
    @Transient
    private String phoneNumber;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date registered_at;

    @Column(nullable = false)
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated_at;

    // Thêm cascade để tự động xóa addresses khi xóa customer
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerAddress> addresses = new ArrayList<>();

    // Thêm relationship với Order - KHÔNG dùng cascade ở đây để tránh xóa nhầm
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> orders = new ArrayList<>();

    // Thêm field deleted vào Customer entity
    @Column(name = "deleted", nullable = false, columnDefinition = "BIT(1)")
    private Boolean deleted = false;

    @Column(name = "activation_code")
    private String activationCode;

    @Column(name = "activated")
    private Boolean activated = false;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

}