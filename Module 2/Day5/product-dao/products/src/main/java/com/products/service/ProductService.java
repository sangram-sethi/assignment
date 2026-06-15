package com.products.service;

import java.util.List;

import com.products.model.Product;

public interface ProductService {
    
    int addProduct(Product product);

    Product getProductById(int id);

    List<Product> getAllProducts();

    int updateProduct(Product product);

    int deleteProductById(int id);
    
}
