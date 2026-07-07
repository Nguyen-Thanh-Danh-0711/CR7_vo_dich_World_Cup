package com.shopcloud.product.repository;

import com.shopcloud.product.document.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // Lấy danh sách sản phẩm thuộc về một cửa hàng cụ thể trong Seller Space.
    List<Product> findByShopId(Long shopId);

    // Tìm kiếm danh sách sản phẩm theo danh mục để phục vụ Buyer Space.
    List<Product> findByCategory(String category);
}
