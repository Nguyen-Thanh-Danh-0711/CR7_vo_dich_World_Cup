package com.shopcloud.product.service.impl;

import com.shopcloud.product.document.Product;
import com.shopcloud.product.dto.ProductRequest;
import com.shopcloud.product.exception.ResourceNotFoundException;
import com.shopcloud.product.repository.ProductRepository;
import com.shopcloud.product.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getProductsForBuyer() {
        return productRepository.findByStockQuantityGreaterThan(0);
    }

    @Override
    public List<Product> getProductsByShop(Long shopId) {
        return productRepository.findByShopId(shopId);
    }

    @Override
    public Product createProduct(ProductRequest request) {
        Product product = Product.builder()
                .shopId(request.getShopId())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .build();

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(String id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại với ID: " + id));
                // Tìm sản phẩm theo id. Nếu không có -> ném lỗi 404 Not Found

        product.setShopId(request.getShopId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());

        return productRepository.save(product);
    }
}
