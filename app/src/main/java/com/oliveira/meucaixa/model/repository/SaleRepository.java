package com.oliveira.meucaixa.model.repository;

import android.app.Application;
import android.util.Log;

import com.oliveira.meucaixa.model.repository.AppDatabase;
import com.oliveira.meucaixa.model.dao.IngredientDao;
import com.oliveira.meucaixa.model.dao.ProductDao;
import com.oliveira.meucaixa.model.dao.ProductIngredientDao;
import com.oliveira.meucaixa.model.dao.SaleDao;
import com.oliveira.meucaixa.model.entity.Ingredient;
import com.oliveira.meucaixa.model.entity.Product;
import com.oliveira.meucaixa.model.entity.ProductIngredient;
import com.oliveira.meucaixa.model.entity.Sale;
import com.oliveira.meucaixa.model.entity.SaleItem;

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
