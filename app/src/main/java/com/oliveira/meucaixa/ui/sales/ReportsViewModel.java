package com.oliveira.meucaixa.ui.sales;

import com.oliveira.meucaixa.data.local.AppDatabase;
import com.oliveira.meucaixa.data.local.SaleDao;
import com.oliveira.meucaixa.data.model.Sale;
import com.oliveira.meucaixa.utils.SessionManager;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class ReportsViewModel extends AndroidViewModel {

    private final SaleDao saleDao;
    private final long userId;

    public ReportsViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        saleDao = db.saleDao();

        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getLoggedInUserId();
    }

    public LiveData<List<Sale>> getSalesForDay(long startOfDay, long endOfDay) {
        return saleDao.getSalesForDay(userId, startOfDay, endOfDay);
    }

    public LiveData<List<Sale>> getSalesForMonth(long startOfMonth, long endOfMonth) {
        return saleDao.getSalesForMonth(userId, startOfMonth, endOfMonth);
    }
}
