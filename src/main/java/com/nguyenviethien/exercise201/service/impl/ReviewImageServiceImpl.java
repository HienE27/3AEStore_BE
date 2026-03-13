package com.nguyenviethien.exercise201.service.impl;

import com.nguyenviethien.exercise201.entity.Review;
import com.nguyenviethien.exercise201.entity.ReviewImage;
import com.nguyenviethien.exercise201.repository.ReviewImageRepository;
import com.nguyenviethien.exercise201.repository.ReviewRepository;
import com.nguyenviethien.exercise201.service.ReviewImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewImageServiceImpl implements ReviewImageService {

    private final ReviewImageRepository reviewImageRepository;
    private final ReviewRepository reviewRepository;

    @Value("${app.upload.dir:${user.home}/uploads/reviews}")
    private String uploadDir;

    @Override
    public List<ReviewImage> uploadImages(Review review, List<MultipartFile> files) throws Exception {
        List<ReviewImage> savedImages = new ArrayList<>();

        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Lấy số thứ tự lớn nhất hiện tại
        List<ReviewImage> existingImages = reviewImageRepository.findByReviewOrderBySortOrderAsc(review);
        int maxOrder = existingImages.stream()
                .mapToInt(ReviewImage::getSortOrder)
                .max()
                .orElse(0);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            // Validate file
            validateImageFile(file);

            // Tạo tên file duy nhất
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;

            // Đường dẫn lưu file
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Copy file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Tạo ReviewImage entity
            ReviewImage reviewImage = new ReviewImage();
            reviewImage.setReview(review);
            reviewImage.setImageName(originalFilename);
            reviewImage.setImageUrl("/uploads/reviews/" + uniqueFilename); // URL tương đối
            reviewImage.setImageSize(file.getSize());
            reviewImage.setSortOrder(maxOrder + i + 1);

            // Lưu vào database
            ReviewImage savedImage = reviewImageRepository.save(reviewImage);
            savedImages.add(savedImage);
        }

        return savedImages;
    }

    @Override
    public void deleteImage(UUID imageId) throws Exception {
        ReviewImage image = reviewImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        // Xóa file vật lý
        try {
            Path filePath = Paths.get(uploadDir, getFilenameFromUrl(image.getImageUrl()));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log warning but don't throw exception
            System.err.println("Warning: Could not delete physical file: " + e.getMessage());
        }

        // Xóa từ database
        reviewImageRepository.delete(image);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewImage> getImagesByReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) return List.of();

        return reviewImageRepository.findByReviewOrderBySortOrderAsc(review);
    }

    @Override
    public void updateImageOrder(UUID imageId, Integer newOrder) throws Exception {
        ReviewImage image = reviewImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        image.setSortOrder(newOrder);
        reviewImageRepository.save(image);
    }

    @Override
    public void validateImageFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Kiểm tra kích thước file (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 5MB");
        }

        // Kiểm tra loại file
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Kiểm tra phần mở rộng file
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (!List.of("jpg", "jpeg", "png", "gif", "webp").contains(extension)) {
            throw new IllegalArgumentException("Only JPG, PNG, GIF, and WebP images are allowed");
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex > 0) ? filename.substring(lastDotIndex + 1) : "";
    }

    private String getFilenameFromUrl(String imageUrl) {
        // imageUrl format: "/uploads/reviews/filename.ext"
        return imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
    }
}
