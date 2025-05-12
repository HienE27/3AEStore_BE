package com.duongthuantri.exercise201.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.duongthuantri.exercise201.entity.StaffAccount;
import com.duongthuantri.exercise201.repository.StaffAccountRepository;
import com.duongthuantri.exercise201.security.JwtResponse;
import com.duongthuantri.exercise201.security.LoginRequest;
import com.duongthuantri.exercise201.service.StaffAccountService;
import com.duongthuantri.exercise201.service.JWT.JwtService;

@RestController
@RequestMapping("/api/staff")
public class StaffAccountController {
    @Autowired
    @Qualifier("staffAuthManager")
    private AuthenticationManager staffAuthManager;

    @Autowired
    private StaffAccountService staffAccountService;

    @Autowired
    private StaffAccountRepository staffAccountRepository;
    //
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;



    //
    @GetMapping
    public ResponseEntity<List<StaffAccount>> getAllStaffAccounts() {
        return ResponseEntity.ok(staffAccountService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffAccount> getStaffAccountById(@PathVariable UUID id) {
        return staffAccountService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<StaffAccount> getStaffAccountByEmail(@PathVariable String email) {
        return staffAccountService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StaffAccount> createStaffAccount(@RequestBody StaffAccount staffAccount) {
        // You might want to add password hashing here in a real application
        return ResponseEntity.ok(staffAccountService.save(staffAccount));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffAccount> updateStaffAccount(
            @PathVariable UUID id,
            @RequestBody StaffAccount staffAccount) {
        if (!staffAccountService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        staffAccount.setId(id);
        return ResponseEntity.ok(staffAccountService.save(staffAccount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaffAccount(@PathVariable UUID id) {
        if (!staffAccountService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        staffAccountService.deleteById(id);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    try {
        Authentication auth = staffAuthManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUser_name(), request.getPassword_hash())
        );

        // Lấy đối tượng StaffAccount
        StaffAccount staff = staffAccountRepository.findByUser_name(request.getUser_name());

        if (staff == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy thông tin nhân viên.");
        }
        
        if (auth.isAuthenticated()) {
                // Tạo token cho người dùng
                final String jwtToken = jwtService.generateToken(request.getUser_name());
                return ResponseEntity.ok(new JwtResponse(jwtToken));
            }
    } catch (AuthenticationException e) {
        return ResponseEntity.badRequest().body("Tên đăng nhập hoặc mật khẩu sai (nhân viên)");
    }
    // Default return statement to ensure all code paths return a value
    return ResponseEntity.status(500).body("Đã xảy ra lỗi không xác định.");
}

}