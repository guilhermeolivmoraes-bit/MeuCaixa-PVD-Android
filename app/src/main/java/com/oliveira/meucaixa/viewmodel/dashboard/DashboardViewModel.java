package com.oliveira.meucaixa.viewmodel.dashboard;

import com.oliveira.meucaixa.model.repository.AppDatabase;
import com.oliveira.meucaixa.model.dao.SaleDao;
import com.oliveira.meucaixa.model.entity.Sale;
import com.oliveira.meucaixa.model.manager.SessionManager;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final SaleDao saleDao;
    private final long userId;

    public DashboardViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        saleDao = db.saleDao();
        
        SessionManager sessionManager = new SessionManager(application);
        userId = sessionManager.getLoggedInUserId();
    }

    public LiveData<List<Sale>> getSalesForDay(long startOfDay, long endOfDay) {
        return saleDao.getSalesForDay(userId, startOfDay, endOfDay);
    }
}
