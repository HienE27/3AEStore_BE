package com.nguyenviethien.exercise201.service;

import com.nguyenviethien.exercise201.entity.Review;

public interface EmailService {
    
    void sendReviewNotification(Review review);
    
    void sendReviewApprovedNotification(Review review);
}

