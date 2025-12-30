package com.nguyenviethien.exercise201.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImageDto {
    private java.util.UUID id;
    private String imageUrl;
    private String imageName;
    private Long imageSize;
    private Integer sortOrder;
}


