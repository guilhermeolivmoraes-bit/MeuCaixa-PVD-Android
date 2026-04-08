package com.oliveira.meucaixa.ui.products;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.oliveira.meucaixa.data.local.AppDatabase;
import com.oliveira.meucaixa.data.local.IngredientDao;
import com.oliveira.meucaixa.data.model.Ingredient;
import com.oliveira.meucaixa.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditIngredientViewModel extends AndroidViewModel {

    private final IngredientDao ingredientDao;
    private final ExecutorService executorService;
    private final long userId;
    
    private final MutableLiveData<Boolean> saveSuccessEvent = new MutableLiveData<>();
    private final MutableLiveData<String> saveErrorEvent = new MutableLiveData<>();

    public AddEditIngredientViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        ingredientDao = db.ingredientDao();
        executorService = Executors.newSingleThreadExecutor();
        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getLoggedInUserId();
    }

    public LiveData<Boolean> getSaveSuccessEvent() {
        return saveSuccessEvent;
    }

    public LiveData<String> getSaveErrorEvent() {
        return saveErrorEvent;
    }

    public void saveIngredient(String name, String priceText, String quantityText) {
        if (TextUtils.isEmpty(name)) {
            saveErrorEvent.setValue("Ingredient name cannot be empty.");
            return;
        }

        double price = 0.0;
        double quantity = 0.0;

        try {
            String cleanPrice = priceText.replaceAll("[^\\d]", "");
            if (!cleanPrice.isEmpty()) {
                price = Double.parseDouble(cleanPrice) / 100.0;
            }

            String cleanQuantity = quantityText.replaceAll("[^\\d]", "");
            if (!cleanQuantity.isEmpty()) {
                quantity = Double.parseDouble(cleanQuantity) / 1000.0;
            }
        } catch (NumberFormatException e) {
            saveErrorEvent.setValue("Invalid number format.");
            return;
        }

        if (price <= 0) {
            saveErrorEvent.setValue("Price must be greater than zero.");
            return;
        }

        if (quantity <= 0) {
            saveErrorEvent.setValue("Quantity must be greater than zero.");
            return;
        }

        Ingredient ingredient = new Ingredient();
        ingredient.setUserId(userId);
        ingredient.setName(name);
        ingredient.setPackagePrice(price);
        ingredient.setPackageQuantity(quantity);
        // By default, assuming "Kg/g" input maps to a standard unit, e.g., "Kg" for the base calculation context
        ingredient.setUnitOfMeasure("Kg/g");

        executorService.execute(() -> {
            try {
                ingredientDao.insert(ingredient);
                saveSuccessEvent.postValue(true);
            } catch (Exception e) {
                saveErrorEvent.postValue("Error saving to database.");
            }
        });
    }
}
