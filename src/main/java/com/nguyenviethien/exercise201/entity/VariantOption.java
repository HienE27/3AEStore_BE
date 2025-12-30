package com.nguyenviethien.exercise201.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "variant_options")
public class VariantOption {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    @JsonIgnore
    private Gallery image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(nullable = false)
    private BigDecimal sale_price = BigDecimal.ZERO;

    @Column
    private BigDecimal compare_price = BigDecimal.ZERO;

    @Column
    private BigDecimal buying_price;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column
    private String sku;

    @Column
    private Boolean active = true;

    @OneToMany(mappedBy = "variantOption", cascade = CascadeType.ALL)
    private List<Variant> variants;
}