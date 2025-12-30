package com.nguyenviethien.exercise201.controller;

import com.nguyenviethien.exercise201.entity.Customer;
import com.nguyenviethien.exercise201.entity.CustomerAddress;
import com.nguyenviethien.exercise201.service.CustomerAddressService;
import com.nguyenviethien.exercise201.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer-addresses")
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerAddressController {

    @Autowired
    private CustomerAddressService customerAddressService;
    
    @Autowired
    private CustomerService customerService;

    // Lấy tất cả địa chỉ của khách hàng
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CustomerAddress>> getAddressesByCustomerId(@PathVariable UUID customerId) {
        try {
            if (!customerService.existsById(customerId)) {
                return ResponseEntity.notFound().build();
            }
            
            Customer customer = customerService.findById(customerId).get();
            List<CustomerAddress> addresses = customerAddressService.findByCustomer(customer);
            
            // Sắp xếp theo thời gian tạo (mới nhất trước)
            addresses.sort((a, b) -> {
                if (a.getCreatedAt() == null || b.getCreatedAt() == null) {
                    return 0;
                }
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
            
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            System.err.println("Error getting addresses for customer " + customerId + ": " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy địa chỉ mới nhất của khách hàng
    @GetMapping("/customer/{customerId}/latest")
    public ResponseEntity<CustomerAddress> getLatestAddressByCustomerId(@PathVariable UUID customerId) {
        try {
            if (!customerService.existsById(customerId)) {
                return ResponseEntity.notFound().build();
            }
            
            Customer customer = customerService.findById(customerId).get();
            List<CustomerAddress> addresses = customerAddressService.findByCustomer(customer);
            
            if (addresses.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Tìm địa chỉ mới nhất
            CustomerAddress latestAddress = addresses.get(0);
            for (CustomerAddress addr : addresses) {
                if (addr.getCreatedAt() != null && 
                    (latestAddress.getCreatedAt() == null || 
                     addr.getCreatedAt().isAfter(latestAddress.getCreatedAt()))) {
                    latestAddress = addr;
                }
            }
            
            return ResponseEntity.ok(latestAddress);
        } catch (Exception e) {
            System.err.println("Error getting latest address for customer " + customerId + ": " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // CẬP NHẬT: Lưu địa chỉ mới với cấu trúc mới
    @PostMapping("/save")
    public ResponseEntity<CustomerAddress> saveAddress(@RequestBody SaveAddressRequest request) {
        try {
            System.out.println("=== SAVE ADDRESS REQUEST ===");
            System.out.println("Customer ID: " + request.getCustomerId());
            System.out.println("Recipient: " + request.getRecipient_name());
            System.out.println("Address: " + request.getAddress_line1());
            System.out.println("Ward: " + request.getWard());
            System.out.println("District: " + request.getDistrict());
            System.out.println("City: " + request.getCity());
            System.out.println("Phone: " + request.getPhone_number());

            Customer customer = customerService.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));

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
            address.setCreatedAt(LocalDateTime.now());

            CustomerAddress savedAddress = customerAddressService.save(address);
            System.out.println("Address saved successfully with ID: " + savedAddress.getId());
            
            return ResponseEntity.ok(savedAddress);
        } catch (Exception e) {
            System.err.println("Error saving address: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // CẬP NHẬT: Cập nhật địa chỉ với cấu trúc mới
    @PutMapping("/{addressId}")
    public ResponseEntity<CustomerAddress> updateAddress(
            @PathVariable UUID addressId, 
            @RequestBody SaveAddressRequest request) {
        try {
            Optional<CustomerAddress> addressOpt = customerAddressService.findById(addressId);
            if (addressOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CustomerAddress address = addressOpt.get();
            
            // Kiểm tra quyền sở hữu (optional - nếu cần bảo mật cao hơn)
            if (request.getCustomerId() != null && 
                !address.getCustomer().getId().equals(request.getCustomerId())) {
                return ResponseEntity.badRequest().build();
            }
            
            // CẬP NHẬT: Cập nhật thông tin với cấu trúc mới
            if (request.getRecipient_name() != null) {
                address.setRecipient_name(request.getRecipient_name());
            }
            if (request.getAddress_line1() != null) {
                address.setAddress_line1(request.getAddress_line1());
            }
            if (request.getWard() != null) {
                address.setWard(request.getWard());
            }
            if (request.getDistrict() != null) {
                address.setDistrict(request.getDistrict());
            }
            if (request.getAddress_line2() != null) {
                address.setAddress_line2(request.getAddress_line2());
            }
            if (request.getPhone_number() != null) {
                address.setPhone_number(request.getPhone_number());
            }
            if (request.getDial_code() != null) {
                address.setDial_code(request.getDial_code());
            }
            if (request.getCountry() != null) {
                address.setCountry(request.getCountry());
            }
            if (request.getPostal_code() != null) {
                address.setPostal_code(request.getPostal_code());
            }
            if (request.getCity() != null) {
                address.setCity(request.getCity());
            }
            
            CustomerAddress updatedAddress = customerAddressService.save(address);
            return ResponseEntity.ok(updatedAddress);
        } catch (Exception e) {
            System.err.println("Error updating address: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Xóa địa chỉ
    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable UUID addressId) {
        try {
            Optional<CustomerAddress> addressOpt = customerAddressService.findById(addressId);
            if (addressOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            customerAddressService.deleteById(addressId);
            System.out.println("Address deleted successfully: " + addressId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error deleting address " + addressId + ": " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Xóa địa chỉ với kiểm tra quyền sở hữu
    @DeleteMapping("/customer/{customerId}/address/{addressId}")
    public ResponseEntity<?> deleteAddressWithOwnershipCheck(
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
            System.out.println("Address deleted successfully: " + addressId + " for customer: " + customerId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error deleting address with ownership check: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy địa chỉ theo ID
    @GetMapping("/{addressId}")
    public ResponseEntity<CustomerAddress> getAddressById(@PathVariable UUID addressId) {
        try {
            Optional<CustomerAddress> addressOpt = customerAddressService.findById(addressId);
            if (addressOpt.isPresent()) {
                return ResponseEntity.ok(addressOpt.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error getting address by ID " + addressId + ": " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Đặt địa chỉ làm mặc định (nếu cần tính năng này)
    @PutMapping("/{addressId}/set-default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable UUID addressId) {
        try {
            Optional<CustomerAddress> addressOpt = customerAddressService.findById(addressId);
            if (addressOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CustomerAddress address = addressOpt.get();
            Customer customer = address.getCustomer();
            
            // Reset tất cả địa chỉ khác của customer này về không mặc định
            List<CustomerAddress> customerAddresses = customerAddressService.findByCustomer(customer);
            for (CustomerAddress addr : customerAddresses) {
                addr.setIsDefault(false);
                customerAddressService.save(addr);
            }
            
            // Đặt địa chỉ này làm mặc định
            address.setIsDefault(true);
            CustomerAddress updatedAddress = customerAddressService.save(address);
            
            return ResponseEntity.ok(updatedAddress);
        } catch (Exception e) {
            System.err.println("Error setting default address: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy địa chỉ mặc định của khách hàng
    @GetMapping("/customer/{customerId}/default")
    public ResponseEntity<CustomerAddress> getDefaultAddress(@PathVariable UUID customerId) {
        try {
            if (!customerService.existsById(customerId)) {
                return ResponseEntity.notFound().build();
            }
            
            Customer customer = customerService.findById(customerId).get();
            List<CustomerAddress> addresses = customerAddressService.findByCustomer(customer);
            
            // Tìm địa chỉ mặc định
            for (CustomerAddress addr : addresses) {
                if (addr.getIsDefault() != null && addr.getIsDefault()) {
                    return ResponseEntity.ok(addr);
                }
            }
            
            // Nếu không có địa chỉ mặc định, trả về địa chỉ mới nhất
            if (!addresses.isEmpty()) {
                CustomerAddress latestAddress = addresses.get(0);
                for (CustomerAddress addr : addresses) {
                    if (addr.getCreatedAt() != null && 
                        (latestAddress.getCreatedAt() == null || 
                         addr.getCreatedAt().isAfter(latestAddress.getCreatedAt()))) {
                        latestAddress = addr;
                    }
                }
                return ResponseEntity.ok(latestAddress);
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error getting default address for customer " + customerId + ": " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // =========================
    // DTO CLASSES - CẬP NHẬT
    // =========================

    public static class SaveAddressRequest {
        private UUID customerId;
        private String recipient_name;
        private String address_line1; // Địa chỉ cụ thể (số nhà, đường)
        private String ward; // Phường/xã - THÊM MỚI
        private String district; // Quận/huyện - THÊM MỚI
        private String address_line2; // Ghi chú
        private String phone_number;
        private String dial_code;
        private String country;
        private String postal_code;
        private String city; // Tỉnh/thành phố

        // Getters and setters
        public UUID getCustomerId() { return customerId; }
        public void setCustomerId(UUID customerId) { this.customerId = customerId; }
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
                    "customerId=" + customerId +
                    ", recipient_name='" + recipient_name + '\'' +
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