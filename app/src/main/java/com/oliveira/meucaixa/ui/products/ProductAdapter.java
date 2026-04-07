package com.oliveira.meucaixa.ui.products;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.data.model.Product;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> products;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
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
        private final TextView name, price, stock;
        private final ImageView stockWarningIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_product_name);
            price = itemView.findViewById(R.id.text_product_price);
            stock = itemView.findViewById(R.id.text_product_stock);
            stockWarningIcon = itemView.findViewById(R.id.icon_stock_warning);
        }

        public void bind(final Product product, final OnProductClickListener listener) {
            name.setText(product.getName());
            price.setText(String.format(Locale.getDefault(), "R$ %.2f", product.getPrice()));
            stock.setText(String.format(Locale.getDefault(), "Estoque: %d", product.getStock()));

            if (product.getStock() <= 5) { 
                stock.setTextColor(Color.RED);
                stockWarningIcon.setVisibility(View.VISIBLE);
            } else {
                stock.setTextColor(Color.GRAY);
                stockWarningIcon.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onProductClick(product));
        }
    }
}
