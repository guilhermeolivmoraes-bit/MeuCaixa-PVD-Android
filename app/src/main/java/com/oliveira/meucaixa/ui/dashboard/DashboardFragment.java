package com.oliveira.meucaixa.ui.dashboard;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.data.model.Sale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private TextView textTodaySalesValue, textTodaySalesCount, textDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        // Bind views
        textTodaySalesValue = view.findViewById(R.id.text_today_sales_value);
        textTodaySalesCount = view.findViewById(R.id.text_today_sales_count);
        textDate = view.findViewById(R.id.text_date);

        CardView buttonRegisterSale = view.findViewById(R.id.button_register_sale);
        CardView buttonMyProducts = view.findViewById(R.id.button_my_products);
        CardView buttonReports = view.findViewById(R.id.button_reports);

        // Configurar data atual
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        textDate.setText(sdf.format(new Date()));

        // Listeners de clique
        buttonRegisterSale.setOnClickListener(v -> navController.navigate(R.id.navigation_sale));
        buttonMyProducts.setOnClickListener(v -> navController.navigate(R.id.navigation_products));
        buttonReports.setOnClickListener(v -> navController.navigate(R.id.navigation_reports));

        // ViewModel
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Observar vendas do dia
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        long endOfDay = calendar.getTimeInMillis();

        dashboardViewModel.getSalesForDay(startOfDay, endOfDay).observe(getViewLifecycleOwner(), sales -> {
            double total = 0;
            for (Sale sale : sales) {
                total += sale.getTotalPrice();
            }
            textTodaySalesValue.setText(String.format(Locale.getDefault(), "R$ %.2f", total));
            textTodaySalesCount.setText(String.format(Locale.getDefault(), "%d venda(s)", sales.size()));
        });
    }
}
