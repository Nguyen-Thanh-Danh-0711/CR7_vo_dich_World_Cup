package com.shopcloud.superapp.store;

import com.shopcloud.superapp.model.Product;
import com.shopcloud.superapp.model.Shop;
import com.shopcloud.superapp.model.User;
import com.shopcloud.superapp.model.ViolationReport;
import com.shopcloud.superapp.model.WarningNotice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Kho dữ liệu duy nhất (Singleton) phục vụ phân hệ Quản Trị Admin (Admin Space).
 * <p>
 * SRP: Quản lý danh sách giả lập cho User, Shop, Product, ViolationReport và WarningNotice.
 * Cung cấp các hàm thao tác đổi trạng thái (Ban User, Ban Shop, Delete Product, Resolve Report, Send Warning)
 * phản ánh lập tức lên các TableView JavaFX thông qua ObservableList.
 */
public final class AdminDataStore {

    private static final AdminDataStore INSTANCE = new AdminDataStore();

    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final ObservableList<Shop> shops = FXCollections.observableArrayList();
    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private final ObservableList<ViolationReport> reports = FXCollections.observableArrayList();
    private final ObservableList<WarningNotice> warningNotices = FXCollections.observableArrayList();

    private final AtomicInteger warningIdSeq = new AtomicInteger(1);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private AdminDataStore() {
        initMockData();
    }

    public static AdminDataStore getInstance() {
        return INSTANCE;
    }

    public ObservableList<User> getUsers() {
        return users;
    }

    public ObservableList<Shop> getShops() {
        return shops;
    }

    public ObservableList<Product> getProducts() {
        return products;
    }

    public ObservableList<ViolationReport> getReports() {
        return reports;
    }

    public ObservableList<WarningNotice> getWarningNotices() {
        return warningNotices;
    }

    /**
     * Khởi tạo dữ liệu giả lập phong phú cho Admin Workspace.
     */
    private void initMockData() {
        // 1. Danh sách Người dùng (User không có SĐT)
        users.setAll(List.of(
                new User("ND001", "nguyen_van_a", "nguyenvana@gmail.com", "Buyer", "ACTIVE"),
                new User("ND002", "shop_cr7_official", "contact@cr7official.com", "Seller", "ACTIVE"),
                new User("ND003", "sport_fake_store", "fake_seller@yahoo.com", "Seller", "ACTIVE"),
                new User("ND004", "le_thi_b", "lethib@gmail.com", "Buyer", "ACTIVE"),
                new User("ND005", "spammer_user99", "spammer@tempmail.org", "Buyer", "BANNED"),
                new User("ND006", "admin_ops", "admin.ops@shopcloud.com", "Admin", "ACTIVE")
        ));

        // 2. Danh sách Shop
        shops.setAll(List.of(
                new Shop("SHOP001", "CR7 Official Store", "ND002 - shop_cr7_official", "2024-01-15", 12, "ACTIVE"),
                new Shop("SHOP002", "Sport Fashion Fake Store", "ND003 - sport_fake_store", "2024-03-20", 8, "ACTIVE"),
                new Shop("SHOP003", "Bóng Đá Chính Hãng Store", "ND002 - shop_cr7_official", "2024-05-10", 15, "ACTIVE"),
                new Shop("SHOP004", "Phụ Kiện Thể Thao Giá Rẻ", "ND005 - spammer_user99", "2024-06-01", 5, "BANNED")
        ));

        // 3. Danh sách Sản phẩm phân theo Shop
        products.setAll(List.of(
                new Product("SP001", "Áo đấu CR7 Home 2026", 890_000, 9.8, 4300,
                        "/fxml/assets/logo.png", "Áo thi đấu chính thức CR7 World Cup 2026.", 120, "SHOP001", "CR7 Official Store"),
                new Product("SP002", "Giày Nike Mercurial CR7", 3_450_000, 9.5, 1250,
                        "/fxml/assets/logo.png", "Giày đá bóng cao cấp đinh TF.", 45, "SHOP001", "CR7 Official Store"),
                new Product("SP003", "Bóng đá CR7 Signature", 650_000, 8.7, 850,
                        "/fxml/assets/logo.png", "Bóng tiêu chuẩn FIFA Quality Pro.", 200, "SHOP001", "CR7 Official Store"),
                new Product("SP004", "Áo đấu CR7 nhái giá rẻ", 120_000, 3.2, 45,
                        "/fxml/assets/logo.png", "Áo chất lượng kém vi phạm bản quyền thương hiệu.", 50, "SHOP002", "Sport Fashion Fake Store"),
                new Product("SP005", "Giày Fake Nike Zoom", 250_000, 2.8, 12,
                        "/fxml/assets/logo.png", "Hàng giả nhãn hiệu Nike bị tố cáo.", 30, "SHOP002", "Sport Fashion Fake Store"),
                new Product("SP006", "Tất Thể Thao Chống Trượt", 45_000, 9.1, 980,
                        "/fxml/assets/logo.png", "Tất thể thao dệt kim êm ái.", 500, "SHOP003", "Bóng Đá Chính Hãng Store")
        ));

        // 4. Báo cáo vi phạm (Violation Reports)
        reports.setAll(List.of(
                new ViolationReport("RP001", "nguyen_van_a", "PRODUCT", "SP004", "Áo đấu CR7 nhái giá rẻ",
                        "Bán hàng giả, hàng nhái vi phạm bản quyền thương hiệu CR7", "Hóa đơn và ảnh mở hộp sản phẩm không logo chứng nhận", "PENDING", LocalDateTime.now().minusHours(5).format(formatter)),
                new ViolationReport("RP002", "le_thi_b", "SHOP", "SHOP002", "Sport Fashion Fake Store",
                        "Shop kinh doanh hàng giả và gian lận thanh toán", "Ảnh chụp màn hình tin nhắn dụ dỗ chuyển khoản ngoài hệ thống", "PENDING", LocalDateTime.now().minusDays(1).format(formatter)),
                new ViolationReport("RP003", "nguyen_van_a", "USER", "ND005", "spammer_user99",
                        "Spam bình luận thô tục trên trang đánh giá sản phẩm", "Link bài đánh giá chứa từ ngữ xúc phạm", "RESOLVED", LocalDateTime.now().minusDays(3).format(formatter))
        ));

        // Khởi tạo phản hồi mẫu cho báo cáo đã xử lý
        reports.get(2).setAdminReply("Đã xác minh và tiến hành Khóa tài khoản ND005 vĩnh viễn.");
    }

