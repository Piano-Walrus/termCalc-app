package com.mirambeau.termcalc;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public abstract class TextValidator implements TextWatcher {
    private final TextView textView;
    private String before;

    public TextValidator(TextView textView) {
        this.textView = textView;
    }

    public abstract void validate(TextView textView, String before, String after);

    @Override
    final public void afterTextChanged(Editable s) {
        String after;

        try {
            after = textView.getText().toString();
        }
        catch (Exception e) {
            after = "";
        }

        if (!before.equals(after))
            validate(textView, before, textView.getText().toString());
    }

    @Override
    final public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        try {
            before = textView.getText().toString();
        }
        catch (Exception e) {
            before = "";
        }
    }

    @Override
    final public void onTextChanged(CharSequence s, int start, int before, int count) {
        //do nothing
    }
}