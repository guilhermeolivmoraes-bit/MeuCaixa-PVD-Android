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
    private Button buttonSave;
    private AddEditIngredientViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_ingredient, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        ImageButton buttonBack = view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> navController.popBackStack());
        
        editName = view.findViewById(R.id.edit_text_ingredient_name);
        editPrice = view.findViewById(R.id.edit_text_ingredient_price);
        editQuantity = view.findViewById(R.id.edit_text_ingredient_quantity);
        buttonSave = view.findViewById(R.id.button_save);
        
        editPrice.addTextChangedListener(new com.oliveira.meucaixa.utils.MoneyTextWatcher(editPrice));
        editQuantity.addTextChangedListener(new com.oliveira.meucaixa.utils.WeightTextWatcher(editQuantity));

        viewModel = new ViewModelProvider(this).get(AddEditIngredientViewModel.class);

        setupObservers();
        setupValidation();

        buttonSave.setOnClickListener(v -> saveIngredient());
    }

    private void setupObservers() {
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

        viewModel.saveIngredient(name, priceText, quantityText);
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
            buttonSave.setBackgroundColor(isValid ? 
                ContextCompat.getColor(requireContext(), R.color.green_500) : 
                ContextCompat.getColor(requireContext(), R.color.gray_400));
        }
    }
}
