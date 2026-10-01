package com.oliveira.meucaixa.repositories;

import android.app.Application;
import android.util.Log;

import com.oliveira.meucaixa.database.AppDatabase;
import com.oliveira.meucaixa.database.IngredientDao;
import com.oliveira.meucaixa.database.ProductDao;
import com.oliveira.meucaixa.database.ProductIngredientDao;
import com.oliveira.meucaixa.database.SaleDao;
import com.oliveira.meucaixa.models.Ingredient;
import com.oliveira.meucaixa.models.Product;
import com.oliveira.meucaixa.models.ProductIngredient;
import com.oliveira.meucaixa.models.Sale;
import com.oliveira.meucaixa.models.SaleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SaleRepository {
    private final SaleDao saleDao;
    private final ProductDao productDao;
    private final IngredientDao ingredientDao;
    private final ProductIngredientDao productIngredientDao;
    private final AppDatabase db;
    private final ExecutorService executorService;

    public SaleRepository(Application application) {
        db = AppDatabase.getDatabase(application);
        saleDao = db.saleDao();
        productDao = db.productDao();
        ingredientDao = db.ingredientDao();
        productIngredientDao = db.productIngredientDao();
        executorService = Executors.newSingleThreadExecutor();
    }


}
