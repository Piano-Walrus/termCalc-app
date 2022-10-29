package com.mirambeau.termcalc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class VariablesAdapter extends RecyclerView.Adapter<VariablesAdapter.ViewHolder> {
    String[] variables;
    String[] values;
    ArrayList<EditText> editTexts = new ArrayList<>();
    VariablesAdapter.OnItemTextChangedListener mListener;
    VariablesAdapter.OnItemEditorActionListener editorActionListener;
    int functionPosition;

    public interface OnItemTextChangedListener {
        void onTextChanged(int position, EditText editText);
    }

    public interface OnItemEditorActionListener {
        void onItemEditorAction(int position, EditText editText, int actionId);
    }

    public void setOnItemTextChangedListener(VariablesAdapter.OnItemTextChangedListener listener) {
        mListener = listener;
    }

    public void setOnItemEditorActionListener(VariablesAdapter.OnItemEditorActionListener listener) {
        editorActionListener = listener;
    }

    public VariablesAdapter(String[] variables, int functionPosition){
        this.variables = variables;
        this.values = new String[variables.length];
        this.functionPosition = functionPosition;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextInputLayout textInputLayout;
        EditText editText;

        public ViewHolder(@NonNull View itemView, final OnItemTextChangedListener listener, final OnItemEditorActionListener editorActionListener) {
            super(itemView);

            editText = itemView.findViewById(R.id.variableInput);
            textInputLayout = itemView.findViewById(R.id.variableLayout);

            editText.addTextChangedListener(new TextValidator(editText) {
                @Override
                public void validate(TextView textView, String before, String after) {
                    if (listener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            listener.onTextChanged(position, editText);
                    }
                }
            });

            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (editorActionListener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            editorActionListener.onItemEditorAction(position, editText, actionId);
                    }

                    return false;
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.enter_variable, parent, false);

        return new VariablesAdapter.ViewHolder(v, mListener, editorActionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final TinyDB tinydb = new TinyDB(MainActivity.mainActivity);
        String current = variables[position];
        final EditText editText = holder.editText;
        final TextInputLayout textInputLayout = holder.textInputLayout;

        int i;
        final int darkGray = Color.parseColor("#3C4043");

        textInputLayout.setHint(current);

        if (Ax.getThemeInt() == 2) {
            editText.setHintTextColor(Color.DKGRAY);
            editText.setTextColor(darkGray);

            editText.setBackgroundTintList(ColorStateList.valueOf(darkGray));
        }
        else
            editText.setHintTextColor(Color.LTGRAY);

        editTexts.add(editText);

        if (position == 0) {
            showKeyboard();

            editText.requestFocus();
        }
    }

    @Override
    public int getItemCount() {
        return variables.length;
    }

    //Show the keyboard from a dialog
    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    //Hide the keyboard from a dialog
    public void hideKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}


