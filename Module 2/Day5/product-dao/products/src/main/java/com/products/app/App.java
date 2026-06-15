package com.products.app;

import com.products.model.Product;
import com.products.service.ProductService;
import com.products.serviceimpl.ProductServiceImpl;

public class App 
{
    public static void main( String[] args ) {
        ProductService service =
            new ProductServiceImpl();

        Product p =
                new Product(
                        0,
                        "Laptop",
                        50000,
                        10);

        service.deleteProductById(1);

    }
}
