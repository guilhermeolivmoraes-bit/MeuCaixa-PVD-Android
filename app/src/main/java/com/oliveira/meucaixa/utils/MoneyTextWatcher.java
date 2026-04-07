package com.oliveira.meucaixa.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class MoneyTextWatcher implements TextWatcher {

    private final WeakReference<EditText> editTextReference;
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public MoneyTextWatcher(EditText editText) {
        this.editTextReference = new WeakReference<>(editText);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) {
        EditText editText = editTextReference.get();
        if (editText == null) return;

        editText.removeTextChangedListener(this);

        // Limpa a string para conter apenas dígitos
        String cleanString = s.toString().replaceAll("[^\\d]", "");

        BigDecimal parsed;
        if (cleanString.isEmpty()) {
            parsed = BigDecimal.ZERO;
        } else {
            parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
        }

        // Formata o número como moeda e remove o símbolo (R$)
        String formatted = currencyFormat.format(parsed);
        String replaceable = String.format("[%s\\s]", currencyFormat.getCurrency().getSymbol());
        String cleanFormatted = formatted.replaceAll(replaceable, "").trim();

        editText.setText(cleanFormatted);
        editText.setSelection(cleanFormatted.length());

        editText.addTextChangedListener(this);
    }
}
