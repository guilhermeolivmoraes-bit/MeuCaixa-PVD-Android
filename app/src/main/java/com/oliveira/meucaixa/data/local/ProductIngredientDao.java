package com.oliveira.meucaixa.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.oliveira.meucaixa.data.model.ProductIngredient;
import com.oliveira.meucaixa.data.model.RecipeIngredientDetail;

import java.util.List;

@Dao
public interface ProductIngredientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProductIngredient productIngredient);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProductIngredient> ingredients);

    @Update
    void update(ProductIngredient productIngredient);

    @Delete
    void delete(ProductIngredient productIngredient);

    // Old method if you want to keep
    @Query("SELECT * FROM product_ingredients WHERE product_id = :productId")
    LiveData<List<ProductIngredient>> getIngredientsForProduct(long productId);

    // Clean old recipe
    @Query("DELETE FROM product_ingredients WHERE product_id = :productId")
    void deleteIngredientsForProduct(long productId);

    // The JOIN query for fetching full recipe detail
    @Query("SELECT i.id AS ingredientId, i.name AS ingredientName, pi.quantity_used AS quantityUsed " +
           "FROM ingredients i " +
           "INNER JOIN product_ingredients pi ON i.id = pi.ingredient_id " +
           "WHERE pi.product_id = :productId")
    LiveData<List<RecipeIngredientDetail>> getRecipeDetailsForProduct(long productId);

    // Transactional replace
    @Transaction
    default void replaceRecipe(long productId, List<ProductIngredient> newRecipe) {
        deleteIngredientsForProduct(productId);
        insertAll(newRecipe);
    }
}
