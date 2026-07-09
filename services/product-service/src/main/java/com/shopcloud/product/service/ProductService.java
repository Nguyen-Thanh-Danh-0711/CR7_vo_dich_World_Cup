package com.shopcloud.product.service;

import com.shopcloud.product.document.Product;
import com.shopcloud.product.dto.ProductRequest;

import java.util.List;

public interface ProductService {

    List<Product> getProductsForBuyer(); // Lấy danh sách sản phẩm cho người mua

    List<Product> getProductsByShop(Long shopId); // Lấy tất cả sản phẩm của 1 shop

    Product createProduct(ProductRequest request); // Tạo sản phẩm mới

    Product updateProduct(String id, ProductRequest request); // Cập nhật sản phẩm
}
