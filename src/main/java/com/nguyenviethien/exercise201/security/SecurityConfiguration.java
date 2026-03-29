package com.nguyenviethien.exercise201.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.beans.factory.annotation.Value;

import com.nguyenviethien.exercise201.security.JWT.JwtFilter;
import com.nguyenviethien.exercise201.security.CustomerSecurityService;
import com.nguyenviethien.exercise201.security.StaffAccountSecurityService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
public class SecurityConfiguration {

    @Value("${APP_BASE_URL:http://localhost:3000}")
    private String frontendBaseUrl;

    @Autowired
    @Lazy
    private JwtFilter jwtFilter;
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
                // Public endpoints - không cần authentication
                .requestMatchers("/api/customers/login/**").permitAll()
                .requestMatchers("/api/staff/login/**").permitAll()
                .requestMatchers("/admin/**").permitAll()
                
                // News endpoints - IMPORTANT FIX: Allow all methods for debugging
                    .requestMatchers("/api/news/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/news").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/news/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/news/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/news").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/news/**").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/api/news/**").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/api/news/**").permitAll()
                
                // Admin API endpoints - allow OPTIONS for CORS preflight
                    .requestMatchers(HttpMethod.OPTIONS, "/api/admin/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/admin/**").permitAll()
                
                // Allow OAuth2 endpoints
                    .requestMatchers("/oauth2/**").permitAll()
                    .requestMatchers("/login/oauth2/**").permitAll()
                // Default - yêu cầu authentication
                    .anyRequest().permitAll()  // Keep permitAll for debugging
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .userDetailsService(staffDetailsService);

        // Configure OAuth2 login: success -> redirect FE với token; failure -> redirect FE với error (tránh trang "Invalid credentials")
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2LoginSuccessHandler())
                .failureHandler(oauth2FailureHandler())
        );

        // CORS configuration - ENHANCED
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
            config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001"));
            config.setAllowedHeaders(Arrays.asList("*"));
            config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
            config.setExposedHeaders(Arrays.asList("*"));
            return config;
        }));

        // TEMPORARILY DISABLE JWT filter for debugging
         http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler oauth2LoginSuccessHandler() {
        return new OAuth2LoginSuccessHandler();
    }

    @Bean
    public AuthenticationFailureHandler oauth2FailureHandler() {
        return (request, response, exception) -> {
            String message = exception != null && exception.getMessage() != null
                    ? exception.getMessage() : "invalid_credentials";
            response.sendRedirect(frontendBaseUrl + "/oauth2/callback?error=oauth_error&message="
                    + URLEncoder.encode(message, StandardCharsets.UTF_8));
        };
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}