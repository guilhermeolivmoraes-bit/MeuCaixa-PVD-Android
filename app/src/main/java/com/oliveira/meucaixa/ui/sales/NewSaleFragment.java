package com.oliveira.meucaixa.ui.sales;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.models.Product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class NewSaleFragment extends Fragment {

    private NewSaleViewModel newSaleViewModel;
    private NavController navController;

    private EditText editTextSearch;
    private RecyclerView cartRecyclerView;
    private LinearLayout emptyCartView;

    private CartAdapter cartAdapter;

    private TextView textTotalValue;
    private Button buttonFinalizeSale;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_sale, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        newSaleViewModel = new ViewModelProvider(this).get(NewSaleViewModel.class);

        bindViews(view);
        setupRecyclerViewCart();
        setupFakeSearch();
        setupFragmentResultListener();
        
        updateCartUI();
    }

    private void bindViews(View view) {
        editTextSearch = view.findViewById(R.id.edit_text_search);
        cartRecyclerView = view.findViewById(R.id.recycler_view_cart);
        emptyCartView = view.findViewById(R.id.empty_cart_view);
        textTotalValue = view.findViewById(R.id.text_total_value);
        buttonFinalizeSale = view.findViewById(R.id.button_finalize_sale);

        ImageButton buttonBack = view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> navController.popBackStack());

        buttonFinalizeSale.setOnClickListener(v -> finalizeSale());
    }

    private void setupRecyclerViewCart() {
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(newSaleViewModel.getCartItemsAsList(), new CartAdapter.OnSaleItemChangeListener() {
            @Override
            public void onItemQuantityChanged() {
                // CartAdapter modifies the item object directly via views (temporary setup).
                // We just refresh UI.
                updateCartUI();
            }

            @Override
            public void onItemDeleted(int position) {
                List<com.oliveira.meucaixa.models.SaleItem> items = newSaleViewModel.getCartItemsAsList();
                if (position >= 0 && position < items.size()) {
                    long productId = items.get(position).getProductId();
                    newSaleViewModel.removeItemFromCart(productId);
                }
                updateCartUI();
            }
        });
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void setupFakeSearch() {
        editTextSearch.setOnClickListener(v -> {
            navController.navigate(R.id.action_newSale_to_productSearch);
        });
    }

    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener("sale_product_req", getViewLifecycleOwner(), (requestKey, result) -> {
            long productId = result.getLong("productId", -1L);
            if (productId != -1L) {
                // Fetch the product from LiveData and add to cart when observed. 
                // Since this is a temporary observing just to add, we observe it once:
                newSaleViewModel.getProductById(productId).observe(getViewLifecycleOwner(), product -> {
                    if (product != null) {
                        addProductToCart(product);
                        // Prevent observing continuously
                        newSaleViewModel.getProductById(productId).removeObservers(getViewLifecycleOwner());
                    }
                });
            }
        });
    }

    private void addProductToCart(Product product) {
        if (product.getStock() > 0) {
            newSaleViewModel.addItemToCart(product, 1.0);
            updateCartUI();
        } else {
            Toast.makeText(getContext(), "Product out of stock", Toast.LENGTH_SHORT).show();
        }
    }

    private void finalizeSale() {
        List<com.oliveira.meucaixa.models.SaleItem> items = newSaleViewModel.getCartItemsAsList();
        if (items.isEmpty()) {
            Toast.makeText(getContext(), "O carrinho está vazio", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalValue = 0;
        double totalCost = 0; // Assuming we would calculate this based on Product's costPrice.
        
        // As a simplification due to the transition, we calculate total price here.
        for (com.oliveira.meucaixa.models.SaleItem item : items) {
            totalValue += (item.getProductPrice() * item.getQuantity());
            // Optionally, accumulate total cost if available.
        }

        newSaleViewModel.saveCompleteSale(totalValue, totalCost);
        Toast.makeText(getContext(), "Venda salva com sucesso!", Toast.LENGTH_SHORT).show();
        navController.popBackStack();
    }

    private void updateCartUI() {
        List<com.oliveira.meucaixa.models.SaleItem> currentItems = newSaleViewModel.getCartItemsAsList();
        
        cartAdapter = new CartAdapter(currentItems, new CartAdapter.OnSaleItemChangeListener() {
            @Override
            public void onItemQuantityChanged() {
                updateCartUI();
            }

            @Override
            public void onItemDeleted(int position) {
                if (position >= 0 && position < currentItems.size()) {
                    long productId = currentItems.get(position).getProductId();
                    newSaleViewModel.removeItemFromCart(productId);
                }
                updateCartUI();
            }
        });
        cartRecyclerView.setAdapter(cartAdapter);

        boolean isCartEmpty = currentItems.isEmpty();
        emptyCartView.setVisibility(isCartEmpty ? View.VISIBLE : View.GONE);
        cartRecyclerView.setVisibility(isCartEmpty ? View.GONE : View.VISIBLE);

        buttonFinalizeSale.setEnabled(!isCartEmpty);

        double total = 0;
        for (com.oliveira.meucaixa.models.SaleItem item : currentItems) {
            total += (item.getProductPrice() * item.getQuantity());
        }
        textTotalValue.setText(String.format(Locale.getDefault(), "R$ %.2f", total));
    }
}
