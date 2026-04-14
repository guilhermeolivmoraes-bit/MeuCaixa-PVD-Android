package com.oliveira.meucaixa.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "product_ingredients",
        primaryKeys = {"product_id", "ingredient_id"},
        foreignKeys = {
                @ForeignKey(entity = Product.class,
                            parentColumns = "id",
                            childColumns = "product_id",
                            onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Ingredient.class,
                            parentColumns = "id",
                            childColumns = "ingredient_id",
                            onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = {"product_id"}),
                @Index(value = {"ingredient_id"})
        })
public class ProductIngredient {

    @ColumnInfo(name = "product_id")
    private long productId;

    @ColumnInfo(name = "ingredient_id")
    private long ingredientId;

    @ColumnInfo(name = "quantity_used")
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
