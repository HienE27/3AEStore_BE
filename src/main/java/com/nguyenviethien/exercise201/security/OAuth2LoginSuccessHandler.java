package com.nguyenviethien.exercise201.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
 
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import java.util.Optional;
import com.nguyenviethien.exercise201.service.JWT.JwtService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JwtService jwtService;

    // Frontend callback URL (adjust if needed)
    private final String FRONTEND_CALLBACK = "http://localhost:3000/oauth2/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;

            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String googleId = oauth2User.getAttribute("sub"); // subject

            if (email == null) {
                // fallback to preferred_username
                email = oauth2User.getAttribute("preferred_username");
            }

            Customer customer = null;
            if (email != null) {
                Optional<Customer> opt = customerRepository.findByEmail(email);
                if (opt.isPresent()) {
                    customer = opt.get();
                }
            }

            if (customer == null) {
                customer = new Customer();
                if (email != null) {
                    customer.setEmail(email);
                    customer.setUser_name(email);
                } else {
                    customer.setUser_name("google_" + (googleId != null ? googleId : System.currentTimeMillis()));
                }
                if (name != null && !name.trim().isEmpty()) {
                    String[] parts = name.split(" ");
                    if (parts.length > 1) {
                        customer.setLast_name(parts[parts.length - 1]);
                        customer.setFirst_name(name.substring(0, name.lastIndexOf(" ")));
                    } else {
                        customer.setFirst_name(name);
                        customer.setLast_name("");
                    }
                } else {
                    customer.setFirst_name("Google");
                    customer.setLast_name("User");
                }
                customer.setPassword_hash("$2a$10$oauth2user"); // placeholder
                customer.setActive(true);
                customer = customerRepository.save(customer);
            }

            String token = jwtService.generateTokenForCustomer(customer.getUser_name());

            // build redirect URL with token and user info
            String redirect = FRONTEND_CALLBACK + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                    + "&userId=" + URLEncoder.encode(customer.getId().toString(), StandardCharsets.UTF_8)
                    + "&email=" + URLEncoder.encode(customer.getEmail(), StandardCharsets.UTF_8);

            response.sendRedirect(redirect);
            return;
        }

        // default fallback
        response.sendRedirect(FRONTEND_CALLBACK + "?error=oauth_error");
    }
}


