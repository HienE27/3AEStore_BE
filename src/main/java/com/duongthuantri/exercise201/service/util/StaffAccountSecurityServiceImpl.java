package com.duongthuantri.exercise201.service.util;
import com.duongthuantri.exercise201.repository.StaffAccountRepository;
import com.duongthuantri.exercise201.repository.CustomerRepository;
import com.duongthuantri.exercise201.repository.RoleRepository;
// import com.example.web_bookstore_be.entity.Role;
import com.duongthuantri.exercise201.entity.Customer;
import com.duongthuantri.exercise201.entity.Role;
import com.duongthuantri.exercise201.entity.StaffAccount;

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
public class StaffAccountSecurityServiceImpl implements StaffAccountSecurityService {
    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Override
    public StaffAccount findByUserName(String user_name) {
        return staffAccountRepository.findByUser_name(user_name);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        StaffAccount staffAccount = staffAccountRepository.findByUser_name(username);
        if (staffAccount == null) {
            throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + username);
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        Role role = staffAccount.getRole();
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role.getRole_name()));
        }
        return new org.springframework.security.core.userdetails.User(
                staffAccount.getUser_name(),
                staffAccount.getPassword_hash(),
                authorities
        );
    }
    //
}
