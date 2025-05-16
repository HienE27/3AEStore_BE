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
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.duongthuantri.exercise201.entity.Customer;
import com.duongthuantri.exercise201.entity.CustomerAddress;
import com.duongthuantri.exercise201.repository.CustomerRepository;
import com.duongthuantri.exercise201.security.LoginRequest;
import com.duongthuantri.exercise201.service.CustomerAddressService;
import com.duongthuantri.exercise201.service.CustomerService;
import com.duongthuantri.exercise201.service.JWT.JwtService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    @Qualifier("customerAuthManager")
    private AuthenticationManager customerAuthManager;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private CustomerAddressService customerAddressService;

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable UUID id) {
        return customerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> getCustomerByEmail(@PathVariable String email) {
        return customerService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        if (customerService.existsByEmail(customer.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Email already exists");
        }
        return ResponseEntity.ok(customerService.save(customer));
    }

    // @PutMapping("/{id}")
    // public ResponseEntity<?> updateCustomer(
    //         @PathVariable UUID id,
    //         @RequestBody Customer customer) {
    //     if (!customerService.existsById(id)) {
    //         return ResponseEntity.notFound().build();
    //     }
    //     if (!customer.getEmail().equals(customerService.findById(id).get().getEmail()) &&
    //             customerService.existsByEmail(customer.getEmail())) {
    //         return ResponseEntity.badRequest()
    //                 .body("Email already exists");
    //     }
    //     customer.setId(id);
    //     return ResponseEntity.ok(customerService.save(customer));
    // }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        if (!customerService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        customerService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/addresses")
    public ResponseEntity<List<CustomerAddress>> getCustomerAddresses(@PathVariable UUID id) {
        if (!customerService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customerAddressService.findByCustomer(customerService.findById(id).get()));
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<CustomerAddress> addCustomerAddress(
            @PathVariable UUID id,
            @RequestBody CustomerAddress address) {
        return customerService.findById(id)
                .map(customer -> {
                    address.setCustomer(customer);
                    return ResponseEntity.ok(customerAddressService.save(address));
                })
                .orElse(ResponseEntity.notFound().build());
    }
   @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@RequestBody LoginRequest loginRequest) {
    try {
        Authentication authentication = customerAuthManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUser_name(), loginRequest.getPassword_hash())
        );

        // Gán vào SecurityContextHolder (nếu cần bảo mật nâng cao)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Customer customer = customerRepository.findByUser_name(loginRequest.getUser_name());
        if (customer == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy thông tin khách hàng.");
        }

        // Tạo token cho customer
        String jwtToken = jwtService.generateTokenForCustomer(customer.getUser_name());

        // Trả về token và thông tin khác nếu cần
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("user_name", customer.getUser_name());
        response.put("email", customer.getEmail());
        response.put("message", "Đăng nhập khách hàng thành công");

        return ResponseEntity.ok(response);
    } catch (AuthenticationException e) {
        return ResponseEntity.badRequest().body("Tên đăng nhập hoặc mật khẩu sai (khách hàng)");
    }
}

   // Cập nhật khách hàng
       @PutMapping("/{id}")
        public ResponseEntity<Customer> updateCustomerDetails(@PathVariable UUID id, @RequestBody Customer customer) {
            try {
                Customer updated = customerService.update(id, customer);
                return ResponseEntity.ok(updated);
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }

}