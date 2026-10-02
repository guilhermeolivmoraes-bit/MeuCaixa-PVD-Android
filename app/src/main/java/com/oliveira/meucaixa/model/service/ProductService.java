package com.oliveira.meucaixa.model.service;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.oliveira.meucaixa.model.repository.AppDatabase;
import com.oliveira.meucaixa.model.dao.ProductDao;
import com.oliveira.meucaixa.model.dao.ProductIngredientDao;
import com.oliveira.meucaixa.model.entity.Product;
import com.oliveira.meucaixa.model.entity.ProductIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductService {
    private final ProductDao productDao;
    private final ProductIngredientDao productIngredientDao;
    private final ExecutorService executorService;

    public ProductService(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.productDao = db.productDao();
        this.productIngredientDao = db.productIngredientDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<Product> getProductById(long productId, long userId) {
        return productDao.getById(productId, userId);
    }

    public LiveData<List<ProductIngredient>> getIngredientsForProduct(long productId) {
        return productIngredientDao.getIngredientsForProduct(productId);
    }

    public void saveProductWithIngredients(Product product, Set<ProductIngredient> ingredients) {
        executorService.execute(() -> {
            long productId = productDao.insert(product);
            for (ProductIngredient pi : ingredients) {
                pi.setProductId(productId);
                productIngredientDao.insert(pi);
            }
        });
    }

    public void updateProductWithIngredients(Product product, Set<ProductIngredient> ingredients) {
        executorService.execute(() -> {
            productDao.update(product);
            for (ProductIngredient pi : ingredients) {
                pi.setProductId(product.getId());
            }
            productIngredientDao.replaceRecipe(product.getId(), new ArrayList<>(ingredients));
        });
    }

    public void updateProduct(Product product) {
        executorService.execute(() -> productDao.update(product));
    }
}
