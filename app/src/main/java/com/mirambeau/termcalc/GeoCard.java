package com.mirambeau.termcalc;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class GeoCard {
    private String title;
    private String answerText = " ";
    public TextView titleText;
    public String[] paramHints = {"Param1", "Param2", "Param3"};
    public String[] inputTexts;

    public GeoCard(){
        this.title = "Shape Title";
    }

    public GeoCard (String title, String[] hints){
        this.title = title;
        paramHints = hints;
    }

    public ConstraintLayout getBackground(){
        if (titleText != null && titleText.getParent() != null)
            return (ConstraintLayout) titleText.getParent();
        else
            return null;
    }

    public ConstraintLayout setBackgroundColor(String color){
        ConstraintLayout card = getBackground();

        card.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));

        return card;
    }

    public void setShapeTitle(String name){
        title = name;
    }

    public String getShapeTitle(){
        return title;
    }

    public void setParamHint(int index, String hint){
        paramHints[index] = hint;
    }

    public void setParamHints(String[] hints){
        paramHints = hints;
    }

    public String getParamHint(int index){
        if (index < paramHints.length)
            return paramHints[index];
        else
            return " ";
    }

    public String[] getParamHints(){
        return paramHints;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setInputTexts(String[] inputTexts, String title) {
        int i;

        this.inputTexts = new String[inputTexts.length + 1];

        for (i=0; i < inputTexts.length; i++){
            this.inputTexts[i] = inputTexts[i];
        }

        this.inputTexts[this.inputTexts.length - 1] = title;
    }

    public String[] getInputTexts() {
        return inputTexts;
    }
}
