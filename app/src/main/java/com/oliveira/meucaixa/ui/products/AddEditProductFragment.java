package com.oliveira.meucaixa.ui.products;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.models.Product;
import com.oliveira.meucaixa.utils.MoneyTextWatcher;

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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class AddEditProductFragment extends Fragment {

    private AddEditProductViewModel addEditProductViewModel;
    private NavController navController;
    private EditText editTextName, editTextPrice, editTextStock, editTextCostPrice;
    private TextInputLayout layoutCostPrice;
    private TextView textTitle;
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
        layoutCostPrice = view.findViewById(R.id.layout_cost_price);
        textTitle = view.findViewById(R.id.text_title);
        buttonDelete = view.findViewById(R.id.button_delete);
        buttonSave = view.findViewById(R.id.button_save);
        buttonManageIngredients = view.findViewById(R.id.button_manage_ingredients);
        radioGroupUnitType = view.findViewById(R.id.radio_group_unit_type);
        switchOwnProduction = view.findViewById(R.id.switch_own_production);
        
        ImageButton buttonBack = view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> navController.popBackStack());
    }

    private void setupListeners() {
        editTextPrice.addTextChangedListener(new MoneyTextWatcher(editTextPrice));
        editTextCostPrice.addTextChangedListener(new MoneyTextWatcher(editTextCostPrice));

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

        buttonSave.setOnClickListener(v -> saveProduct());
        buttonDelete.setOnClickListener(v -> deleteProduct());
        
        buttonManageIngredients.setOnClickListener(v -> {
            RecipeIngredientsBottomSheet bottomSheet = RecipeIngredientsBottomSheet.newInstance();
            bottomSheet.show(getChildFragmentManager(), "RecipeBottomSheet");
        });

        setupDynamicToggles();
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
        String stockStr = editTextStock.getText().toString().trim();
        boolean isStockValid = false;
        try {
            double stock = Double.parseDouble(stockStr.replace(",", "."));
            if (stock >= 0) { // Changed to >= 0 since users might leave stock empty or zero
                isStockValid = true;
            }
        } catch (NumberFormatException e) {
            isStockValid = false;
        }

        buttonSave.setEnabled(isStockValid);
    }

    private void saveProduct() {
        String name = editTextName.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String stockStr = editTextStock.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(stockStr)) {
            Toast.makeText(getContext(), "Por favor, preencha nome, preço e estoque", Toast.LENGTH_SHORT).show();
            return;
        }

        String priceAsNumber = priceStr.replaceAll("[^\\d]", "");
        double price = priceAsNumber.isEmpty() ? 0 : Double.parseDouble(priceAsNumber) / 100.0;
        
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
            double cost = costAsNumber.isEmpty() ? 0 : Double.parseDouble(costAsNumber) / 100.0;
            currentProduct.setCostPrice(cost);
        } else {
            // Se for produção própria o custo virá da soma dos ingredientes
            // currentProduct.setCostPrice( calculatedValue );
        }

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
