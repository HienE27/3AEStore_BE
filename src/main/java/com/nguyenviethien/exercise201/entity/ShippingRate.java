package com.nguyenviethien.exercise201.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shipping_rates")
public class ShippingRate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_zone_id", nullable = false)
    @JsonIgnore
    private ShippingZone shippingZone;

    @Column(name = "weight_unit", length = 10)
    @Enumerated(EnumType.STRING)
    private WeightUnit weightUnit;

    @Column(name = "min_value", nullable = false)
    private BigDecimal minValue = BigDecimal.ZERO;

    @Column(name = "max_value")
    private BigDecimal maxValue;

    @Column(name = "no_max")
    private Boolean noMax = true;

    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    public enum WeightUnit {
        g,
        kg
    }
}