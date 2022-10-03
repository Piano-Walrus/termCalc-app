package com.mirambeau.termcalc;

import java.util.ArrayList;
import java.util.Arrays;

public class FunctionCard {
    String title, function;
    String[] variables;
    ArrayList<String> vars = new ArrayList<>();
    boolean isExpanded = false;

    public FunctionCard(String title, String function, String variables){
        this.title = (title == null || title.equals("\0")) ? " " : title;
        this.function = (function == null || function.equals("\0")) ? " " : function;

        if (variables == null || variables.equals("\0"))
            this.variables = new String[]{" "};
        else
            this.vars = new ArrayList<>(Arrays.asList(variables.replace(" ", "").split("`")));
    }

    public void setIsExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public void toggleExpanded() {
        this.isExpanded = !this.isExpanded;
    }
}
