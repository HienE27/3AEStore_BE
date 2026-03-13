package com.nguyenviethien.exercise201.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ReviewReplyDto {
    private UUID id;
    private String content;
    private String authorName;
    private String authorRole;
    private Timestamp createdAt;

    public ReviewReplyDto(UUID id, String content, String authorName, String authorRole, Timestamp createdAt) {
        this.id = id;
        this.content = content;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.createdAt = createdAt;
    }
}

