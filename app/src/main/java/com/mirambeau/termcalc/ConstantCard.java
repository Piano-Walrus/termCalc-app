package com.mirambeau.termcalc;

public class ConstantCard {
    String title, constant, unit;

    public ConstantCard(String title, String constant, String unit){
        this.title = title;

        if (title == null || title.equals("\0"))
            this.title = " ";

        this.constant = constant.replace("*", Ax.multi).replace("/", Ax.divi);

        if (constant == null || constant.equals("\0"))
            this.constant = " ";

        this.unit = unit;

        if (unit == null || unit.equals("\0"))
            this.unit = " ";
    }
}
