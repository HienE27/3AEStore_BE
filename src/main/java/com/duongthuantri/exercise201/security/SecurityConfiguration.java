package com.duongthuantri.exercise201.security;
// import com.example.web_bookstore_be.service.JWT.JwtFilter;
// import com.example.web_bookstore_be.service.UserSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.duongthuantri.exercise201.service.JWT.JwtFilter;
import com.duongthuantri.exercise201.service.util.CustomerSecurityService;
import com.duongthuantri.exercise201.service.util.StaffAccountSecurityService;

import java.util.Arrays;

@Configuration
public class SecurityConfiguration {
    @Autowired
    @Lazy
    private JwtFilter jwtFilter;
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Khi đăng nhâp thì sẽ vào hàm này đâu tiên để kiểm tra
    @Bean("customerAuthManager")
        public AuthenticationManager customerAuthManager(CustomerSecurityService service) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(service);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
}

    @Bean("staffAuthManager")
    public AuthenticationManager staffAuthManager(StaffAccountSecurityService service) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(service);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
}


@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http, StaffAccountSecurityService staffDetailsService) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/customers/login/**").permitAll()
            .requestMatchers("/api/staff/login/**").permitAll()
            .requestMatchers("/admin/**").permitAll() // Cho phép tất cả người dùng truy cập trang admin
            .requestMatchers("/customer/**").permitAll() // Cho phép tất cả người dùng truy cập trang admin
            // .requestMatchers(HttpMethod.GET, "/cards").hasRole("ADMIN") // Cho phép tất cả người dùng truy cập trang admin
            .anyRequest().permitAll() // Mặc định cho phép tất cả các yêu cầu khác
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .userDetailsService(staffDetailsService); // Giữ lại phân quyền nếu cần
    //cấu hình cors
    http.cors(cors -> cors.configurationSource(request -> {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        return config;
    }));

    // thêm jwtFilter vào chuỗi bảo mật
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.httpBasic(Customizer.withDefaults());
    http.csrf(AbstractHttpConfigurer::disable);
    return http.build();
}


    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
