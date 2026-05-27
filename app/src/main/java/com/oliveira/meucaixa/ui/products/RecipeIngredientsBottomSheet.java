package com.oliveira.meucaixa.ui.products;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.oliveira.meucaixa.R;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.lifecycle.ViewModelProvider;
import com.oliveira.meucaixa.models.Ingredient;
import com.oliveira.meucaixa.models.ProductIngredient;
import com.oliveira.meucaixa.utils.WeightTextWatcher;

public class RecipeIngredientsBottomSheet extends BottomSheetDialogFragment {

    private AutoCompleteTextView autoCompleteIngredient;
    private TextInputEditText editTextQuantity;
    private MaterialButton buttonAdd;
    private RecyclerView recyclerView;
    private TextView textTotalCost;
    private MaterialButton buttonConfirm;

    private RecipeIngredientAdapter adapter;
    private final List<RecipeIngredient> ingredientList = new ArrayList<>();
    
    private AddEditProductViewModel viewModel;
    private List<Ingredient> availableIngredients = new ArrayList<>();
    private Ingredient selectedIngredient = null;

    public static RecipeIngredientsBottomSheet newInstance() {
        return new RecipeIngredientsBottomSheet();
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal != null) {
                // Ensure it opens expanded to see the list properly
                BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_recipe_ingredients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        autoCompleteIngredient = view.findViewById(R.id.auto_complete_ingredient);
        editTextQuantity = view.findViewById(R.id.edit_text_recipe_quantity);
        buttonAdd = view.findViewById(R.id.button_add_ingredient);
        recyclerView = view.findViewById(R.id.recycler_view_recipe_ingredients);
        textTotalCost = view.findViewById(R.id.text_total_cost);
        buttonConfirm = view.findViewById(R.id.button_confirm_recipe);

        editTextQuantity.addTextChangedListener(new WeightTextWatcher(editTextQuantity));

        viewModel = new ViewModelProvider(requireActivity()).get(AddEditProductViewModel.class);

        setupRecyclerView();
        setupListeners();
        setupObservers();
    }

    private void setupObservers() {
        viewModel.getAllIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            if (ingredients != null) {
                availableIngredients = ingredients;
                List<String> ingredientNames = new ArrayList<>();
                for (Ingredient ing : availableIngredients) {
                    ingredientNames.add(ing.getName());
                }
                
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, ingredientNames);
                autoCompleteIngredient.setAdapter(arrayAdapter);
                
                // Refresh list if needed
                updateRecyclerView();
            }
        });

        viewModel.getTotalRecipeCost().observe(getViewLifecycleOwner(), cost -> {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            textTotalCost.setText("Custo Total da Receita: " + format.format(cost));
        });
    }

    private void setupRecyclerView() {
        adapter = new RecipeIngredientAdapter(ingredientList, this::removeIngredient);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        autoCompleteIngredient.setOnItemClickListener((parent, view, position, id) -> {
            // Find the selected ingredient by matching the string
            String selectedName = (String) parent.getItemAtPosition(position);
            for (Ingredient ing : availableIngredients) {
                if (ing.getName().equals(selectedName)) {
                    selectedIngredient = ing;
                    break;
                }
            }
        });

        buttonAdd.setOnClickListener(v -> {
            if (selectedIngredient == null) {
                Toast.makeText(getContext(), "Selecione um insumo válido da lista", Toast.LENGTH_SHORT).show();
                return;
            }

            String quantityStr = editTextQuantity.getText().toString().trim();
            if (TextUtils.isEmpty(quantityStr)) {
                Toast.makeText(getContext(), "Preencha a quantidade", Toast.LENGTH_SHORT).show();
                return;
            }

            double quantity = 0;
            try {
                String cleanNumber = quantityStr.replaceAll("[^\\d]", "");
                quantity = Double.parseDouble(cleanNumber) / 1000.0;
            } catch (Exception e) {
                Toast.makeText(getContext(), "Quantidade inválida", Toast.LENGTH_SHORT).show();
                return;
            }

            if (quantity <= 0) {
                Toast.makeText(getContext(), "Quantidade deve ser maior que zero", Toast.LENGTH_SHORT).show();
                return;
            }

            ProductIngredient pi = new ProductIngredient(0, selectedIngredient.getId(), quantity);
            boolean added = viewModel.addIngredientToRecipe(pi);
            
            if (added) {
                autoCompleteIngredient.setText("");
                editTextQuantity.setText("");
                selectedIngredient = null;
                updateRecyclerView();
            } else {
                Toast.makeText(getContext(), "Este insumo já foi adicionado", Toast.LENGTH_SHORT).show();
            }
        });

        buttonConfirm.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Receita confirmada. Grave o produto para salvar no banco.", Toast.LENGTH_LONG).show();
            dismiss();
        });
    }

    private void removeIngredient(int position) {
        if (position >= 0 && position < ingredientList.size()) {
            RecipeIngredient itemUI = ingredientList.get(position);
            viewModel.removeIngredientFromRecipe(itemUI.productIngredientRef);
            updateRecyclerView();
        }
    }

    private void updateRecyclerView() {
        ingredientList.clear();
        Set<ProductIngredient> recipeSet = viewModel.getRecipeIngredients();
        
        if (availableIngredients != null && recipeSet != null) {
            for (ProductIngredient pi : recipeSet) {
                for (Ingredient dbIngredient : availableIngredients) {
                    if (pi.getIngredientId() == dbIngredient.getId()) {
                        double cost = viewModel.calculateIngredientCost(dbIngredient, pi.getQuantityUsed());
                        ingredientList.add(new RecipeIngredient(
                            dbIngredient.getName(), 
                            String.format(Locale.getDefault(), "%.3f", pi.getQuantityUsed()), 
                            cost,
                            pi
                        ));
                        break;
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // --- Inner Classes for UI Management ---
    
    public static class RecipeIngredient {
        String name;
        String quantity;
        double cost;
        ProductIngredient productIngredientRef;

        public RecipeIngredient(String name, String quantity, double cost, ProductIngredient ref) {
            this.name = name;
            this.quantity = quantity;
            this.cost = cost;
            this.productIngredientRef = ref;
        }
    }

    public static class RecipeIngredientAdapter extends RecyclerView.Adapter<RecipeIngredientAdapter.ViewHolder> {

        private final List<RecipeIngredient> items;
        private final OnItemRemoveListener removeListener;

        public interface OnItemRemoveListener {
            void onRemove(int position);
        }

        public RecipeIngredientAdapter(List<RecipeIngredient> items, OnItemRemoveListener listener) {
            this.items = items;
            this.removeListener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_ingredient, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecipeIngredient item = items.get(position);
            holder.name.setText(item.name);
            holder.quantity.setText(item.quantity);
            holder.cost.setText(String.format(Locale.getDefault(), "Custo: R$ %.2f", item.cost));
            
            holder.remove.setOnClickListener(v -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    removeListener.onRemove(currentPos);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, quantity, cost;
            ImageButton remove;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.text_ingredient_name);
                quantity = itemView.findViewById(R.id.text_ingredient_quantity);
                cost = itemView.findViewById(R.id.text_ingredient_cost);
                remove = itemView.findViewById(R.id.button_remove_ingredient);
            }
        }
    }
}
