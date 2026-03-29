package com.nguyenviethien.exercise201.security.JWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nguyenviethien.exercise201.security.StaffAccountSecurityService;
import com.nguyenviethien.exercise201.repository.StaffAccountRepository;
import com.nguyenviethien.exercise201.entity.StaffAccount;
import com.nguyenviethien.exercise201.entity.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private StaffAccountSecurityService staffAccountSecurityService;
    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        System.out.println("🔐 JWT Filter: " + request.getMethod() + " " + request.getRequestURI());
        
        try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                username = jwtService.extractUsername(token);
                System.out.println("🔐 JWT: Tìm thấy token cho user: " + username);
            } else {
                System.out.println("🔐 JWT: Không tìm thấy Bearer token");
            }
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = null;
                    try {
                        userDetails = staffAccountSecurityService.loadUserByUsername(username);
                        if (userDetails != null) {
                            System.out.println("🔐 JWT: Loaded UserDetails for username: " + userDetails.getUsername()
                                + " authorities: " + userDetails.getAuthorities());
                        }
                    } catch (Exception ex) {
                        try {
                            UUID idFromToken = jwtService.extractId(token);
                            if (idFromToken != null) {
                                StaffAccount sa = staffAccountRepository.findById(idFromToken).orElse(null);
                                if (sa != null) {
                                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                                    Role role = sa.getRole();
                                    if (role != null) {
                                        String roleName = role.getRole_name();
                                        if (roleName != null && !roleName.startsWith("ROLE_")) {
                                            roleName = "ROLE_" + roleName;
                                        }
                                        authorities.add(new SimpleGrantedAuthority(roleName));
                                    }
                                    userDetails = new org.springframework.security.core.userdetails.User(
                                            sa.getUser_name(), sa.getPassword_hash(), authorities);
                                    System.out.println("🔐 JWT: Loaded StaffAccount by id fallback for: " + sa.getUser_name()
                                        + " authorities: " + authorities);
                                }
                            }
                        } catch (Exception idEx) {
                            System.out.println("🔐 JWT: Fallback by id failed: " + idEx.getMessage());
                        }
                    }

                    if (userDetails != null) {
                        System.out.println("🔐 JWT: About to validate token for subject: " + username
                            + " vs userDetails.username: " + userDetails.getUsername()
                            + " authorities: " + userDetails.getAuthorities());
                    }
                    if (userDetails != null && jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        System.out.println("🔐 JWT: Xác thực thành công cho: " + username
                            + " grantedAuthorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
                    } else {
                        System.out.println("🔐 JWT: Xác thực token thất bại cho: " + username);
                    }

                } catch (Exception authException) {
                    System.out.println("🔐 JWT: Lỗi xác thực: " + authException.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("🔐 JWT Filter exception: " + e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        String[] publicPaths = {
            "/api/customers/login",
            "/api/staff/login",
            "/api/products",
            "/api/categories",
            "/api/home",
            "/api/news",
            "/api/slideshow",
            "/admin",
            "/error",
            "/uploads",
            "/login",
            "/favicon.ico",
            "/oauth2",
            "/assets",
            "/gallerys"
        };
        
        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                System.out.println("🔐 JWT: Bỏ qua filter cho đường dẫn công khai: " + path);
                return true;
            }
        }
        
        if (path.startsWith("/api/news") && "GET".equals(method)) {
            System.out.println("🔐 JWT: Cho phép GET " + path + " mà không cần xác thực");
            return true;
        }
        
        if ("OPTIONS".equals(method)) {
            System.out.println("🔐 JWT: Bỏ qua filter cho OPTIONS request");
            return true;
        }
        
        return false;
    }
}
