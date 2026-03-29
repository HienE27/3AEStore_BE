package com.nguyenviethien.exercise201.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import java.util.Optional;
import com.nguyenviethien.exercise201.security.JWT.JwtService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.Cookie;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JwtService jwtService;

    // Frontend callback base URL (set via env var APP_BASE_URL, default to http://localhost:3000)
    @Value("${APP_BASE_URL:http://localhost:3000}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) principal;

                String email = oauth2User.getAttribute("email");
                String name = oauth2User.getAttribute("name");
                String googleId = oauth2User.getAttribute("sub"); // subject

                if (email == null) {
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
                    String userName = (email != null && !email.isEmpty())
                            ? email
                            : "google_" + (googleId != null ? googleId : System.currentTimeMillis());
                    // Entity bắt buộc email NOT NULL -> dùng placeholder nếu Google không trả email
                    customer.setEmail(email != null && !email.isEmpty() ? email : (userName + "@oauth.local"));
                    customer.setUser_name(userName);
                    if (name != null && !name.trim().isEmpty()) {
                        String[] parts = name.split(" ");
                        if (parts.length > 1) {
                            customer.setLast_name(parts[parts.length - 1]);
                            customer.setFirst_name(name.substring(0, name.lastIndexOf(" ")));
                        } else {
                            customer.setFirst_name(name);
                            customer.setLast_name("-");
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

                String targetFrontend = frontendBaseUrl;
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie c : cookies) {
                        if ("oauth_frontend".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                            targetFrontend = c.getValue();
                            Cookie clear = new Cookie("oauth_frontend", "");
                            clear.setPath("/");
                            clear.setMaxAge(0);
                            response.addCookie(clear);
                            break;
                        }
                    }
                }

                String emailParam = customer.getEmail() != null ? customer.getEmail() : "";
                String redirect = targetFrontend + "/oauth2/callback?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                        + "&userId=" + URLEncoder.encode(customer.getId().toString(), StandardCharsets.UTF_8)
                        + "&email=" + URLEncoder.encode(emailParam, StandardCharsets.UTF_8);

                response.sendRedirect(redirect);
                return;
            }
        } catch (Exception e) {
            log.error("OAuth2 login success handler failed", e);
            response.sendRedirect(frontendBaseUrl + "/oauth2/callback?error=oauth_error&message="
                    + URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "server_error", StandardCharsets.UTF_8));
            return;
        }

        response.sendRedirect(frontendBaseUrl + "/oauth2/callback?error=oauth_error");
    }
}


