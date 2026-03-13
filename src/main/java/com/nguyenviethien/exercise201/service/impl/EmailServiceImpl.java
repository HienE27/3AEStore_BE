package com.nguyenviethien.exercise201.service.impl;

import com.nguyenviethien.exercise201.entity.Review;
import com.nguyenviethien.exercise201.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    private static final String ADMIN_EMAIL = "viethienhb@gmail.com";
    private static final String STORE_NAME = "BookStore";

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendReviewNotification(Review review) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@bookstore.com");
            message.setTo(ADMIN_EMAIL);
            message.setSubject("[BookStore] Đánh giá mới cần duyệt - " + review.getProduct().getProductName());
            
            String content = buildReviewNotificationContent(review);
            message.setText(content);
            
            mailSender.send(message);
            logger.info("Review notification sent successfully for review: {}", review.getId());
        } catch (Exception e) {
            logger.error("Failed to send review notification for review: {}", review.getId(), e);
        }
    }

    @Override
    public void sendReviewApprovedNotification(Review review) {
        // Optionally notify customer when their review is approved
        // This requires customer email which may be available
        logger.debug("Review approved: {}. Customer notification skipped (customer email not configured).", review.getId());
    }

    private String buildReviewNotificationContent(Review review) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================\n");
        sb.append("           ĐÁNH GIÁ MỚI CẦN DUYỆT\n");
        sb.append("=================================================\n\n");
        
        sb.append("Sản phẩm: ").append(review.getProduct().getProductName()).append("\n");
        sb.append("Đánh giá: ").append(review.getRatingPoint()).append("/5 sao\n");
        
        if (review.getContent() != null && !review.getContent().isEmpty()) {
            sb.append("\nNội dung đánh giá:\n");
            sb.append("----------------------------------------\n");
            sb.append(review.getContent());
            sb.append("\n----------------------------------------\n");
        }
        
        sb.append("\nKhách hàng: ");
        if (review.getIsAnonymous()) {
            sb.append("Ẩn danh");
        } else if (review.getCustomer() != null) {
            String name = "";
            if (review.getCustomer().getFirst_name() != null) {
                name += review.getCustomer().getFirst_name() + " ";
            }
            if (review.getCustomer().getLast_name() != null) {
                name += review.getCustomer().getLast_name();
            }
            sb.append(name.trim().isEmpty() ? "Khách hàng" : name.trim());
        } else {
            sb.append("Không xác định");
        }
        
        sb.append("\n");
        sb.append("Trạng thái: ").append(review.getStatus().getDisplayName()).append("\n");
        sb.append("Ngày tạo: ").append(review.getCreatedAt()).append("\n");
        
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            sb.append("\nẢnh đính kèm: ").append(review.getImages().size()).append(" ảnh\n");
        }
        
        sb.append("\n=================================================\n");
        sb.append("Vui lòng đăng nhập hệ thống admin để duyệt đánh giá.\n");
        sb.append("=================================================\n");
        
        return sb.toString();
    }
}

