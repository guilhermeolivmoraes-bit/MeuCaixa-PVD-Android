package com.oliveira.meucaixa.data.model;

import androidx.room.ColumnInfo;

public class RecipeIngredientDetail {

    public long ingredientId;

    public String ingredientName;

    public double quantityUsed;

    public RecipeIngredientDetail(long ingredientId, String ingredientName, double quantityUsed) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.quantityUsed = quantityUsed;
    }

    // Getters and Setters

    public long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public double getQuantityUsed() {
        return quantityUsed;
    }

    public void setQuantityUsed(double quantityUsed) {
        this.quantityUsed = quantityUsed;
    }
}
