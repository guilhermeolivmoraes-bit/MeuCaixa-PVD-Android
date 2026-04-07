package com.oliveira.meucaixa.ui.auth;

import com.oliveira.meucaixa.R;
import com.oliveira.meucaixa.utils.DateMaskTextWatcher;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class SignUpFragment extends Fragment {

    private AuthViewModel authViewModel;
    private NavController navController;
    private EditText editTextName, editTextDob;
    private Button buttonConfirm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        navController = Navigation.findNavController(view);

        editTextName = view.findViewById(R.id.edit_text_name_signup);
        editTextDob = view.findViewById(R.id.edit_text_dob_signup);
        buttonConfirm = view.findViewById(R.id.button_confirm_signup);

        editTextDob.addTextChangedListener(new DateMaskTextWatcher(editTextDob));

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateSignUpButton();
            }
        };

        editTextName.addTextChangedListener(textWatcher);
        editTextDob.addTextChangedListener(textWatcher);

        validateSignUpButton(); // Valida o estado inicial

        buttonConfirm.setOnClickListener(v -> {
            String nome = editTextName.getText().toString().trim();
            String dob = editTextDob.getText().toString().trim();

            // A validação de clique agora só precisa verificar se o botão está habilitado
            if (!buttonConfirm.isEnabled()) {
                Toast.makeText(getContext(), "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.signUp(nome, dob, () -> {
                Toast.makeText(getContext(), "Usuário cadastrado!", Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                bundle.putString("registeredUserName", nome);
                navController.navigate(R.id.action_signUpFragment_to_loginFragment, bundle);
            });
        });
    }

    private void validateSignUpButton() {
        String nome = editTextName.getText().toString().trim();
        String dob = editTextDob.getText().toString().trim();
        boolean isEnabled = !TextUtils.isEmpty(nome) && dob.length() == 10;

        buttonConfirm.setEnabled(isEnabled);
        buttonConfirm.setBackgroundColor(isEnabled ? ContextCompat.getColor(getContext(), R.color.green_500) : ContextCompat.getColor(getContext(), R.color.gray_400));
    }
}
