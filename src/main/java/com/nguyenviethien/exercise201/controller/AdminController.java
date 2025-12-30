package com.nguyenviethien.exercise201.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.StaffAccount;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import com.nguyenviethien.exercise201.repository.StaffAccountRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // Fetch all staff accounts (không cần phân quyền ADMIN nữa)
    @GetMapping("/staff-accounts")
    public ResponseEntity<List<StaffAccount>> getAllStaffAccounts() {
        List<StaffAccount> staffAccounts = staffAccountRepository.findAll();
        return ResponseEntity.ok(staffAccounts);
    }

    // Fetch all customers (không cần phân quyền ADMIN nữa)
    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return ResponseEntity.ok(customers);
    }

    // Các phương thức khác cũng có thể bỏ phân quyền như trên
}
