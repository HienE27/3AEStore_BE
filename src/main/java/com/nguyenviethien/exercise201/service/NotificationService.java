package com.nguyenviethien.exercise201.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nguyenviethien.exercise201.entity.Notification;
import com.nguyenviethien.exercise201.entity.StaffAccount;

import java.util.Date;

public interface NotificationService {
    List<Notification> findAll();

    Optional<Notification> findById(UUID id);

    List<Notification> findByAccount(StaffAccount account);

    List<Notification> findByAccountAndSeen(StaffAccount account, Boolean seen);

    List<Notification> findByNotificationExpiryDateBefore(Date date);

    List<Notification> findByAccountOrderByCreatedAtDesc(StaffAccount account);

    Notification save(Notification notification);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    void markAsSeen(UUID id);

    void deleteExpiredNotifications();
}