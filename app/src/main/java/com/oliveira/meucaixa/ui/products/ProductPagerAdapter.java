package com.oliveira.meucaixa.ui.products;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.oliveira.meucaixa.R;

public class ProductPagerAdapter extends RecyclerView.Adapter<ProductPagerAdapter.PagerViewHolder> {

    private final RecyclerView.Adapter<?>[] adapters;

    public ProductPagerAdapter(RecyclerView.Adapter<?>... adapters) {
        this.adapters = adapters;
    }

    @NonNull
    @Override
    public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pager_recycler, parent, false);
        return new PagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerView.setAdapter(adapters[position]);
    }

    @Override
    public int getItemCount() {
        return adapters.length;
    }

    static class PagerViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;

        public PagerViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.pager_recycler_view);
        }
    }
}
