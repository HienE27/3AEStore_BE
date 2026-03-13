package com.nguyenviethien.exercise201.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "review_reports")
public class ReviewReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Customer reporter;

    @Column(name = "reason", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "handled_by")
    private String handledBy;

    @Column(name = "handled_at")
    private Timestamp handledAt;

    @Column(name = "handled_note", columnDefinition = "TEXT")
    private String handledNote;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
    }

    public enum ReportReason {
        SPAM("Spam hoặc quảng cáo"),
        HARASSMENT("Xúc phạm hoặc lăng mạ"),
        MISINFORMATION("Thông tin sai sự thật"),
        INAPPROPRIATE("Nội dung không phù hợp"),
        OTHER("Khác");

        private final String displayName;

        ReportReason(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * Case-insensitive deserialization
         */
        @JsonCreator
        public static ReportReason fromValue(String value) {
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("ReportReason cannot be null or empty");
            }
            try {
                return ReportReason.valueOf(value.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Invalid ReportReason: '" + value + "'. Valid values are: " +
                    java.util.Arrays.toString(ReportReason.values())
                );
            }
        }
    }

    public enum ReportStatus {
        PENDING("Chờ xử lý"),
        RESOLVED("Đã xử lý"),
        DISMISSED("Bỏ qua"),
        ESCALATED("Escalated");

        private final String displayName;

        ReportStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * Case-insensitive deserialization
         */
        @JsonCreator
        public static ReportStatus fromValue(String value) {
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("ReportStatus cannot be null or empty");
            }
            try {
                return ReportStatus.valueOf(value.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Invalid ReportStatus: '" + value + "'. Valid values are: " +
                    java.util.Arrays.toString(ReportStatus.values())
                );
            }
        }
    }
}

