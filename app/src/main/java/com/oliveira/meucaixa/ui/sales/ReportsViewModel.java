package com.oliveira.meucaixa.ui.sales;

import com.oliveira.meucaixa.database.AppDatabase;
import com.oliveira.meucaixa.database.SaleDao;
import com.oliveira.meucaixa.models.Sale;
import com.oliveira.meucaixa.utils.SessionManager;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class ReportsViewModel extends AndroidViewModel {

    private final SaleDao saleDao;
    private final com.oliveira.meucaixa.database.ReportsDao reportsDao;
    private final long userId;

    public ReportsViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        saleDao = db.saleDao();
        reportsDao = db.reportsDao();

        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getLoggedInUserId();
    }

    public LiveData<List<Sale>> getSalesForDay(long startOfDay, long endOfDay) {
        return saleDao.getSalesForDay(userId, startOfDay, endOfDay);
    }

    public LiveData<List<Sale>> getSalesForMonth(long startOfMonth, long endOfMonth) {
        return saleDao.getSalesForMonth(userId, startOfMonth, endOfMonth);
    }

    public LiveData<Double> getNetProfitForDay(long startOfDay, long endOfDay) {
        return reportsDao.getNetProfitForPeriod(userId, startOfDay, endOfDay);
    }

    public LiveData<Double> getNetProfitForMonth(long startOfMonth, long endOfMonth) {
        return reportsDao.getNetProfitForPeriod(userId, startOfMonth, endOfMonth);
    }
}
