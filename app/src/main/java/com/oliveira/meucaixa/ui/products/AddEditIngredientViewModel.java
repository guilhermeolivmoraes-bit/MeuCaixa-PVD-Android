package com.oliveira.meucaixa.ui.products;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.oliveira.meucaixa.database.AppDatabase;
import com.oliveira.meucaixa.database.IngredientDao;
import com.oliveira.meucaixa.models.Ingredient;
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

    private final MutableLiveData<Ingredient> ingredientLiveData = new MutableLiveData<>();

    public LiveData<Ingredient> getIngredient() {
        return ingredientLiveData;
    }

    public void fetchIngredient(long id) {
        executorService.execute(() -> {
            Ingredient ingredient = ingredientDao.getIngredientById(id);
            if (ingredient != null) {
                ingredientLiveData.postValue(ingredient);
            }
        });
    }

    public void saveIngredient(Ingredient currentIngredient, String name, String priceText, String quantityText, String currentStockText) {
        if (TextUtils.isEmpty(name)) {
            saveErrorEvent.setValue("O nome do insumo não pode ser vazio.");
            return;
        }

        double price = 0.0;
        double quantity = 0.0;
        double currentStock = 0.0;

        try {
            String cleanPrice = priceText.replaceAll("[^\\d]", "");
            if (!cleanPrice.isEmpty()) {
                price = Double.parseDouble(cleanPrice) / 100.0;
            }

            String cleanQuantity = quantityText.replaceAll("[^\\d]", "");
            if (!cleanQuantity.isEmpty()) {
                quantity = Double.parseDouble(cleanQuantity) / 1000.0;
            }

            String cleanCurrentStock = currentStockText.replaceAll("[^\\d]", "");
            if (!cleanCurrentStock.isEmpty()) {
                currentStock = Double.parseDouble(cleanCurrentStock) / 1000.0;
            }
        } catch (NumberFormatException e) {
            saveErrorEvent.setValue("Formato numérico inválido.");
            return;
        }

        Ingredient ingredient = currentIngredient != null ? currentIngredient : new Ingredient();
        ingredient.setUserId(userId);
        ingredient.setName(name);
        ingredient.setPackagePrice(price);
        ingredient.setPackageQuantity(quantity);
        ingredient.setCurrentStock(currentStock);
        // By default, assuming "Kg/g" input maps to a standard unit, e.g., "Kg" for the base calculation context
        ingredient.setUnitOfMeasure("Kg/g");

        executorService.execute(() -> {
            try {
                if (currentIngredient == null) {
                    ingredientDao.insert(ingredient);
                } else {
                    ingredientDao.update(ingredient);
                }
                saveSuccessEvent.postValue(true);
            } catch (Exception e) {
                saveErrorEvent.postValue("Erro ao salvar no banco de dados.");
            }
        });
    }

    public void deleteIngredient(Ingredient currentIngredient) {
        if (currentIngredient == null) return;
        executorService.execute(() -> {
            try {
                ingredientDao.delete(currentIngredient);
                saveSuccessEvent.postValue(true);
            } catch (Exception e) {
                saveErrorEvent.postValue("Erro ao deletar do banco de dados.");
            }
        });
    }
}
