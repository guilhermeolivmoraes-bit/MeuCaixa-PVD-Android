package com.oliveira.meucaixa.ui.main;

import com.oliveira.meucaixa.R;

import android.os.Bundle;
import android.view.View;
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

        // Configura a navegação para a barra inferior
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Adiciona um listener para mostrar/esconder a barra de navegação
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.welcomeFragment || 
                destination.getId() == R.id.loginFragment || 
                destination.getId() == R.id.signUpFragment) {
                bottomNav.setVisibility(View.GONE);
            } else {
                bottomNav.setVisibility(View.VISIBLE);
            }
        });
        
        // CORREÇÃO do bug de voltar para a tela de início
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_dashboard) {
                navController.navigate(R.id.action_main_app_to_dashboard);
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }
}
