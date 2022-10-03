package com.mirambeau.termcalc;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryDateGroupAdapter extends RecyclerView.Adapter<HistoryDateGroupAdapter.ViewHolder> {
    ArrayList<String> groupTitles;
    ArrayList<HistoryAdapter> adapters;

    public HistoryDateGroupAdapter(ArrayList<String> groupTitles, ArrayList<HistoryAdapter> adapters){
        this.groupTitles = groupTitles;
        this.adapters = adapters;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        CardView cardBG;
        RecyclerView recyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.dateGroupTitle);
            cardBG = itemView.findViewById(R.id.historyCard);
            recyclerView = itemView.findViewById(R.id.dateGroupRv);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_group, parent, false);

        return new HistoryDateGroupAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TinyDB tinydb = Aux.tinydb();
        String theme = tinydb.getString("theme");

        final int darkGray = Color.parseColor("#3C4043");

        holder.title.setText(groupTitles.get(position));

        if (tinydb.getBoolean("custom"))
            holder.title.setTextColor(Color.parseColor(Aux.getNavAccentColor()));
        else {
            if (Aux.isColor(tinydb.getString("accentPrimary")))
                holder.title.setTextColor(Color.parseColor(tinydb.getString("accentPrimary")));
            else {
                if (theme.equals("2"))
                    holder.title.setTextColor(darkGray);
                else
                    holder.title.setTextColor(Color.WHITE);
            }
        }

        if (theme.equals("2"))
            holder.cardBG.setCardBackgroundColor(Color.WHITE);
        else if (!theme.equals("1"))
            holder.cardBG.setCardBackgroundColor(Color.parseColor("#111111"));

        holder.recyclerView.setHasFixedSize(true);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.mainActivity) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        holder.recyclerView.setAdapter(adapters.get(position));

        if (holder.recyclerView.getAdapter() != null && holder.recyclerView.getAdapter().getItemCount() < 1){
            holder.title.setVisibility(View.GONE);
            holder.cardBG.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return groupTitles.size();
    }
}


