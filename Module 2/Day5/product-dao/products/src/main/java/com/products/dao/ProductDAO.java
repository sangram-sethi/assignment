package com.products.dao;

import java.sql.SQLException;
import java.util.List;

import com.products.model.Product;

public interface ProductDAO {
    int save(Product product);

    Product findById(int id);

    List<Product> findAll();

    int update(Product product);

    int deleteById(int id);
}
