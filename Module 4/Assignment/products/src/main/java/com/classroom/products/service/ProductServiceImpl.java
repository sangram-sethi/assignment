package com.classroom.products.service;

import com.classroom.products.model.Product;
import com.classroom.products.repository.ProductRepository;

import jakarta.transaction.Transactional;

import com.classroom.products.enums.Category;
import com.classroom.products.exception.BadRequestException;
import com.classroom.products.exception.ResourceNotFoundException;
import com.classroom.products.dto.ProductResponseDTO;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("productName", "price", "brand", "category", "id");

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private ProductResponseDTO mapToDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getCategory(),
                product.getProductName(),
                product.getBrand(),
                product.getPrice()
        );
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponseDTO addProduct(Product product) {
        return mapToDTO(productRepository.save(product));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll(
            PageRequest.of(0, 5, Sort.by("id"))
        ).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ProductResponseDTO getProductById(Long productId) {
        return mapToDTO(findProductById(productId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<ProductResponseDTO> getProducts(Category category, String brand,
            String sortBy, String direction, int page, int size) {
        String sortField = (sortBy == null || sortBy.isBlank()) ? "productName" : sortBy;
        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            throw new BadRequestException(
                    "Invalid sort field: " + sortField + ". Allowed: " + ALLOWED_SORT_FIELDS);
        }
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        if (page < 0) {
            throw new BadRequestException("Page index must not be negative");
        }
        if (size <= 0 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }
        String normalizedBrand = (brand == null || brand.isBlank()) ? null : brand;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        return productRepository.filterProducts(category, normalizedBrand, pageable).getContent();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponseDTO updateProduct(Long productId, Product product) {
        Product existingProduct = findProductById(productId);
        existingProduct.setProductName(product.getProductName());
        existingProduct.setPrice(product.getPrice());
        return mapToDTO(productRepository.save(existingProduct));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponseDTO updateProductPrice(Long productId, double newPrice) {
        int updatedRows = productRepository.updateProductPrice(productId, newPrice);
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        return getProductById(productId);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ProductResponseDTO findProductByBrandAndPriceRange(String brand, 
                                                            double minPrice, 
                                                            double maxPrice) {
        ProductResponseDTO product = productRepository
                                    .findProductByBrandAndPriceRange(brand, 
                                                                    minPrice, 
                                                                    maxPrice);
        if (product == null) {
            throw new ResourceNotFoundException(
                "No product found for brand: " + brand + " in the price range: " + minPrice + " - " + maxPrice
            );
        }
        
        return product;
    }
}
