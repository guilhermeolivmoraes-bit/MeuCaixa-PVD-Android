package com.oliveira.minhacaixa;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);

        // Configura a navegação padrão
        NavigationUI.setupWithNavController(bottomNav, navController);

        // CORREÇÃO: Adiciona um listener para tratar o clique no botão "Início"
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_dashboard) {
                // Usa a ação customizada para limpar a pilha e voltar ao início
                navController.navigate(R.id.action_global_to_dashboard);
                return true;
            } else {
                // Para todos os outros itens, usa o comportamento padrão
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });
    }
}
