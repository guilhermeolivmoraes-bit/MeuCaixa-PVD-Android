package com.oliveira.meucaixa.ui.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.oliveira.meucaixa.R;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

public class AddEditIngredientFragment extends Fragment {

    private NavController navController;
    private EditText editName;
    private EditText editPrice;
    private EditText editQuantity;
    private EditText editCurrentStock;
    private Button buttonSave;
    private AddEditIngredientViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_ingredient, container, false);
    }

    private android.widget.TextView textTitle;
    private Button buttonDelete;
    private com.oliveira.meucaixa.models.Ingredient currentIngredient;
    private long ingredientId = -1L;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        ImageButton buttonBack = view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> navController.popBackStack());
        
        textTitle = view.findViewById(R.id.text_title);
        editName = view.findViewById(R.id.edit_text_ingredient_name);
        editPrice = view.findViewById(R.id.edit_text_ingredient_price);
        editQuantity = view.findViewById(R.id.edit_text_ingredient_quantity);
        editCurrentStock = view.findViewById(R.id.edit_text_ingredient_current_stock);
        buttonSave = view.findViewById(R.id.button_save);
        buttonDelete = view.findViewById(R.id.button_delete);
        
        editPrice.addTextChangedListener(new com.oliveira.meucaixa.utils.MoneyTextWatcher(editPrice));
        editQuantity.addTextChangedListener(new com.oliveira.meucaixa.utils.WeightTextWatcher(editQuantity));
        editCurrentStock.addTextChangedListener(new com.oliveira.meucaixa.utils.WeightTextWatcher(editCurrentStock));

        viewModel = new ViewModelProvider(this).get(AddEditIngredientViewModel.class);

        setupObservers();
        setupValidation();

        buttonSave.setOnClickListener(v -> saveIngredient());
        
        buttonDelete.setOnClickListener(v -> {
            if (currentIngredient != null) {
                viewModel.deleteIngredient(currentIngredient);
            }
        });

        setupInitialState();
    }

    private void setupInitialState() {
        if (getArguments() != null) {
            ingredientId = getArguments().getLong("ingredientId", -1L);
        }

        if (ingredientId != -1L) {
            textTitle.setText("Editar Insumo");
            buttonDelete.setVisibility(View.VISIBLE);
            viewModel.fetchIngredient(ingredientId);
        } else {
            textTitle.setText("Cadastrar Insumo");
            buttonDelete.setVisibility(View.GONE);
        }
    }

    private void setupObservers() {
        viewModel.getIngredient().observe(getViewLifecycleOwner(), ingredient -> {
            if (ingredient != null) {
                currentIngredient = ingredient;
                editName.setText(ingredient.getName());
                editPrice.setText(String.format(java.util.Locale.getDefault(), "%.2f", ingredient.getPackagePrice()));
                editQuantity.setText(String.format(java.util.Locale.getDefault(), "%.3f", ingredient.getPackageQuantity()));
                editCurrentStock.setText(String.format(java.util.Locale.getDefault(), "%.3f", ingredient.getCurrentStock()));
            }
        });

        viewModel.getSaveSuccessEvent().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Insumo salvo com sucesso!", Toast.LENGTH_SHORT).show();
                navController.popBackStack();
            }
        });

        viewModel.getSaveErrorEvent().observe(getViewLifecycleOwner(), errorMessage -> {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    private void saveIngredient() {
        String name = editName.getText().toString().trim();
        String priceText = editPrice.getText().toString().trim();
        String quantityText = editQuantity.getText().toString().trim();
        String currentStockText = editCurrentStock.getText().toString().trim();

        viewModel.saveIngredient(currentIngredient, name, priceText, quantityText, currentStockText);
    }

    private void setupValidation() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateSaveButton();
            }
        };

        editName.addTextChangedListener(validationWatcher);
        editPrice.addTextChangedListener(validationWatcher);
        editQuantity.addTextChangedListener(validationWatcher);
        editCurrentStock.addTextChangedListener(validationWatcher);

        // Dispara a validação inicial para deixar o botão cinza
        validateSaveButton();
    }

    private void validateSaveButton() {
        boolean isValid = true;

        if (TextUtils.isEmpty(editName.getText().toString().trim())) {
            isValid = false;
        }
        
        String priceText = editPrice.getText().toString().trim();
        if (TextUtils.isEmpty(priceText) || priceText.equals("0,00")) {
            isValid = false;
        }

        String quantityText = editQuantity.getText().toString().trim();
        if (TextUtils.isEmpty(quantityText) || quantityText.equals("0,000")) {
            isValid = false;
        }

        if (buttonSave != null) {
            buttonSave.setEnabled(isValid);
        }
    }
}
