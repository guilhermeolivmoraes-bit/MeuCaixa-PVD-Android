package com.oliveira.meucaixa.ui.products;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.data.model.Product;
import com.oliveira.meucaixa.utils.MoneyTextWatcher;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.Locale;

public class AddEditProductFragment extends Fragment {

    private AddEditProductViewModel addEditProductViewModel;
    private NavController navController;
    private EditText editTextName, editTextPrice, editTextStock;
    private TextView textTitle;
    private Button buttonDelete, buttonSave;
    private Product currentProduct;
    private long productId = -1L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        addEditProductViewModel = new ViewModelProvider(this).get(AddEditProductViewModel.class);

        editTextName = view.findViewById(R.id.edit_text_product_name);
        editTextPrice = view.findViewById(R.id.edit_text_product_price);
        editTextStock = view.findViewById(R.id.edit_text_product_stock);
        textTitle = view.findViewById(R.id.text_title);
        buttonDelete = view.findViewById(R.id.button_delete);
        buttonSave = view.findViewById(R.id.button_save);
        ImageButton buttonBack = view.findViewById(R.id.button_back);

        editTextPrice.addTextChangedListener(new MoneyTextWatcher(editTextPrice));

        editTextStock.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateSaveButton();
            }
        });

        if (getArguments() != null) {
            productId = getArguments().getLong("productId", -1L);
        }

        if (productId != -1L) {
            textTitle.setText("Editar Produto");
            addEditProductViewModel.getProductById(productId).observe(getViewLifecycleOwner(), product -> {
                if (product != null) {
                    currentProduct = product;
                    editTextName.setText(product.getName());
                    editTextPrice.setText(String.format(new Locale("pt", "BR"), "%.2f", product.getPrice()));
                    editTextStock.setText(String.format(Locale.getDefault(), "%.2f", product.getStock()));
                    validateSaveButton();
                }
            });
            buttonDelete.setVisibility(View.VISIBLE);
        } else {
            textTitle.setText("Cadastrar Produto");
            buttonDelete.setVisibility(View.GONE);
            validateSaveButton();
        }

        buttonBack.setOnClickListener(v -> navController.popBackStack());
        buttonSave.setOnClickListener(v -> saveProduct());
        buttonDelete.setOnClickListener(v -> deleteProduct());
    }

    private void validateSaveButton() {
        String stockStr = editTextStock.getText().toString().trim();
        boolean isStockValid = false;
        try {
            double stock = Double.parseDouble(stockStr);
            if (stock > 0) {
                isStockValid = true;
            }
        } catch (NumberFormatException e) {
            isStockValid = false;
        }

        buttonSave.setEnabled(isStockValid);
        buttonSave.setBackgroundColor(isStockValid ? ContextCompat.getColor(getContext(), R.color.green_500) : ContextCompat.getColor(getContext(), R.color.gray_400));
    }

    private void saveProduct() {
        String name = editTextName.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String stockStr = editTextStock.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(stockStr)) {
            Toast.makeText(getContext(), "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String priceAsNumber = priceStr.replaceAll("[^\\d]", "");
        double price = Double.parseDouble(priceAsNumber) / 100.0;
        double stock = Double.parseDouble(stockStr);

        if (currentProduct == null) {
            currentProduct = new Product();
        }
        currentProduct.setName(name);
        currentProduct.setPrice(price);
        currentProduct.setStock(stock);

        addEditProductViewModel.saveProduct(currentProduct);
        Toast.makeText(getContext(), "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();
        navController.popBackStack();
    }

    private void deleteProduct() {
        if (currentProduct != null) {
            addEditProductViewModel.deleteProduct(currentProduct);
            Toast.makeText(getContext(), "Produto excluído com sucesso!", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
        }
    }
}
