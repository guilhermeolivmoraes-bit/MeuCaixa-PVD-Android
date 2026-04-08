package com.oliveira.meucaixa.ui.sales;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.data.model.SaleItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final List<SaleItem> saleItems;
    private final OnSaleItemChangeListener listener;

    public interface OnSaleItemChangeListener {
        void onItemQuantityChanged();

        void onItemDeleted(int position);
    }

    public CartAdapter(List<SaleItem> saleItems, OnSaleItemChangeListener listener) {
        this.saleItems = saleItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SaleItem saleItem = saleItems.get(position);
        holder.bind(saleItem);
    }

    @Override
    public int getItemCount() {
        return saleItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name, quantity, totalPrice;
        private final ImageButton decrease, increase, delete;
        private final OnSaleItemChangeListener listener;

        public ViewHolder(@NonNull View itemView, OnSaleItemChangeListener listener) {
            super(itemView);
            this.listener = listener;
            name = itemView.findViewById(R.id.text_cart_item_name);
            quantity = itemView.findViewById(R.id.text_cart_item_quantity);
            totalPrice = itemView.findViewById(R.id.text_cart_item_price);
            decrease = itemView.findViewById(R.id.button_decrease_quantity);
            increase = itemView.findViewById(R.id.button_increase_quantity);
            delete = itemView.findViewById(R.id.button_delete_item);
        }

        public void bind(final SaleItem saleItem) {
            name.setText(saleItem.getProductName());

            double q = saleItem.getQuantity();
            if (q == Math.floor(q)) {
                quantity.setText(String.valueOf((long) q));
            } else {
                quantity.setText(String.format(Locale.getDefault(), "%.3f", q));
            }
            totalPrice.setText(String.format(Locale.getDefault(), "R$ %.2f", saleItem.getProductPrice() * saleItem.getQuantity()));

            increase.setOnClickListener(v -> {
                saleItem.setQuantity(saleItem.getQuantity() + 1);
                listener.onItemQuantityChanged();
            });

            decrease.setOnClickListener(v -> {
                if (saleItem.getQuantity() > 1) {
                    saleItem.setQuantity(saleItem.getQuantity() - 1);
                    listener.onItemQuantityChanged();
                } else {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemDeleted(position);
                    }
                }
            });

            delete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemDeleted(position);
                }
            });
        }
    }
}
