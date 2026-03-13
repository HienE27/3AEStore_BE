package com.nguyenviethien.exercise201.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "review_images")
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl; // Đường dẫn đến ảnh

    @Column(name = "image_name", nullable = false)
    private String imageName; // Tên file ảnh gốc

    @Column(name = "image_size")
    private Long imageSize; // Kích thước file (bytes)

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0; // Thứ tự sắp xếp ảnh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review; // Thuộc về review nào
}
