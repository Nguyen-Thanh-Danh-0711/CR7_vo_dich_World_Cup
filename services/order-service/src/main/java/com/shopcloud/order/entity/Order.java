package com.shopcloud.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity // Thực thể
@Table(name = "orders") // Chỉ định tên bảng ở database
// Giúp tránh nhầm lẫn và chủ động hơn
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private Long userId; // Trùng với userId của người mua

    private Long shopId; // Mã shop nhận đơn hàng

    private Double totalAmount;

    private String status; // PENDING, PAID, SHIPPING, DELIVERED, CANCELLED

    private LocalDateTime createdAt;
}
