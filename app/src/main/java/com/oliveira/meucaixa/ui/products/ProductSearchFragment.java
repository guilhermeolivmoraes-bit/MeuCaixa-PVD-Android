package com.oliveira.meucaixa.ui.products;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.data.model.Product;
import com.oliveira.meucaixa.ui.sales.NewSaleViewModel; // Will reuse the sale view model for search query

import java.util.ArrayList;

public class ProductSearchFragment extends Fragment {

    private NewSaleViewModel viewModel;
    private NavController navController;
    private EditText editTextSearch;
    private RecyclerView recyclerView;
    private ProductSearchAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        // Using activity class or parent required context as NewSaleViewModel expects user scoped queries.
        // Or construct its own viewmodel here if preferred. Let's just use it at Fragment scope.
        viewModel = new ViewModelProvider(this).get(NewSaleViewModel.class);

        ImageButton buttonBack = view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> navController.popBackStack());

        editTextSearch = view.findViewById(R.id.edit_text_real_search);
        recyclerView = view.findViewById(R.id.recycler_view_search_results);

        setupRecyclerView();
        setupSearch();
        observeSearchResults();

        // Auto Focus and show keyboard
        editTextSearch.requestFocus();
        editTextSearch.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editTextSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductSearchAdapter(new ArrayList<>(), product -> {
            // fragment result api
            Bundle result = new Bundle();
            result.putLong("productId", product.getId());
            getParentFragmentManager().setFragmentResult("sale_product_req", result);
            navController.popBackStack();
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Trigger initial empty search to load all products or keep empty
        viewModel.setSearchQuery("");
    }

    private void observeSearchResults() {
        viewModel.searchResults.observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                adapter.setProducts(products);
            }
        });
    }
}
