package com.oliveira.meucaixa.ui.products;

import com.oliveira.meucaixa.data.local.AppDatabase;
import com.oliveira.meucaixa.data.local.ProductDao;
import com.oliveira.meucaixa.data.model.Ingredient;
import com.oliveira.meucaixa.data.model.Product;
import com.oliveira.meucaixa.data.model.ProductIngredient;
import com.oliveira.meucaixa.utils.SessionManager;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditProductViewModel extends AndroidViewModel {

    private final ProductDao productDao;
    private final ExecutorService executorService;
    private final long userId;

    // In-memory Set collection to guarantee uniqueness of ingredients in the recipe
    private final Set<ProductIngredient> recipeIngredients = new HashSet<>();

    public AddEditProductViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
        executorService = Executors.newSingleThreadExecutor();

        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getLoggedInUserId();
    }

    public LiveData<Product> getProductById(long productId) {
        return productDao.getById(productId, userId);
    }

    public boolean addIngredientToRecipe(ProductIngredient ingredient) {
        // Prevents adding the same ingredient reference twice
        return recipeIngredients.add(ingredient);
    }

    public void removeIngredientFromRecipe(ProductIngredient ingredient) {
        recipeIngredients.remove(ingredient);
    }

    public Set<ProductIngredient> getRecipeIngredients() {
        return recipeIngredients;
    }

    public void clearRecipeIngredients() {
        recipeIngredients.clear();
    }

    public double calculateProductionCost(Set<ProductIngredient> recipe, List<Ingredient> databaseIngredients) {
        double totalCost = 0.0;

        for (ProductIngredient productIngredient : recipe) {
            for (Ingredient dbIngredient : databaseIngredients) {
                if (productIngredient.getIngredientId() == dbIngredient.getId()) {
                    if (dbIngredient.getPackageQuantity() > 0) {
                        double fractionCost = (dbIngredient.getPackagePrice() / dbIngredient.getPackageQuantity());
                        totalCost += (productIngredient.getQuantityUsed() * fractionCost);
                    }
                    break;
                }
            }
        }
        return totalCost;
    }

    public void saveProduct(Product product) {
        executorService.execute(() -> {
            product.setUserId(userId);
            // In a real scenario, you would insert the product and then map its ID to save the Set of ProductIngredients
            productDao.insert(product); 
        });
    }

    public void deleteProduct(Product product) {
        executorService.execute(() -> {
            product.setStock(0.0);
            productDao.update(product);
        });
    }
}
