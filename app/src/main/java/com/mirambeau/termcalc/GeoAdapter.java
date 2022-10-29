package com.mirambeau.termcalc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import io.github.kexanie.library.MathView;

public class GeoAdapter extends RecyclerView.Adapter<GeoAdapter.GeoViewHolder> {
    private static ArrayList<GeoCard> mCardList;
    private OnItemClickListener mListener;

    public final int maxParams = 3;

    public static String formColor;
    public static String[] formTexts;
    public String accentColor = "#03DAC5";

    public interface OnItemClickListener {
        void onEqualsClick(int position, String[] inputs, TextInputLayout[] params);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class GeoViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView answer;
        ConstraintLayout layout;
        Button calculate;
        TextInputLayout[] params;
        MathView formula;

        public GeoViewHolder (View itemView, final OnItemClickListener listener){
            super(itemView);
            title = itemView.findViewById(R.id.shapeTitle);
            layout = itemView.findViewById(R.id.shapeLayout);
            calculate = itemView.findViewById(R.id.shapeEquals);
            answer = itemView.findViewById(R.id.shapeAnswer);
            formula = itemView.findViewById(R.id.shapeFormula);

            params = new TextInputLayout[]{itemView.findViewById(R.id.param1), itemView.findViewById(R.id.param2), itemView.findViewById(R.id.param3)};

            calculate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int i;
                        int position = getAdapterPosition();

                        ArrayList<String> inputs = new ArrayList<>();
                        int[] ids = {R.id.param1Input, R.id.param2Input, R.id.param3Input};

                        for (i = 0; i < params.length; i++){
                            inputs.add(((EditText) params[i].findViewById(ids[i])).getText().toString());
                        }

                        if (position != RecyclerView.NO_POSITION) {
                            listener.onEqualsClick(position, inputs.toArray(new String[0]), params);
                        }
                    }
                }
            });
        }
    }

    public GeoAdapter(ArrayList<GeoCard> cardList) {
        mCardList = cardList;
    }

    @NonNull
    @Override
    public GeoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.geo_card, parent, false);

        return new GeoViewHolder(v, mListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final GeoViewHolder holder, final int position) {
        final GeoCard current = mCardList.get(position);
        TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

        String theme = tinydb.getString("theme");

        int i;
        String textColor = "#FFFFFF";
        String answerText = current.getAnswerText();

        holder.title.setText(current.getShapeTitle());

        final String[] inputs = current.getInputTexts();

        if (theme.equals("2")) {
            textColor = "#222222";

            holder.layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.title.setTextColor(Color.parseColor(textColor));
        }
        else if (theme.equals("3") || theme.equals("4")) {
            holder.layout.setBackgroundColor(Color.parseColor("#111111"));
            holder.title.setTextColor(Color.parseColor(textColor));
        }

        final String[] params = current.getParamHints();

        for (i = 0; i < params.length; i++) {
            holder.params[i].setVisibility(View.VISIBLE);
            holder.params[i].setHint(params[i]);
        }

        for (i = i; i < maxParams; i++) {
            holder.params[i].setVisibility(View.GONE);
        }

        boolean isCustomTheme = tinydb.getBoolean("custom");

        if (!isCustomTheme && Ax.isColor(tinydb.getString("accentPrimary")))
            accentColor = tinydb.getString("accentPrimary");

        String bigColor = accentColor;

        if (theme.equals("5") && Ax.isColor(tinydb.getString("accentPrimary")) && !isCustomTheme)
            bigColor = tinydb.getString("accentPrimary");
        else if (isCustomTheme) {
            if (Ax.isColor(tinydb.getString("cPrimary")) && !Ax.isGray(tinydb.getString("cPrimary")))
                bigColor = tinydb.getString("cPrimary");
            else if (Ax.isColor(tinydb.getString("cSecondary")) && !Ax.isGray(tinydb.getString("cSecondary")))
                bigColor = tinydb.getString("cSecondary");
            else if (Ax.isColor(tinydb.getString("-b=t")) && !Ax.isGray(tinydb.getString("-b=t")))
                bigColor = tinydb.getString("-b=t");
            else if (Ax.isColor(tinydb.getString("cTop")) && !Ax.isGray(tinydb.getString("cTop")))
                bigColor = tinydb.getString("cTop");
            else if (Ax.isColor(tinydb.getString("cTertiary")) && !Ax.isGray(tinydb.getString("cTertiary")))
                bigColor = tinydb.getString("cTertiary");
            else if (Ax.isColor(tinydb.getString("-b+t")) && !Ax.isGray(tinydb.getString("-b+t")))
                bigColor = tinydb.getString("-b+t");
        }

        holder.calculate.setTextColor(Color.parseColor(bigColor));

        if (answerText != null)
            holder.answer.setText(answerText);

        if (inputs != null && holder.title != null && holder.title.getText() != null && inputs[inputs.length - 1] != null && holder.title.getText().toString().equals(inputs[inputs.length - 1])) {
            for (i = 0; i < params.length; i++) {
                if (inputs[i] != null && holder.params != null && holder.params[i] != null) {
                    EditText editText = holder.params[i].getEditText();

                    if (editText != null) {
                        editText.setText(inputs[i]);
                        editText.setSelection(inputs[i].length());
                    }
                }
            }
        }
        else if (holder.params != null) {
            for (i = 0; i < params.length; i++) {
                if (holder.params != null && holder.params[i] != null) {
                    EditText editText = holder.params[i].getEditText();

                    if (editText != null) {
                        editText.setText("");
                    }
                }
            }
        }

        holder.formula.config(
                "MathJax.Hub.Config({\n" +
                        "  { TeX: { extensions: [\"color.js\"] } }\n" +
                        "});"
        );

        holder.formula.setText(formColor + formTexts[position]);

        for (i = 0; i < holder.params.length; i++) {
            if (holder.params[i] != null) {
                EditText editText = holder.params[i].getEditText();

                //Enter key setup
                if (editText != null) {
                    editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            v.clearFocus();

                            holder.calculate.performClick();

                            InputMethodManager imm = (InputMethodManager) MainActivity.mainActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);

                            if (MainActivity.mainActivity.getWindow().getCurrentFocus() != null)
                                imm.hideSoftInputFromWindow(MainActivity.mainActivity.getWindow().getCurrentFocus().getApplicationWindowToken(), 0);

                            return false;
                        }
                    });
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mCardList.size();
    }

    public void setFormColor(String formColor) {
        this.formColor = formColor;
    }

    public void setFormTexts(String[] formTexts) {
        this.formTexts = formTexts;
    }
}
