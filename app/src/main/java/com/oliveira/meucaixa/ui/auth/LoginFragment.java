package com.oliveira.meucaixa.ui.auth;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.utils.DateMaskTextWatcher;

// A importação da classe R não é mais necessária, pois o namespace do Gradle foi corrigido.

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class LoginFragment extends Fragment {

    private AuthViewModel authViewModel;
    private NavController navController;
    private EditText editTextName, editTextDob;
    private TextView textGreeting;
    private Button buttonConfirm, buttonSwitchUser;
    private LinearLayout signUpPromptContainer;
    private TextView buttonSignUpPrompt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        navController = Navigation.findNavController(view);

        bindViews(view);
        setupInitialState();
        setupClickListeners();
    }

    private void bindViews(View view) {
        editTextName = view.findViewById(R.id.edit_text_name_login);
        editTextDob = view.findViewById(R.id.edit_text_dob_login);
        textGreeting = view.findViewById(R.id.text_greeting);
        buttonConfirm = view.findViewById(R.id.button_confirm_login);
        buttonSwitchUser = view.findViewById(R.id.button_switch_user);
        signUpPromptContainer = view.findViewById(R.id.signup_prompt_container);
        buttonSignUpPrompt = view.findViewById(R.id.button_signup_prompt);
        editTextDob.addTextChangedListener(new DateMaskTextWatcher(editTextDob));
    }

    private void setupInitialState() {
        String registeredUserName = getArguments() != null ? getArguments().getString("registeredUserName") : null;
        if (registeredUserName != null) {
            showGreetingState(registeredUserName);
        } else {
            authViewModel.getLastUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    showGreetingState(user.getFullName());
                } else {
                    showFullLoginState(true); // Mostra o prompt para se inscrever
                }
            });
        }
    }

    private void setupClickListeners() {
        buttonConfirm.setOnClickListener(v -> loginUser());
        buttonSwitchUser.setOnClickListener(v -> showFullLoginState(false));
        buttonSignUpPrompt.setOnClickListener(v -> {
            navController.navigate(R.id.action_loginFragment_to_signUpFragment);
        });
    }

    private void loginUser() {
        String nome = editTextName.getText().toString().trim();
        String dob = editTextDob.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || dob.length() != 10) {
            Toast.makeText(getContext(), "Por favor, preencha todos os campos corretamente", Toast.LENGTH_SHORT).show();
            return;
        }

        authViewModel.login(nome, dob, new AuthViewModel.LoginCallback() {
            @Override
            public void onSuccess() {
                navController.navigate(R.id.action_loginFragment_to_main_app);
            }

            @Override
            public void onFailure() {
                Toast.makeText(getContext(), "Usuário ou data de nascimento inválidos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGreetingState(String userName) {
        textGreeting.setText("Olá, " + userName);
        textGreeting.setVisibility(View.VISIBLE);
        editTextName.setText(userName);
        editTextName.setVisibility(View.GONE);
        buttonSwitchUser.setVisibility(View.VISIBLE);
        signUpPromptContainer.setVisibility(View.GONE);
        editTextDob.requestFocus();
    }

    private void showFullLoginState(boolean showSignUpPrompt) {
        textGreeting.setVisibility(View.GONE);
        editTextName.setVisibility(View.VISIBLE);
        editTextName.setText("");
        editTextDob.setText("");
        buttonSwitchUser.setVisibility(View.GONE);
        signUpPromptContainer.setVisibility(showSignUpPrompt ? View.VISIBLE : View.GONE);
        editTextName.requestFocus();
    }
}
