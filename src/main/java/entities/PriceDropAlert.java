package entities;

import java.util.Objects;

public final class PriceDropAlert {
    private String productSpecification;
    private double price;
    private String email;

    // Constructor
    public PriceDropAlert(String productSpecification, double price, String email) {
        this.productSpecification = productSpecification;
        this.price = price;
        this.email = email;
    }

    @Override
    public String toString() {
        return "PriceDropAlert{" +
                "productSpecification='" + productSpecification + '\'' +
                ", price=" + price +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceDropAlert that = (PriceDropAlert) o;
        return Objects.equals(productSpecification, that.productSpecification) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productSpecification, price, email);
    }

    // Getters and Setters
    public String getProductSpecification() {
        return productSpecification;
    }

    public void setProductSpecification(String productSpecification) {
        this.productSpecification = productSpecification;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}