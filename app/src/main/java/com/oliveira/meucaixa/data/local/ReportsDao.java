package com.oliveira.meucaixa.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface ReportsDao {

    @Query("SELECT " +
           "IFNULL((SELECT SUM(s.totalPrice) FROM sales s WHERE s.user_id = :userId AND s.date BETWEEN :startTime AND :endTime), 0.0) " +
           "- " +
           "IFNULL((SELECT SUM(si.quantity * pi.quantity_used * (i.packagePrice / i.packageQuantity)) " +
           " FROM sale_items si " +
           " INNER JOIN sales s ON si.saleId = s.id " +
           " INNER JOIN products p ON si.productId = p.id " +
           " INNER JOIN product_ingredients pi ON p.id = pi.product_id " +
           " INNER JOIN ingredients i ON pi.ingredient_id = i.id " +
           " WHERE s.user_id = :userId AND s.date BETWEEN :startTime AND :endTime AND p.is_own_production = 1 AND i.packageQuantity > 0), 0.0) " +
           "- " +
           "IFNULL((SELECT SUM(si.quantity * p.cost_price) " +
           " FROM sale_items si " +
           " INNER JOIN sales s ON si.saleId = s.id " +
           " INNER JOIN products p ON si.productId = p.id " +
           " WHERE s.user_id = :userId AND s.date BETWEEN :startTime AND :endTime AND p.is_own_production = 0), 0.0)")
    LiveData<Double> getNetProfitForPeriod(long userId, long startTime, long endTime);
}
