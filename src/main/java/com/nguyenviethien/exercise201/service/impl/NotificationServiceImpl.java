package com.nguyenviethien.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Notification;
import com.nguyenviethien.exercise201.entity.StaffAccount;
import com.nguyenviethien.exercise201.repository.NotificationRepository;
import com.nguyenviethien.exercise201.service.NotificationService;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return notificationRepository.findById(id);
    }

    @Override
    public List<Notification> findByAccount(StaffAccount account) {
        return notificationRepository.findByAccount(account);
    }

    @Override
    public List<Notification> findByAccountAndSeen(StaffAccount account, Boolean seen) {
        return notificationRepository.findByAccountAndSeen(account, seen);
    }

    @Override
    public List<Notification> findByNotificationExpiryDateBefore(Date date) {
        return notificationRepository.findByNotificationExpiryDateBefore(date);
    }

    @Override
    public List<Notification> findByAccountOrderByCreatedAtDesc(StaffAccount account) {
        return notificationRepository.findByAccountOrderByCreatedAtDesc(account);
    }

    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public void deleteById(UUID id) {
        notificationRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return notificationRepository.existsById(id);
    }

    @Override
    @Transactional
    public void markAsSeen(UUID id) {
        findById(id).ifPresent(notification -> {
            notification.setSeen(true);
            save(notification);
        });
    }

    @Override
    @Transactional
    public void deleteExpiredNotifications() {
        Date currentDate = new Date();
        List<Notification> expiredNotifications = findByNotificationExpiryDateBefore(currentDate);
        notificationRepository.deleteAll(expiredNotifications);
    }
}