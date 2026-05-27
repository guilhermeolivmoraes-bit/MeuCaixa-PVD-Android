package com.oliveira.meucaixa.ui.products;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.oliveira.meucaixa.models.Ingredient;
import com.oliveira.meucaixa.models.Product;
import com.oliveira.meucaixa.models.ProductIngredient;
import com.oliveira.meucaixa.services.IngredientService;
import com.oliveira.meucaixa.services.ProductService;
import com.oliveira.meucaixa.utils.SessionManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddEditProductViewModel extends AndroidViewModel {

    private final ProductService productService;
    private final IngredientService ingredientService;
    private final long userId;

    // In-memory Set collection to guarantee uniqueness of ingredients in the recipe
    private final Set<ProductIngredient> recipeIngredients = new HashSet<>();
    private final MutableLiveData<Double> totalRecipeCost = new MutableLiveData<>(0.0);
    private List<Ingredient> allDatabaseIngredients;
    private boolean isRecipeLoaded = false;

    private final Observer<List<Ingredient>> ingredientsObserver = ingredients -> {
        allDatabaseIngredients = ingredients;
        recalculateTotalCost();
    };
    private final LiveData<List<Ingredient>> allIngredientsLiveData;

    public AddEditProductViewModel(Application application) {
        super(application);
        productService = new ProductService(application);
        ingredientService = new IngredientService(application);

        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getLoggedInUserId();

        allIngredientsLiveData = ingredientService.getAllIngredients(userId);
        allIngredientsLiveData.observeForever(ingredientsObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        allIngredientsLiveData.removeObserver(ingredientsObserver);
    }

    public LiveData<List<Ingredient>> getAllIngredients() {
        return allIngredientsLiveData;
    }

    public LiveData<Double> getTotalRecipeCost() {
        return totalRecipeCost;
    }

    public LiveData<Product> getProductById(long productId) {
        return productService.getProductById(productId, userId);
    }

    public LiveData<List<ProductIngredient>> getIngredientsForProduct(long productId) {
        return productService.getIngredientsForProduct(productId);
    }

    public void loadRecipe(List<ProductIngredient> existing) {
        if (!isRecipeLoaded) {
            recipeIngredients.clear();
            if (existing != null) {
                recipeIngredients.addAll(existing);
            }
            recalculateTotalCost();
            isRecipeLoaded = true;
        }
    }

    public boolean addIngredientToRecipe(ProductIngredient ingredient) {
        boolean added = recipeIngredients.add(ingredient);
        if (added) {
            recalculateTotalCost();
        }
        return added;
    }

    public void removeIngredientFromRecipe(ProductIngredient ingredient) {
        boolean removed = recipeIngredients.remove(ingredient);
        if (removed) {
            recalculateTotalCost();
        }
    }

    public Set<ProductIngredient> getRecipeIngredients() {
        return recipeIngredients;
    }

    public void clearRecipeIngredients() {
        recipeIngredients.clear();
        isRecipeLoaded = false;
        recalculateTotalCost();
    }

    public void recalculateTotalCost() {
        if (allDatabaseIngredients == null || allDatabaseIngredients.isEmpty()) {
            totalRecipeCost.postValue(0.0);
            return;
        }

        double totalCost = 0.0;
        for (ProductIngredient productIngredient : recipeIngredients) {
            for (Ingredient dbIngredient : allDatabaseIngredients) {
                if (productIngredient.getIngredientId() == dbIngredient.getId()) {
                    totalCost += calculateIngredientCost(dbIngredient, productIngredient.getQuantityUsed());
                    break;
                }
            }
        }
        totalRecipeCost.postValue(totalCost);
    }

    public double calculateIngredientCost(Ingredient ingredient, double quantityUsed) {
        if (ingredient == null || ingredient.getPackageQuantity() <= 0) return 0.0;
        double unitPrice = ingredient.getPackagePrice() / ingredient.getPackageQuantity();
        return unitPrice * quantityUsed;
    }

    public void saveProduct(Product product) {
        product.setUserId(userId);
        if (product.getId() == 0) {
            productService.saveProductWithIngredients(product, recipeIngredients);
        } else {
            productService.updateProductWithIngredients(product, recipeIngredients);
        }
    }

    public void updateProduct(Product product) {
        productService.updateProduct(product);
    }

    public void deleteProduct(Product product) {
        product.setStock(0.0);
        productService.updateProduct(product);
    }
}
