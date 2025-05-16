package com.duongthuantri.exercise201.service.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.duongthuantri.exercise201.entity.Customer;
import com.duongthuantri.exercise201.entity.Role;
import com.duongthuantri.exercise201.entity.StaffAccount;
import com.duongthuantri.exercise201.repository.CustomerRepository;
import com.duongthuantri.exercise201.repository.StaffAccountRepository;
import com.duongthuantri.exercise201.service.StaffAccountService;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtService {
    private static final String KEY_SECRET = "MTIzNDU2NDU5OThEMzIxM0F6eGMzNTE2NTQzMjEzMjE2NTQ5OHEzMTNhMnMxZDMyMnp4M2MyMQ==";
    //
    @Autowired
    private StaffAccountService staffAccountService;

    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;
    
    //
    // Tạo jwt dựa trên username (tạo thông tin cần trả về cho FE khi đăng nhập thành công)
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        StaffAccount staffAccount = staffAccountRepository.findByUser_name(username);
        claims.put("id", staffAccount.getId());
        claims.put("lastName", staffAccount.getLast_name());
        claims.put("Active", staffAccount.isActive());
        Role role = staffAccount.getRole();
        if (role != null) {
            if (role.getRole_name().equals("ADMIN")) {
                claims.put("role", "ADMIN");
            }
        }
        return createToken(claims, username);
    }
        public String generateTokenForCustomer(String username) {
            Map<String, Object> claims = new HashMap<>();
            Customer customer = customerRepository.findByUser_name(username);
            claims.put("id", customer.getId());
            claims.put("email", customer.getEmail());
            claims.put("role", "CUSTOMER"); // thêm role nếu cần
            return createToken(claims, username);
        }

    // Toạ jwt với các claims đã chọn
    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000) ) // Hết hạn sau 30 phút
                .setExpiration(new Date(System.currentTimeMillis() + 100000L * 60 * 60 * 1000) )
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
    public UUID extractId(String token) {
        // Example implementation: extract the "id" claim as a String and convert to UUID
        return extractClaims(token, claims -> UUID.fromString(claims.get("id", String.class)));
    }
}
