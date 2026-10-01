package com.oliveira.meucaixa.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.math.BigDecimal;

@Entity(tableName = "products",
        foreignKeys = @ForeignKey(entity = User.class,
                                  parentColumns = "id",
                                  childColumns = "user_id",
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"user_id"})})
public class Product {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "product_name")
    private String name;

    @ColumnInfo(name = "product_price")
    private BigDecimal price;

    @ColumnInfo(name = "product_stock")
    private double stock;

    @ColumnInfo(name = "unit_type")
    private String unitType;

    @ColumnInfo(name = "is_own_production")
    private boolean isOwnProduction;

    @ColumnInfo(name = "cost_price")
    private BigDecimal costPrice;

    // Getters and Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public boolean isOwnProduction() {
        return isOwnProduction;
    }

    public void setOwnProduction(boolean ownProduction) {
        isOwnProduction = ownProduction;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }
}
