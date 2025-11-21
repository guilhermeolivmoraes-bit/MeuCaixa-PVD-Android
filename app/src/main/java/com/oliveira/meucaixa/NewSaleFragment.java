package com.oliveira.meucaixa;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewSaleFragment extends Fragment {

    private NewSaleViewModel newSaleViewModel;
    private NavController navController;

    private EditText editTextSearch;
    private RecyclerView searchResultsRecyclerView;
    private RecyclerView cartRecyclerView;
    private LinearLayout emptyCartView;

    private ProductSearchAdapter searchAdapter;
    private CartAdapter cartAdapter;

    private TextView textTotalValue;
    private Button buttonFinalizeSale;

    private final List<SaleItem> cartItems = new ArrayList<>();

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
        setupRecyclerViews();
        setupSearch();
        observeSearchResults();
        updateCartVisibility();
    }

    private void bindViews(View view) {
        editTextSearch = view.findViewById(R.id.edit_text_search);
        searchResultsRecyclerView = view.findViewById(R.id.recycler_view_search_results);
        cartRecyclerView = view.findViewById(R.id.recycler_view_cart);
        emptyCartView = view.findViewById(R.id.empty_cart_view);
        textTotalValue = view.findViewById(R.id.text_total_value);
        buttonFinalizeSale = view.findViewById(R.id.button_finalize_sale);

        ImageButton buttonBack = view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> navController.popBackStack());

        buttonFinalizeSale.setOnClickListener(v -> finalizeSale());
    }

    private void setupRecyclerViews() {
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new ProductSearchAdapter(new ArrayList<>(), product -> {
            addProductToCart(product);
            editTextSearch.setText(""); 
            searchResultsRecyclerView.setVisibility(View.GONE);
        });
        searchResultsRecyclerView.setAdapter(searchAdapter);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onItemQuantityChanged() {
                cartAdapter.notifyDataSetChanged();
                updateTotal();
            }

            @Override
            public void onItemDeleted(int position) {
                cartItems.remove(position);
                cartAdapter.notifyItemRemoved(position);
                updateTotal();
                updateCartVisibility();
            }
        });
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // CORREÇÃO: Gerencia a visibilidade dos componentes
                boolean isSearching = s.length() > 0;
                if (isSearching) {
                    // Enquanto busca, esconde o carrinho e a tela de carrinho vazio
                    cartRecyclerView.setVisibility(View.GONE);
                    emptyCartView.setVisibility(View.GONE);
                } else {
                    // Quando a busca é limpa, mostra o carrinho ou a tela de vazio novamente
                    updateCartVisibility();
                }
                newSaleViewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeSearchResults() {
        newSaleViewModel.searchResults.observe(getViewLifecycleOwner(), products -> {
            searchAdapter.setProducts(products);
            boolean hasResults = products != null && !products.isEmpty();
            boolean isSearching = editTextSearch.getText().length() > 0;
            // Só mostra os resultados se o usuário estiver de fato buscando
            searchResultsRecyclerView.setVisibility(isSearching && hasResults ? View.VISIBLE : View.GONE);
        });
    }

    private void addProductToCart(Product product) {
        for (SaleItem item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                if (item.getQuantity() < product.getStock()) {
                    item.setQuantity(item.getQuantity() + 1);
                    cartAdapter.notifyDataSetChanged();
                    updateTotal();
                } else {
                    Toast.makeText(getContext(), "Estoque máximo atingido", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }

        if (product.getStock() > 0) {
            cartItems.add(new SaleItem(product, 1));
            cartAdapter.notifyDataSetChanged();
            updateTotal();
            updateCartVisibility();
        } else {
            Toast.makeText(getContext(), "Produto sem estoque", Toast.LENGTH_SHORT).show();
        }
    }

    private void finalizeSale() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "O carrinho está vazio", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalValue = 0;
        for (SaleItem item : cartItems) {
            totalValue += item.getTotalPrice();
        }

        newSaleViewModel.salvarVendaCompleta(cartItems, totalValue);
        Toast.makeText(getContext(), "Venda salva com sucesso!", Toast.LENGTH_SHORT).show();
        navController.popBackStack();
    }

    private void updateTotal() {
        double total = 0;
        for (SaleItem item : cartItems) {
            total += item.getTotalPrice();
        }
        textTotalValue.setText(String.format(Locale.getDefault(), "R$ %.2f", total));
    }

    private void updateCartVisibility() {
        boolean isCartEmpty = cartItems.isEmpty();
        emptyCartView.setVisibility(isCartEmpty ? View.VISIBLE : View.GONE);
        cartRecyclerView.setVisibility(isCartEmpty ? View.GONE : View.VISIBLE);

        buttonFinalizeSale.setEnabled(!isCartEmpty);
        if (isCartEmpty) {
            buttonFinalizeSale.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_400));
        } else {
            buttonFinalizeSale.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_500));
            int textColor = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                    ? ContextCompat.getColor(getContext(), R.color.black)
                    : ContextCompat.getColor(getContext(), R.color.white);
            buttonFinalizeSale.setTextColor(textColor);
        }
    }
}
