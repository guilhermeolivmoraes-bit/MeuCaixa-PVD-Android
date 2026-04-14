package com.oliveira.meucaixa.ui.sales;

import com.oliveira.meucaixa.data.local.AppDatabase;
import com.oliveira.meucaixa.data.local.ProductDao;
import com.oliveira.meucaixa.data.model.Product;
import com.oliveira.meucaixa.data.model.SaleItem;
import com.oliveira.meucaixa.data.repository.SaleRepository;
import com.oliveira.meucaixa.utils.SessionManager;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewSaleViewModel extends AndroidViewModel {
    private final ProductDao productDao;
    private final SaleRepository saleRepository;
    private final long userId;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    public final LiveData<List<Product>> searchResults;

    // O(1) performance Map for Cart Items (Key: productId)
    private final Map<Long, SaleItem> cartMap = new HashMap<>();

    public NewSaleViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        this.productDao = db.productDao();
        this.saleRepository = new SaleRepository(application);

        SessionManager sessionManager = new SessionManager(application);
        this.userId = sessionManager.getLoggedInUserId();

        searchResults = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return productDao.getAll(userId);
            }
            return productDao.searchByName(userId, "%" + query + "%");
        });
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void addItemToCart(Product product, double quantity) {
        long productId = product.getId();
        if (cartMap.containsKey(productId)) {
            SaleItem existingItem = cartMap.get(productId);
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
            }
        } else {
            SaleItem newItem = new SaleItem(0L, productId, product.getName(), product.getPrice(), 0.0, quantity);
            cartMap.put(productId, newItem);
        }
    }

    public void removeItemFromCart(long productId) {
        cartMap.remove(productId);
    }

    public void updateItemQuantity(long productId, double newQuantity) {
        if (cartMap.containsKey(productId)) {
            SaleItem item = cartMap.get(productId);
            if (item != null) {
                if (newQuantity <= 0) {
                    cartMap.remove(productId);
                } else {
                    item.setQuantity(newQuantity);
                }
            }
        }
    }

    public List<SaleItem> getCartItemsAsList() {
        return new ArrayList<>(cartMap.values());
    }

    public LiveData<Product> getProductById(long productId) {
        return productDao.getById(productId, userId);
    }

    public void saveCompleteSale(double totalValue, double totalCost) {
        // Obter de forma síncrona/segura os itens ANTES de pular para a thread de background
        List<SaleItem> frozenCartItems = getCartItemsAsList();
        
        saleRepository.processCheckoutAsync(userId, totalValue, totalCost, frozenCartItems, () -> {
            cartMap.clear();
            Log.d("CheckoutFlow", "Carrinho limpo após Checkout de Sucesso.");
        });
    }
}
