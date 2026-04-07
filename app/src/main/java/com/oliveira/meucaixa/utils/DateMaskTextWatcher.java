package com.oliveira.meucaixa.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class DateMaskTextWatcher implements TextWatcher {

    private final EditText editText;
    private String current = "";

    public DateMaskTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().equals(current)) {
            return;
        }

        String clean = s.toString().replaceAll("[^\\d]", "");
        String formatted = clean;

        if (clean.length() > 4) {
            formatted = clean.substring(0, 2) + "/" + clean.substring(2, 4) + "/" + clean.substring(4, Math.min(clean.length(), 8));
        } else if (clean.length() > 2) {
            formatted = clean.substring(0, 2) + "/" + clean.substring(2);
        }

        current = formatted;
        editText.setText(current);
        editText.setSelection(current.length());
    }
}