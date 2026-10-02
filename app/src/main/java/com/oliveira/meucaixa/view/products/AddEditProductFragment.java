package com.oliveira.meucaixa.view.products;

import com.google.android.material.snackbar.Snackbar;
import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.viewmodel.products.AddEditProductViewModel;
import com.oliveira.meucaixa.model.entity.Product;
import com.oliveira.meucaixa.view.utils.MoneyTextWatcher;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class AddEditProductFragment extends Fragment {

    private AddEditProductViewModel addEditProductViewModel;
    private NavController navController;
    private EditText editTextName, editTextPrice, editTextStock, editTextCostPrice;
    private TextInputLayout layoutName, layoutPrice, layoutStock, layoutCostPrice;
    private TextView textTitle, textSaveHint;
    private Button buttonDelete, buttonSave, buttonManageIngredients;
    private RadioGroup radioGroupUnitType;
    private SwitchMaterial switchOwnProduction;
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
        addEditProductViewModel = new ViewModelProvider(requireActivity()).get(AddEditProductViewModel.class);

        bindViews(view);
        setupListeners();
        setupInitialState();
    }

    private void bindViews(View view) {
        editTextName = view.findViewById(R.id.edit_text_product_name);
        editTextPrice = view.findViewById(R.id.edit_text_product_price);
        editTextStock = view.findViewById(R.id.edit_text_product_stock);
        editTextCostPrice = view.findViewById(R.id.edit_text_cost_price);
        layoutName = view.findViewById(R.id.layout_product_name);
        layoutPrice = view.findViewById(R.id.layout_product_price);
        layoutStock = view.findViewById(R.id.layout_product_stock);
        layoutCostPrice = view.findViewById(R.id.layout_cost_price);
        textTitle = view.findViewById(R.id.text_title);
        textSaveHint = view.findViewById(R.id.text_save_hint);
        buttonDelete = view.findViewById(R.id.button_delete);
        buttonSave = view.findViewById(R.id.button_save);
        buttonManageIngredients = view.findViewById(R.id.button_manage_ingredients);
        radioGroupUnitType = view.findViewById(R.id.radio_group_unit_type);
        switchOwnProduction = view.findViewById(R.id.switch_own_production);
        
        ImageButton buttonBack = view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> confirmExit());
    }

    private void setupListeners() {
        editTextPrice.addTextChangedListener(new MoneyTextWatcher(editTextPrice));
        editTextCostPrice.addTextChangedListener(new MoneyTextWatcher(editTextCostPrice));

        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Limpa o erro ao digitar
                if (layoutName != null) layoutName.setError(null);
                if (layoutPrice != null) layoutPrice.setError(null);
                if (layoutStock != null) layoutStock.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {
                validateSaveButton();
            }
        };

        editTextName.addTextChangedListener(validationWatcher);
        editTextPrice.addTextChangedListener(validationWatcher);
        editTextStock.addTextChangedListener(validationWatcher);
        editTextCostPrice.addTextChangedListener(validationWatcher);

        buttonSave.setOnClickListener(v -> saveProduct());
        buttonDelete.setOnClickListener(v -> deleteProduct());
        
        buttonManageIngredients.setOnClickListener(v -> {
            RecipeIngredientsBottomSheet bottomSheet = RecipeIngredientsBottomSheet.newInstance();
            bottomSheet.show(getChildFragmentManager(), "RecipeBottomSheet");
        });

        setupDynamicToggles();
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

    private void setupDynamicToggles() {
        radioGroupUnitType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_peso) {
                editTextStock.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            } else {
                editTextStock.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });

        switchOwnProduction.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutCostPrice.setVisibility(View.GONE);
                buttonManageIngredients.setVisibility(View.VISIBLE);
            } else {
                layoutCostPrice.setVisibility(View.VISIBLE);
                buttonManageIngredients.setVisibility(View.GONE);
            }
        });
    }

    private void setupInitialState() {
        addEditProductViewModel.clearRecipeIngredients();
        
        if (getArguments() != null) {
            productId = getArguments().getLong("productId", -1L);
        }

        if (productId != -1L) {
            textTitle.setText("Editar Produto");
            buttonDelete.setVisibility(View.VISIBLE);
            addEditProductViewModel.getProductById(productId).observe(getViewLifecycleOwner(), product -> {
                if (product != null) {
                    currentProduct = product;
                    populateFields(product);
                    validateSaveButton();
                }
            });
            // Carregar ingredientes para evitar deletá-los ao salvar
            addEditProductViewModel.getIngredientsForProduct(productId).observe(getViewLifecycleOwner(), ingredients -> {
                addEditProductViewModel.loadRecipe(ingredients);
            });
        } else {
            textTitle.setText("Cadastrar Produto");
            buttonDelete.setVisibility(View.GONE);
            validateSaveButton();
        }
    }

    private void populateFields(Product product) {
        editTextName.setText(product.getName());
        editTextPrice.setText(String.format(new Locale("pt", "BR"), "%.2f", product.getPrice()));
        editTextCostPrice.setText(String.format(new Locale("pt", "BR"), "%.2f", product.getCostPrice()));
        
        switchOwnProduction.setChecked(product.isOwnProduction());
        if(product.getUnitType() != null && product.getUnitType().equals("kg/g")) {
            radioGroupUnitType.check(R.id.radio_peso);
            editTextStock.setText(String.format(Locale.getDefault(), "%.2f", product.getStock()));
        } else {
            radioGroupUnitType.check(R.id.radio_unidade);
            // Mostrar como número inteiro se for unidade
            long isInt = (long) product.getStock();
            editTextStock.setText(String.valueOf(isInt));
        }
    }

    private void validateSaveButton() {
        boolean isValid = true;
        
        String name = editTextName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            isValid = false;
        }

        String priceStr = editTextPrice.getText().toString().trim();
        if (TextUtils.isEmpty(priceStr) || priceStr.equals("0,00")) {
            isValid = false;
        }

        String stockStr = editTextStock.getText().toString().trim();
        try {
            double stock = Double.parseDouble(stockStr.replace(",", "."));
            if (stock < 0) {
                isValid = false;
            }
        } catch (NumberFormatException e) {
            isValid = false;
        }

        buttonSave.setEnabled(isValid);
        
        if (textSaveHint != null) {
            textSaveHint.setVisibility(isValid ? View.GONE : View.VISIBLE);
        }
    }

    private void saveProduct() {
        String name = editTextName.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String stockStr = editTextStock.getText().toString().trim();
        
        boolean hasError = false;

        if (TextUtils.isEmpty(name)) {
            layoutName.setError(getString(R.string.error_required_field));
            hasError = true;
        }

        if (TextUtils.isEmpty(priceStr) || priceStr.equals("0,00")) {
            layoutPrice.setError(getString(R.string.error_required_field));
            hasError = true;
        }

        if (TextUtils.isEmpty(stockStr)) {
            layoutStock.setError(getString(R.string.error_required_field));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        String priceAsNumber = priceStr.replaceAll("[^\\d]", "");
        BigDecimal price = priceAsNumber.isEmpty() ? BigDecimal.ZERO : new BigDecimal(priceAsNumber).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        
        double stock = 0;
        try {
             stock = Double.parseDouble(stockStr.replace(",", "."));
        } catch(Exception ignored){}

        if (currentProduct == null) {
            currentProduct = new Product();
        }
        
        currentProduct.setName(name);
        currentProduct.setPrice(price);
        currentProduct.setStock(stock);
        currentProduct.setOwnProduction(switchOwnProduction.isChecked());
        
        int selectedUnitId = radioGroupUnitType.getCheckedRadioButtonId();
        currentProduct.setUnitType(selectedUnitId == R.id.radio_peso ? "kg/g" : "un");

        if (!switchOwnProduction.isChecked()) {
            String costStr = editTextCostPrice.getText().toString().trim();
            String costAsNumber = costStr.replaceAll("[^\\d]", "");
            BigDecimal cost = costAsNumber.isEmpty() ? BigDecimal.ZERO : new BigDecimal(costAsNumber).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            currentProduct.setCostPrice(cost);
        } else {
            // Se for produção própria o custo virá da soma dos ingredientes
            // currentProduct.setCostPrice( calculatedValue );
        }

        addEditProductViewModel.saveProduct(currentProduct);
        Snackbar.make(requireView(), "Produto salvo com sucesso!", Snackbar.LENGTH_SHORT).show();
        navController.popBackStack();
    }

    private void deleteProduct() {
        if (currentProduct != null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Excluir Produto")
                    .setMessage("Tem certeza que deseja excluir o produto '" + currentProduct.getName() + "'?\n\nEsta ação excluirá o produto e não pode ser desfeita.")
                    .setPositiveButton("Excluir", (dialog, which) -> {
                        addEditProductViewModel.deleteProduct(currentProduct);
                        Toast.makeText(getContext(), "Produto excluído com sucesso!", Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
    }
}
