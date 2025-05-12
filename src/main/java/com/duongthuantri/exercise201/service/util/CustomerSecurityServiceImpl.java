package com.duongthuantri.exercise201.service.util;

// import com.example.web_bookstore_be.dao.RoleRepository;
import com.duongthuantri.exercise201.repository.CustomerRepository;
import com.duongthuantri.exercise201.repository.RoleRepository;
// import com.example.web_bookstore_be.entity.Role;
import com.duongthuantri.exercise201.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerSecurityServiceImpl implements CustomerSecurityService {
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Customer findByUserName(String user_name) {
        return customerRepository.findByUser_name(user_name);
    }

    // Khi "dap" ở Security Configuration được gọi thì nó sẽ chay hàm này để lấy ra user trong csdl
    @Override
    public UserDetails loadUserByUsername(String user_name) throws UsernameNotFoundException {
        Customer customer = findByUserName(user_name);
    if (customer == null) {
        throw new UsernameNotFoundException("Tài khoản không tồn tại!");
    }

    // Tạo quyền cho Customer. Đây là ví dụ, bạn có thể tùy chỉnh thêm quyền của khách hàng nếu cần
    List<GrantedAuthority> authorities = new ArrayList<>();  // Không có quyền đặc biệt cho customer

    return new org.springframework.security.core.userdetails.User(
        customer.getUser_name(),
        customer.getPassword_hash(),
        authorities
    );
}
}
