package com.duongthuantri.exercise201.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "product_name",nullable = false)
    private String productName;

    @Column
    private String sku;

    @Column(name = "sale_price",nullable = false)
    private BigDecimal salePrice = BigDecimal.ZERO;

    @Column(name = "compare_price")
    private BigDecimal comparePrice = BigDecimal.ZERO;

    @Column(name = "buying_price")
    private BigDecimal buyingPrice;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "short_description",nullable = false,columnDefinition = "LONGTEXT")
    @Lob
    private String shortDescription;

    @Lob
    @Column(name = "product_description",nullable = false, columnDefinition = "LONGTEXT")
    private String productDescription;

    @Column(length = 64)
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @Column
    private Boolean published = false;

    @Column(name = "disable_out_of_stock")
    private Boolean disableOutOfStock = true;

    @Column
    private String note;

    @Column(name = "created_at",nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at",nullable = false)
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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductCategory> productCategories;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, optional = true)
    private ProductShippingInfo shippingInfo;

    public enum ProductType {
        simple,
        variable
    }
}