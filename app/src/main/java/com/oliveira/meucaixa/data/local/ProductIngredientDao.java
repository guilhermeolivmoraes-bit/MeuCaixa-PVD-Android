package com.oliveira.meucaixa.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.oliveira.meucaixa.data.model.ProductIngredient;

import java.util.List;

@Dao
public interface ProductIngredientDao {

    @Insert
    void insert(ProductIngredient productIngredient);

    @Update
    void update(ProductIngredient productIngredient);

    @Delete
    void delete(ProductIngredient productIngredient);

    @Query("SELECT * FROM product_ingredients WHERE productId = :productId")
    LiveData<List<ProductIngredient>> getIngredientsForProduct(long productId);

    @Query("DELETE FROM product_ingredients WHERE productId = :productId")
    void deleteIngredientsForProduct(long productId);
}
