package com.classroom.products.dto;

import com.classroom.products.enums.Category;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;

    private Category category;

    private String productName;

    private String brand;

    private Double price;

}
