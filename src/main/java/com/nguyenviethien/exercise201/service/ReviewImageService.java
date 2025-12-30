package com.nguyenviethien.exercise201.service;

import com.nguyenviethien.exercise201.entity.Review;
import com.nguyenviethien.exercise201.entity.ReviewImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ReviewImageService {

    // Upload nhiều ảnh cho một review
    List<ReviewImage> uploadImages(Review review, List<MultipartFile> files) throws Exception;

    // Xóa ảnh
    void deleteImage(UUID imageId) throws Exception;

    // Lấy tất cả ảnh của một review
    List<ReviewImage> getImagesByReview(UUID reviewId);

    // Cập nhật thứ tự ảnh
    void updateImageOrder(UUID imageId, Integer newOrder) throws Exception;

    // Validate file ảnh
    void validateImageFile(MultipartFile file) throws Exception;
}
