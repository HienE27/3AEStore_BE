package com.nguyenviethien.exercise201.repository;

import com.nguyenviethien.exercise201.entity.Review;
import com.nguyenviethien.exercise201.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, UUID> {

    // Tìm tất cả ảnh của một review, sắp xếp theo thứ tự
    List<ReviewImage> findByReviewOrderBySortOrderAsc(Review review);

    // Xóa tất cả ảnh của một review
    void deleteByReview(Review review);
}
