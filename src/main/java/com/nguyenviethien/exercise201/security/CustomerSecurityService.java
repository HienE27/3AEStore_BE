package com.nguyenviethien.exercise201.security;

import com.nguyenviethien.exercise201.entity.Customer;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomerSecurityService extends UserDetailsService {

    public Customer findByUserName(String user_name);
    
}
