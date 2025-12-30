package com.nguyenviethien.exercise201.repository;

import java.util.UUID;
import java.util.List;
import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenviethien.exercise201.entity.Notification;
import com.nguyenviethien.exercise201.entity.StaffAccount;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByAccount(StaffAccount account);

    List<Notification> findByAccountAndSeen(StaffAccount account, Boolean seen);

    List<Notification> findByNotificationExpiryDateBefore(Date date);

    List<Notification> findByAccountOrderByCreatedAtDesc(StaffAccount account);
}