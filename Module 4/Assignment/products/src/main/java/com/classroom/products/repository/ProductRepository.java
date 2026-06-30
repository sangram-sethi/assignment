package com.classroom.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroom.products.enums.Category;
import com.classroom.products.model.Product;
import com.classroom.products.dto.ProductResponseDTO;

public interface ProductRepository extends JpaRepository<Product, Long>  {

    @Query("""
            SELECT new com.classroom.products.dto.ProductResponseDTO(
                p.id, p.category, p.productName, p.brand, p.price)
            FROM Product p
            WHERE (:category IS NULL OR p.category = :category)
              AND (:brand IS NULL OR LOWER(p.brand) = LOWER(:brand))
            """)
    Page<ProductResponseDTO> filterProducts(
            @Param("category") Category category,
            @Param("brand") String brand,
            Pageable pageable);

    @Query("""
            SELECT new com.classroom.products.dto.ProductResponseDTO(
                p.id,
                p.category,
                p.productName,
                p.brand,
                p.price
            )
        FROM Product p
        WHERE p.brand = :brand AND p.price BETWEEN :minPrice AND :maxPrice
        """)
    ProductResponseDTO findProductByBrandAndPriceRange(
        @Param("brand") String brand, 
        @Param("minPrice") Double minPrice, 
        @Param("maxPrice") Double maxPrice);

    @Modifying
    @Query("""
            Update Product p
            SET p.price = :newPrice
            WHERE p.id = :productId
            """)
    int updateProductPrice(@Param("productId") Long productId, @Param("newPrice") Double newPrice);
}
