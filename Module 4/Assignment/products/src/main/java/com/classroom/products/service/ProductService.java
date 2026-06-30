package com.classroom.products.service;

import com.classroom.products.model.Product;
import com.classroom.products.enums.Category;
import com.classroom.products.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {
    
    ProductResponseDTO addProduct(Product product);

    ProductResponseDTO getProductById(Long productId);

    List<ProductResponseDTO> getAllProducts();

    List<ProductResponseDTO> getProducts(Category category, String brand,
            String sortBy, String direction, int page, int size);

    ProductResponseDTO updateProduct(Long productId, Product product);

    ProductResponseDTO updateProductPrice(Long productId, double newPrice);

    ProductResponseDTO findProductByBrandAndPriceRange(String brand, double minPrice, double maxPrice);

    void deleteProduct(Long productId);

}
