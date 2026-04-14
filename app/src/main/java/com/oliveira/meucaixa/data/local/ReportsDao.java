package com.oliveira.meucaixa.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface ReportsDao {

    @Query("SELECT IFNULL(SUM(si.productPrice * si.quantity), 0.0) - IFNULL(SUM(si.costPrice * si.quantity), 0.0) " +
           "FROM sale_items si INNER JOIN sales s ON si.saleId = s.id " +
           "WHERE s.user_id = :userId AND s.date >= :startTime AND s.date < :endTime")
    LiveData<Double> getNetProfitForPeriod(long userId, long startTime, long endTime);
}
