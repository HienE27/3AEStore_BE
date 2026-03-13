package com.nguyenviethien.exercise201.repository;

import com.nguyenviethien.exercise201.entity.ReviewReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, UUID> {
    
    Page<ReviewReport> findByStatus(ReviewReport.ReportStatus status, Pageable pageable);
    
    List<ReviewReport> findByReviewId(UUID reviewId);
    
    Optional<ReviewReport> findByReviewIdAndReporterId(UUID reviewId, UUID reporterId);
    
    @Query("SELECT COUNT(r) FROM ReviewReport r WHERE r.status = :status")
    long countByStatus(@Param("status") ReviewReport.ReportStatus status);
    
    @Query("SELECT r FROM ReviewReport r WHERE r.review.id = :reviewId ORDER BY r.createdAt DESC")
    List<ReviewReport> findAllByReviewIdOrderByCreatedAtDesc(@Param("reviewId") UUID reviewId);
}

