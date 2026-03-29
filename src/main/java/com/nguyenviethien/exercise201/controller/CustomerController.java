package com.nguyenviethien.exercise201.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nguyenviethien.exercise201.DTO.CustomerDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.nguyenviethien.exercise201.exception.ApiResponse;
import com.nguyenviethien.exercise201.repository.CustomerRepository;
import com.nguyenviethien.exercise201.security.LoginRequest;
import com.nguyenviethien.exercise201.service.CustomerAddressService;
import com.nguyenviethien.exercise201.service.CustomerService;
import com.nguyenviethien.exercise201.security.JWT.JwtService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final AuthenticationManager customerAuthManager;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final JwtService jwtService;
    private final CustomerAddressService customerAddressService;

    @Autowired
    public CustomerController(
            @Qualifier("customerAuthManager") AuthenticationManager customerAuthManager,
            CustomerRepository customerRepository,
            CustomerService customerService,
            JwtService jwtService,
            CustomerAddressService customerAddressService) {
        this.customerAuthManager = customerAuthManager;
        this.customerRepository = customerRepository;
        this.customerService = customerService;
        this.jwtService = jwtService;
        this.customerAddressService = customerAddressService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CustomerPageResponse>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customerPage = customerService.findAll(pageable);

        List<CustomerDto> customerDtos = customerPage.getContent().stream()
                .map(this::toCustomerDto)
                .collect(Collectors.toList());

        CustomerPageResponse.PageMetadata pageMetadata = new CustomerPageResponse.PageMetadata(
                customerPage.getSize(),
                customerPage.getTotalElements(),
                customerPage.getTotalPages(),
                customerPage.getNumber());
        CustomerPageResponse response = CustomerPageResponse.builder()
                .customers(customerDtos)
                .pageMetadata(pageMetadata)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("API OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable UUID id) {
        return customerService.findById(id)
                .map(customer -> ResponseEntity.ok(ApiResponse.success(customer)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy khách hàng")));
    }

    @GetMapping("/{customerId}/profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getCustomerProfile(@PathVariable UUID customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            CustomerProfileResponse response = new CustomerProfileResponse();
            response.setId(customer.getId());
            response.setFirstName(customer.getFirst_name());
            response.setLastName(customer.getLast_name());
            response.setEmail(customer.getEmail());

            try {
                List<CustomerAddress> addresses = customerAddressService.findByCustomer(customer);
                String phoneNumber = "";

                if (!addresses.isEmpty()) {
                    CustomerAddress defaultAddress = addresses.stream()
                            .filter(addr -> addr.getIsDefault() != null && addr.getIsDefault())
                            .findFirst()
                            .orElse(addresses.get(0));
                    phoneNumber = defaultAddress.getPhone_number();
                }
                response.setPhoneNumber(phoneNumber);
            } catch (Exception e) {
                log.warn("Could not get phone number from addresses: {}", e.getMessage());
                response.setPhoneNumber("");
            }

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error getting customer profile: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy thông tin profile"));
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerByEmail(@PathVariable String email) {
        return customerService.findByEmail(email)
                .map(customer -> ResponseEntity.ok(ApiResponse.success(customer)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy khách hàng")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createCustomer(@RequestBody Customer customer) {
        try {
            log.info("Creating customer with email: {}", customer.getEmail());

            if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email không được để trống"));
            }

            if (customerService.existsByEmail(customer.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email " + customer.getEmail() + " đã được sử dụng"));
            }

            if (customer.getId() != null) {
                log.debug("ID provided in request will be ignored. JPA will auto-generate ID.");
                customer.setId(null);
            }

            if (customer.getUser_name() == null || customer.getUser_name().trim().isEmpty()) {
                customer.setUser_name(customer.getEmail());
            }

            if (customer.getFirst_name() == null || customer.getFirst_name().trim().isEmpty()) {
                customer.setFirst_name("Customer");
            }

            if (customer.getLast_name() == null || customer.getLast_name().trim().isEmpty()) {
                customer.setLast_name("User");
            }

            if (customer.getPassword_hash() == null || customer.getPassword_hash().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Mật khẩu không được để trống"));
            }

            Customer savedCustomer = customerService.save(customer);
            log.info("Customer saved successfully with ID: {}", savedCustomer.getId());

            try {
                if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
                    customerService.sendActivationCode(savedCustomer.getEmail());
                }
            } catch (Exception e) {
                log.warn("Could not send activation code: {}", e.getMessage());
            }

            return ResponseEntity.ok(ApiResponse.success("Tạo khách hàng thành công", savedCustomer));

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation: {}", e.getMessage());
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("email")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email đã được sử dụng"));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ"));
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo khách hàng"));
        }
    }

    @GetMapping("/check/{id}")
    public ResponseEntity<ApiResponse<?>> checkCustomerExists(@PathVariable UUID id) {
        boolean exists = customerService.existsById(id);
        if (exists) {
            Customer customer = customerService.findById(id).orElse(null);
            return ResponseEntity.ok(ApiResponse.success(customer));
        }
        return ResponseEntity.ok(ApiResponse.success("exists", false));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchCustomers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String id) {
        try {
            List<Customer> customers = new ArrayList<>();

            if (email != null && !email.trim().isEmpty()) {
                Optional<Customer> customerOpt = customerService.findByEmail(email.trim());
                customerOpt.ifPresent(customers::add);
            } else if (username != null && !username.trim().isEmpty()) {
                Customer customer = customerRepository.findByUser_name(username.trim());
                if (customer != null) {
                    customers.add(customer);
                }
            } else if (id != null && !id.trim().isEmpty()) {
                try {
                    UUID customerId = UUID.fromString(id.trim());
                    Optional<Customer> customerOpt = customerService.findById(customerId);
                    customerOpt.ifPresent(customers::add);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("ID không đúng định dạng UUID"));
                }
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Vui lòng cung cấp ít nhất một tiêu chí tìm kiếm"));
            }

            return ResponseEntity.ok(ApiResponse.success(customers));

        } catch (Exception e) {
            log.error("Error in search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi tìm kiếm"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomerDetails(@PathVariable UUID id, @RequestBody Customer customer) {
        try {
            Customer updated = customerService.update(id, customer);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Customer not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy khách hàng"));
            }
            log.warn("Update customer error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage() != null ? e.getMessage() : "Dữ liệu không hợp lệ"));
        }
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<ApiResponse<?>> uploadAvatar(@PathVariable UUID id, @RequestBody java.util.Map<String, String> request) {
        try {
            if (!customerService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy khách hàng"));
            }

            String avatarUrl = request.get("avatarUrl");
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Vui lòng cung cấp URL ảnh"));
            }

            customerService.updateAvatar(id, avatarUrl);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật avatar thành công", avatarUrl));

        } catch (Exception e) {
            log.error("Error uploading avatar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi cập nhật ảnh đại diện"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteCustomer(@PathVariable UUID id) {
        if (!customerService.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy khách hàng"));
        }
        try {
            customerService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa khách hàng thành công"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Không thể xóa khách hàng do ràng buộc dữ liệu"));
        } catch (Exception e) {
            log.error("Error deleting customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi máy chủ khi xóa khách hàng"));
        }
    }

    @GetMapping("/{id}/addresses")
    public ResponseEntity<ApiResponse<List<CustomerAddress>>> getCustomerAddresses(@PathVariable UUID id) {
        try {
            if (!customerService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy khách hàng"));
            }
            Customer customer = customerService.findById(id).get();
            List<CustomerAddress> addresses = customerAddressService.findByCustomer(customer);
            return ResponseEntity.ok(ApiResponse.success(addresses));
        } catch (Exception e) {
            log.error("Error getting addresses: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy danh sách địa chỉ"));
        }
    }

    @GetMapping("/{customerId}/addresses/latest")
    public ResponseEntity<ApiResponse<CustomerAddress>> getLatestCustomerAddress(@PathVariable UUID customerId) {
        try {
            if (!customerService.existsById(customerId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy khách hàng"));
            }

            Customer customer = customerService.findById(customerId).get();
            List<CustomerAddress> addresses = customerAddressService.findByCustomer(customer);

            if (addresses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy địa chỉ"));
            }

            CustomerAddress latestAddress = addresses.get(0);
            for (CustomerAddress addr : addresses) {
                if (addr.getCreatedAt() != null && latestAddress.getCreatedAt() != null) {
                    if (addr.getCreatedAt().isAfter(latestAddress.getCreatedAt())) {
                        latestAddress = addr;
                    }
                }
            }

            return ResponseEntity.ok(ApiResponse.success(latestAddress));
        } catch (Exception e) {
            log.error("Error getting latest address: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy địa chỉ"));
        }
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<ApiResponse<CustomerAddress>> addCustomerAddress(
            @PathVariable UUID id,
            @RequestBody SaveAddressRequest request) {
        try {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            CustomerAddress address = new CustomerAddress();
            address.setCustomer(customer);
            address.setRecipient_name(request.getRecipient_name());
            address.setAddress_line1(request.getAddress_line1());
            address.setWard(request.getWard());
            address.setDistrict(request.getDistrict());
            address.setAddress_line2(request.getAddress_line2());
            address.setPhone_number(request.getPhone_number());
            address.setDial_code(request.getDial_code() != null ? request.getDial_code() : "+84");
            address.setCountry(request.getCountry() != null ? request.getCountry() : "Vietnam");
            address.setPostal_code(request.getPostal_code() != null ? request.getPostal_code() : "00000");
            address.setCity(request.getCity());

            CustomerAddress savedAddress = customerAddressService.save(address);
            return ResponseEntity.ok(ApiResponse.success(savedAddress));
        } catch (Exception e) {
            log.error("Error adding customer address: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi thêm địa chỉ"));
        }
    }

    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<?>> deleteCustomerAddress(
            @PathVariable UUID customerId,
            @PathVariable UUID addressId) {
        try {
            if (!customerService.existsById(customerId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy khách hàng"));
            }

            Optional<CustomerAddress> addressOpt = customerAddressService.findById(addressId);
            if (addressOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy địa chỉ"));
            }

            CustomerAddress address = addressOpt.get();
            if (!address.getCustomer().getId().equals(customerId)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Địa chỉ không thuộc về khách hàng này"));
            }

            customerAddressService.deleteById(addressId);
            return ResponseEntity.ok(ApiResponse.success("Xóa địa chỉ thành công"));
        } catch (Exception e) {
            log.error("Error deleting address: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi xóa địa chỉ"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> loginCustomer(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = customerAuthManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUser_name(),
                            loginRequest.getPassword_hash()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            Customer customer = customerRepository.findByUser_name(loginRequest.getUser_name());
            if (customer == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không tìm thấy thông tin khách hàng"));
            }

            String jwtToken = jwtService.generateTokenForCustomer(customer.getUser_name());

            return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", java.util.Map.of(
                    "token", jwtToken,
                    "id", customer.getId().toString(),
                    "user_name", customer.getUser_name(),
                    "email", customer.getEmail())));

        } catch (AuthenticationException e) {
            log.warn("Login failed for user: {}", loginRequest.getUser_name());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Tên đăng nhập hoặc mật khẩu sai"));
        }
    }

    @PostMapping("/send-activation-code")
    public ResponseEntity<ApiResponse<?>> sendActivationCode(@RequestParam String email) {
        try {
            customerService.sendActivationCode(email);
            return ResponseEntity.ok(ApiResponse.success("Mã kích hoạt đã được gửi đến email"));
        } catch (RuntimeException e) {
            log.warn("Failed to send activation code: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không tìm thấy email hoặc lỗi khi gửi mã kích hoạt"));
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<ApiResponse<?>> activateCustomer(@RequestParam String email, @RequestParam String code) {
        boolean activated = customerService.activateAccount(email, code);
        if (activated) {
            return ResponseEntity.ok(ApiResponse.success("Kích hoạt tài khoản thành công"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã kích hoạt không hợp lệ hoặc đã hết hạn"));
        }
    }

    public static class CustomerProfileResponse {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;

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
        public String getFullName() { return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""); }
    }

    public static class SaveAddressRequest {
        private String recipient_name;
        private String address_line1;
        private String ward;
        private String district;
        private String address_line2;
        private String phone_number;
        private String dial_code;
        private String country;
        private String postal_code;
        private String city;

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
    }

    private CustomerDto toCustomerDto(Customer c) {
        return CustomerDto.builder()
                .id(c.getId())
                .firstName(c.getFirst_name())
                .lastName(c.getLast_name())
                .email(c.getEmail())
                .userName(c.getUser_name())
                .active(c.getActive())
                .activated(c.getActivated())
                .avatarUrl(c.getAvatarUrl())
                .build();
    }
}
