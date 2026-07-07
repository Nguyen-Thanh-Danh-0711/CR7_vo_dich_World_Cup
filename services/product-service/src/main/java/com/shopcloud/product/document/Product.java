package com.shopcloud.product.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products") // Định nghĩa tên Collection trong MongoDB
@Getter
@Setter
@NoArgsConstructor // Sinh constructor rỗng
@AllArgsConstructor // Sinh constructor đầy đủ
@Builder // Cung cấp mẫu thiết kế builder để linh hoạt và dễ đọc
public class Product {

    @Id
    private String id; // MongoDB thường dùng String (ObjectId) làm khóa chính tự sinh

    // Thuộc tính cực kỳ quan trọng để liên kết sản phẩm này thuộc về Gian hàng nào
    private Long shopId;

    private String name; // Tên sản phẩm
    
    private String description;
    
    private Double price;

    // Số lượng tồn kho (Sẽ được đồng bộ lên Redis phục vụ tính năng Khóa kho/Lock Stock khi Flash Sale)
    private Integer stockQuantity;

    private String category;

    private String imageUrl;
}
