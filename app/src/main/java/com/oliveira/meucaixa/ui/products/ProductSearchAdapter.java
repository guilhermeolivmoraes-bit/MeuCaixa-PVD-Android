package com.oliveira.meucaixa.ui.products;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.data.model.Product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ProductSearchAdapter extends RecyclerView.Adapter<ProductSearchAdapter.ViewHolder> {

    private List<Product> products;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductSearchAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name, stock, price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.search_result_name);
            stock = itemView.findViewById(R.id.search_result_stock);
            price = itemView.findViewById(R.id.search_result_price);
        }

        public void bind(final Product product, final OnProductClickListener listener) {
            name.setText(product.getName());
            
            String stockStr;
            if(product.getUnitType() != null && product.getUnitType().equals("kg/g")) {
                stockStr = String.format(Locale.getDefault(), "Estoque: %.3f", product.getStock());
            } else {
                stockStr = String.format(Locale.getDefault(), "Estoque: %d", (long) product.getStock());
            }
            stock.setText(stockStr);
            
            price.setText(String.format(Locale.getDefault(), "R$ %.2f", product.getPrice()));
            itemView.setOnClickListener(v -> listener.onProductClick(product));
        }
    }
}
