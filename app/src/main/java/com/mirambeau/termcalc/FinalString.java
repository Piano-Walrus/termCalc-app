package com.mirambeau.termcalc;

public class FinalString{
    String str;

    public FinalString() {
        this.str = "";
    }

    public FinalString(String str) {
        this.str = str;
    }

    public void add(String str) {
        this.str += str;
    }

    public void remove(String str) {
        this.str = this.str.replace(str, "");
    }

    public void replace(String str, String replacement) {
        this.str = this.str.replace(str, replacement);
    }

    public boolean contains(String str) {
        return this.str.contains(str);
    }

    public boolean equals(String str) {
        return this.str.equals(str);
    }

    public int length() {
        return this.str.length();
    }

    @Override
    public String toString() {
        return this.str;
    }
}
