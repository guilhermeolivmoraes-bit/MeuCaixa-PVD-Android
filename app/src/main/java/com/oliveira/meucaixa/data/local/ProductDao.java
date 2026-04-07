package com.oliveira.meucaixa.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.oliveira.meucaixa.data.model.Product;

import java.util.List;

@Dao
public interface ProductDao {

    @Query("SELECT * FROM products WHERE user_id = :userId AND product_stock > 0 ORDER BY product_name ASC")
    LiveData<List<Product>> getAll(long userId);

    @Query("SELECT * FROM products WHERE user_id = :userId AND product_name LIKE :query AND product_stock > 0 ORDER BY product_name ASC")
    LiveData<List<Product>> searchByName(long userId, String query);

    @Query("SELECT * FROM products WHERE id = :id AND user_id = :userId LIMIT 1")
    LiveData<Product> getById(long id, long userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Product product);

    @Update
    void update(Product product);

    @Query("DELETE FROM products WHERE id = :productId")
    void deleteById(long productId);
}
