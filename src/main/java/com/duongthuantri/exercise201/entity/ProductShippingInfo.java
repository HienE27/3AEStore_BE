package com.duongthuantri.exercise201.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_shipping_info")
public class ProductShippingInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true)  // allow null for product_id
    @JsonIgnore
    private Product product;

    @Column(nullable = false)
    private BigDecimal weight = BigDecimal.ZERO;

    @Column(name = "weight_unit",length = 10)
    @Enumerated(EnumType.STRING)
    private WeightUnit weightUnit;

    @Column(nullable = false)
    private BigDecimal volume = BigDecimal.ZERO;

    @Column(name = "volume_unit",length = 10)
    @Enumerated(EnumType.STRING)
    private VolumeUnit volumeUnit;

    @Column(name = "dimension_width",nullable = false)
    private BigDecimal dimensionWidth = BigDecimal.ZERO;

    @Column(name = "dimension_height",nullable = false)
    private BigDecimal dimensionHeight = BigDecimal.ZERO;

    @Column(name = "dimension_depth",nullable = false)
    private BigDecimal dimensionDepth = BigDecimal.ZERO;

    @Column(name = "dimension_unit",length = 10)
    @Enumerated(EnumType.STRING)
    private DimensionUnit dimensionUnit;

    public enum WeightUnit {
        g,
        kg
    }

    public enum VolumeUnit {
        l,
        ml
    }

    public enum DimensionUnit {
        l,
        ml,
        cm
    }
}