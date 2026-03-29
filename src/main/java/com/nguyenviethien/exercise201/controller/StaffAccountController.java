package com.nguyenviethien.exercise201.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nguyenviethien.exercise201.DTO.StaffAccountDto;
import com.nguyenviethien.exercise201.entity.StaffAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.entity.StaffAccount;
import com.nguyenviethien.exercise201.repository.StaffAccountRepository;
import com.nguyenviethien.exercise201.exception.ApiResponse;
import com.nguyenviethien.exercise201.security.LoginRequest;
import com.nguyenviethien.exercise201.service.StaffAccountService;
import com.nguyenviethien.exercise201.security.JWT.JwtService;

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
    public ResponseEntity<List<StaffAccountDto>> getAllStaffAccounts() {
        List<StaffAccountDto> staffDtos = staffAccountService.findAll().stream()
                .map(StaffAccountDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(staffDtos);
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
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = staffAuthManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUser_name(), request.getPassword_hash())
            );

            StaffAccount staff = staffAccountRepository.findByUser_name(request.getUser_name());
            if (staff == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không tìm thấy thông tin nhân viên."));
            }

            if (auth.isAuthenticated()) {
                String jwtToken = jwtService.generateToken(request.getUser_name());
                Map<String, Object> data = new HashMap<>();
                data.put("token", jwtToken);
                data.put("id", staff.getId() != null ? staff.getId().toString() : null);
                data.put("user_name", staff.getUser_name());
                if (staff.getRole() != null) {
                    data.put("role", staff.getRole().getRole_name());
                }
                Map<String, Object> user = new HashMap<>();
                user.put("id", staff.getId() != null ? staff.getId().toString() : null);
                user.put("user_name", staff.getUser_name());
                if (staff.getRole() != null) {
                    user.put("role", staff.getRole().getRole_name());
                }
                data.put("user", user);
                return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", data));
            }
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Tên đăng nhập hoặc mật khẩu sai (nhân viên)"));
        }
        return ResponseEntity.status(500).body(ApiResponse.error("Đã xảy ra lỗi không xác định."));
    }


}