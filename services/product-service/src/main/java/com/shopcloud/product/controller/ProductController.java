package com.shopcloud.product.controller;

import com.shopcloud.product.document.Product;
import com.shopcloud.product.dto.ProductRequest;
import com.shopcloud.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService; // Chuyển yêu cầu xuống service

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/buyer/list") // Lấy danh sách sản phẩm cho người mua
    public ResponseEntity<List<Product>> getProductsForBuyer() {
        List<Product> products = productService.getProductsForBuyer();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/seller/shop/{shopId}") // Lấy sản phẩm theo shop
    public ResponseEntity<List<Product>> getProductsByShop(@PathVariable Long shopId) {
        List<Product> products = productService.getProductsByShop(shopId);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/seller/create") // Tạo sản phẩm
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequest request) {
        Product product = productService.createProduct(request);
        // Gọi service
        // Service tạo product lưu vào mongodb, trả product vừa lưu
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/seller/update/{id}") // Cập nhật sản phẩm
    public ResponseEntity<Product> updateProduct(
            @PathVariable String id,
            @RequestBody ProductRequest request) {
        Product product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }
}
