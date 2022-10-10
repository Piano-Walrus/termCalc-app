package com.mirambeau.termcalc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FunctionsAdapter extends RecyclerView.Adapter<FunctionsAdapter.ViewHolder> {
    ArrayList<FunctionCard> cards;
    ArrayList<VariablesAdapter> adapters;
    FunctionsAdapter.OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onOverflowClick(int position, View anchor);
        void onCardClick(int position, RecyclerView recyclerView, CardView card, TextView functionTv, Button insert, ImageButton copy, ImageButton expand);
    }

    public void setOnItemClickListener(FunctionsAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public FunctionsAdapter(ArrayList<FunctionCard> cards, ArrayList<VariablesAdapter> adapters){
        this.cards = cards;
        this.adapters = adapters;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, function;
        Button insert;
        ImageButton expand, copy;
        ImageButton overflow;
        CardView card;
        RecyclerView recyclerView;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            title = itemView.findViewById(R.id.functionTitle);
            function = itemView.findViewById(R.id.functionText);

            insert = itemView.findViewById(R.id.functionInsert);
            copy = itemView.findViewById(R.id.functionCopy);

            expand = itemView.findViewById(R.id.functionExpand);
            overflow = itemView.findViewById(R.id.functionOverflow);

            card = itemView.findViewById(R.id.functionCard);

            recyclerView = itemView.findViewById(R.id.functionRecyclerView);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            listener.onCardClick(position, recyclerView, card, function, insert, copy, expand);
                        }
                    }
                }
            });

            overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            listener.onOverflowClick(position, v);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.function_rv_card, parent, false);

        return new FunctionsAdapter.ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final TinyDB tinydb = new TinyDB(MainActivity.mainActivity);
        FunctionCard current = cards.get(position);

        int i;
        final int darkGray = Color.parseColor("#3C4043");

        String functionText = current.function.replace("*", Aux.multiDot).replace(Aux.divi, "/");

        holder.title.setText(current.title);

        for (i=0; i < functionText.length(); i++){
            if (functionText.length() >= i + 1 && Aux.chat(functionText, i).equals("^") && (Aux.isDigit(Aux.chat(functionText, i + 1)) || Aux.isLetter(Aux.chat(functionText, i + 1)))) {
                int j;

                functionText = Aux.newReplace(i, functionText, "");

                for (j=i; j < functionText.length(); j++) {
                    String currentStr = Aux.chat(functionText, j);

                    if (currentStr != null && (Aux.isDigit(currentStr) || currentStr.equals(".") || Aux.isLetter(currentStr)))
                        Aux.newReplace(j, functionText, Aux.toSuper(Aux.chat(functionText, j)));
                    else
                        break;
                }

                i = j;
            }
        }

        String functionBackup = functionText;

        try {
            for (i=functionText.indexOf("/"); i < functionText.length(); i++) {
                if(i == -1)
                    break;

                if (Aux.isDigit(Aux.chat(functionText, i+1)) && Aux.isDigit(Aux.chat(functionText, i-1)))
                    Aux.newReplace(i, functionText, "⁄");
            }
        }
        catch (Exception e) {
            e.printStackTrace();

            functionText = functionBackup;
        }

        holder.function.setText(functionText);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
        String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

        if (theme == null || !Aux.isDigit(theme))
            theme = "1";

        if (theme.equals("2")) {
            holder.title.setTextColor(darkGray);
            holder.function.setTextColor(darkGray);
            holder.overflow.setColorFilter(darkGray);
            holder.card.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        }
        else {
            holder.title.setTextColor(Color.WHITE);
            holder.function.setTextColor(Color.WHITE);

            if (!theme.equals("1"))
                holder.card.setCardBackgroundColor(Color.parseColor("#111111"));
        }

        String accentColor;
        boolean isCustomTheme = tinydb.getBoolean("custom");

        if (!isCustomTheme && Aux.isColor(tinydb.getString("accentPrimary")))
            accentColor = tinydb.getString("accentPrimary");
        else
            accentColor = "#FFFFFF";

        String bigColor = accentColor;

        if (theme.equals("5") && Aux.isColor(tinydb.getString("accentPrimary")) && !isCustomTheme)
            bigColor = tinydb.getString("accentSecondary");
        else if (isCustomTheme) {
            if (Aux.isColor(tinydb.getString("cFabText")) && !Aux.isGray(tinydb.getString("cFabText")))
                bigColor = tinydb.getString("cFabText");
            else if (Aux.isColor(tinydb.getString("cFab")) && !Aux.isGray(tinydb.getString("cFab")))
                bigColor = tinydb.getString("cFab");
            else if (Aux.isColor(tinydb.getString("-b=t")) && !Aux.isGray(tinydb.getString("-b=t")))
                bigColor = tinydb.getString("-b=t");
            else if (Aux.isColor(tinydb.getString("cPrimary")) && !Aux.isGray(tinydb.getString("cPrimary")))
                bigColor = tinydb.getString("cPrimary");
            else if (Aux.isColor(tinydb.getString("cSecondary")) && !Aux.isGray(tinydb.getString("cSecondary")))
                bigColor = tinydb.getString("cSecondary");
            else if (Aux.isColor(tinydb.getString("cTop")) && !Aux.isGray(tinydb.getString("cTop")))
                bigColor = tinydb.getString("cTop");
            else if (Aux.isColor(tinydb.getString("cTertiary")) && !Aux.isGray(tinydb.getString("cTertiary")))
                bigColor = tinydb.getString("cTertiary");
            else if (Aux.isColor(tinydb.getString("-b+t")) && !Aux.isGray(tinydb.getString("-b+t")))
                bigColor = tinydb.getString("-b+t");
        }
        else {
            if (!Aux.isColor(bigColor))
                bigColor = "#FFFFFF";
        }

        int buttonColor = Color.parseColor(bigColor);

        holder.insert.setTextColor(buttonColor);
        holder.copy.setColorFilter(buttonColor);

        holder.expand.setColorFilter(theme.equals("2") ? darkGray : Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void expandCard(int position) {
        cards.get(position).setIsExpanded(true);
    }

    //Show the keyboard from a dialog
    public static void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    //Hide the keyboard from a dialog
    public void hideKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}


