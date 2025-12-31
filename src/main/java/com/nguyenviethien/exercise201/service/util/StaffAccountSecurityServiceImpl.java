package com.nguyenviethien.exercise201.service.util;

import com.nguyenviethien.exercise201.repository.StaffAccountRepository;
import com.nguyenviethien.exercise201.entity.Role;
import com.nguyenviethien.exercise201.entity.StaffAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class StaffAccountSecurityServiceImpl implements StaffAccountSecurityService {
    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Override
    public StaffAccount findByUserName(String user_name) {
        return staffAccountRepository.findByUser_name(user_name);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        StaffAccount staffAccount = staffAccountRepository.findByUser_name(username);
        if (staffAccount == null) {
            throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + username);
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        Role role = staffAccount.getRole();
        if (role != null) {
            // Ensure authority uses ROLE_ prefix so Spring's hasRole checks work correctly.
            String roleName = role.getRole_name();
            if (roleName != null && !roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }
            authorities.add(new SimpleGrantedAuthority(roleName));
        }
        return new org.springframework.security.core.userdetails.User(
                staffAccount.getUser_name(),
                staffAccount.getPassword_hash(),
                authorities
        );
    }
    //
}
