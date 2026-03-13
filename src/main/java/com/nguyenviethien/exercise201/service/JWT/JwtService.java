package com.nguyenviethien.exercise201.service.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.Role;
import com.nguyenviethien.exercise201.entity.StaffAccount;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import com.nguyenviethien.exercise201.repository.StaffAccountRepository;
import com.nguyenviethien.exercise201.service.StaffAccountService;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtService {
    private static final String KEY_SECRET = "MTIzNDU2NDU5OThEMzIxM0F6eGMzNTE2NTQzMjEzMjE2NTQ5OHEzMTNhMnMxZDMyMnp4M2MyMQ==";
    
    @Autowired
    private StaffAccountService staffAccountService;

    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;
    
    // Tạo jwt dựa trên username (tạo thông tin cần trả về cho FE khi đăng nhập thành công)
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        StaffAccount staffAccount = staffAccountRepository.findByUser_name(username);
        if (staffAccount != null) {
            // store id as String to ensure consistent extraction
            if (staffAccount.getId() != null) claims.put("id", staffAccount.getId().toString());
            claims.put("lastName", staffAccount.getLast_name());
            claims.put("Active", staffAccount.isActive());
            claims.put("userType", "STAFF"); // Thêm để phân biệt loại user
            Role role = staffAccount.getRole();
            if (role != null) {
                if (role.getRole_name().equals("ADMIN")) {
                    claims.put("role", "ADMIN");
                } else {
                    claims.put("role", role.getRole_name());
                }
            }
        }
        return createToken(claims, username);
    }

    public String generateTokenForCustomer(String username) {
        Map<String, Object> claims = new HashMap<>();
        Customer customer = customerRepository.findByUser_name(username);
        if (customer != null) {
            //claims.put("id", customer.getId());
            claims.put("id", customer.getId().toString()); // Đảm bảo dùng ID từ database
            //claims.put("customerId", customer.getId().toString());
            claims.put("email", customer.getEmail());
            claims.put("role", "CUSTOMER");
            claims.put("userType", "CUSTOMER"); // Thêm để phân biệt loại user
        }
        return createToken(claims, username);
    }

    // Methods mới cần thiết cho compatibility với OrderService
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    public String generateTokenForStaff(String staffUserName) {
        return generateToken(staffUserName); // Sử dụng method hiện có
    }

    // Toạ jwt với các claims đã chọn
    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 100000L * 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, getSigneKey())
                .compact();
    }

    // Lấy key_secret
    private Key getSigneKey() {
        byte[] keyByte = Decoders.BASE64.decode(KEY_SECRET);
        return Keys.hmacShaKeyFor(keyByte);
    }

    // Trích xuất thông tin (lấy ra tất cả thông số)
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(getSigneKey()).parseClaimsJws(token).getBody();
    }

    // Trích xuất thông tin cụ thể nhưng triển khai tổng quát (Method Generic)
    public <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    // Lấy ra thời gian hết hạn
    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    // Lấy ra username
    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    // Kiểm tra token đó hết hạn chưa
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Kiểm tra tính hợp lệ của token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Method mới cho OrderService compatibility
    public Boolean isTokenValid(String token, UserDetails userDetails) {
        return validateToken(token, userDetails);
    }

    public UUID extractId(String token) {
        return extractClaims(token, claims -> {
            Object idObj = claims.get("id");
            if (idObj == null) return null;
            try {
                if (idObj instanceof String) {
                    return UUID.fromString((String) idObj);
                } else if (idObj instanceof UUID) {
                    return (UUID) idObj;
                } else {
                    return UUID.fromString(idObj.toString());
                }
            } catch (Exception e) {
                return null;
            }
        });
    }

    // Method để lấy user type từ token
    public String getUserTypeFromToken(String token) {
        return extractClaims(token, claims -> claims.get("userType", String.class));
    }

    // Method để lấy role từ token
    public String getRoleFromToken(String token) {
        return extractClaims(token, claims -> claims.get("role", String.class));
    }
}