package com.shopcloud.order.service;

import com.shopcloud.order.dto.OrderRequest;
import com.shopcloud.order.entity.Order;

public interface OrderService {

    Order createOrder(OrderRequest request);
}
