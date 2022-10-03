package com.mirambeau.termcalc;

public class HistoryEntry {
    String equation, answer;

    public HistoryEntry(String equation, String answer){
        this.equation = equation;

        if (equation == null || equation.equals("\0"))
            this.equation = " ";

        this.answer = answer;

        if (answer == null || answer.equals("\0"))
            this.answer = " ";
    }
}
