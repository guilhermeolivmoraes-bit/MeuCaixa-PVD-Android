package com.oliveira.meucaixa.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.oliveira.meucaixa.data.local.AppDatabase;
import com.oliveira.meucaixa.data.local.ProductDao;
import com.oliveira.meucaixa.data.local.ProductIngredientDao;
import com.oliveira.meucaixa.data.model.Product;
import com.oliveira.meucaixa.data.model.ProductIngredient;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductRepository {
    private final ProductDao productDao;
    private final ProductIngredientDao productIngredientDao;
    private final ExecutorService executorService;

    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.productDao = db.productDao();
        this.productIngredientDao = db.productIngredientDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<Product> getProductById(long productId, long userId) {
        return productDao.getById(productId, userId);
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

    public void updateProduct(Product product) {
        executorService.execute(() -> productDao.update(product));
    }
}
