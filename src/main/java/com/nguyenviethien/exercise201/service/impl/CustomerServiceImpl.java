package com.nguyenviethien.exercise201.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.Order;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import com.nguyenviethien.exercise201.repository.OrderItemRepository;
import com.nguyenviethien.exercise201.repository.OrderRepository;
import com.nguyenviethien.exercise201.service.CustomerService;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public Page<Customer> findAll(Pageable pageable) {
        //return customerRepository.findAll(pageable);
        return customerRepository.findAllActive(pageable);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return customerRepository.findById(id);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    public List<Customer> searchCustomers(String searchTerm) {
        return customerRepository.findByEmailContainingIgnoreCase(searchTerm);
    }


    @Override
public Optional<Customer> findByEmailSingle(String email) {
    return customerRepository.findByEmail(email);
}

@Override
public List<Customer> findByEmailContainingIgnoreCase(String email) {
    return customerRepository.findByEmailContainingIgnoreCase(email);
}


  
    

@Override
public Customer save(Customer customer) {
    if (customer.getUser_name() == null || customer.getUser_name().isEmpty()) {
        throw new IllegalArgumentException("Hãy nhập tên đăng nhập.");
    }
    if (customer.getFirst_name() == null || customer.getFirst_name().isEmpty()) {
        throw new IllegalArgumentException("Hãy nhập firstName.");
    }
    if (customer.getLast_name() == null || customer.getLast_name().isEmpty()) {
        throw new IllegalArgumentException("Hãy nhập lastName.");
    }
    if (customer.getEmail() == null || customer.getEmail().isEmpty()) {
        throw new IllegalArgumentException("Hãy nhập email.");
    }
    if (customer.getPassword_hash() == null || customer.getPassword_hash().isEmpty()) {
        throw new IllegalArgumentException("Hãy nhập password.");
    }

    String encodePassword = passwordEncoder.encode(customer.getPassword_hash());
    customer.setPassword_hash(encodePassword);

    customer.setActive(true);
    // Không set registered_at và updated_at thủ công - để @CreationTimestamp và @UpdateTimestamp tự động xử lý
    // customer.setRegistered_at(new Date()); // Removed - handled by @CreationTimestamp
    // customer.setUpdated_at(new Date()); // Removed - handled by @UpdateTimestamp

    // Mặc định khi tạo mới chưa kích hoạt, cần kích hoạt qua email
    customer.setActivated(false);
    customer.setActivationCode(null);
    
    // Đảm bảo deleted = false cho customer mới
    if (customer.getDeleted() == null) {
        customer.setDeleted(false);
    }

    // Lưu customer - JPA sẽ tự động generate ID và set timestamps
    Customer savedCustomer = customerRepository.save(customer);

    // Gửi mail kích hoạt ngay sau khi tạo thành công
    try {
        sendActivationCode(savedCustomer.getEmail());
    } catch (Exception e) {
        System.err.println("Lỗi khi gửi mail kích hoạt: " + e.getMessage());
    }

    return savedCustomer;
}


    @Override
    public Customer update(UUID id, Customer updatedCustomer) {
        Customer existingCustomer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));

        existingCustomer.setFirst_name(updatedCustomer.getFirst_name());
        existingCustomer.setLast_name(updatedCustomer.getLast_name());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        existingCustomer.setGender(updatedCustomer.getGender());
        existingCustomer.setUser_name(updatedCustomer.getUser_name());

        if (updatedCustomer.getPassword_hash() != null && !updatedCustomer.getPassword_hash().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(updatedCustomer.getPassword_hash());
            existingCustomer.setPassword_hash(encodedPassword);
        }

        existingCustomer.setUpdated_at(new Date());

        return customerRepository.save(existingCustomer);
    }

 

    @Override
@Transactional
public void deleteById(UUID id) {
    Optional<Customer> customerOpt = customerRepository.findById(id);
    if (customerOpt.isEmpty()) {
        throw new RuntimeException("Customer not found with ID: " + id);
    }
    Customer customer = customerOpt.get();
    customer.setDeleted(true); // XÓA MỀM
    customerRepository.save(customer);
}


    @Override
    public boolean existsById(UUID id) {
        return customerRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    // --- Phần mới: gửi mã kích hoạt ---
    @Override
    public void sendActivationCode(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy email: " + email));

        if (customer.getActivated()) {
            throw new RuntimeException("Tài khoản đã được kích hoạt.");
        }

        String activationCode = java.util.UUID.randomUUID().toString();
        customer.setActivationCode(activationCode);
        customerRepository.save(customer);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Mã kích hoạt tài khoản");
            message.setText("Mã kích hoạt của bạn là: " + activationCode + "\n" +
                    "Vui lòng kích hoạt tài khoản tại: http://localhost:3000/activate?email=" + email + "&code=" + activationCode);
            mailSender.send(message);
            System.out.println("Mail kích hoạt đã gửi đến: " + email);
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail kích hoạt tới " + email + ": " + e.getMessage());
            throw new RuntimeException("Gửi mail kích hoạt thất bại.");
        }
    }

    // --- Phần mới: kích hoạt tài khoản ---
    @Override
    public boolean activateAccount(String email, String code) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy email: " + email));

        if (customer.getActivated()) {
            return true; // Đã kích hoạt rồi
        }

        if (code.equals(customer.getActivationCode())) {
            customer.setActivated(true);
            customer.setActivationCode(null);
            customerRepository.save(customer);
            return true;
        }

        return false;
    }

    @Override
    public Customer updateAvatar(UUID id, String avatarUrl) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng: " + id));
        customer.setAvatarUrl(avatarUrl);
        return customerRepository.save(customer);
    }
}