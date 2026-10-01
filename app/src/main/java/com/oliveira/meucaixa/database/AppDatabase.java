package com.oliveira.meucaixa.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.oliveira.meucaixa.models.User;
import com.oliveira.meucaixa.models.Product;
import com.oliveira.meucaixa.models.Sale;
import com.oliveira.meucaixa.models.SaleItem;

import com.oliveira.meucaixa.models.Ingredient;
import com.oliveira.meucaixa.models.ProductIngredient;

@Database(entities = {User.class, Product.class, Sale.class, SaleItem.class, Ingredient.class, ProductIngredient.class}, version = 12, exportSchema = false)
@TypeConverters({Converters.class})
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

    static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // PRODUCTS: price and costPrice to TEXT
            database.execSQL("CREATE TABLE IF NOT EXISTS `products_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `user_id` INTEGER NOT NULL, `product_name` TEXT, `product_price` TEXT, `product_stock` REAL NOT NULL, `unit_type` TEXT, `is_own_production` INTEGER NOT NULL, `cost_price` TEXT, FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("INSERT INTO `products_new` (`id`, `user_id`, `product_name`, `product_price`, `product_stock`, `unit_type`, `is_own_production`, `cost_price`) SELECT `id`, `user_id`, `product_name`, CAST(`product_price` AS TEXT), `product_stock`, `unit_type`, `is_own_production`, CAST(`cost_price` AS TEXT) FROM `products`");
            database.execSQL("DROP TABLE `products`");
            database.execSQL("ALTER TABLE `products_new` RENAME TO `products`");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_products_user_id` ON `products` (`user_id`)");

            // SALES: totalPrice and totalCost to TEXT
            database.execSQL("CREATE TABLE IF NOT EXISTS `sales_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `user_id` INTEGER NOT NULL, `date` INTEGER NOT NULL, `totalPrice` TEXT, `totalCost` TEXT, FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("INSERT INTO `sales_new` (`id`, `user_id`, `date`, `totalPrice`, `totalCost`) SELECT `id`, `user_id`, `date`, CAST(`totalPrice` AS TEXT), CAST(`totalCost` AS TEXT) FROM `sales`");
            database.execSQL("DROP TABLE `sales`");
            database.execSQL("ALTER TABLE `sales_new` RENAME TO `sales`");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_user_id` ON `sales` (`user_id`)");

            // SALE_ITEMS: productPrice and costPrice to TEXT
            database.execSQL("CREATE TABLE IF NOT EXISTS `sale_items_new` (`saleId` INTEGER NOT NULL, `productId` INTEGER NOT NULL, `productName` TEXT, `productPrice` TEXT, `costPrice` TEXT, `quantity` REAL NOT NULL, PRIMARY KEY(`saleId`, `productId`), FOREIGN KEY(`saleId`) REFERENCES `sales`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )");
            database.execSQL("INSERT INTO `sale_items_new` (`saleId`, `productId`, `productName`, `productPrice`, `costPrice`, `quantity`) SELECT `saleId`, `productId`, `productName`, CAST(`productPrice` AS TEXT), CAST(`costPrice` AS TEXT), `quantity` FROM `sale_items`");
            database.execSQL("DROP TABLE `sale_items`");
            database.execSQL("ALTER TABLE `sale_items_new` RENAME TO `sale_items`");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_sale_items_saleId` ON `sale_items` (`saleId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_sale_items_productId` ON `sale_items` (`productId`)");

            // INGREDIENTS: packagePrice to TEXT
            database.execSQL("CREATE TABLE IF NOT EXISTS `ingredients_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `user_id` INTEGER NOT NULL, `name` TEXT, `packagePrice` TEXT, `packageQuantity` REAL NOT NULL, `unitOfMeasure` TEXT, `current_stock` REAL NOT NULL, FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("INSERT INTO `ingredients_new` (`id`, `user_id`, `name`, `packagePrice`, `packageQuantity`, `unitOfMeasure`, `current_stock`) SELECT `id`, `user_id`, `name`, CAST(`packagePrice` AS TEXT), `packageQuantity`, `unitOfMeasure`, `current_stock` FROM `ingredients`");
            database.execSQL("DROP TABLE `ingredients`");
            database.execSQL("ALTER TABLE `ingredients_new` RENAME TO `ingredients`");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ingredients_user_id` ON `ingredients` (`user_id`)");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "meu_caixa_database")
                            .addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
