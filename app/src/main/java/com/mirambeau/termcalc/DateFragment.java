package com.mirambeau.termcalc;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DateFragment extends Fragment {
    Activity main = MainActivity.mainActivity;

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
    String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");
    String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
    boolean isCustomTheme = sp.getBoolean("custom", false);

    String primary, secondary, tertiary;

    int initDay, initMonth, initYear, finalDay, finalMonth, finalYear;

    static final String[] primaryColors = {"#03DAC5", "#009688", "#54AF57", "#00C7E0", "#2196F3", "#0D2A89", "#3F51B5", "LILAC", "PINK", "#F44336", "#E77369", "#FF9800", "#FFC107", "#FEF65B", "#66BB6A", "#873804", "#B8E2F8"};
    static final String[][] secondaryColors = {{"#53E2D4", "#4DB6AC", "#77C77B", "#51D6E8", "#64B5F6", "#1336A9", "#7986CB", "#8C6DCA", "#F06292", "#FF5956", "#EC8F87", "#FFB74D", "#FFD54F", "#FBF68D", "#EF5350", "#BD5E1E", "#B8E2F8"}, {"#00B5A3", "#00796B", "#388E3C", "#0097A7", "#1976D2", "#0A2068", "#303F9F", "#5E35B1", "#C2185B", "#D32F2F", "#D96459", "#F57C00", "#FFA000", "#F4E64B", "#EF5350", "#572300", "#9BCEE9"}};
    static final String[][] tertiaryColors = {{"#3CDECE", "#26A69A", "#68B86E", "#39CFE3", "#42A5F5", "#0D2F9E", "#5C6BC0", "#7857BA", "#EC407A", "#FA4E4B", "#EB837A", "#FFA726", "#FFCB2E", "#F8F276", "#FF5754", "#A14D15", "#ABDBF4"}, {"#00C5B1", "#00897B", "#43A047", "#00ACC1", "#1E88E5", "#0A2373", "#3949AB", "#663ABD", "#D81B60", "#E33532", "#DE685D", "#FB8C00", "#FFB300", "#FCEE54", "#FF5754", "#612703", "#ABDBF4"}};

    Boolean isEquals = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_date, container, false);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

        tinydb.putString("fragTag", "Date");

        MainActivity.mainActivity.recreate();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        savedInstanceState = null;

        super.onViewCreated(view, savedInstanceState);

        try {
            final TinyDB tinydb = new TinyDB(MainActivity.mainActivity);
            String cKeypad = tinydb.getString("cKeypad");
            String cMain = tinydb.getString("cMain");
            String cKeytext = tinydb.getString("cNum");
            String cFabText = tinydb.getString("cFabText");
            String cFab = tinydb.getString("cFab");

            boolean theme_boolean = tinydb.getBoolean("theme_boolean");

            int i;

            if (color != null && isCustomTheme) {
                if (!Ax.isColor(cFabText)) {
                    if (theme.equals("5"))
                        cFabText = primary;
                    else
                        cFabText = "#FFFFFF";
                }
            }

            if (color == null || color.equals("\0"))
                color = "1";

            if (!isCustomTheme) {
                primary = primaryColors[Integer.parseInt(color) - 1];

                //Light
                if (!theme_boolean) {
                    secondary = secondaryColors[0][Integer.parseInt(color) - 1];
                    tertiary = tertiaryColors[0][Integer.parseInt(color) - 1];
                }
                //Dark
                else {
                    secondary = secondaryColors[1][Integer.parseInt(color) - 1];
                    tertiary = tertiaryColors[1][Integer.parseInt(color) - 1];
                }

                if (primary.equals("LILAC")) {
                    if (!theme_boolean)
                        primary = "#6C42B6";
                    else
                        primary = "#7357C2";
                }
                else if (primary.equals("PINK")) {
                    if (!theme_boolean)
                        primary = "#E32765";
                    else
                        primary = "#E91E63";
                }
                else if (primary.equals("#B8E2F8")) {
                    if (!theme_boolean)
                        primary = "#9BCEE9";
                    else
                        primary = "#B8E2F8";
                }
            }
            else {
                String cPrimary = tinydb.getString("cPrimary");
                String cSecondary = tinydb.getString("cSecondary");
                String cTertiary = tinydb.getString("cTertiary");

                //Primary
                if (cPrimary.length() == 7) {
                    primary = cPrimary;
                }
                else {
                    color = "1";
                    primary = "#03DAC5";
                }

                //Secondary
                if (cSecondary.length() == 7) {
                    secondary = cSecondary;
                }
                else {
                    color = "1";
                    secondary = "#00B5A3";
                }

                //Tertiary
                if (cTertiary.length() == 7) {
                    tertiary = cTertiary;
                }
                else {
                    color = "1";
                    tertiary = "#00897B";
                }
            }

            String bgColor = secondary;
            String monochromeTextColor = "#303030";

            ConstraintLayout input = main.findViewById(R.id.input), mainDate = main.findViewById(R.id.mainDate);

            FloatingActionButton dateEquals = main.findViewById(R.id.dateEquals);

            TextView fromLabel = main.findViewById(R.id.fromLabel), toLabel = main.findViewById(R.id.toLabel);
            EditText fromIn = main.findViewById(R.id.fromInput), toIn = main.findViewById(R.id.toInput);

            TextView[] labels = {main.findViewById(R.id.daysLabel), main.findViewById(R.id.numDays), main.findViewById(R.id.yearsLabel), main.findViewById(R.id.numYears), main.findViewById(R.id.monthsLabel), main.findViewById(R.id.numMonths)};

            if (theme != null) {
                if (theme.equals("4") || theme.equals("3")) {
                    if (input != null)
                        input.setBackgroundColor(Color.parseColor("#000000"));

                    if (mainDate != null) {
                        mainDate.setBackgroundColor(Color.parseColor("#000000"));
                        mainDate.setBackgroundColor(Color.parseColor("#000000"));
                    }
                }
                else if (theme.equals("5")) {
                    if (input != null) {
                        input.setBackgroundColor(Color.parseColor(bgColor));
                        input.setElevation(32);
                    }

                    if (mainDate != null) {
                        mainDate.setBackgroundColor(Color.parseColor(bgColor));
                        mainDate.setBackgroundColor(Color.parseColor(bgColor));
                    }

                    if (fromIn != null) {
                        fromIn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(monochromeTextColor)));
                        fromIn.setTextColor(Color.parseColor(monochromeTextColor));
                    }

                    if (toIn != null) {
                        toIn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(monochromeTextColor)));
                        toIn.setTextColor(Color.parseColor(monochromeTextColor));
                    }

                    if (fromLabel != null)
                        fromLabel.setTextColor(Color.parseColor(monochromeTextColor));

                    if (toLabel != null)
                        toLabel.setTextColor(Color.parseColor(monochromeTextColor));


                    for (i = 0; i < 6; i++) {
                        if (labels[i] != null)
                            labels[i].setTextColor(Color.parseColor(monochromeTextColor));
                    }
                }
                else if (theme.equals("2")) {
                    if (input != null) {
                        input.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        input.setElevation(36);
                    }

                    if (mainDate != null) {
                        mainDate.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        mainDate.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    }

                    if (fromIn != null) {
                        fromIn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#222222")));
                        fromIn.setTextColor(Color.parseColor("#222222"));
                    }

                    if (toIn != null) {
                        toIn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#222222")));
                        toIn.setTextColor(Color.parseColor("#222222"));
                    }

                    if (fromLabel != null)
                        fromLabel.setTextColor(Color.parseColor("#222222"));

                    if (toLabel != null)
                        toLabel.setTextColor(Color.parseColor("#222222"));


                    for (i = 0; i < 6; i++) {
                        if (labels[i] != null)
                            labels[i].setTextColor(Color.parseColor("#222222"));
                    }
                }
            }

            if (isCustomTheme) {
                if (!Ax.isColor(cMain)) {
                    if (theme.equals("1"))
                        cMain = "#272C33";
                    else if (theme.equals("2"))
                        cMain = "#FFFFFF";
                    else if (theme.equals("3") || theme.equals("4"))
                        cMain = "#000000";
                    else if (theme.equals("5"))
                        cMain = secondary;
                }

                if (!Ax.isColor(cMain))
                    cMain = "#00B5A3";

                if (mainDate != null) {
                    mainDate.setBackgroundColor(Color.parseColor(cMain));
                    mainDate.setBackgroundColor(Color.parseColor(cMain));
                }

                if (!Ax.isColor(cKeypad)) {
                    if (theme.equals("2"))
                        cKeypad = "#FFFFFF";
                    else if (theme.equals("1"))
                        cKeypad = "#212227";
                    else if (theme.equals("5"))
                        cKeypad = secondary;
                    else
                        cKeypad = "#000000";
                }

                if (Ax.isColor(cMain) && Ax.isColor(cKeypad) && cKeypad.equalsIgnoreCase(cMain) && Build.VERSION.SDK_INT < 29 && theme.equals("5")) {
                    cKeypad = Ax.hexAdd(cKeypad, -15);
                    cFabText = cKeypad;
                }

                if (input != null) {
                    if (Ax.isColor(cKeypad))
                        input.setBackgroundColor(Color.parseColor(cKeypad));
                }

                if (cKeytext != null && !cKeytext.equals("\0") && cKeytext.startsWith("#")) {
                    ColorStateList dateCSL = ColorStateList.valueOf(Color.parseColor(cKeytext));

                    if (fromIn != null) {
                        fromIn.setBackgroundTintList(dateCSL);
                        fromIn.setTextColor(Color.parseColor(cKeytext));
                        fromIn.setHintTextColor(Color.parseColor(cKeytext));

                        if (fromLabel != null)
                            fromLabel.setTextColor(Color.parseColor(cKeytext));
                    }

                    if (toIn != null) {
                        toIn.setBackgroundTintList(dateCSL);
                        toIn.setTextColor(Color.parseColor(cKeytext));
                        toIn.setHintTextColor(Color.parseColor(cKeytext));

                        if (toLabel != null)
                            toLabel.setTextColor(Color.parseColor(cKeytext));
                    }
                }
            }

            if (fromIn != null)
                fromIn.setText("\0");

            if (toIn != null)
                toIn.setText("\0");

            if (labels[1] != null)
                labels[1].setText("\0");

            if (labels[3] != null)
                labels[3].setText("\0");

            if (labels[5] != null)
                labels[5].setText("\0");

            initDay = 0;
            initMonth = 0;
            initYear = 0;
            finalDay = 0;
            finalMonth = 0;
            finalYear = 0;

            if (dateEquals != null) {
                if (theme != null && color != null && (((color.equals("14") || (color.equals("17") && (theme.equals("3") || theme.equals("1"))))) || theme.equals("5"))) {
                    dateEquals.setImageDrawable(ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.ic_check_dark_24));
                }
                else {
                    dateEquals.setImageDrawable(ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.ic_check_24));
                }
            }

            isEquals = false;

            if (dateEquals != null) {
                if (theme.equals("5")) {
                    dateEquals.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(monochromeTextColor)));
                    dateEquals.setColorFilter(Color.parseColor(bgColor));
                }
                else
                    dateEquals.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(primary)));

                dateEquals.setElevation(14);
            }

            if (isCustomTheme) {
                if (!Ax.isColor(cFab) || cFab.equals("#reset0")) {
                    if (theme.equals("4"))
                        cFab = "#222222";
                    else if (theme.equals("5"))
                        cFab = monochromeTextColor;
                    else
                        cFab = primary;
                }

                if (!Ax.isColor(cFabText) || cFabText.equals("#reset0")) {
                    if (theme.equals("5"))
                        cFabText = primary;
                    else
                        cFabText = "#FFFFFF";
                }

                if (dateEquals != null) {
                    dateEquals.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(cFab)));
                    dateEquals.setColorFilter(Color.parseColor(cFabText));
                }

                if (Ax.isTinyColor("-mt")) {
                    int uiTextColor = Ax.getTinyColor("-mt");

                    for (TextView label : labels){
                        if (label != null)
                            label.setTextColor(uiTextColor);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Ax.saveStack(e);
            MainActivity.mainActivity.finishAffinity();
        }
    }
}
