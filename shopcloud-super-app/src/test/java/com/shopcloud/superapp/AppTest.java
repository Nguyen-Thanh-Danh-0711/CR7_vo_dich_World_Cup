package com.shopcloud.superapp;

import com.shopcloud.superapp.model.Product;
import com.shopcloud.superapp.model.Shop;
import com.shopcloud.superapp.model.User;
import com.shopcloud.superapp.model.ViolationReport;
import com.shopcloud.superapp.store.AdminDataStore;
import junit.framework.TestCase;

public class AppTest extends TestCase {

    public void testAdminDataStoreInitialization() {
        AdminDataStore store = AdminDataStore.getInstance();
        assertNotNull(store.getUsers());
        assertFalse(store.getUsers().isEmpty());
        assertNotNull(store.getShops());
        assertFalse(store.getShops().isEmpty());
        assertNotNull(store.getProducts());
        assertFalse(store.getProducts().isEmpty());
        assertNotNull(store.getReports());
        assertFalse(store.getReports().isEmpty());
    }

    public void testBanUser() {
        AdminDataStore store = AdminDataStore.getInstance();
        store.banUser("ND001");
        User user = store.getUsers().stream().filter(u -> "ND001".equalsIgnoreCase(u.getId())).findFirst().orElse(null);
        assertNotNull(user);
        assertTrue(user.isBanned());

        store.unbanUser("ND001");
        assertFalse(user.isBanned());
    }

    public void testBanShop() {
        AdminDataStore store = AdminDataStore.getInstance();
        store.banShop("SHOP001");
        Shop shop = store.getShops().stream().filter(s -> "SHOP001".equalsIgnoreCase(s.getId())).findFirst().orElse(null);
        assertNotNull(shop);
        assertTrue(shop.isBanned());

        store.unbanShop("SHOP001");
        assertFalse(shop.isBanned());
    }

    public void testDeleteProduct() {
        AdminDataStore store = AdminDataStore.getInstance();
        store.deleteProduct("SP004");
        Product product = store.getProducts().stream().filter(p -> "SP004".equalsIgnoreCase(p.getId())).findFirst().orElse(null);
        assertNotNull(product);
        assertEquals("REMOVED_BY_ADMIN", product.getAdminStatus());
        assertFalse(product.isActive());
    }

    public void testResolveReport() {
        AdminDataStore store = AdminDataStore.getInstance();
        store.resolveReport("RP001", "Đã xử lý theo đúng quy trình.");
        ViolationReport report = store.getReports().stream().filter(r -> "RP001".equalsIgnoreCase(r.getReportId())).findFirst().orElse(null);
        assertNotNull(report);
        assertEquals("RESOLVED", report.getStatus());
        assertEquals("Đã xử lý theo đúng quy trình.", report.getAdminReply());
    }

    public void testSendWarning() {
        AdminDataStore store = AdminDataStore.getInstance();
        int initialCount = store.getWarningNotices().size();
        store.sendWarning("ND002", "shop_cr7_official", "USER", "Cảnh báo vi phạm", "Chi tiết vi phạm...", "24h");
        assertEquals(initialCount + 1, store.getWarningNotices().size());
    }
}
