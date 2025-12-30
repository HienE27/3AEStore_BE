package com.nguyenviethien.exercise201.service.util;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.nguyenviethien.exercise201.entity.StaffAccount;
public interface StaffAccountSecurityService extends UserDetailsService{

    public StaffAccount findByUserName(String user_name);

    UserDetails loadUserByUsername(String user_name) throws UsernameNotFoundException;
    
}
