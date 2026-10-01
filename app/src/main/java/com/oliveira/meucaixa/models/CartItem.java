package com.oliveira.meucaixa.models;

import com.oliveira.meucaixa.models.Product;

import java.math.BigDecimal;

/**
 * Represents an item inside the shopping cart UI.
 * This is a simple model class, not a database entity.
 */
public class CartItem {
    private final Product product;
    private double quantity;

    public CartItem(Product product, double quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        if (product != null) {
            return product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}
