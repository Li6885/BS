package queries;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PriceHistoryQuery {

    // 内部类：代表查询结果中的单个商品历史价格记录
    public static class Item {
        private String productName;
        private String platformName;
        private double price;
        private String time;

        public Item(String productName, String platformName, double price, String category, String specification, String time) {
            this.productName = productName;
            this.platformName = platformName;
            this.price = price;
            this.time = time;
        }

        @Override
        public String toString() {
            return "Item {" +
                    "productName='" + productName + '\'' +
                    ", platformName='" + platformName + '\'' +
                    ", price=" + price +
                    ", time='" + time + '\'' +
                    '}';
        }

        // Getters and Setters
        public String getProductName() {
            return productName;
        }

        public String getPlatformName() {
            return platformName;
        }

        public double getPrice() {
            return price;
        }

        public String getTime() {
            return time;
        }
    }

    // 查询商品历史价格的方法
    public List<Item> getPriceHistory(Connection conn, String productName, String platformName) throws SQLException {
        List<Item> priceHistoryItems = new ArrayList<>();

        String sql = "SELECT p.product_name, p.platform_name, p.price, p.category, p.specification, ph.time " +
                "FROM product_history_price ph " +
                "JOIN product p ON ph.product_name = p.product_name AND ph.platform_name = p.platform_name " +
                "WHERE p.product_name = ? AND p.platform_name = ? " +
                "ORDER BY ph.time DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productName);
            pstmt.setString(2, platformName);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("product_name");
                String platform = rs.getString("platform_name");
                double price = rs.getDouble("price");
                String category = rs.getString("category");
                String specification = rs.getString("specification");
                String time = rs.getString("time");

                Item item = new Item(name, platform, price, category, specification, time);
                priceHistoryItems.add(item);
            }
        }
        return priceHistoryItems;
    }
}
