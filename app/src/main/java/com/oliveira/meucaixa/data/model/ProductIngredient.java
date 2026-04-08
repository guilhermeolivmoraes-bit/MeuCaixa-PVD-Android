package com.oliveira.meucaixa.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "product_ingredients",
        primaryKeys = {"productId", "ingredientId"},
        foreignKeys = {
                @ForeignKey(entity = Product.class,
                            parentColumns = "id",
                            childColumns = "productId",
                            onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Ingredient.class,
                            parentColumns = "id",
                            childColumns = "ingredientId",
                            onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = {"productId"}),
                @Index(value = {"ingredientId"})
        })
public class ProductIngredient {

    private long productId;
    private long ingredientId;
    private double quantityUsed;

    public ProductIngredient(long productId, long ingredientId, double quantityUsed) {
        this.productId = productId;
        this.ingredientId = ingredientId;
        this.quantityUsed = quantityUsed;
    }

    // Getters and Setters

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public double getQuantityUsed() {
        return quantityUsed;
    }

    public void setQuantityUsed(double quantityUsed) {
        this.quantityUsed = quantityUsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductIngredient that = (ProductIngredient) o;
        return ingredientId == that.ingredientId;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(ingredientId);
    }
}
