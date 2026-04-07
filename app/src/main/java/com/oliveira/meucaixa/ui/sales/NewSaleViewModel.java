package com.oliveira.meucaixa.ui.sales;

import com.oliveira.meucaixa.data.local.AppDatabase;
import com.oliveira.meucaixa.data.local.ProductDao;
import com.oliveira.meucaixa.data.local.SaleDao;
import com.oliveira.meucaixa.data.model.Product;
import com.oliveira.meucaixa.data.model.Sale;
import com.oliveira.meucaixa.data.model.SaleItem;
import com.oliveira.meucaixa.utils.SessionManager;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewSaleViewModel extends AndroidViewModel {
    private final ProductDao productDao;
    private final SaleDao saleDao;
    private final ExecutorService databaseExecutor;
    private final long userId;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    public final LiveData<List<Product>> searchResults;

    public NewSaleViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        this.productDao = db.productDao();
        this.saleDao = db.saleDao();
        this.databaseExecutor = Executors.newSingleThreadExecutor();

        SessionManager sessionManager = new SessionManager(application);
        this.userId = sessionManager.getLoggedInUserId();

        searchResults = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return new MutableLiveData<>(new ArrayList<>());
            }
            return productDao.searchByName(userId, "%" + query + "%");
        });
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void saveCompleteSale(List<CartItem> cartItems, double totalValue) {
        databaseExecutor.execute(() -> {
            Sale newSale = new Sale();
            newSale.setUserId(userId);
            newSale.setTotalPrice(totalValue);
            newSale.setDate(System.currentTimeMillis());

            List<SaleItem> itemsToSave = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
                if (newSale.getProductName() == null) {
                    newSale.setProductName(cartItem.getProduct().getName());
                    newSale.setQuantity(cartItem.getQuantity());
                }
                itemsToSave.add(new SaleItem(0, cartItem.getProduct().getId(), cartItem.getProduct().getName(), cartItem.getProduct().getPrice(), cartItem.getQuantity()));
            }
            saleDao.saveCompleteSale(newSale, itemsToSave);

            for (CartItem item : cartItems) {
                Product product = item.getProduct();
                double newStock = product.getStock() - item.getQuantity();
                product.setStock(Math.max(0, newStock));
                productDao.update(product);
            }
        });
    }
}
