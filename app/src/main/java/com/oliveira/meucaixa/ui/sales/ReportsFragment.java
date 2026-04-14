package com.oliveira.meucaixa.ui.sales;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.data.model.Sale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.util.Calendar;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private ReportsViewModel reportsViewModel;
    private TextView textTodaySalesValue, textTodaySalesCount, textAvgTicketDay;
    private TextView textMonthSalesValue, textMonthSalesCount, textAvgTicketMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reportsViewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        bindViews(view);
        observeSalesData();
    }

    private void bindViews(View view) {
        textTodaySalesValue = view.findViewById(R.id.text_today_sales_value);
        textTodaySalesCount = view.findViewById(R.id.text_today_sales_count);
        textAvgTicketDay = view.findViewById(R.id.text_avg_ticket_day);
        textMonthSalesValue = view.findViewById(R.id.text_month_sales_value);
        textMonthSalesCount = view.findViewById(R.id.text_month_sales_count);
        textAvgTicketMonth = view.findViewById(R.id.text_avg_ticket_month);
    }

    private void observeSalesData() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        long endOfDay = calendar.getTimeInMillis();

        reportsViewModel.getSalesForDay(startOfDay, endOfDay).observe(getViewLifecycleOwner(), sales -> {
            double total = 0;
            for (Sale sale : sales) {
                total += sale.getTotalPrice();
            }

            textTodaySalesValue.setText(String.format(Locale.getDefault(), "R$ %.2f", total));
            textTodaySalesCount.setText(String.format(Locale.getDefault(), "%d venda(s)", sales.size()));
        });

        reportsViewModel.getNetProfitForDay(startOfDay, endOfDay).observe(getViewLifecycleOwner(), profit -> {
            textAvgTicketDay.setText(String.format(Locale.getDefault(), "R$ %.2f", profit != null ? profit : 0.0));
        });

        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        long startOfMonth = calendar.getTimeInMillis();

        calendar.add(Calendar.MONTH, 1);
        long endOfMonth = calendar.getTimeInMillis();

        reportsViewModel.getSalesForMonth(startOfMonth, endOfMonth).observe(getViewLifecycleOwner(), sales -> {
            double total = 0;
            for (Sale sale : sales) {
                total += sale.getTotalPrice();
            }

            textMonthSalesValue.setText(String.format(Locale.getDefault(), "R$ %.2f", total));
            textMonthSalesCount.setText(String.format(Locale.getDefault(), "%d venda(s)", sales.size()));
        });

        reportsViewModel.getNetProfitForMonth(startOfMonth, endOfMonth).observe(getViewLifecycleOwner(), profit -> {
            textAvgTicketMonth.setText(String.format(Locale.getDefault(), "R$ %.2f", profit != null ? profit : 0.0));
        });
    }
}
