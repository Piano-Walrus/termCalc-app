package com.mirambeau.termcalc;

public class EditorCard {
    String title = "~", code = "~", bgColor = "~", textColor = "~";

    String defaultBGColor = "#03DAC5";
    String defaultTextColor = "#FFFFFF";

    public EditorCard(String title, String code, String bgColor, String textColor, String defaultBGColor, String defaultTextColor){
        if (title != null && !title.equals("\0") && !title.equals(""))
            this.title = title;

        if (code != null && !code.equals("\0") && !code.equals(""))
            this.code = code;

        if (bgColor != null && !bgColor.equals("\0") && !bgColor.equals(""))
            this.bgColor = bgColor;

        if (textColor != null && !textColor.equals("\0") && !textColor.equals(""))
            this.textColor = textColor;

        if (defaultBGColor != null && !defaultBGColor.equals("\0") && !defaultBGColor.equals(""))
            this.defaultBGColor = defaultBGColor;

        if (defaultTextColor != null && !defaultTextColor.equals("\0") && !defaultTextColor.equals(""))
            this.defaultTextColor = defaultTextColor;
    }
}
