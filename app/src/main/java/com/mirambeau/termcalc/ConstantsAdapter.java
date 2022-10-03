package com.mirambeau.termcalc;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ConstantsAdapter extends RecyclerView.Adapter<ConstantsAdapter.ViewHolder> {
    ArrayList<ConstantCard> cards;
    ConstantsAdapter.OnItemClickListener mListener;

    boolean isRv;

    public interface OnItemClickListener {
        void onOverflowClick(int position, View anchor);
        void onCopyClick(int position);
        void onPasteClick(int position);
    }

    public void setOnItemClickListener(ConstantsAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public ConstantsAdapter (ArrayList<ConstantCard> cards, boolean isRv){
        this.cards = cards;
        this.isRv = isRv;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, constant, unit;
        ImageButton copy, paste, overflow;
        ConstraintLayout card;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            title = itemView.findViewById(R.id.constantTitle);
            constant = itemView.findViewById(R.id.constantNum);
            unit = itemView.findViewById(R.id.constantUnits);

            copy = itemView.findViewById(R.id.constantCopy);
            paste = itemView.findViewById(R.id.constantPaste);
            overflow = itemView.findViewById(R.id.constantOverflow);

            card = itemView.findViewById(R.id.constantLayout);

            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            listener.onCopyClick(position);
                    }
                }
            });

            paste.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            listener.onPasteClick(position);
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.constants_rv_card, parent, false);

        return new ConstantsAdapter.ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TinyDB tinydb = new TinyDB(MainActivity.mainActivity);
        ConstantCard current = cards.get(position);

        final int darkGray = Color.parseColor("#3C4043");

        holder.title.setText(current.title);
        holder.constant.setText(parseExponents(current.constant));
        holder.unit.setText(parseExponents(current.unit));

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
        String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

        if (theme == null || !Aux.isDigit(theme))
            theme = "1";

        if (theme.equals("2")) {
            holder.title.setTextColor(darkGray);
            holder.constant.setTextColor(darkGray);
            holder.unit.setTextColor(darkGray);
            holder.overflow.setColorFilter(darkGray);
            holder.card.setBackgroundColor(Color.WHITE);
        }
        else {
            holder.title.setTextColor(Color.WHITE);
            holder.constant.setTextColor(Color.WHITE);
            holder.unit.setTextColor(Color.WHITE);

            if (!theme.equals("1"))
                holder.card.setBackgroundColor(Color.parseColor("#111111"));
        }

        String accentColor;
        boolean isCustomTheme = tinydb.getBoolean("custom");

        if (!isCustomTheme && Aux.isTinyColor("accentPrimary"))
            accentColor = tinydb.getString("accentPrimary");
        else
            accentColor = "#FFFFFF";

        String bigColor = accentColor;

        if (theme.equals("5") && Aux.isTinyColor("accentPrimary") && !isCustomTheme)
            bigColor = tinydb.getString("accentSecondary");
        else if (isCustomTheme) {
            if (Aux.isTinyColor("cFabText") && !Aux.isGray(tinydb.getString("cFabText")))
                bigColor = tinydb.getString("cFabText");
            else if (Aux.isTinyColor("cFab") && !Aux.isGray(tinydb.getString("cFab")))
                bigColor = tinydb.getString("cFab");
            else if (Aux.isTinyColor("-b=t") && !Aux.isGray(tinydb.getString("-b=t")))
                bigColor = tinydb.getString("-b=t");
            else if (Aux.isTinyColor("cPrimary") && !Aux.isGray(tinydb.getString("cPrimary")))
                bigColor = tinydb.getString("cPrimary");
            else if (Aux.isTinyColor("cSecondary") && !Aux.isGray(tinydb.getString("cSecondary")))
                bigColor = tinydb.getString("cSecondary");
            else if (Aux.isTinyColor("cTop") && !Aux.isGray(tinydb.getString("cTop")))
                bigColor = tinydb.getString("cTop");
            else if (Aux.isTinyColor("cTertiary") && !Aux.isGray(tinydb.getString("cTertiary")))
                bigColor = tinydb.getString("cTertiary");
            else if (Aux.isTinyColor("-b+t") && !Aux.isGray(tinydb.getString("-b+t")))
                bigColor = tinydb.getString("-b+t");
        }
        else {
            if (!Aux.isColor(bigColor))
                bigColor = "#FFFFFF";
        }

        int buttonColor = Color.parseColor(bigColor);

        holder.copy.setColorFilter(buttonColor);
        holder.paste.setColorFilter(buttonColor);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public String parseExponents(String str) {
        if (Aux.isNull(str))
            return "";

        int i, j;

        for (i=0; i < Aux.countChars(str, "^"); i++){
            for (j=Aux.searchFor(str, "^"); j < str.length(); j++){
                if (Aux.chat(str, j).equals("^"))
                    str = Aux.newReplace(j, str, "");

                String currentChar = Aux.chat(str, j);

                if (currentChar.equals("-"))
                    str = Aux.newReplace(j, str, "⁻");
                else if (Aux.isDigit(currentChar))
                    str = Aux.newReplace(j, str, Aux.superscripts[Integer.parseInt(currentChar)]);
                else if (currentChar.equals("e"))
                    str = Aux.newReplace(j, str, "ᵉ");
                else if (currentChar.equals("."))
                    str = Aux.newReplace(j, str, "⋅");
                else
                    break;
            }
        }

        return str;
    }
}


