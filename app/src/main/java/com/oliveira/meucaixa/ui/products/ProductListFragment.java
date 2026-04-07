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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class ProductListFragment extends Fragment {

    private ProductListViewModel productListViewModel;
    private ProductAdapter productAdapter;
    private NavController navController;
    private TextView textProductCount, textTitle;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        textTitle = view.findViewById(R.id.text_title);
        textProductCount = view.findViewById(R.id.text_product_count);
        recyclerView = view.findViewById(R.id.recycler_view_products);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_product);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productAdapter = new ProductAdapter(new ArrayList<>(), product -> {
            Bundle bundle = new Bundle();
            bundle.putLong("productId", product.getId());
            navController.navigate(R.id.action_global_to_addEditProduct, bundle);
        });
        recyclerView.setAdapter(productAdapter);

        productListViewModel = new ViewModelProvider(this).get(ProductListViewModel.class);
        productListViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            productAdapter.setProducts(products);
            textProductCount.setText(String.format("%d produto(s)", products.size()));
            recyclerView.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
        });

        fab.setOnClickListener(v -> {
            navController.navigate(R.id.action_global_to_addEditProduct);
        });
    }
}
