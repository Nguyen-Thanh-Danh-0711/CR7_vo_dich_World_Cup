package main.java.com.shopcloud.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private String orderId; // Có thể sinh dạng UUID hoặc Snowflake ID để đảm bảo phân tán

    private Long customerId; // Trùng với userId của người mua 

    private Long sellerId; // Mã shop bị đặt hàng [cite: 248, 275]

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // PENDING, PREPARING, SHIPPED, CANCELLED [cite: 263]

    private LocalDateTime createdAt;

    // Chi tiết các mặt hàng trong đơn hàng này
    // 1 order có thể có nhiều orderitem
    // 1 đơn hàng có thể mua nhiều sản phẩm
    // Cascade: làm tương tự với các item liên quan

    // LAZY: chưa lấy item ngay
    // Nhanh hơn, tiết kiệm ram hơn, phù hợp hơn so với EAGER
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // Chỉ định khóa ngoại
    private List<OrderItem> items;
}
