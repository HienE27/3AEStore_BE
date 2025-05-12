package com.duongthuantri.exercise201.service.util;

import com.duongthuantri.exercise201.entity.Customer;
import org.springframework.security.core.userdetails.UserDetailsService;
public interface CustomerSecurityService extends UserDetailsService{

    public Customer findByUserName(String user_name);
    
}
