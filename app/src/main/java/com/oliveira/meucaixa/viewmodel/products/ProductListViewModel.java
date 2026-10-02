package com.oliveira.meucaixa.viewmodel.products;

import com.oliveira.meucaixa.model.repository.AppDatabase;
import com.oliveira.meucaixa.model.dao.IngredientDao;
import com.oliveira.meucaixa.model.dao.ProductDao;
import com.oliveira.meucaixa.model.entity.Ingredient;
import com.oliveira.meucaixa.model.entity.Product;
import com.oliveira.meucaixa.model.manager.SessionManager;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.Executors;

public class ProductListViewModel extends AndroidViewModel {

    private final ProductDao productDao;
    private final IngredientDao ingredientDao;
    private final long userId;

    public ProductListViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
        ingredientDao = db.ingredientDao();
        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getLoggedInUserId();
    }

    public LiveData<List<Product>> getAllProducts() {
        return productDao.getAll(userId);
    }
    
    public LiveData<List<Ingredient>> getAllIngredients() {
        return ingredientDao.getAllIngredients(userId);
    }

    public void deleteProduct(Product product) {
        Executors.newSingleThreadExecutor().execute(() -> {
            productDao.deleteById(product.getId());
        });
    }

    public void deleteIngredient(Ingredient ingredient) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ingredientDao.delete(ingredient);
        });
    }
}
