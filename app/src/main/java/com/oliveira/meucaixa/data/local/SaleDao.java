package com.oliveira.meucaixa.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.oliveira.meucaixa.data.model.Sale;
import com.oliveira.meucaixa.data.model.SaleItem;

import java.util.List;

@Dao
public abstract class SaleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insertSale(Sale sale);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertSaleItems(List<SaleItem> items);

    @Transaction
    public void saveCompleteSale(Sale sale, List<SaleItem> items) {
        long saleId = insertSale(sale);
        for (SaleItem item : items) {
            item.setSaleId(saleId);
        }
        insertSaleItems(items);
    }

    @Query("SELECT * FROM sales WHERE user_id = :userId AND date >= :startOfDay AND date < :endOfDay ORDER BY date DESC")
    public abstract LiveData<List<Sale>> getSalesForDay(long userId, long startOfDay, long endOfDay);

    @Query("SELECT * FROM sales WHERE user_id = :userId AND date >= :startOfMonth AND date < :endOfMonth ORDER BY date DESC")
    public abstract LiveData<List<Sale>> getSalesForMonth(long userId, long startOfMonth, long endOfMonth);
}
