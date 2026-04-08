package com.oliveira.meucaixa.ui.products;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.data.model.Product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class ProductListFragment extends Fragment {

    private ProductListViewModel productListViewModel;
    private ProductAdapter productAdapter;
    private IngredientAdapter ingredientAdapter;
    
    private NavController navController;
    private TextView textProductCount, textTitle;
    private RecyclerView recyclerView;
    private ChipGroup chipGroupFilters;
    private FloatingActionButton fab;
    
    private boolean isShowingIngredients = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        productListViewModel = new ViewModelProvider(this).get(ProductListViewModel.class);

        textTitle = view.findViewById(R.id.text_title);
        textProductCount = view.findViewById(R.id.text_product_count);
        recyclerView = view.findViewById(R.id.recycler_view_products);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        fab = view.findViewById(R.id.fab_add_product);

        setupRecyclerView();
        setupChips();
        observeData();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        productAdapter = new ProductAdapter(new ArrayList<>(), product -> {
            Bundle bundle = new Bundle();
            bundle.putLong("productId", product.getId());
            navController.navigate(R.id.action_global_to_addEditProduct, bundle);
        });
        
        ingredientAdapter = new IngredientAdapter(new ArrayList<>(), ingredient -> {
            Bundle bundle = new Bundle();
            bundle.putLong("ingredientId", ingredient.getId());
            navController.navigate(R.id.action_global_to_addEditIngredient, bundle);
        });

        // initial state
        recyclerView.setAdapter(productAdapter);
        setupFab(R.id.action_global_to_addEditProduct);
    }

    private void setupChips() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_ingredients) {
                isShowingIngredients = true;
                recyclerView.setAdapter(ingredientAdapter);
                textTitle.setText("Meus Insumos");
                setupFab(R.id.action_global_to_addEditIngredient);
            } else {
                isShowingIngredients = false;
                recyclerView.setAdapter(productAdapter);
                textTitle.setText("Meus Produtos");
                setupFab(R.id.action_global_to_addEditProduct);
            }
            updateCountText();
        });
    }
    
    private void setupFab(int actionId) {
        fab.setOnClickListener(v -> navController.navigate(actionId));
    }

    private void observeData() {
        productListViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            productAdapter.setProducts(products);
            if (!isShowingIngredients) {
                updateCountText();
                recyclerView.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
        
        productListViewModel.getAllIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            ingredientAdapter.setIngredients(ingredients);
            if (isShowingIngredients) {
                updateCountText();
                recyclerView.setVisibility(ingredients.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }
    
    private void updateCountText() {
        if (isShowingIngredients) {
            int size = ingredientAdapter.getItemCount();
            textProductCount.setText(String.format("%d insumo(s)", size));
        } else {
            int size = productAdapter.getItemCount();
            textProductCount.setText(String.format("%d produto(s)", size));
        }
    }
}
