package entities;

import java.util.Objects;

public final class ProductHistoryPrice {
    private String time;
    private String productId;
    private double price;

    // Constructor
    public ProductHistoryPrice(String time, String productId, double price) {
        this.time = time;
        this.productId = productId;
        this.price = price;
    }

    @Override
    public String toString() {
        return "ProductHistoryPrice{" +
                "time='" + time + '\'' +
                ", productId='" + productId + '\'' +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductHistoryPrice that = (ProductHistoryPrice) o;
        return Objects.equals(time, that.time) &&
                Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, productId, price);
    }

    // Getters and Setters
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getProductName() {
        return productId;
    }

    public void setProductName(String productId) {
        this.productId = productId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}