package com.duongthuantri.exercise201.DTO;

import java.util.List;

import lombok.Data;

@Data
public class ProductDTO {
    private String productName;
    private List<String> categoryNames;
}
