package com.mirambeau.termcalc;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BackupAdapter extends RecyclerView.Adapter<BackupAdapter.BackupViewHolder> {
    private final ArrayList<BackupCard> mCardList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void onApplyClick(int position);
        void onShareClick(int position);
        void onRenameClick(int position);
        void onFavoriteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public BackupAdapter(ArrayList<BackupCard> cardList) {
        mCardList = cardList;
    }

    public static class BackupViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ConstraintLayout buttonBG;
        ConstraintLayout titleBG;
        Button apply;
        ImageButton rename;
        ImageButton delete;
        ImageButton share;
        ImageButton favorite;

        public BackupViewHolder (View itemView, final OnItemClickListener listener){
            super(itemView);
            title = itemView.findViewById(R.id.themeName);
            buttonBG = itemView.findViewById(R.id.buttonBG);
            titleBG = itemView.findViewById(R.id.titleBG);
            apply = itemView.findViewById(R.id.apply);
            delete = itemView.findViewById(R.id.delete);
            share = itemView.findViewById(R.id.share);
            rename = itemView.findViewById(R.id.rename);
            favorite = itemView.findViewById(R.id.favorite);

            apply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            listener.onApplyClick(position);
                    }
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            listener.onDeleteClick(position);
                    }
                }
            });

            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            listener.onShareClick(position);
                    }
                }
            });

            rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            listener.onRenameClick(position);
                    }
                }
            });

            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            listener.onFavoriteClick(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public BackupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.backup_card, parent, false);

        return new BackupViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final BackupViewHolder holder, int position) {
        BackupCard current = mCardList.get(position);

        TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

        //cPrimary, cMinus, cMulti, cDiv, cKeypad, cNum
        String[] colors = current.getCardColors();
        String cEquals = current.getEqualsColor();

        holder.title.setText(current.getThemeName());

        try {
            if (current.getFavorite())
                holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
            else
                holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        catch (NullPointerException ignored) {}

        try {
            holder.titleBG.setBackgroundColor(Color.parseColor(colors[4]));
            holder.buttonBG.setBackgroundColor(Color.parseColor(colors[0]));

            if (Aux.isColor(cEquals) && !cEquals.equals(colors[4])) {
                if (Aux.getAverageBrightness(cEquals) == Aux.getAverageBrightness(colors[4])) {
                    if (Aux.getAverageBrightness(colors[4]) < 6) {
                        holder.apply.setTextColor(Color.WHITE);
                    }
                    else {
                        holder.apply.setTextColor(Color.parseColor("#222222"));
                    }
                }
                else {
                    holder.apply.setTextColor(Color.parseColor(cEquals));
                }
            }
            else {
                if (Aux.getAverageBrightness(colors[5]) == Aux.getAverageBrightness(colors[4])) {
                    if (Aux.getAverageBrightness(colors[4]) < 6) {
                        holder.apply.setTextColor(Color.WHITE);
                    }
                    else {
                        holder.apply.setTextColor(Color.parseColor("#222222"));
                    }
                }
                else {
                    holder.apply.setTextColor(Color.parseColor(colors[5]));
                }
            }

            holder.title.setTextColor(Color.parseColor(colors[5]));
            holder.share.setColorFilter(Color.parseColor(colors[3]));
            holder.delete.setColorFilter(Color.parseColor(colors[2]));
            holder.rename.setColorFilter(Color.parseColor(colors[1]));
            holder.favorite.setColorFilter(Color.parseColor(colors[1]));

            if (Aux.getAverageBrightness(colors[5]) == Aux.getAverageBrightness(colors[4])) {
                if (Aux.getAverageBrightness(colors[4]) < 6) {
                    holder.title.setTextColor(Color.WHITE);
                }
                else {
                    holder.title.setTextColor(Color.parseColor("#222222"));
                }
            }
        }
        catch (NullPointerException | IndexOutOfBoundsException e) {
            try {
                if (Aux.isColor(tinydb.getString("cKeypad")))
                    holder.titleBG.setBackgroundColor(Color.parseColor(tinydb.getString("cKeypad")));
                if (Aux.isColor(tinydb.getString("cPrimary")))
                    holder.buttonBG.setBackgroundColor(Color.parseColor(tinydb.getString("cPrimary")));

                if (Aux.isColor(tinydb.getString("-b=t")))
                    holder.apply.setTextColor(Color.parseColor(tinydb.getString("-b=t")));
                else if (Aux.isColor(tinydb.getString("cNum")))
                    holder.apply.setTextColor(Color.parseColor(tinydb.getString("cNum")));

                if (Aux.isColor(tinydb.getString("cNum")))
                    holder.title.setTextColor(Color.parseColor(tinydb.getString("cNum")));

                //What even is this
                if ((Aux.isTinyColor("cNum") && Aux.isTinyColor("cKeypad") && Aux.getTinyColor("cNum") == Aux.getTinyColor("cKeypad")) ||
                    Aux.getAverageBrightness(tinydb.getString("cNum")) == Aux.getAverageBrightness(tinydb.getString("cKeypad"))) {
                    if (Aux.getAverageBrightness(tinydb.getString("cKeypad")) < 6) {
                        holder.title.setTextColor(Color.WHITE);
                    }
                    else {
                        holder.title.setTextColor(Color.parseColor("#222222"));
                    }
                }
                else if (!Aux.isTinyColor("cKeypad") && !Aux.isTinyColor("cNum")){
                    int theme = Aux.getThemeInt();
                    if (theme == 2) {
                        holder.title.setTextColor(Color.parseColor("#222222"));
                    }
                    else if (theme == 5){
                        holder.title.setTextColor(Color.parseColor("#303030"));
                    }
                    else {
                        holder.title.setTextColor(Color.WHITE);
                    }
                }

                if (Aux.isColor(tinydb.getString("-b" + Aux.divi + "t")))
                    holder.share.setColorFilter(Color.parseColor(tinydb.getString("-b" + Aux.divi + "t")));
                if (Aux.isColor(tinydb.getString("-b" + Aux.multi + "t")))
                    holder.delete.setColorFilter(Color.parseColor(tinydb.getString("-b" + Aux.multi + "t")));
                if (Aux.isColor(tinydb.getString("-b-t"))) {
                    holder.rename.setColorFilter(Color.parseColor(tinydb.getString("-b-t")));
                    holder.favorite.setColorFilter(Color.parseColor(tinydb.getString("-b-t")));
                }

                if (tinydb.getString("cPrimary").equals(tinydb.getString("cKeypad"))) {
                    holder.buttonBG.setElevation(4);
                }
            }
            catch (NullPointerException | IndexOutOfBoundsException e2) {
                e2.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mCardList.size();
    }
}
