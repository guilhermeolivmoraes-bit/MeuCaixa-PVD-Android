package com.oliveira.meucaixa.ui.products;

import com.oliveira.meucaixa.data.local.AppDatabase;
import com.oliveira.meucaixa.data.local.ProductDao;
import com.oliveira.meucaixa.data.model.Product;
import com.oliveira.meucaixa.utils.SessionManager;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class ProductListViewModel extends AndroidViewModel {

    private final ProductDao productDao;
    private final com.oliveira.meucaixa.data.local.IngredientDao ingredientDao;
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
    
    public LiveData<java.util.List<com.oliveira.meucaixa.data.model.Ingredient>> getAllIngredients() {
        return ingredientDao.getAllIngredients(userId);
    }
}
