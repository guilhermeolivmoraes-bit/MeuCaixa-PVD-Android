package com.oliveira.meucaixa.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.oliveira.meucaixa.data.model.User;
import com.oliveira.meucaixa.data.model.Product;
import com.oliveira.meucaixa.data.model.Sale;
import com.oliveira.meucaixa.data.model.SaleItem;

import com.oliveira.meucaixa.data.model.Ingredient;
import com.oliveira.meucaixa.data.model.ProductIngredient;

@Database(entities = {User.class, Product.class, Sale.class, SaleItem.class, Ingredient.class, ProductIngredient.class}, version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract SaleDao saleDao();
    public abstract IngredientDao ingredientDao();
    public abstract ProductIngredientDao productIngredientDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "meu_caixa_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
