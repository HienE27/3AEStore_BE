package com.nguyenviethien.exercise201.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ReviewStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    HIDDEN("Đã ẩn"),
    REJECTED("Đã từ chối"),
    LOCKED("Đã khóa");

    private final String displayName;

    ReviewStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Case-insensitive deserialization
     * Handles: "pending", "PENDING", "Pending", etc.
     */
    @JsonCreator
    public static ReviewStatus fromValue(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("ReviewStatus cannot be null or empty");
        }
        try {
            return ReviewStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid ReviewStatus: '" + value + "'. Valid values are: " +
                java.util.Arrays.toString(ReviewStatus.values())
            );
        }
    }
}

