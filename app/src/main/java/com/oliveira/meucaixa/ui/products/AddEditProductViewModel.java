package com.oliveira.meucaixa.ui.products;

import com.oliveira.meucaixa.data.local.AppDatabase;
import com.oliveira.meucaixa.data.local.ProductDao;
import com.oliveira.meucaixa.data.model.Product;
import com.oliveira.meucaixa.utils.SessionManager;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditProductViewModel extends AndroidViewModel {

    private final ProductDao productDao;
    private final ExecutorService executorService;
    private final long userId;

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

    public void saveProduct(Product product) {
        executorService.execute(() -> {
            product.setUserId(userId);
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
