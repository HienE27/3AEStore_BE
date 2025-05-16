package com.duongthuantri.exercise201.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duongthuantri.exercise201.entity.Customer;
import com.duongthuantri.exercise201.repository.CustomerRepository;
import com.duongthuantri.exercise201.service.CustomerService;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
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

        if (customer.getEmail() == null || customer.getEmail().isEmpty()) {
        throw new IllegalArgumentException("Hãy nhập email.");
        }

        if (customer.getPassword_hash() == null || customer.getPassword_hash().isEmpty()) {
        throw new IllegalArgumentException("Hãy nhập password.");
        }
        //mã hóa mật khẩu trước khi lưu vào cơ sở dữ liệu
        String encodePassword = passwordEncoder.encode(customer.getPassword_hash());
        customer.setPassword_hash(encodePassword);
        customer.setActive(true);
        customer.setRegistered_at(new Date());
        customer.setUpdated_at(new Date());
        //nếu không có tên người dùng thì báo lỗi làm tên người dùng
        return customerRepository.save(customer);
    }
    @Override
    public Customer update(UUID id, Customer updatedCustomer) {
        Customer existingCustomer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));

        // Cập nhật các trường cho phép
        existingCustomer.setFirst_name(updatedCustomer.getFirst_name());
        existingCustomer.setLast_name(updatedCustomer.getLast_name());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        existingCustomer.setGender(updatedCustomer.getGender());
        // existingCustomer.setPhone_number(updatedCustomer.getPhone_number());
        existingCustomer.setUser_name(updatedCustomer.getUser_name());

        // Nếu có mật khẩu mới thì mã hóa
        if (updatedCustomer.getPassword_hash() != null && !updatedCustomer.getPassword_hash().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(updatedCustomer.getPassword_hash());
            existingCustomer.setPassword_hash(encodedPassword);
        }

        return customerRepository.save(existingCustomer);
    }
    @Override
    public void deleteById(UUID id) {
        customerRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return customerRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }
}