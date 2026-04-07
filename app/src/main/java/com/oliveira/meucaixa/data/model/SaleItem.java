package com.oliveira.meucaixa.data.model;

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
    private int quantity;

    public SaleItem(long saleId, long productId, String productName, double productPrice, int quantity) {
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
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
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
