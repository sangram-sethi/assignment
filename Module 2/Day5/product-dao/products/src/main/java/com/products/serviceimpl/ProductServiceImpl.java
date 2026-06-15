package com.products.serviceimpl;

import java.util.List;

import com.products.dao.ProductDAO;
import com.products.daoimpl.ProductDAOImpl;
import com.products.model.Product;
import com.products.service.ProductService;

public class ProductServiceImpl implements ProductService{
    
    private ProductDAO dao = new ProductDAOImpl();

    @Override
    public int addProduct(Product product) {

        return dao.save(product);

    }

    @Override
    public Product getProductById(int id) {

        return dao.findById(id);

    }

    @Override
    public List<Product> getAllProducts() {

        return dao.findAll();

    }

    @Override
    public int updateProduct(Product product) {

        return dao.update(product);

    }

    @Override
    public int deleteProductById(int id) {

        return dao.deleteById(id);
        
    }
}
