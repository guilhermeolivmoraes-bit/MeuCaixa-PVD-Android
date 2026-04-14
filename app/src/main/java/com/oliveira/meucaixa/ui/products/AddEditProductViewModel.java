package com.oliveira.meucaixa.ui.products;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.oliveira.meucaixa.data.model.Ingredient;
import com.oliveira.meucaixa.data.model.Product;
import com.oliveira.meucaixa.data.model.ProductIngredient;
import com.oliveira.meucaixa.data.repository.IngredientRepository;
import com.oliveira.meucaixa.data.repository.ProductRepository;
import com.oliveira.meucaixa.utils.SessionManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddEditProductViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final long userId;

    // In-memory Set collection to guarantee uniqueness of ingredients in the recipe
    private final Set<ProductIngredient> recipeIngredients = new HashSet<>();
    private final MutableLiveData<Double> totalRecipeCost = new MutableLiveData<>(0.0);
    private List<Ingredient> allDatabaseIngredients;

    private final Observer<List<Ingredient>> ingredientsObserver = ingredients -> {
        allDatabaseIngredients = ingredients;
        recalculateTotalCost();
    };
    private final LiveData<List<Ingredient>> allIngredientsLiveData;

    public AddEditProductViewModel(Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        ingredientRepository = new IngredientRepository(application);

        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getLoggedInUserId();

        allIngredientsLiveData = ingredientRepository.getAllIngredients();
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
        return productRepository.getProductById(productId, userId);
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

    public void confirmRecipe(Product product) {
        product.setUserId(userId);
        productRepository.saveProductWithIngredients(product, recipeIngredients);
    }

    public void updateProduct(Product product) {
        productRepository.updateProduct(product);
    }

    public void deleteProduct(Product product) {
        product.setStock(0.0);
        productRepository.updateProduct(product);
    }
}
