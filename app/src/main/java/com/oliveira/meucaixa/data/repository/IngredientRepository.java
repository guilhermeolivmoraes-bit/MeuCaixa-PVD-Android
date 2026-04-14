package com.oliveira.meucaixa.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.oliveira.meucaixa.data.local.AppDatabase;
import com.oliveira.meucaixa.data.local.IngredientDao;
import com.oliveira.meucaixa.data.model.Ingredient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IngredientRepository {
    private final IngredientDao ingredientDao;
    private final ExecutorService executorService;

    public IngredientRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.ingredientDao = db.ingredientDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Ingredient>> getAllIngredients(long userId) {
        return ingredientDao.getAllIngredients(userId);
    }

    public void insert(Ingredient ingredient) {
        executorService.execute(() -> ingredientDao.insert(ingredient));
    }
    
    public Ingredient getIngredientById(long id) {
        return ingredientDao.getIngredientById(id);
    }
}
