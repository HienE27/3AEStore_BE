package com.nguyenviethien.exercise201.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.nguyenviethien.exercise201.DTO.CustomerPageResponse;
import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.CustomerAddress;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import com.nguyenviethien.exercise201.security.LoginRequest;
import com.nguyenviethien.exercise201.service.CustomerAddressService;
import com.nguyenviethien.exercise201.service.CustomerService;
import com.nguyenviethien.exercise201.service.JWT.JwtService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
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

    // Lấy danh sách khách hàng có phân trang, trả về JSON
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerPageResponse> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customerPage = customerService.findAll(pageable);

        CustomerPageResponse response = new CustomerPageResponse(
            customerPage.getContent(),
            new CustomerPageResponse.PageMetadata(
                customerPage.getSize(),
                customerPage.getTotalElements(),
                customerPage.getTotalPages(),
                customerPage.getNumber()
            )
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public String test() {
        return "API OK";
    }

    // Lấy khách hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable UUID id) {
        return customerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Lấy thông tin profile khách hàng cho checkout
    @GetMapping("/{customerId}/profile")
    public ResponseEntity<CustomerProfileResponse> getCustomerProfile(@PathVariable UUID customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            CustomerProfileResponse response = new CustomerProfileResponse();
            response.setId(customer.getId());
            response.setFirstName(customer.getFirst_name());
            response.setLastName(customer.getLast_name());
            response.setEmail(customer.getEmail());
            
            // Lấy số điện thoại từ địa chỉ mặc định hoặc địa chỉ mới nhất
            try {
                List<CustomerAddress> addresses = customerAddressService.findByCustomer(customer);
                String phoneNumber = "";
                
                if (!addresses.isEmpty()) {
                    // Tìm địa chỉ mặc định
                    CustomerAddress defaultAddress = addresses.stream()
                        .filter(addr -> addr.getIsDefault() != null && addr.getIsDefault())
                        .findFirst()
                        .orElse(addresses.get(0)); // Nếu không có default, lấy địa chỉ đầu tiên
                    
                    phoneNumber = defaultAddress.getPhone_number();
                }
                
                response.setPhoneNumber(phoneNumber);
            } catch (Exception e) {
                // Nếu có lỗi khi lấy address, để trống phone number
                response.setPhoneNumber("");
                System.out.println("Warning: Could not get phone number from addresses: " + e.getMessage());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error getting customer profile: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy khách hàng theo email
    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> getCustomerByEmail(@PathVariable String email) {
        return customerService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATED: Tạo khách hàng mới với hỗ trợ ID cụ thể
    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        try {
            System.out.println("=== CREATE CUSTOMER REQUEST ===");
            System.out.println("Request data: " + customer.toString());
            System.out.println("Customer ID: " + customer.getId());
            System.out.println("Customer Email: " + customer.getEmail());
            System.out.println("Customer Username: " + customer.getUser_name());

            // Validation cơ bản
            if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email không được để trống");
            }

            // Nếu customer có ID, kiểm tra xem đã tồn tại chưa
            if (customer.getId() != null) {
                if (customerService.existsById(customer.getId())) {
                    return ResponseEntity.badRequest()
                        .body("Customer với ID " + customer.getId() + " đã tồn tại");
                }
                System.out.println("Using provided customer ID: " + customer.getId());
            } else {
                // Nếu không có ID, tạo ID mới
                customer.setId(UUID.randomUUID());
                System.out.println("Generated new customer ID: " + customer.getId());
            }

            // Kiểm tra email đã tồn tại chưa
            if (customerService.existsByEmail(customer.getEmail())) {
                return ResponseEntity.badRequest()
                    .body("Email " + customer.getEmail() + " đã được sử dụng");
            }

            // Thiết lập các giá trị mặc định nếu chưa có
            if (customer.getUser_name() == null || customer.getUser_name().trim().isEmpty()) {
                customer.setUser_name(customer.getEmail()); // Dùng email làm username mặc định
            }

            if (customer.getFirst_name() == null || customer.getFirst_name().trim().isEmpty()) {
                customer.setFirst_name("Customer");
            }

            if (customer.getLast_name() == null || customer.getLast_name().trim().isEmpty()) {
                customer.setLast_name("User");
            }

            // Thiết lập password mặc định nếu chưa có (nên mã hóa)
            if (customer.getPassword_hash() == null || customer.getPassword_hash().trim().isEmpty()) {
                customer.setPassword_hash("$2a$10$defaultpasswordhash"); // Placeholder - cần mã hóa thực tế
            }

            System.out.println("Saving customer with final data:");
            System.out.println("ID: " + customer.getId());
            System.out.println("Email: " + customer.getEmail());
            System.out.println("Username: " + customer.getUser_name());
            System.out.println("Name: " + customer.getFirst_name() + " " + customer.getLast_name());

            Customer savedCustomer = customerService.save(customer);
            System.out.println("Customer saved successfully with ID: " + savedCustomer.getId());

            // Gửi mã kích hoạt sau khi tạo khách hàng thành công (optional)
            try {
                if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
                    customerService.sendActivationCode(savedCustomer.getEmail());
                    System.out.println("Activation code sent to: " + savedCustomer.getEmail());
                }
            } catch (Exception e) {
                // Log error nhưng vẫn trả về success vì customer đã được tạo
                System.out.println("Warning: Could not send activation code: " + e.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo customer thành công");
            response.put("customer", savedCustomer);

            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException e) {
            System.out.println("Data integrity violation: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body("Dữ liệu không hợp lệ hoặc vi phạm ràng buộc: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error creating customer: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi tạo customer: " + e.getMessage());
        }
    }

    // NEW: Endpoint để kiểm tra customer tồn tại
    @GetMapping("/check/{id}")
    public ResponseEntity<?> checkCustomerExists(@PathVariable UUID id) {
        boolean exists = customerService.existsById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("customerId", id.toString());
        
        if (exists) {
            Customer customer = customerService.findById(id).orElse(null);
            response.put("customer", customer);
        }
        
        return ResponseEntity.ok(response);
    }

    // NEW: Endpoint để tìm kiếm customer theo nhiều tiêu chí
    @GetMapping("/search")
    public ResponseEntity<?> searchCustomers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String id) {
        
        try {
            System.out.println("=== SEARCH CUSTOMERS ===");
            System.out.println("Email: " + email);
            System.out.println("Username: " + username);
            System.out.println("ID: " + id);
            
            List<Customer> customers = new ArrayList<>();
            
            if (email != null && !email.trim().isEmpty()) {
                System.out.println("Searching by email: " + email);
                Optional<Customer> customerOpt = customerService.findByEmail(email.trim());
                if (customerOpt.isPresent()) {
                    customers.add(customerOpt.get());
                    System.out.println("Found customer by email: " + customerOpt.get().getId());
                } else {
                    System.out.println("No customer found with email: " + email);
                }
            } else if (username != null && !username.trim().isEmpty()) {
                System.out.println("Searching by username: " + username);
                Customer customer = customerRepository.findByUser_name(username.trim());
                if (customer != null) {
                    customers.add(customer);
                    System.out.println("Found customer by username: " + customer.getId());
                } else {
                    System.out.println("No customer found with username: " + username);
                }
            } else if (id != null && !id.trim().isEmpty()) {
                System.out.println("Searching by ID: " + id);
                try {
                    UUID customerId = UUID.fromString(id.trim());
                    Optional<Customer> customerOpt = customerService.findById(customerId);
                    if (customerOpt.isPresent()) {
                        customers.add(customerOpt.get());
                        System.out.println("Found customer by ID: " + customerOpt.get().getId());
                    } else {
                        System.out.println("No customer found with ID: " + id);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid UUID format: " + id);
                    return ResponseEntity.badRequest()
                        .body("ID không đúng định dạng UUID: " + id);
                }
            } else {
                System.out.println("No search criteria provided");
                return ResponseEntity.badRequest()
                    .body("Vui lòng cung cấp ít nhất một tiêu chí tìm kiếm (email, username, hoặc id)");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("customers", customers);
            response.put("count", customers.size());
            response.put("searchCriteria", Map.of(
                "email", email != null ? email : "",
                "username", username != null ? username : "",
                "id", id != null ? id : ""
            ));
            
            System.out.println("Search completed. Found " + customers.size() + " customers");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("Error in search: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi tìm kiếm: " + e.getMessage());
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

    // Xóa khách hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable UUID id) {
        if (!customerService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            customerService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xóa khách hàng do ràng buộc dữ liệu.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi máy chủ khi xóa khách hàng.");
        }
    }

    // =========================
    // CUSTOMER ADDRESS ENDPOINTS - CẬP NHẬT
    // =========================

    // Lấy tất cả địa chỉ của khách hàng
    @GetMapping("/{id}/addresses")
    public ResponseEntity<List<CustomerAddress>> getCustomerAddresses(@PathVariable UUID id) {
        try {
            if (!customerService.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            Customer customer = customerService.findById(id).get();
            List<CustomerAddress> addresses = customerAddressService.findByCustomer(customer);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy địa chỉ mới nhất của khách hàng
    @GetMapping("/{customerId}/addresses/latest")
    public ResponseEntity<CustomerAddress> getLatestCustomerAddress(@PathVariable UUID customerId) {
        try {
            if (!customerService.existsById(customerId)) {
                return ResponseEntity.notFound().build();
            }
            
            Customer customer = customerService.findById(customerId).get();
            List<CustomerAddress> addresses = customerAddressService.findByCustomer(customer);
            
            if (addresses.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Lấy địa chỉ mới nhất (giả sử có sắp xếp theo thời gian tạo)
            CustomerAddress latestAddress = addresses.get(0);
            for (CustomerAddress addr : addresses) {
                if (addr.getCreatedAt() != null && latestAddress.getCreatedAt() != null) {
                    if (addr.getCreatedAt().isAfter(latestAddress.getCreatedAt())) {
                        latestAddress = addr;
                    }
                }
            }
            
            return ResponseEntity.ok(latestAddress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // CẬP NHẬT: Thêm địa chỉ mới cho khách hàng với cấu trúc mới
    @PostMapping("/{id}/addresses")
    public ResponseEntity<CustomerAddress> addCustomerAddress(
            @PathVariable UUID id,
            @RequestBody SaveAddressRequest request) {
        try {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            CustomerAddress address = new CustomerAddress();
            address.setCustomer(customer);
            address.setRecipient_name(request.getRecipient_name());
            address.setAddress_line1(request.getAddress_line1()); // Chỉ địa chỉ cụ thể
            address.setWard(request.getWard()); // Phường/xã riêng biệt
            address.setDistrict(request.getDistrict()); // Quận/huyện riêng biệt
            address.setAddress_line2(request.getAddress_line2()); // Ghi chú
            address.setPhone_number(request.getPhone_number());
            address.setDial_code(request.getDial_code() != null ? request.getDial_code() : "+84");
            address.setCountry(request.getCountry() != null ? request.getCountry() : "Vietnam");
            address.setPostal_code(request.getPostal_code() != null ? request.getPostal_code() : "00000");
            address.setCity(request.getCity()); // Tỉnh/thành phố

            CustomerAddress savedAddress = customerAddressService.save(address);
            return ResponseEntity.ok(savedAddress);
        } catch (Exception e) {
            System.err.println("Error adding customer address: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Xóa địa chỉ của khách hàng
    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<?> deleteCustomerAddress(
            @PathVariable UUID customerId, 
            @PathVariable UUID addressId) {
        try {
            if (!customerService.existsById(customerId)) {
                return ResponseEntity.notFound().build();
            }
            
            Optional<CustomerAddress> addressOpt = customerAddressService.findById(addressId);
            if (addressOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CustomerAddress address = addressOpt.get();
            if (!address.getCustomer().getId().equals(customerId)) {
                return ResponseEntity.badRequest().body("Địa chỉ không thuộc về khách hàng này");
            }
            
            customerAddressService.deleteById(addressId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // =========================
    // AUTHENTICATION ENDPOINTS
    // =========================

    // Đăng nhập khách hàng, trả về token JWT
    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = customerAuthManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUser_name(),
                            loginRequest.getPassword_hash()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            Customer customer = customerRepository.findByUser_name(loginRequest.getUser_name());
            if (customer == null) {
                return ResponseEntity.badRequest().body("Không tìm thấy thông tin khách hàng.");
            }

            String jwtToken = jwtService.generateTokenForCustomer(customer.getUser_name());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwtToken);
            response.put("id", customer.getId().toString());
            response.put("user_name", customer.getUser_name());
            response.put("email", customer.getEmail());
            response.put("message", "Đăng nhập khách hàng thành công");

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Tên đăng nhập hoặc mật khẩu sai (khách hàng)");
        }
    }

    // Gửi mã kích hoạt qua email cho khách hàng
    @PostMapping("/send-activation-code")
    public ResponseEntity<?> sendActivationCode(@RequestParam String email) {
        try {
            customerService.sendActivationCode(email);
            return ResponseEntity.ok("Mã kích hoạt đã được gửi đến email.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Không tìm thấy email hoặc lỗi khi gửi mã kích hoạt.");
        }
    }

    // Kích hoạt tài khoản khách hàng bằng email và mã kích hoạt
    @GetMapping("/activate")
    public ResponseEntity<?> activateCustomer(@RequestParam String email, @RequestParam String code) {
        boolean activated = customerService.activateAccount(email, code);
        if (activated) {
            return ResponseEntity.ok("Kích hoạt tài khoản thành công.");
        } else {
            return ResponseEntity.badRequest().body("Mã kích hoạt không hợp lệ hoặc đã hết hạn.");
        }
    }

    // =========================
    // DTO CLASSES - CẬP NHẬT
    // =========================

    // DTO cho thông tin profile khách hàng
    public static class CustomerProfileResponse {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        
        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getFullName() {
            return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        }
    }

    // CẬP NHẬT: DTO cho việc lưu địa chỉ với cấu trúc mới
    public static class SaveAddressRequest {
        private String recipient_name;
        private String address_line1; // Chỉ địa chỉ cụ thể (số nhà, đường)
        private String ward; // Phường/xã - THÊM MỚI
        private String district; // Quận/huyện - THÊM MỚI  
        private String address_line2; // Ghi chú
        private String phone_number;
        private String dial_code;
        private String country;
        private String postal_code;
        private String city; // Tỉnh/thành phố

        // Getters and setters
        public String getRecipient_name() { return recipient_name; }
        public void setRecipient_name(String recipient_name) { this.recipient_name = recipient_name; }
        public String getAddress_line1() { return address_line1; }
        public void setAddress_line1(String address_line1) { this.address_line1 = address_line1; }
        public String getWard() { return ward; }
        public void setWard(String ward) { this.ward = ward; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getAddress_line2() { return address_line2; }
        public void setAddress_line2(String address_line2) { this.address_line2 = address_line2; }
        public String getPhone_number() { return phone_number; }
        public void setPhone_number(String phone_number) { this.phone_number = phone_number; }
        public String getDial_code() { return dial_code; }
        public void setDial_code(String dial_code) { this.dial_code = dial_code; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPostal_code() { return postal_code; }
        public void setPostal_code(String postal_code) { this.postal_code = postal_code; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        @Override
        public String toString() {
            return "SaveAddressRequest{" +
                    "recipient_name='" + recipient_name + '\'' +
                    ", address_line1='" + address_line1 + '\'' +
                    ", ward='" + ward + '\'' +
                    ", district='" + district + '\'' +
                    ", address_line2='" + address_line2 + '\'' +
                    ", phone_number='" + phone_number + '\'' +
                    ", dial_code='" + dial_code + '\'' +
                    ", country='" + country + '\'' +
                    ", postal_code='" + postal_code + '\'' +
                    ", city='" + city + '\'' +
                    '}';
        }
    }
}