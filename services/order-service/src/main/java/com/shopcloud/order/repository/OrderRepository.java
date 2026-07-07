package com.shopcloud.order.repository;

import com.shopcloud.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Lấy toàn bộ lịch sử đơn đặt hàng của một khách hàng theo userId.
    List<Order> findByUserId(Long userId);

    // Lấy toàn bộ danh sách đơn hàng đổ về một cửa hàng theo shopId.
    List<Order> findByShopId(Long shopId);
}
