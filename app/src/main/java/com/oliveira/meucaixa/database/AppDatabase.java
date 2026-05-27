package com.oliveira.meucaixa.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.oliveira.meucaixa.models.User;
import com.oliveira.meucaixa.models.Product;
import com.oliveira.meucaixa.models.Sale;
import com.oliveira.meucaixa.models.SaleItem;

import com.oliveira.meucaixa.models.Ingredient;
import com.oliveira.meucaixa.models.ProductIngredient;

@Database(entities = {User.class, Product.class, Sale.class, SaleItem.class, Ingredient.class, ProductIngredient.class}, version = 11, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract SaleDao saleDao();
    public abstract IngredientDao ingredientDao();
    public abstract ProductIngredientDao productIngredientDao();
    public abstract ReportsDao reportsDao();

    private static volatile AppDatabase INSTANCE;

    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE ingredients ADD COLUMN current_stock REAL NOT NULL DEFAULT 0.0");
        }
    };
    
    static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE sale_items ADD COLUMN costPrice REAL NOT NULL DEFAULT 0.0");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "meu_caixa_database")
                            .addMigrations(MIGRATION_9_10, MIGRATION_10_11)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
