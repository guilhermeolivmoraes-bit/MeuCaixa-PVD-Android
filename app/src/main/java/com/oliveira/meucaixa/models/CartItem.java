package com.oliveira.meucaixa.models;

import com.oliveira.meucaixa.models.Product;

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

    public double getTotalPrice() {
        if (product != null) {
            return product.getPrice() * quantity;
        }
        return 0.0;
    }
}
