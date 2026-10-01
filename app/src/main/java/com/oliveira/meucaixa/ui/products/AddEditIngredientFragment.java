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
import com.oliveira.meucaixa.models.Ingredient;
import com.oliveira.meucaixa.utils.MoneyTextWatcher;
import com.oliveira.meucaixa.utils.WeightTextWatcher;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class AddEditIngredientFragment extends Fragment {

    private NavController navController;
    private EditText editName;
    private EditText editPrice;
    private EditText editQuantity;
    private EditText editCurrentStock;
    private TextInputLayout layoutName, layoutPrice, layoutQuantity, layoutCurrentStock;
    private TextView textTitle, textSaveHint;
    private Button buttonSave;
    private AddEditIngredientViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_ingredient, container, false);
    }
    private Button buttonDelete;
    private Ingredient currentIngredient;
    private long ingredientId = -1L;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        ImageButton buttonBack = view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> confirmExit());
        
        textTitle = view.findViewById(R.id.text_title);
        textSaveHint = view.findViewById(R.id.text_save_hint);
        
        layoutName = view.findViewById(R.id.layout_ingredient_name);
        layoutPrice = view.findViewById(R.id.layout_ingredient_price);
        layoutQuantity = view.findViewById(R.id.layout_ingredient_quantity);
        layoutCurrentStock = view.findViewById(R.id.layout_ingredient_current_stock);
        
        editName = view.findViewById(R.id.edit_text_ingredient_name);
        editPrice = view.findViewById(R.id.edit_text_ingredient_price);
        editQuantity = view.findViewById(R.id.edit_text_ingredient_quantity);
        editCurrentStock = view.findViewById(R.id.edit_text_ingredient_current_stock);
        buttonSave = view.findViewById(R.id.button_save);
        buttonDelete = view.findViewById(R.id.button_delete);
        
        editPrice.addTextChangedListener(new MoneyTextWatcher(editPrice));
        editQuantity.addTextChangedListener(new WeightTextWatcher(editQuantity));
        editCurrentStock.addTextChangedListener(new WeightTextWatcher(editCurrentStock));

        viewModel = new ViewModelProvider(this).get(AddEditIngredientViewModel.class);

        setupObservers();
        setupValidation();

        buttonSave.setOnClickListener(v -> saveIngredient());
        
        buttonDelete.setOnClickListener(v -> {
            if (currentIngredient != null) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Excluir Insumo")
                        .setMessage("Tem certeza que deseja excluir o insumo '" + currentIngredient.getName() + "'?\n\nIsso pode afetar em cascata as receitas de produtos que o utilizam.")
                        .setPositiveButton("Excluir", (dialog, which) -> {
                            viewModel.deleteIngredient(currentIngredient);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        setupInitialState();
        setupBackNavigation();
    }

    private void setupBackNavigation() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmExit();
            }
        });
    }

    private void confirmExit() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Descartar rascunho?")
                .setMessage("Você tem alterações não salvas. Se você sair agora, os dados preenchidos serão perdidos.")
                .setPositiveButton("Sair", (dialog, which) -> {
                    navController.popBackStack();
                })
                .setNegativeButton("Continuar Editando", null)
                .show();
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
                editPrice.setText(String.format(Locale.getDefault(), "%.2f", ingredient.getPackagePrice()));
                editQuantity.setText(String.format(Locale.getDefault(), "%.3f", ingredient.getPackageQuantity()));
                editCurrentStock.setText(String.format(Locale.getDefault(), "%.3f", ingredient.getCurrentStock()));
            }
        });

        viewModel.getSaveSuccessEvent().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                // Remove popBackStack from here to let UI show snackbar properly or handle it gracefully
                Snackbar.make(requireView(), "Insumo salvo com sucesso!", Snackbar.LENGTH_SHORT).show();
                navController.popBackStack();
            }
        });

        viewModel.getSaveErrorEvent().observe(getViewLifecycleOwner(), errorMessage -> {
            Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG).show();
        });
    }

    private void saveIngredient() {
        boolean hasError = false;

        String name = editName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            layoutName.setError(getString(R.string.error_required_field));
            hasError = true;
        }

        String priceText = editPrice.getText().toString().trim();
        if (TextUtils.isEmpty(priceText) || priceText.equals("0,00")) {
            layoutPrice.setError(getString(R.string.error_required_field));
            hasError = true;
        }

        String quantityText = editQuantity.getText().toString().trim();
        if (TextUtils.isEmpty(quantityText) || quantityText.equals("0,000")) {
            layoutQuantity.setError(getString(R.string.error_required_field));
            hasError = true;
        }

        if (hasError) {
            return;
        }
        
        String currentStockText = editCurrentStock.getText().toString().trim();

        viewModel.saveIngredient(currentIngredient, name, priceText, quantityText, currentStockText);
    }

    private void setupValidation() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layoutName != null) layoutName.setError(null);
                if (layoutPrice != null) layoutPrice.setError(null);
                if (layoutQuantity != null) layoutQuantity.setError(null);
            }
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
        
        if (textSaveHint != null) {
            textSaveHint.setVisibility(isValid ? View.GONE : View.VISIBLE);
        }
    }
}
