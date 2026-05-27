package com.oliveira.meucaixa.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.oliveira.meucaixa.models.Sale;
import com.oliveira.meucaixa.models.SaleItem;

import java.util.List;

@Dao
public abstract class SaleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insertSale(Sale sale);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertSaleItems(List<SaleItem> items);


    @Query("SELECT * FROM sales WHERE user_id = :userId AND date >= :startOfDay AND date < :endOfDay ORDER BY date DESC")
    public abstract LiveData<List<Sale>> getSalesForDay(long userId, long startOfDay, long endOfDay);

    @Query("SELECT * FROM sales WHERE user_id = :userId AND date >= :startOfMonth AND date < :endOfMonth ORDER BY date DESC")
    public abstract LiveData<List<Sale>> getSalesForMonth(long userId, long startOfMonth, long endOfMonth);
}
