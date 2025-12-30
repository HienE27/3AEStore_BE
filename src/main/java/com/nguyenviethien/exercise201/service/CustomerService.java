package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nguyenviethien.exercise201.entity.Customer;

public interface CustomerService {
    // Phân trang lấy tất cả khách hàng
    Page<Customer> findAll(Pageable pageable);
    // List<Customer> findAll();


    // Tìm khách hàng theo id
    Optional<Customer> findById(UUID id);

    // Tìm khách hàng theo email
    Optional<Customer> findByEmail(String email);
    
    List<Customer> searchCustomers(String searchTerm);
    Optional<Customer> findByEmailSingle(String email);

    // Lưu hoặc cập nhật khách hàng
    Customer save(Customer customer);

    // Xóa khách hàng theo id
    void deleteById(UUID id);

    // Kiểm tra tồn tại theo id
    boolean existsById(UUID id);

    // Cập nhật khách hàng
    Customer update(UUID id, Customer updatedCustomer);

    // Kiểm tra tồn tại theo email
    boolean existsByEmail(String email);

    // Find by email containing (case insensitive)
    List<Customer> findByEmailContainingIgnoreCase(String email);

     
    // --- Phương thức mới thêm để hỗ trợ kích hoạt tài khoản ---
    
    /**
     * Gửi mã kích hoạt đến email khách hàng.
     * @param email Email khách hàng cần gửi mã kích hoạt.
     * @throws RuntimeException nếu không tìm thấy email hoặc lỗi gửi mail.
     */
    void sendActivationCode(String email);

    /**
     * Kích hoạt tài khoản khách hàng dựa trên email và mã kích hoạt.
     * @param email Email khách hàng.
     * @param code Mã kích hoạt được gửi đến email.
     * @return true nếu kích hoạt thành công, false nếu mã không hợp lệ.
     * @throws RuntimeException nếu không tìm thấy email.
     */
    boolean activateAccount(String email, String code);
}
