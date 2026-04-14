package com.oliveira.meucaixa.ui.products;

import com.oliveira.meucaixa.R;

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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class ProductListFragment extends Fragment {

    private ProductListViewModel productListViewModel;
    private ProductAdapter productAdapter;
    private IngredientAdapter ingredientAdapter;
    
    private NavController navController;
    private TextView textTitle; // Remover textProductCount se não tiver onde colocar, ou manter se quiser usar
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
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
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);
        fab = view.findViewById(R.id.fab_add_product);

        setupAdapters();
        setupPagerAndTabs();
        observeData();
    }

    private void setupAdapters() {
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
    }

    private void setupPagerAndTabs() {
        ProductPagerAdapter pagerAdapter = new ProductPagerAdapter(productAdapter, ingredientAdapter);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Produtos");
            } else {
                tab.setText("Insumos");
            }
        }).attach();

        // Sincroniza estado de botões/titulos quando rolar a página
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    isShowingIngredients = false;
                    textTitle.setText("Meus Produtos");
                    setupFab(R.id.action_global_to_addEditProduct);
                } else {
                    isShowingIngredients = true;
                    textTitle.setText("Meus Insumos");
                    setupFab(R.id.action_global_to_addEditIngredient);
                }
            }
        });
    }
    
    private void setupFab(int actionId) {
        fab.setOnClickListener(v -> navController.navigate(actionId));
    }

    private void observeData() {
        productListViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            productAdapter.setProducts(products);
        });
        
        productListViewModel.getAllIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            ingredientAdapter.setIngredients(ingredients);
        });
    }
}
