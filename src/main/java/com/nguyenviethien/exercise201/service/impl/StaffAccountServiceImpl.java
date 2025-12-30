package com.nguyenviethien.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.StaffAccount;
import com.nguyenviethien.exercise201.repository.StaffAccountRepository;
import com.nguyenviethien.exercise201.service.StaffAccountService;

@Service
@Transactional
public class StaffAccountServiceImpl implements StaffAccountService {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Override
    public List<StaffAccount> findAll() {
        return staffAccountRepository.findAll();
    }

    @Override
    public Optional<StaffAccount> findById(UUID id) {
        return staffAccountRepository.findById(id);
    }

    @Override
    public Optional<StaffAccount> findByEmail(String email) {
        return staffAccountRepository.findByEmail(email);
    }

    @Override
    public StaffAccount save(StaffAccount staffAccount) {
        //mã hóa mật khẩu trước khi lưu vào cơ sở dữ liệu
        String encodePassword = passwordEncoder.encode(staffAccount.getPassword_hash());
        staffAccount.setPassword_hash(encodePassword);
        
        return staffAccountRepository.save(staffAccount);
    }

    @Override
    public void deleteById(UUID id) {
        staffAccountRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return staffAccountRepository.existsById(id);
    }
}