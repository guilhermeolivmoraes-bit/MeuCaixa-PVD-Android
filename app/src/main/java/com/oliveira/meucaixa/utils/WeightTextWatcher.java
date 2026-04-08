package com.oliveira.meucaixa.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class WeightTextWatcher implements TextWatcher {

    private final WeakReference<EditText> editTextReference;
    private static final DecimalFormat weightFormat;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        weightFormat = new DecimalFormat("#,##0.000", symbols);
    }

    public WeightTextWatcher(EditText editText) {
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

        String cleanString = s.toString().replaceAll("[^\\d]", "");
        BigDecimal parsed;
        if (cleanString.isEmpty()) {
            parsed = BigDecimal.ZERO;
        } else {
            parsed = new BigDecimal(cleanString).setScale(3, java.math.RoundingMode.FLOOR).divide(new BigDecimal(1000), java.math.RoundingMode.FLOOR);
        }

        String formatted = weightFormat.format(parsed);

        editText.setText(formatted);
        editText.setSelection(formatted.length());

        editText.addTextChangedListener(this);
    }
}
