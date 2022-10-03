package com.mirambeau.termcalc;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class GeoItem {
    public TextView title;
    public EditText field;

    public void hide(){
        if (title != null && field != null) {
            title.setVisibility(View.GONE);
            field.setVisibility(View.GONE);
        }
    }

    public void show(){
        if (title != null && field != null) {
            title.setVisibility(View.VISIBLE);
            field.setVisibility(View.VISIBLE);
        }
    }
}
