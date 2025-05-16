package com.duongthuantri.exercise201.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "slideshows")
public class Slideshow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 80)
    private String title;

    @Column(name = "destination_url")
    private String destinationUrl;

    @Column(nullable = false,columnDefinition = "LONGTEXT")
    @Lob
    private String image;

    @Column(nullable = false)
    private String placeholder;

    @Column(length = 160)
    private String description;

    @Column(name = "btn_label", length = 50)
    private String btnLabel;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column
    private Boolean published = false;

    @Column(nullable = false)
    private Integer clicks = 0;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    private String styles;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private StaffAccount createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @JsonIgnore
    private StaffAccount updatedBy;
}