    // --- CÁC THAO TÁC NGHIỆP VỤ ADMIN ---

    /**
     * Khóa tài khoản Người dùng (Chuyển trạng thái sang BANNED).
     */
    public void banUser(String userId) {
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.getId().equalsIgnoreCase(userId)) {
                u.setStatus("BANNED");
                users.set(i, u); // Trigger TableView update
                break;
            }
        }
    }

    /**
     * Mở khóa tài khoản Người dùng.
     */
    public void unbanUser(String userId) {
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.getId().equalsIgnoreCase(userId)) {
                u.setStatus("ACTIVE");
                users.set(i, u);
                break;
            }
        }
    }

    /**
     * Khóa Cửa hàng / Shop (Chuyển trạng thái sang BANNED).
     */
    public void banShop(String shopId) {
        for (int i = 0; i < shops.size(); i++) {
            Shop s = shops.get(i);
            if (s.getId().equalsIgnoreCase(shopId)) {
                s.setStatus("BANNED");
                shops.set(i, s);
                break;
            }
        }
    }

    /**
     * Mở khóa Cửa hàng.
     */
    public void unbanShop(String shopId) {
        for (int i = 0; i < shops.size(); i++) {
            Shop s = shops.get(i);
            if (s.getId().equalsIgnoreCase(shopId)) {
                s.setStatus("ACTIVE");
                shops.set(i, s);
                break;
            }
        }
    }

    /**
     * Xóa sản phẩm vi phạm (Gán adminStatus thành REMOVED_BY_ADMIN và ẩn sản phẩm).
     */
    public void deleteProduct(String productId) {
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            if (p.getId().equalsIgnoreCase(productId)) {
                p.setAdminStatus("REMOVED_BY_ADMIN");
                p.setActive(false);
                products.set(i, p);
                break;
            }
        }
    }

    /**
     * Xử lý Phản hồi báo cáo vi phạm.
     */
    public void resolveReport(String reportId, String adminReply) {
        for (int i = 0; i < reports.size(); i++) {
            ViolationReport r = reports.get(i);
            if (r.getReportId().equalsIgnoreCase(reportId)) {
                r.setStatus("RESOLVED");
                r.setAdminReply(adminReply);
                reports.set(i, r);
                break;
            }
        }
    }

    /**
     * Gửi Cảnh báo vi phạm (Warning Notice) vào hệ thống.
     */
    public void sendWarning(String targetId, String targetName, String targetType, String title, String content, String deadline) {
        String warnId = String.format("WARN%03d", warningIdSeq.getAndIncrement());
        String nowStr = LocalDateTime.now().format(formatter);
        WarningNotice notice = new WarningNotice(warnId, targetId, targetName, targetType, title, content, deadline, nowStr);
        warningNotices.add(0, notice);
    }
}
