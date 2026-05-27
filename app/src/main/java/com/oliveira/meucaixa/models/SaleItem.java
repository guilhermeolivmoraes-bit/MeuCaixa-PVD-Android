package com.oliveira.meucaixa.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "sale_items",
        primaryKeys = {"saleId", "productId"},
        foreignKeys = {
                @ForeignKey(
                        entity = Sale.class,
                        parentColumns = "id",
                        childColumns = "saleId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Product.class,
                        parentColumns = "id",
                        childColumns = "productId",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {@Index("saleId"), @Index("productId")}
)
public class SaleItem {
    private long saleId;
    private long productId;
    private String productName;
    private double productPrice;
    private double costPrice;
    private double quantity;

    public SaleItem(long saleId, long productId, String productName, double productPrice, double costPrice, double quantity) {
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.costPrice = costPrice;
        this.quantity = quantity;
    }

    // Getters and Setters
    public long getSaleId() { return saleId; }
    public void setSaleId(long saleId) { this.saleId = saleId; }
    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }
    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
}
