package com.oliveira.meucaixa.ui.sales;

import com.oliveira.meucaixa.data.local.AppDatabase;
import com.oliveira.meucaixa.data.local.ProductDao;
import com.oliveira.meucaixa.data.local.SaleDao;
import com.oliveira.meucaixa.data.local.IngredientDao;
import com.oliveira.meucaixa.data.local.ProductIngredientDao;
import com.oliveira.meucaixa.data.model.Ingredient;
import com.oliveira.meucaixa.data.model.Product;
import com.oliveira.meucaixa.data.model.ProductIngredient;
import com.oliveira.meucaixa.data.model.Sale;
import com.oliveira.meucaixa.data.model.SaleItem;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewSaleViewModel extends AndroidViewModel {
    private final ProductDao productDao;
    private final SaleDao saleDao;
    private final IngredientDao ingredientDao;
    private final ProductIngredientDao productIngredientDao;
    private final ExecutorService databaseExecutor;
    private final long userId;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    public final LiveData<List<Product>> searchResults;

    // O(1) performance Map for Cart Items (Key: productId)
    private final Map<Long, SaleItem> cartMap = new HashMap<>();

    public NewSaleViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        this.productDao = db.productDao();
        this.saleDao = db.saleDao();
        this.ingredientDao = db.ingredientDao();
        this.productIngredientDao = db.productIngredientDao();
        this.databaseExecutor = Executors.newSingleThreadExecutor();

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
        databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplication());
            db.runInTransaction(() -> {
                Log.d("Checkout", "Iniciando transação de Checkout. Total de Itens: " + getCartItemsAsList().size());
                Sale newSale = new Sale();
                newSale.setUserId(userId);
                newSale.setTotalPrice(totalValue);
                newSale.setDate(System.currentTimeMillis());

                // 1. Chame saleDao.insertSale(sale) e pegue o saleId.
                long saleId = saleDao.insertSale(newSale);
                Log.d("Checkout", "Sale persistida com ID: " + saleId);

                List<SaleItem> itemsToSave = getCartItemsAsList();
                List<SaleItem> finalItemsToSave = new ArrayList<>();

                // 2. Itere sobre os itens do carrinho:
                for (SaleItem item : itemsToSave) {
                    Product product = productDao.getByIdSynchronous(item.getProductId(), userId);
                    double itemUnitCost = 0.0;
                    
                    if (product != null) {
                        if (!product.isOwnProduction()) {
                            itemUnitCost += product.getCostPrice();
                        }
                        List<ProductIngredient> recipe = productIngredientDao.getIngredientsForProductSynchronous(product.getId());
                        if (recipe != null) {
                            for (ProductIngredient pi : recipe) {
                                Ingredient ingredient = ingredientDao.getIngredientById(pi.getIngredientId());
                                if (ingredient != null && ingredient.getPackageQuantity() > 0) {
                                    double unitIngredientCost = ingredient.getPackagePrice() / ingredient.getPackageQuantity();
                                    itemUnitCost += unitIngredientCost * pi.getQuantityUsed();
                                }
                            }
                        }
                    }

                    // a. Crie o SaleItem (passando o costPrice atual do produto/receita).
                    SaleItem finalItem = new SaleItem(saleId, item.getProductId(), item.getProductName(), item.getProductPrice(), itemUnitCost, item.getQuantity());
                    finalItemsToSave.add(finalItem);

                    if (product != null) {
                        Log.d("Checkout", "Processando item: " + product.getName() + " | Venda: " + finalItem.getQuantity() + " | OwnProduction: " + product.isOwnProduction());
                        
                        // c. Busque o Product no ProductDao, reduza o stock e dê update.
                        double newStock = product.getStock() - finalItem.getQuantity();
                        if (newStock < 0.001) newStock = 0.0; // Evita resíduos double
                        product.setStock(Math.max(0, newStock));
                        productDao.update(product);
                        Log.d("Checkout", "Dedução no Produto Físico. Novo saldo: " + newStock);
                        
                        // d. Se o produto for isOwnProduction (e embalagens associadas)
                        List<ProductIngredient> recipe = productIngredientDao.getIngredientsForProductSynchronous(product.getId());
                        Log.d("Checkout", "Receita deste produto possui " + (recipe != null ? recipe.size() : 0) + " insumos.");
                        if (recipe != null) {
                            for (ProductIngredient pi : recipe) {
                                Ingredient ingredient = ingredientDao.getIngredientById(pi.getIngredientId());
                                if (ingredient != null) {
                                    double deduction = pi.getQuantityUsed() * finalItem.getQuantity();
                                    double newIngredientStock = ingredient.getCurrentStock() - deduction;
                                    if (newIngredientStock < 0.001) newIngredientStock = 0.0; // Evita resíduos double
                                    ingredient.setCurrentStock(Math.max(0, newIngredientStock));
                                    ingredientDao.update(ingredient);
                                    Log.d("Checkout", "Dedução no Insumo: " + ingredient.getName() + " | Abatido: " + deduction + " | Novo Saldo: " + newIngredientStock);
                                }
                            }
                        }
                    } else {
                        Log.w("Checkout", "Produto do carrinho não encontrado no banco: " + item.getProductId());
                    }
                }
                
                // b. Chame saleDao.insertSaleItems().
                saleDao.insertSaleItems(finalItemsToSave);
                Log.d("Checkout", "Transação de Checkout concluída com SUCESSO!");
            });
            // Clear cart map after save (outside transaction to update UI properly if observed)
            cartMap.clear();
        });
    }
}
