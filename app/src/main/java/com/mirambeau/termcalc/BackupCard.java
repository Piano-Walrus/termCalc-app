package com.mirambeau.termcalc;

import androidx.appcompat.app.AppCompatActivity;

public class BackupCard extends AppCompatActivity {
    private final String title;
    private boolean isFavorite;

    //cPrimary, cMinus, cMulti, cDiv, cKeypad, cNum
    String[] cardColors = new String[6];

    String cEqualsColor;

    public BackupCard(String title) {
        this.title = title;
    }

    public BackupCard(String title, String[] colors) {
        this.title = title;
        this.cardColors = colors;
    }

    public BackupCard(String title, String[] colors, String cEquals) {
        this.title = title;
        this.cardColors = colors;
        this.cEqualsColor = cEquals;
    }

    public String getThemeName() {
        return title;
    }

    public void setCardColors(String[] colors){
        if (colors != null) {
            cardColors = colors;
        }
    }

    public String[] getCardColors() {
        return cardColors;
    }

    public void setEqualsColor(String equalsColor) {
        cEqualsColor = equalsColor;
    }

    public String getEqualsColor(){
        return cEqualsColor;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean getFavorite() {
        return isFavorite;
    }
}