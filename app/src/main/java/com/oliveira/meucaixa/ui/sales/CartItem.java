package com.oliveira.meucaixa.ui.sales;

import com.oliveira.meucaixa.data.model.Product;

/**
 * Represents an item inside the shopping cart UI.
 * This is a simple model class, not a database entity.
 */
public class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        if (product != null) {
            return product.getPrice() * quantity;
        }
        return 0.0;
    }
}
