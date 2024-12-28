package entities;

import java.sql.Blob;
import java.util.Arrays;
import java.util.Objects;

public final class Product {
    private String productId;
    private String productName;
    private String platformName;
    private double price;
    private String specification;
    private String image;

    // Constructor
    public Product(String productId, String productName, String platformName, double price, String specification, String image) {
        this.productId = productId;
        this.productName = productName;
        this.platformName = platformName;
        this.price = price;
        this.specification = specification;
        this.image = image;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                "productName='" + productName + '\'' +
                ", platformName='" + platformName + '\'' +
                ", price=" + price +
                ", specification='" + specification + '\'' +
                ", image=" + image + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, platformName, price, specification, image);
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
