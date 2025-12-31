package com.nguyenviethien.exercise201.repository;

import com.nguyenviethien.exercise201.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, UUID> {
    List<ReviewReply> findByReviewId(UUID reviewId);
}


