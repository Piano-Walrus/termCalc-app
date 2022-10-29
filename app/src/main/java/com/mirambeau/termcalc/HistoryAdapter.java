package com.mirambeau.termcalc;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    ArrayList<HistoryEntry> entries;
    HistoryAdapter.OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onAnswerClick(int position);
        void onEquationClick(int position);
    }

    public void setOnItemClickListener(HistoryAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public HistoryAdapter(ArrayList<HistoryEntry> entries){
        this.entries = entries;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView answer, equation;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            answer = itemView.findViewById(R.id.answer);
            equation = itemView.findViewById(R.id.eq);

            answer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            listener.onAnswerClick(position);
                    }
                }
            });

            equation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            listener.onEquationClick(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_entry, parent, false);

        return new HistoryAdapter.ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryEntry current = entries.get(position);

        final int darkGray = Color.parseColor("#3C4043");

        holder.answer.setText(current.answer);
        holder.equation.setText(current.equation);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
        String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

        if (theme == null || !Ax.isDigit(theme))
            theme = "1";

        if (theme.equals("2")) {
            holder.answer.setTextColor(darkGray);
            holder.equation.setTextColor(darkGray);
        }
        else {
            holder.answer.setTextColor(Color.WHITE);
            holder.equation.setTextColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }
}


