package com.shopcloud.order.service.impl;

import com.shopcloud.order.dto.OrderItemRequest;
import com.shopcloud.order.dto.OrderRequest;
import com.shopcloud.order.entity.Order;
import com.shopcloud.order.entity.OrderItem;
import com.shopcloud.order.repository.OrderRepository;
import com.shopcloud.order.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private static final String DEFAULT_STATUS = "PENDING";

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional // Nếu bước cuối bị lỗi thì trả về trạng thái ban đầu
    public Order createOrder(OrderRequest request) {
        Order order = Order.builder()
                .userId(request.getUserId())
                .shopId(request.getShopId())
                .status(DEFAULT_STATUS)
                .createdAt(LocalDateTime.now())
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .order(order)
                    .build();

            orderItems.add(orderItem);
            totalAmount = totalAmount + (itemRequest.getQuantity() * itemRequest.getPrice());
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        return orderRepository.save(order); // Lưu vào database
    }
}
