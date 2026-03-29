package com.nguyenviethien.exercise201.security;

import com.nguyenviethien.exercise201.repository.CustomerRepository;
import com.nguyenviethien.exercise201.repository.RoleRepository;
import com.nguyenviethien.exercise201.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CustomerSecurityServiceImpl implements CustomerSecurityService {
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Customer findByUserName(String user_name) {
        return customerRepository.findByUser_name(user_name);
    }

    @Override
    public UserDetails loadUserByUsername(String user_name) throws UsernameNotFoundException {
        Customer customer = findByUserName(user_name);
        if (customer == null) {
            throw new UsernameNotFoundException("Tài khoản không tồn tại!");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        return new org.springframework.security.core.userdetails.User(
            customer.getUser_name(),
            customer.getPassword_hash(),
            authorities
        );
    }
}
