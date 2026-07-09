package com.shopcloud.order.controller;

import com.shopcloud.order.dto.OrderRequest;
import com.shopcloud.order.entity.Order;
import com.shopcloud.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout") // API tạo đơn hàng
    public ResponseEntity<Order> checkout(@RequestBody OrderRequest orderRequest) {
        Order order = orderService.createOrder(orderRequest);
        // Chuyển sang service
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}
