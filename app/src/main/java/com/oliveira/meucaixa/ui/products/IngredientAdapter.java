package com.oliveira.meucaixa.ui.products;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.data.model.Ingredient;

import java.util.List;
import java.util.Locale;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private List<Ingredient> ingredients;
    private final OnIngredientClickListener listener;

    public interface OnIngredientClickListener {
        void onIngredientClick(Ingredient ingredient);
    }

    public IngredientAdapter(List<Ingredient> ingredients, OnIngredientClickListener listener) {
        this.ingredients = ingredients;
        this.listener = listener;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false); // Reuse item_product layout if compatible, or change later if needed. Assume compatible for now (name, info, price format).
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        holder.bind(ingredients.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return ingredients == null ? 0 : ingredients.size();
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView stock; // using stock textview for package qty
        private final TextView price; // using price textview for package price

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_primary);
            stock = itemView.findViewById(R.id.text_tertiary);
            price = itemView.findViewById(R.id.text_secondary);
        }

        public void bind(final Ingredient ingredient, final OnIngredientClickListener listener) {
            name.setText(ingredient.getName());
            
            // Re-using product item layout:
            price.setText(String.format(Locale.getDefault(), "R$ %.2f", ingredient.getPackagePrice()));
            
            // Format package quantity and unit
            stock.setText(String.format(Locale.getDefault(), "Embalagem: %.2f %s", 
                ingredient.getPackageQuantity(), 
                ingredient.getUnitOfMeasure() != null ? ingredient.getUnitOfMeasure() : ""));

            itemView.findViewById(R.id.btn_edit).setOnClickListener(v -> listener.onIngredientClick(ingredient));
            
            // TODO: Call listener.onIngredientDelete(ingredient) in the future
            itemView.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            });
        }
    }
}
