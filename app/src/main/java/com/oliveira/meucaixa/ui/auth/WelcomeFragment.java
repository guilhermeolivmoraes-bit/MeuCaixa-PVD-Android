package com.oliveira.meucaixa.ui.auth;

import com.oliveira.meucaixa.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class WelcomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        Button buttonLogin = view.findViewById(R.id.button_login);
        Button buttonSignUp = view.findViewById(R.id.button_signup);

        buttonLogin.setOnClickListener(v -> {
            // Ação de navegação para a tela de Login (vamos criar no nav_graph)
            navController.navigate(R.id.action_welcomeFragment_to_loginFragment);
        });

        buttonSignUp.setOnClickListener(v -> {
            // Ação de navegação para a tela de Inscrição (vamos criar no nav_graph)
            navController.navigate(R.id.action_welcomeFragment_to_signUpFragment);
        });
    }
}