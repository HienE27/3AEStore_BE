package com.nguyenviethien.exercise201.config;

import com.nguyenviethien.exercise201.entity.ReviewStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter for ReviewStatus enum to handle case-insensitive database values.
 * Converts "pending" to ReviewStatus.PENDING, etc.
 */
@Converter(autoApply = true)
public class ReviewStatusConverter implements AttributeConverter<ReviewStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(ReviewStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public ReviewStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return ReviewStatus.valueOf(dbData.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            // Log the problematic value and throw a more informative exception
            throw new IllegalArgumentException(
                "Unknown ReviewStatus value in database: '" + dbData + "'. Valid values are: " + 
                java.util.Arrays.toString(ReviewStatus.values()), e);
        }
    }
}

