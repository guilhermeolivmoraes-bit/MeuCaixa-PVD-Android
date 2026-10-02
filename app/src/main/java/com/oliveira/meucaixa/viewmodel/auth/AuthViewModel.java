package com.oliveira.meucaixa.viewmodel.auth;

import com.oliveira.meucaixa.model.repository.AppDatabase;
import com.oliveira.meucaixa.model.dao.UserDao;
import com.oliveira.meucaixa.model.entity.User;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthViewModel extends AndroidViewModel {

    private final UserDao userDao;
    private final SharedPreferences sharedPreferences;
    private final ExecutorService executorService;

    public AuthViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<User> getLastUser() {
        return userDao.getLastUser();
    }

    // CORREÇÃO: O método signUp agora aceita um callback
    public void signUp(String name, String birthDate, Runnable onSignUpFinished) {
        executorService.execute(() -> {
            User user = new User();
            user.setFullName(name);
            user.setBirthDate(birthDate);
            long userId = userDao.insert(user);
            saveLastUserId(userId);

            // Executa o callback na thread principal para notificar a UI
            if (onSignUpFinished != null) {
                new Handler(Looper.getMainLooper()).post(onSignUpFinished);
            }
        });
    }

    public void login(String name, String birthDate, LoginCallback callback) {
        executorService.execute(() -> {
            User user = userDao.findUser(name, birthDate);
            if (user != null) {
                saveLastUserId(user.getId());
                // Executa o callback de sucesso na thread principal
                new Handler(Looper.getMainLooper()).post(callback::onSuccess);
            } else {
                // Executa o callback de falha na thread principal
                new Handler(Looper.getMainLooper()).post(callback::onFailure);
            }
        });
    }

    private void saveLastUserId(long userId) {
        sharedPreferences.edit().putLong("last_user_id", userId).apply();
    }

    public interface LoginCallback {
        void onSuccess();
        void onFailure();
    }
}
