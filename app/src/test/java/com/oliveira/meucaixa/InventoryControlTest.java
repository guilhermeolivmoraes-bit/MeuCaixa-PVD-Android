package com.oliveira.meucaixa;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.oliveira.meucaixa.database.AppDatabase;
import com.oliveira.meucaixa.database.IngredientDao;
import com.oliveira.meucaixa.database.ProductDao;
import com.oliveira.meucaixa.database.ProductIngredientDao;
import com.oliveira.meucaixa.database.SaleDao;
import com.oliveira.meucaixa.models.Ingredient;
import com.oliveira.meucaixa.models.Product;
import com.oliveira.meucaixa.models.ProductIngredient;
import com.oliveira.meucaixa.models.SaleItem;
import com.oliveira.meucaixa.services.SaleService;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InventoryControlTest {

    private SaleService saleService;
    private AppDatabase mockDb;
    private SaleDao mockSaleDao;
    private ProductDao mockProductDao;
    private IngredientDao mockIngredientDao;
    private ProductIngredientDao mockProductIngredientDao;

    @Before
    public void setup() {
        mockDb = mock(AppDatabase.class);
        mockSaleDao = mock(SaleDao.class);
        mockProductDao = mock(ProductDao.class);
        mockIngredientDao = mock(IngredientDao.class);
        mockProductIngredientDao = mock(ProductIngredientDao.class);

        // Mock runInTransaction para executar imediatamente
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(mockDb).runInTransaction(any(Runnable.class));

        // Executor síncrono para testes
        ExecutorService syncExecutor = mock(ExecutorService.class);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(syncExecutor).execute(any(Runnable.class));

        saleService = new SaleService(
                mockDb, mockSaleDao, mockProductDao,
                mockIngredientDao, mockProductIngredientDao, syncExecutor
        );
    }

    @Test
    public void testSaleReducesProductStock() {
        // Setup de um produto normal
        long userId = 1L;
        long productId = 100L;
        
        Product product = new Product();
        product.setId(productId);
        product.setName("Refrigerante");
        product.setStock(10.0);
        product.setOwnProduction(false);

        when(mockProductDao.getByIdSynchronous(productId, userId)).thenReturn(product);
        when(mockProductIngredientDao.getIngredientsForProductSynchronous(productId)).thenReturn(Collections.emptyList());

        // Item do carrinho
        SaleItem cartItem = new SaleItem(0L, productId, "Refrigerante", 10.0, 5.0, 2.0);
        List<SaleItem> cartItems = Collections.singletonList(cartItem);

        // Executa o checkout
        saleService.processCheckoutAsync(userId, 20.0, 10.0, cartItems, () -> {});

        // Verifica se o estoque do produto diminuiu
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(mockProductDao).update(productCaptor.capture());

        Product updatedProduct = productCaptor.getValue();
        assertEquals(8.0, updatedProduct.getStock(), 0.001);
    }

    @Test
    public void testSaleReducesIngredientStockForOwnProduction() {
        // Setup de um produto de produção própria (Ex: Bolo)
        long userId = 1L;
        long productId = 200L;
        long ingredientId = 300L;

        Product product = new Product();
        product.setId(productId);
        product.setName("Bolo");
        product.setStock(5.0); // 5 bolos no estoque
        product.setOwnProduction(true);

        // Setup do insumo (Ex: Farinha)
        Ingredient farinha = new Ingredient();
        farinha.setId(ingredientId);
        farinha.setName("Farinha de Trigo");
        farinha.setPackageQuantity(1.0); // 1kg = 1.0
        farinha.setPackagePrice(5.0); // R$ 5.00
        farinha.setCurrentStock(2.0); // 2kg no estoque atual

        // Setup da receita (Usa 0.5kg de farinha por bolo)
        ProductIngredient recipeIngredient = new ProductIngredient(productId, ingredientId, 0.5);

        when(mockProductDao.getByIdSynchronous(productId, userId)).thenReturn(product);
        when(mockProductIngredientDao.getIngredientsForProductSynchronous(productId)).thenReturn(Collections.singletonList(recipeIngredient));
        when(mockIngredientDao.getIngredientById(ingredientId)).thenReturn(farinha);

        // Vendendo 2 bolos
        SaleItem cartItem = new SaleItem(0L, productId, "Bolo", 20.0, 0.0, 2.0);
        List<SaleItem> cartItems = Collections.singletonList(cartItem);

        // Executa o checkout
        saleService.processCheckoutAsync(userId, 40.0, 0.0, cartItems, () -> {});

        // 1. Verifica se deduziu o estoque do bolo (5 - 2 = 3)
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(mockProductDao).update(productCaptor.capture());
        assertEquals(3.0, productCaptor.getValue().getStock(), 0.001);

        // 2. Verifica se deduziu o estoque do ingrediente (2kg - (0.5kg * 2) = 1kg restante)
        ArgumentCaptor<Ingredient> ingredientCaptor = ArgumentCaptor.forClass(Ingredient.class);
        verify(mockIngredientDao).update(ingredientCaptor.capture());
        assertEquals(1.0, ingredientCaptor.getValue().getCurrentStock(), 0.001);
        
        // 3. Verifica se o custo calculado do item da venda bate com o custo do ingrediente
        // Farinha custa R$ 5,00 por 1kg. Usamos 0.5kg por bolo = Custo R$ 2,50 por bolo
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SaleItem>> saleItemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockSaleDao).insertSaleItems(saleItemsCaptor.capture());
        
        SaleItem savedSaleItem = saleItemsCaptor.getValue().get(0);
        assertEquals(2.50, savedSaleItem.getCostPrice(), 0.001);
    }
    
    @Test
    public void testProductStockDoesNotDropBelowZero() {
        long userId = 1L;
        long productId = 100L;
        
        Product product = new Product();
        product.setId(productId);
        product.setStock(1.0); // Apenas 1 no estoque
        
        when(mockProductDao.getByIdSynchronous(productId, userId)).thenReturn(product);
        
        SaleItem cartItem = new SaleItem(0L, productId, "Produto", 10.0, 5.0, 5.0);
        
        saleService.processCheckoutAsync(userId, 50.0, 20.0, Collections.singletonList(cartItem), () -> {});

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(mockProductDao).update(productCaptor.capture());

        // O estoque não pode ficar negativo, deve parar em 0
        assertEquals(0.0, productCaptor.getValue().getStock(), 0.001);
    }
}
