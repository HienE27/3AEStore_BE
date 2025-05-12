package com.duongthuantri.exercise201.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductTagId implements Serializable {
    private UUID product;
    private UUID tag;
}