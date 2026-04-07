package com.oliveira.meucaixa.ui.sales;

import com.oliveira.meucaixa.R;

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

    private final List<CartItem> saleItems;
    private final OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onItemQuantityChanged();
        void onItemDeleted(int position);
    }

    public CartAdapter(List<CartItem> saleItems, OnCartItemChangeListener listener) {
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
        CartItem saleItem = saleItems.get(position);
        holder.bind(saleItem);
    }

    @Override
    public int getItemCount() {
        return saleItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name, quantity, totalPrice;
        private final ImageButton decrease, increase, delete;
        private final OnCartItemChangeListener listener;

        public ViewHolder(@NonNull View itemView, OnCartItemChangeListener listener) {
            super(itemView);
            this.listener = listener;
            name = itemView.findViewById(R.id.text_cart_item_name);
            quantity = itemView.findViewById(R.id.text_cart_item_quantity);
            totalPrice = itemView.findViewById(R.id.text_cart_item_price);
            decrease = itemView.findViewById(R.id.button_decrease_quantity);
            increase = itemView.findViewById(R.id.button_increase_quantity);
            delete = itemView.findViewById(R.id.button_delete_item);
        }

        public void bind(final CartItem saleItem) {
            name.setText(saleItem.getProduct().getName());
            quantity.setText(String.valueOf(saleItem.getQuantity()));
            totalPrice.setText(String.format(Locale.getDefault(), "R$ %.2f", saleItem.getTotalPrice()));

            increase.setOnClickListener(v -> {
                if (saleItem.getQuantity() < saleItem.getProduct().getStock()) {
                    saleItem.setQuantity(saleItem.getQuantity() + 1);
                    listener.onItemQuantityChanged();
                } else {
                    Toast.makeText(itemView.getContext(), "Estoque máximo atingido", Toast.LENGTH_SHORT).show();
                }
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
