package com.nguyenviethien.exercise201.service.JWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nguyenviethien.exercise201.service.util.StaffAccountSecurityService;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private StaffAccountSecurityService staffAccountSecurityService;

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
            
            // Chỉ xử lý JWT nếu có token và chưa được xác thực
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = staffAccountSecurityService.loadUserByUsername(username);
                    if (jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        System.out.println("🔐 JWT: Xác thực thành công cho: " + username);
                    } else {
                        System.out.println("🔐 JWT: Xác thực token thất bại cho: " + username);
                    }
                } catch (Exception authException) {
                    System.out.println("🔐 JWT: Lỗi xác thực: " + authException.getMessage());
                    // Không throw exception, chỉ log và tiếp tục
                }
            }
            
        } catch (Exception e) {
            System.out.println("🔐 JWT Filter exception: " + e.getMessage());
            // Không throw exception, để Spring Security xử lý
        }
        
        // LUÔN LUÔN gọi filterChain.doFilter()
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Bỏ qua JWT cho các endpoint công khai
        String[] publicPaths = {
            "/api/customers/login",
            "/api/staff/login",
            "/admin",
            "/error"
        };
        
        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                System.out.println("🔐 JWT: Bỏ qua filter cho đường dẫn công khai: " + path);
                return true;
            }
        }
        
        // Bỏ qua JWT cho GET requests đến /api/news (cho phép xem tin tức mà không cần đăng nhập)
        if (path.startsWith("/api/news") && "GET".equals(method)) {
            System.out.println("🔐 JWT: Cho phép GET " + path + " mà không cần xác thực");
            return false; // Vẫn qua filter nhưng không yêu cầu token
        }
        
        // Bỏ qua cho OPTIONS (CORS preflight)
        if ("OPTIONS".equals(method)) {
            System.out.println("🔐 JWT: Bỏ qua filter cho OPTIONS request");
            return true;
        }
        
        return false;
    }
}