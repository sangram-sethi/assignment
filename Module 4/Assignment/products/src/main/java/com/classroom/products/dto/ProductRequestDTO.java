package com.classroom.products.dto;

import com.classroom.products.enums.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDTO {

    @NotNull(message = "Category cannot be null")
    private Category category;

    @NotBlank(message = "Product name cannot be blank")
    private String productName;

    @NotBlank(message = "Brand cannot be blank")
    private String brand;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be greater than zero")
    private Double price;

}
