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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeIngredientsBottomSheet extends BottomSheetDialogFragment {

    private AutoCompleteTextView autoCompleteIngredient;
    private TextInputEditText editTextQuantity;
    private MaterialButton buttonAdd;
    private RecyclerView recyclerView;
    private TextView textTotalCost;
    private MaterialButton buttonConfirm;

    private RecipeIngredientAdapter adapter;
    private List<RecipeIngredient> ingredientList = new ArrayList<>();

    // Mock data for UI presentation as requested
    private final String[] mockDatabaseIngredients = {"Farinha de Trigo", "Açúcar", "Leite", "Chocolate em Pó", "Fermento", "Manteiga"};

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

        setupAutoComplete();
        setupRecyclerView();
        setupListeners();
        updateTotalCost();
    }

    private void setupAutoComplete() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, mockDatabaseIngredients);
        autoCompleteIngredient.setAdapter(arrayAdapter);
    }

    private void setupRecyclerView() {
        adapter = new RecipeIngredientAdapter(ingredientList, this::removeIngredient);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        buttonAdd.setOnClickListener(v -> {
            String name = autoCompleteIngredient.getText().toString().trim();
            String quantityStr = editTextQuantity.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(quantityStr)) {
                Toast.makeText(getContext(), "Preencha o insumo e a quantidade", Toast.LENGTH_SHORT).show();
                return;
            }

            // Exemplo de cálculo: backend fará o real. (5.00 usado de mockup fee)
            double quantity = 0;
            try {
                quantity = Double.parseDouble(quantityStr.replace(",", "."));
            } catch (Exception ignored) {}

            double cost = quantity * 5.0; 
            
            ingredientList.add(new RecipeIngredient(name, quantityStr, cost));
            adapter.notifyItemInserted(ingredientList.size() - 1);
            
            autoCompleteIngredient.setText("");
            editTextQuantity.setText("");
            
            updateTotalCost();
        });

        buttonConfirm.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Receita confirmada com " + ingredientList.size() + " insumos", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void removeIngredient(int position) {
        if (position >= 0 && position < ingredientList.size()) {
            ingredientList.remove(position);
            adapter.notifyItemRemoved(position);
            updateTotalCost();
        }
    }

    private void updateTotalCost() {
        double total = 0;
        for (RecipeIngredient item : ingredientList) {
            total += item.cost;
        }
        textTotalCost.setText(String.format(Locale.getDefault(), "Custo Total da Receita: R$ %.2f", total));
    }

    // --- Inner Classes for UI Management ---
    
    public static class RecipeIngredient {
        String name;
        String quantity;
        double cost;

        public RecipeIngredient(String name, String quantity, double cost) {
            this.name = name;
            this.quantity = quantity;
            this.cost = cost;
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
