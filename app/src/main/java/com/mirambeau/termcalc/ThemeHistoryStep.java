package com.mirambeau.termcalc;

import android.view.View;

public class ThemeHistoryStep {
    View view;
    String code, initColor, finalColor;

    public ThemeHistoryStep(View view, String code, String initColor, String finalColor){
        if (view == null)
            this.view = EditorActivity.editorActivity.findViewById(R.id.preview);
        else
            this.view = view;

        if (initColor == null || initColor.equals("\0"))
            this.initColor = "";
        else
            this.initColor = initColor;

        if (finalColor == null || finalColor.equals("\0"))
            this.finalColor = "";
        else
            this.finalColor = finalColor;

        if (code == null || code.equals("\0"))
            this.code = "";
        else
            this.code = code;
    }
}
