package com.oliveira.meucaixa.services;

import android.app.Application;
import android.util.Log;

import com.oliveira.meucaixa.database.AppDatabase;
import com.oliveira.meucaixa.database.IngredientDao;
import com.oliveira.meucaixa.database.ProductDao;
import com.oliveira.meucaixa.database.ProductIngredientDao;
import com.oliveira.meucaixa.database.SaleDao;
import com.oliveira.meucaixa.models.Ingredient;
import com.oliveira.meucaixa.models.Product;
import com.oliveira.meucaixa.models.ProductIngredient;
import com.oliveira.meucaixa.models.Sale;
import com.oliveira.meucaixa.models.SaleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SaleService {
    private final AppDatabase db;
    private final SaleDao saleDao;
    private final ProductDao productDao;
    private final IngredientDao ingredientDao;
    private final ProductIngredientDao productIngredientDao;
    private final ExecutorService executorService;

    public SaleService(Application application) {
        db = AppDatabase.getDatabase(application);
        saleDao = db.saleDao();
        productDao = db.productDao();
        ingredientDao = db.ingredientDao();
        productIngredientDao = db.productIngredientDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void processCheckoutAsync(long userId, double totalValue, double totalCost, List<SaleItem> frozenCartItems, Runnable onSuccess) {
        executorService.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    Log.d("CheckoutFlow", "=== INICIANDO TRANSAÇÃO DE CHECKOUT ===");
                    
                    Sale sale = new Sale();
                    sale.setUserId(userId);
                    sale.setTotalPrice(totalValue);
                    sale.setTotalCost(totalCost); 
                    sale.setDate(System.currentTimeMillis());

                    long saleId = saleDao.insertSale(sale);
                    Log.d("CheckoutFlow", "Cabeçalho da Venda persistido -> ID: " + saleId);

                    List<SaleItem> finalItemsToSave = new ArrayList<>();

                    for (SaleItem cartItem : frozenCartItems) {
                        Log.d("CheckoutFlow", "Processando Item Carrinho - Product ID: " + cartItem.getProductId());
                        
                        Product product = productDao.getByIdSynchronous(cartItem.getProductId(), userId);

                        if (product == null) {
                            Log.e("CheckoutFlow", "CRÍTICO: Produto do carrinho [" + cartItem.getProductId() + "] não achou o BD. Skip.");
                            continue;
                        }

                        double newStock = product.getStock() - cartItem.getQuantity();
                        if (newStock < 0.001) newStock = 0.0;
                        product.setStock(Math.max(0, newStock));
                        productDao.update(product);
                        Log.d("CheckoutFlow", "Dedução efetuada no Produto Físico (" + product.getName() + ") -> Saldo: " + newStock);

                        double itemUnitCost = (!product.isOwnProduction()) ? product.getCostPrice() : 0.0;

                        List<ProductIngredient> recipe = productIngredientDao.getIngredientsForProductSynchronous(product.getId());
                        if (recipe != null && !recipe.isEmpty()) {
                            Log.d("CheckoutFlow", product.getName() + " possui " + recipe.size() + " insumos na receita.");
                            for (ProductIngredient pi : recipe) {
                                Ingredient ingredient = ingredientDao.getIngredientById(pi.getIngredientId());
                                if (ingredient != null) {
                                    if (ingredient.getPackageQuantity() > 0) {
                                        double unitIngredientCost = ingredient.getPackagePrice() / ingredient.getPackageQuantity();
                                        itemUnitCost += unitIngredientCost * pi.getQuantityUsed();
                                    }

                                    double deduction = pi.getQuantityUsed() * cartItem.getQuantity();
                                    double newIngredientStock = ingredient.getCurrentStock() - deduction;
                                    if (newIngredientStock < 0.001) newIngredientStock = 0.0;
                                    ingredient.setCurrentStock(Math.max(0, newIngredientStock));
                                    ingredientDao.update(ingredient);
                                    
                                    Log.d("CheckoutFlow", "Abate no Insumo: " + ingredient.getName() + " | Qtd Abatida: " + deduction + " | Novo Saldo: " + newIngredientStock);
                                }
                            }
                        }

                        SaleItem finalItem = new SaleItem(saleId, cartItem.getProductId(), cartItem.getProductName(), cartItem.getProductPrice(), itemUnitCost, cartItem.getQuantity());
                        finalItemsToSave.add(finalItem);
                    }

                    saleDao.insertSaleItems(finalItemsToSave);
                    Log.d("CheckoutFlow", "=== TRANSAÇÃO FINALIZADA COM SUCESSO: Todos os itens persistidos! ===");
                });

                if (onSuccess != null) {
                    onSuccess.run();
                }
                
            } catch (Exception e) {
                Log.e("CheckoutFlow", "ERRO NO FLUXO DE VENDA. Ocorreu um Rollback!", e);
            }
        });
    }
}
