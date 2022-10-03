package com.mirambeau.termcalc;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ClipboardColorAdapter extends RecyclerView.Adapter<ClipboardColorAdapter.ViewHolder> {
    ArrayList<String> colors;
    ClipboardColorAdapter.OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onColorClick(int position);
    }

    public void setOnItemClickListener(ClipboardColorAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public ClipboardColorAdapter(ArrayList<String> colors){
        this.colors = colors;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout button;
        ImageButton color;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            button = itemView.findViewById(R.id.cpLayout);
            color = itemView.findViewById(R.id.bgPrimary2);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            listener.onColorClick(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_circle, parent, false);

        return new ClipboardColorAdapter.ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String current = colors.get(position);

        holder.color.setColorFilter(Color.parseColor("#" + current.replace("#", "")));
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }
}


