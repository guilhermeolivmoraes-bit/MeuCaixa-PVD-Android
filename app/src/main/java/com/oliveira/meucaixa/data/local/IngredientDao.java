package com.oliveira.meucaixa.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.oliveira.meucaixa.data.model.Ingredient;

import java.util.List;

@Dao
public interface IngredientDao {

    @Insert
    long insert(Ingredient ingredient);

    @Update
    void update(Ingredient ingredient);

    @Delete
    void delete(Ingredient ingredient);

    @Query("SELECT * FROM ingredients WHERE user_id = :userId ORDER BY name ASC")
    LiveData<List<Ingredient>> getAllIngredients(long userId);

    @Query("SELECT * FROM ingredients WHERE id = :id")
    Ingredient getIngredientById(long id);
}
