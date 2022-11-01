package com.mirambeau.termcalc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.text.DecimalFormat;

public class ConversionsFragmentNew extends Fragment {
    Activity main = MainActivity.mainActivity;

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
    String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");
    String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
    boolean isCustomTheme = sp.getBoolean("custom", false);

    int c;
    int selectedType = 0;
    int selectedFrom = 0, selectedTo = 1;

    String primary, secondary, tertiary, bgColor;
    String num1, eqConv;
    String unitDefault, unitSelected;

    double result, fromDub;

    ImageButton expandFrom, expandTo, swap;
    TextView unitFrom, unitTo, fromUnitLabel, toUnitLabel;
    EditText fromBase, toBase;

    static final String[] primaryColors = {"#03DAC5", "#009688", "#54AF57", "#00C7E0", "#2196F3", "#0D2A89", "#3F51B5", "LILAC", "PINK", "#F44336", "#E77369", "#FF9800", "#FFC107", "#FEF65B", "#66BB6A", "#873804", "#B8E2F8"};
    static final String[][] secondaryColors = {{"#53E2D4", "#4DB6AC", "#77C77B", "#51D6E8", "#64B5F6", "#1336A9", "#7986CB", "#8C6DCA", "#F06292", "#FF5956", "#EC8F87", "#FFB74D", "#FFD54F", "#FBF68D", "#EF5350", "#BD5E1E", "#B8E2F8"}, {"#00B5A3", "#00796B", "#388E3C", "#0097A7", "#1976D2", "#0A2068", "#303F9F", "#5E35B1", "#C2185B", "#D32F2F", "#D96459", "#F57C00", "#FFA000", "#F4E64B", "#EF5350", "#572300", "#9BCEE9"}};
    static final String[][] tertiaryColors = {{"#3CDECE", "#26A69A", "#68B86E", "#39CFE3", "#42A5F5", "#0D2F9E", "#5C6BC0", "#7857BA", "#EC407A", "#FA4E4B", "#EB837A", "#FFA726", "#FFCB2E", "#F8F276", "#FF5754", "#A14D15", "#ABDBF4"}, {"#00C5B1", "#00897B", "#43A047", "#00ACC1", "#1E88E5", "#0A2373", "#3949AB", "#663ABD", "#D81B60", "#E33532", "#DE685D", "#FB8C00", "#FFB300", "#FCEE54", "#FF5754", "#612703", "#ABDBF4"}};

    boolean isConvDec, isSmol, isDynamic;
    boolean isError = false, internetChecked = false;

    DecimalFormat edf, pidf, userdf;

    final int darkGray = Color.parseColor("#222222");
    final int monochromeTextColor = Color.parseColor("#303030");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            int i, j;

            final TinyDB tinydb = new TinyDB(MainActivity.mainActivity);
            String cKeypad = tinydb.getString("cKeypad");
            String cMain = tinydb.getString("cMain");
            String cEquals = tinydb.getString("cbEquals");
            String cKeytext = tinydb.getString("cNum");

            //Check precision
            isDynamic = tinydb.getBoolean("isDynamic");

            if (!isDynamic) {
                StringBuilder dfStr = new StringBuilder("#,###.");
                int precision = tinydb.getInt("precision");

                if (precision == 0) {
                    precision = 1;
                    tinydb.putInt("precision", 1);
                }

                for (i = 0; i < precision; i++) {
                    dfStr.append("#");
                }

                userdf = new DecimalFormat(dfStr.toString());

                edf = userdf;
                pidf = userdf;
            }
            else {
                edf = new DecimalFormat("#,###.#########");
                pidf = new DecimalFormat("#,###.#####");
            }

            boolean theme_boolean;

            if (theme == null || theme.equals("\0"))
                theme = "1";

            if (theme.equals("5"))
                theme_boolean = tinydb.getBoolean("theme_boolean");
            else {
                final SharedPreferences mPrefs = main.getSharedPreferences("THEME", 0);
                theme_boolean = mPrefs.getBoolean("theme_boolean", true);
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

            final ConstraintLayout bgConv = MainActivity.mainActivity.findViewById(R.id.convbg);
            final ConstraintLayout keypadConv = main.findViewById(R.id.buttonViewConv);
            final ConstraintLayout scrollConv = main.findViewById(R.id.scrollBarConv);

            final ConstraintLayout unitLayout = main.findViewById(R.id.unitsLayout);

            final ImageButton clear = main.findViewById(R.id.toBackspace);

            final TextView fromConv = main.findViewById(R.id.fromLabelConv);
            final TextView toConv = main.findViewById(R.id.toLabelConv);
            final TextView fromNum = main.findViewById(R.id.fromNumConv);
            final TextView toNum = main.findViewById(R.id.toNumConv);

            final ImageButton fromBackspace = main.findViewById(R.id.fromBackspace);

            expandFrom = main.findViewById(R.id.expandFrom);
            unitFrom = main.findViewById(R.id.fromSelectedUnit);
            expandTo = main.findViewById(R.id.expandTo);
            unitTo = main.findViewById(R.id.toSelectedUnit);
            fromUnitLabel = main.findViewById(R.id.fromUnitLabel);
            toUnitLabel = main.findViewById(R.id.toUnitLabel);
            fromBase = main.findViewById(R.id.fromBase);
            toBase = main.findViewById(R.id.toBase);
            swap = main.findViewById(R.id.swap);

            final Button[] convKeys = {main.findViewById(R.id.b0C), main.findViewById(R.id.b1C), main.findViewById(R.id.b2C), main.findViewById(R.id.b3C), main.findViewById(R.id.b4C), main.findViewById(R.id.b5C), main.findViewById(R.id.b6C), main.findViewById(R.id.b7C), main.findViewById(R.id.b8C), main.findViewById(R.id.b9C), main.findViewById(R.id.bDecC)};
            final Button[] scrollConvKeys = {main.findViewById(R.id.bDistance), main.findViewById(R.id.bMass), main.findViewById(R.id.bVolume), main.findViewById(R.id.bTime), main.findViewById(R.id.bCurrency), main.findViewById(R.id.bSpeed), main.findViewById(R.id.bTemp), main.findViewById(R.id.bBase)};

            final Button bEqualsConv = main.findViewById(R.id.bEqualsConv);

            if (fromConv != null && fromConv.getText() != null)
                isSmol = fromConv.getText().toString().endsWith(" ");
            else
                isSmol = false;

            if (tinydb.getString("fetchRate").equals("2")) {
                final HandlerThread thread = new HandlerThread("SendRatesThread");
                thread.start();

                new Handler(thread.getLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        while (!Ax.checkRates()) {
                            CurrencyConverter.calculate(1.5, "USD", "EUR", new CurrencyConverter.Callback() {
                                @Override
                                public void onValueCalculated(Double value, Exception e) {

                                }
                            });

                            CurrencyConverter.sendRatesToAux(Ax.currencyCodes);
                        }

                        thread.quitSafely();
                    }
                }, 3);
            }

            convClear(clear);

            num1 = "\0";
            isConvDec = false;

            if (clear != null)
                clear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(primary)));

            if (bEqualsConv != null)
                bEqualsConv.setTextColor(Color.parseColor(primary));

            int numTypes = scrollConvKeys.length;

            if (convKeys[10] != null)
                convKeys[10].setText(".");

            if (theme != null) {
                if (theme.equals("3") || theme.equals("4")) {
                    if (theme.equals("4")) {
                        if (scrollConv != null)
                            scrollConv.setBackgroundColor(Color.BLACK);

                        for (i = 0; i < numTypes; i++) {
                            if (scrollConvKeys[i] != null) {
                                scrollConvKeys[i].setTextColor(Color.parseColor(secondary));
                                scrollConvKeys[i].setBackgroundColor(Color.BLACK);
                            }
                        }

                        if (scrollConvKeys[0] != null)
                            scrollConvKeys[0].setBackgroundColor(Color.parseColor("#2E2E2E"));
                    }
                    else {
                        if (scrollConv != null)
                            scrollConv.setBackgroundColor(Color.parseColor(secondary));

                        for (i = 0; i < numTypes; i++) {
                            if (scrollConvKeys[i] != null)
                                scrollConvKeys[i].setBackgroundColor(Color.parseColor(secondary));
                        }

                        if (scrollConvKeys[0] != null)
                            scrollConvKeys[0].setBackgroundColor(Color.parseColor(primary));

                        if (keypadConv != null)
                            keypadConv.setBackgroundColor(Color.parseColor("#444444"));
                    }

                    if (bgConv != null)
                        bgConv.setBackgroundColor(Color.BLACK);

                    if (keypadConv != null)
                        keypadConv.setBackgroundColor(Color.BLACK);
                }
                else if (theme.equals("2")) {
                    if (scrollConv != null)
                        scrollConv.setBackgroundColor(Color.parseColor(secondary));

                    for (i = 0; i < numTypes; i++) {
                        if (scrollConvKeys[i] != null)
                            scrollConvKeys[i].setBackgroundColor(Color.parseColor(secondary));
                    }

                    if (scrollConvKeys[0] != null)
                        scrollConvKeys[0].setBackgroundColor(Color.parseColor(primary));

                    if (bgConv != null)
                        bgConv.setBackgroundColor(Color.WHITE);

                    if (keypadConv != null)
                        keypadConv.setBackgroundColor(Color.WHITE);

                    if (fromConv != null)
                        fromConv.setTextColor(darkGray);

                    if (fromNum != null)
                        fromNum.setTextColor(darkGray);

                    if (toConv != null)
                        toConv.setTextColor(darkGray);

                    if (toNum != null)
                        toNum.setTextColor(darkGray);

                    for (i = 0; i < 11; i++) {
                        if (convKeys[i] != null)
                            convKeys[i].setTextColor(darkGray);
                    }
                }
                else if (theme.equals("5")) {
                    if (scrollConv != null)
                        scrollConv.setBackgroundColor(Color.parseColor(secondary));

                    for (i = 0; i < numTypes; i++) {
                        if (scrollConvKeys[i] != null) {
                            scrollConvKeys[i].setBackgroundColor(Color.parseColor(secondary));
                            scrollConvKeys[i].setTextColor(monochromeTextColor);
                        }
                    }

                    if (bEqualsConv != null)
                        bEqualsConv.setTextColor(monochromeTextColor);

                    if (scrollConvKeys[0] != null)
                        scrollConvKeys[0].setBackgroundColor(Color.parseColor(secondary));

                    if (bgConv != null)
                        bgConv.setBackgroundColor(Color.parseColor(secondary));

                    if (keypadConv != null) {
                        keypadConv.setBackgroundColor(Color.parseColor(secondary));
                        keypadConv.setElevation(0);
                    }

                    if (fromConv != null)
                        fromConv.setTextColor(monochromeTextColor);

                    if (fromNum != null) {
                        fromNum.setTextColor(monochromeTextColor);
                        fromNum.setBackgroundTintList(ColorStateList.valueOf(monochromeTextColor));
                    }

                    if (toConv != null)
                        toConv.setTextColor(monochromeTextColor);

                    if (toNum != null) {
                        toNum.setTextColor(monochromeTextColor);
                        toNum.setBackgroundTintList(ColorStateList.valueOf(monochromeTextColor));
                    }

                    for (i = 0; i < 11; i++) {
                        if (convKeys[i] != null)
                            convKeys[i].setTextColor(monochromeTextColor);
                    }
                }
                else {
                    if (scrollConv != null)
                        scrollConv.setBackgroundColor(Color.parseColor(secondary));

                    for (i = 0; i < numTypes; i++) {
                        if (scrollConvKeys[i] != null)
                            scrollConvKeys[i].setBackgroundColor(Color.parseColor(secondary));
                    }

                    if (scrollConvKeys[0] != null)
                        scrollConvKeys[0].setBackgroundColor(Color.parseColor(primary));
                }
            }

            String cTop = tinydb.getString("cTop");
            String cSecondary = tinydb.getString("cSecondary");

            if (isCustomTheme) {
                for (i = 0; i < scrollConvKeys.length; i++) {
                    if (scrollConvKeys[i] != null) {
                        if (!Ax.isNull(cTop) && Ax.isColor(cTop))
                            scrollConvKeys[i].setTextColor(Color.parseColor(cTop));

                        if (!Ax.isNull(cSecondary) && Ax.isColor(cSecondary)) {
                            scrollConvKeys[i].setBackgroundColor(Color.parseColor(cSecondary));

                            if (i == 0)
                                scrollConvKeys[0].setBackgroundColor(Color.parseColor(Ax.hexAdd(cSecondary, -16)));
                        }
                    }
                }
            }

            //Dodie Yellow
            if (theme != null && color != null && (color.equals("14") || (color.equals("17") && (theme.equals("3") || theme.equals("1")))) && !isCustomTheme) {
                if (clear != null)
                    clear.setImageDrawable(ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.ic_close_dark_24));

                if (bEqualsConv != null) {
                    if (theme.equals("5"))
                        bEqualsConv.setTextColor(monochromeTextColor);
                    else if (color.equals("14"))
                        bEqualsConv.setTextColor(Color.parseColor("#f4e64b"));
                    else
                        bEqualsConv.setTextColor(Color.parseColor(primary));
                }

                if (theme.equals("5")) {
                    for (i = 0; i < numTypes; i++) {
                        if (scrollConvKeys[i] != null)
                            scrollConvKeys[i].setTextColor(monochromeTextColor);
                    }
                }
                else if (!theme.equals("4")) {
                    for (i = 0; i < numTypes; i++) {
                        if (scrollConvKeys[i] != null)
                            scrollConvKeys[i].setTextColor(darkGray);
                    }
                }
            }

            if (isCustomTheme) {
                if (Ax.isColor(cMain) && bgConv != null)
                    bgConv.setBackgroundColor(Color.parseColor(cMain));

                if (Ax.isColor(cKeypad) && keypadConv != null)
                    keypadConv.setBackgroundColor(Color.parseColor(cKeypad));

                if (Ax.isColor(cKeytext)) {
                    for (j = 0; j < 11; j++) {
                        if (convKeys[j] != null)
                            convKeys[j].setTextColor(Color.parseColor(cKeytext));
                    }
                }

                if (Ax.isColor(cEquals) && bEqualsConv != null)
                    bEqualsConv.setTextColor(Color.parseColor(cEquals));
            }
            else if (theme != null && bEqualsConv != null && !theme.equals("5"))
                    bEqualsConv.setTextColor(Color.parseColor(primary));

            if (isCustomTheme) {
                if (cMain.equals(cKeypad) && keypadConv != null)
                    keypadConv.setElevation(0);
            }

            for (i = 0; i < convKeys.length; i++) {
                if (convKeys[i] != null) {
                    convKeys[i].setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(View v) {
                            fromInputHandler(v);
                        }
                    });
                }
            }

            for (j = 0; j < scrollConvKeys.length; j++) {
                if (scrollConvKeys[j] != null) {
                    final int fj = j;

                    scrollConvKeys[j].setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(View v) {
                            Button type = (Button) v;

                            Button[] allTypes = {main.findViewById(R.id.bDistance), main.findViewById(R.id.bMass), main.findViewById(R.id.bVolume), main.findViewById(R.id.bTime), main.findViewById(R.id.bCurrency), main.findViewById(R.id.bSpeed), main.findViewById(R.id.bTemp), main.findViewById(R.id.bBase)};

                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
                            String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

                            int i;
                            final int numTypes = allTypes.length;

                            TinyDB tinydb = new TinyDB(MainActivity.mainActivity);
                            String cMain = tinydb.getString("cMain");

                            if ((bgColor == null || bgColor.equals("\0")) && theme != null) {
                                if (theme.equals("1"))
                                    bgColor = "#272C33";
                                else if (theme.equals("2"))
                                    bgColor = "#FFFFFF";
                                else if (theme.equals("3") || theme.equals("4"))
                                    bgColor = "#000000";
                                else if (theme.equals("5"))
                                    bgColor = secondary;
                            }

                            if (isCustomTheme && !Ax.isNull(cMain) && Ax.isColor(cMain))
                                bgColor = cMain;

                            unitDefault = bgColor;

                            //Unit Selection Colors
                            if (theme != null && !theme.equals("\0")) {
                                if (isCustomTheme) {
                                    if (theme.equals("1") || theme.equals("3") || theme.equals("4"))
                                        unitSelected = Ax.hexAdd(unitDefault, 18);
                                    else
                                        unitSelected = Ax.hexAdd(unitDefault, -15);
                                }
                                else {
                                    if (theme.equals("1"))
                                        unitSelected = Ax.hexAdd(unitDefault, 18);
                                    else if (theme.equals("3") || theme.equals("4"))
                                        unitSelected = Ax.hexAdd(unitDefault, 28);
                                    else if (theme.equals("2"))
                                        unitSelected = "#E0E0E0";
                                    else
                                        unitSelected = Ax.hexAdd(unitDefault, -15);
                                }
                            }

                            if (theme != null) {
                                for (i = 0; i < numTypes; i++) {
                                    if (fj != i) {
                                        if (isCustomTheme && !theme.equals("4") && !theme.equals("5"))
                                            allTypes[i].setBackgroundColor(Color.parseColor(secondary));
                                        else {
                                            switch (theme) {
                                                case "1":
                                                case "3":
                                                    allTypes[i].setBackgroundColor(Color.parseColor(secondary));
                                                case "2":
                                                    allTypes[i].setBackgroundColor(Color.parseColor(primary));
                                                    break;
                                                case "4":
                                                    allTypes[i].setBackgroundColor(Color.BLACK);
                                                    break;
                                                case "5":
                                                    allTypes[i].setBackgroundColor(Color.parseColor(unitDefault));
                                                    break;
                                            }
                                        }
                                    }
                                }

                                if (theme.equals("4"))
                                    unitSelected = Ax.hexAdd(unitSelected, -1);

                                if (selectedType != fj) {
                                    if (theme.equals("5") || theme.equals("4"))
                                        type.setBackgroundColor(Color.parseColor(unitSelected));
                                    else if (theme.equals("2")) {
                                        if (isCustomTheme)
                                            type.setBackgroundColor(Color.parseColor(Ax.hexAdd(secondary, -15)));
                                        else
                                            type.setBackgroundColor(Color.parseColor(Ax.hexAdd(primary, -15)));
                                    }
                                    else
                                        type.setBackgroundColor(Color.parseColor(Ax.hexAdd(secondary, -15)));
                                }
                            }

                            if (selectedType != fj)
                                selectedType = fj;

                            selectedFrom = 0;
                            selectedTo = 1;

                            Activity main = MainActivity.mainActivity;

                            if (selectedType == 7) {
                                fromUnitLabel.setText(main.getString(R.string.from) + " " + main.getString(R.string.base_representation));
                                toUnitLabel.setText(main.getString(R.string.to) + " " + main.getString(R.string.base_representation));

                                expandFrom.setVisibility(View.GONE);
                                expandTo.setVisibility(View.GONE);

                                unitFrom.setVisibility(View.GONE);
                                unitTo.setVisibility(View.GONE);

                                fromBase.setVisibility(View.VISIBLE);
                                toBase.setVisibility(View.VISIBLE);
                            }
                            else {
                                String[] initFrom = {main.getString(R.string.distance_inches), main.getString(R.string.mass_micrograms),
                                        main.getString(R.string.volume_milliliters), main.getString(R.string.time_nanoseconds), "USD",
                                        main.getString(R.string.miles_per_hour), main.getString(R.string.temp_fahrenheit)};
                                String[] initTo = {main.getString(R.string.distance_feet), main.getString(R.string.mass_milligrams),
                                        main.getString(R.string.volume_liters), main.getString(R.string.time_microseconds), "EUR",
                                        main.getString(R.string.kilometers_per_hour), main.getString(R.string.temp_celsius)};

                                if (unitFrom != null)
                                    unitFrom.setText(initFrom[selectedType]);

                                if (unitTo != null)
                                    unitTo.setText(initTo[selectedType]);

                                fromUnitLabel.setText(main.getString(R.string.from));
                                toUnitLabel.setText(main.getString(R.string.to));

                                expandFrom.setVisibility(View.VISIBLE);
                                expandTo.setVisibility(View.VISIBLE);

                                unitFrom.setVisibility(View.VISIBLE);
                                unitTo.setVisibility(View.VISIBLE);

                                fromBase.setVisibility(View.GONE);
                                toBase.setVisibility(View.GONE);
                            }

                            if (selectedType == 4 && tinydb.getString("fetchRate").equals("3")) {
                                final HandlerThread thread = new HandlerThread("SendRatesThread");
                                thread.start();

                                new Handler(thread.getLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (!Ax.checkRates()) {
                                            CurrencyConverter.calculate(1.5, "USD", "EUR", new CurrencyConverter.Callback() {
                                                @Override
                                                public void onValueCalculated(Double value, Exception e) {

                                                }
                                            });

                                            CurrencyConverter.sendRatesToAux(Ax.currencyCodes);
                                        }

                                        thread.quitSafely();
                                    }
                                }, 3);
                            }
                        }
                    });
                }
            }

            if (bEqualsConv != null) {
                bEqualsConv.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {
                        convEquals(v);
                    }
                });
            }

            if (scrollConvKeys[0] != null && scrollConvKeys[1] != null) {
                scrollConvKeys[1].performClick();
                scrollConvKeys[0].performClick();
            }

            if (unitFrom != null)
                unitFrom.setOnClickListener(selectFromUnit);

            if (expandFrom != null)
                expandFrom.setOnClickListener(selectFromUnit);

            if (unitTo != null)
                unitTo.setOnClickListener(selectToUnit);

            if (expandTo != null)
                expandTo.setOnClickListener(selectToUnit);

            if (fromBackspace != null) {
                fromBackspace.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {
                        TextView tv = main.findViewById(R.id.fromNumConv);
                        String eq = tv.getText().toString();

                        ImageButton clear = main.findViewById(R.id.toBackspace);

                        if (clear != null && clear.getVisibility() == View.VISIBLE) {
                            clear.setVisibility(View.GONE);

                            if (toNum != null)
                                toNum.setText("");
                        }

                        if (!isError) {
                            if (eq.endsWith(".") && isConvDec)
                                isConvDec = false;

                            eqConv = Ax.newTrim(eqConv, 1);

                            if (eqConv.length() > 3 && !isConvDec && selectedType != 7)
                                eqConv = Ax.checkCommas(eqConv);

                            tv.setText(eqConv);
                        }
                        else {
                            convClear(clear);
                        }
                    }
                });

                fromBackspace.setOnLongClickListener(new View.OnLongClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public boolean onLongClick(View v) {
                        convClear(v);

                        final Vibrator vibe = (Vibrator) MainActivity.mainActivity.getSystemService(Context.VIBRATOR_SERVICE);

                        vibe.vibrate(25);

                        return true;
                    }
                });
            }

            if (swap != null) {
                swap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int temp = selectedTo;
                        String tempStr;

                        selectedTo = selectedFrom;
                        selectedFrom = temp;

                        if (selectedType == 7) {
                            tempStr = toBase.getText().toString();

                            toBase.setText(fromBase.getText().toString());
                            fromBase.setText(tempStr);
                        }
                        else {
                            tempStr = unitTo.getText().toString();

                            unitTo.setText(unitFrom.getText().toString());
                            unitFrom.setText(tempStr);
                        }
                    }
                });
            }

            if (isCustomTheme) {
                int a;
                Button[] allButtons = new Button[convKeys.length + 1];

                for (a = 0; a < allButtons.length; a++) {
                    if (a == convKeys.length)
                        allButtons[a] = bEqualsConv;
                    else
                        allButtons[a] = convKeys[a];
                }

                for (a = 0; a < allButtons.length; a++) {
                    if (allButtons[a] != null) {
                        String buttonText = allButtons[a].getText().toString();

                        if (Ax.isColor(tinydb.getString("-b" + buttonText))) {
                            allButtons[a].setBackgroundColor(Color.parseColor(tinydb.getString("-b" + buttonText)));
                        }
                        if (Ax.isColor(tinydb.getString("-b" + buttonText + "t"))) {
                            allButtons[a].setTextColor(Color.parseColor(tinydb.getString("-b" + buttonText + "t")));
                        }
                    }
                }

                if (bEqualsConv != null) {
                    if (Ax.isColor(tinydb.getString("-b=t")))
                        bEqualsConv.setTextColor(Color.parseColor(tinydb.getString("-b=t")));
                    else if (Ax.isColor(tinydb.getString("cNum")))
                        bEqualsConv.setTextColor(Color.parseColor(tinydb.getString("cNum")));
                    else if (Ax.isColor(tinydb.getString("cPrimary")))
                        bEqualsConv.setTextColor(Color.parseColor(tinydb.getString("cPrimary")));
                }

                if (unitLayout != null) {
                    if (Ax.isColor(tinydb.getString("cFab")))
                        unitLayout.setBackgroundColor(Color.parseColor(tinydb.getString("cFab")));
                    else if (!theme.equals("4") && Ax.isColor(tinydb.getString("cPrimary")))
                        unitLayout.setBackgroundColor(Color.parseColor(tinydb.getString("cPrimary")));
                    else
                        unitLayout.setBackgroundColor(darkGray);
                }

                if (Ax.isColor(tinydb.getString("cFabText"))) {
                    int fabText = Color.parseColor(tinydb.getString("cFabText"));

                    if (expandFrom != null)
                        expandFrom.setColorFilter(fabText);
                    if (expandTo != null)
                        expandTo.setColorFilter(fabText);
                    if (unitFrom != null)
                        unitFrom.setTextColor(fabText);
                    if (unitTo != null)
                        unitTo.setTextColor(fabText);
                    if (fromBase != null)
                        fromBase.setTextColor(fabText);
                    if (toBase != null)
                        toBase.setTextColor(fabText);
                    if (fromUnitLabel != null)
                        fromUnitLabel.setTextColor(fabText);
                    if (toUnitLabel != null)
                        toUnitLabel.setTextColor(fabText);
                    if (swap != null)
                        swap.setColorFilter(fabText);
                }

                if (theme.equals("2")) {
                    if (fromBackspace != null)
                        fromBackspace.setColorFilter(darkGray);

                    if (clear != null)
                        clear.setColorFilter(darkGray);
                }
                else if (theme.equals("5")) {
                    if (fromBackspace != null)
                        fromBackspace.setColorFilter(monochromeTextColor);

                    if (clear != null)
                        clear.setColorFilter(monochromeTextColor);
                }

                if (Ax.isTinyColor("-mt")){
                    int uiTextColor = Ax.getTinyColor("-mt");

                    if (clear != null)
                        clear.setColorFilter(uiTextColor);
                    if (fromBackspace != null)
                        fromBackspace.setColorFilter(uiTextColor);
                    if (fromNum != null) {
                        fromNum.setBackgroundTintList(ColorStateList.valueOf(uiTextColor));
                        fromNum.setTextColor(uiTextColor);
                    }
                    if (toNum != null) {
                        toNum.setBackgroundTintList(ColorStateList.valueOf(uiTextColor));
                        toNum.setTextColor(uiTextColor);
                    }
                    if (fromConv != null)
                        fromConv.setTextColor(uiTextColor);
                    if (toConv != null)
                        toConv.setTextColor(uiTextColor);
                }
            }
            else {
                int textColor = 0, layoutColor = 0;

                if (theme.equals("2")) {
                    textColor = darkGray;
                    layoutColor = Color.parseColor("#EFEFEF");
                }
                else if (theme.equals("4")) {
                    textColor = Color.parseColor(secondary);
                    layoutColor = Color.BLACK;
                }
                else if (theme.equals("5")) {
                    textColor = Color.parseColor("#303030");
                    layoutColor = Color.parseColor(secondary);
                    ColorStateList editTextColor = ColorStateList.valueOf(textColor);

                    if (fromNum != null)
                        fromNum.setBackgroundTintList(editTextColor);

                    if (toNum != null)
                        toNum.setBackgroundTintList(editTextColor);
                }

                if (textColor != 0) {
                    if (expandFrom != null)
                        expandFrom.setColorFilter(textColor);

                    if (expandTo != null)
                        expandTo.setColorFilter(textColor);

                    if (swap != null)
                        swap.setColorFilter(textColor);

                    if (!theme.equals("4")) {
                        if (fromBackspace != null)
                            fromBackspace.setColorFilter(textColor);

                        if (clear != null)
                            clear.setColorFilter(textColor);
                    }

                    if (!theme.equals("2")) {
                        if (unitFrom != null)
                            unitFrom.setTextColor(textColor);
                        if (unitTo != null)
                            unitTo.setTextColor(textColor);
                        if (fromBase != null)
                            fromBase.setTextColor(textColor);
                        if (toBase != null)
                            toBase.setTextColor(textColor);
                        if (fromUnitLabel != null)
                            fromUnitLabel.setTextColor(textColor);
                        if (toUnitLabel != null)
                            toUnitLabel.setTextColor(textColor);
                    }
                }

                if (layoutColor != 0 && unitLayout != null)
                    unitLayout.setBackgroundColor(layoutColor);
            }

            if (clear != null)
                clear.setVisibility(View.GONE);
        }
        catch (Exception e){
            e.printStackTrace();
            Ax.saveStack(e);
            MainActivity.mainActivity.finishAffinity();
        }
    }

    public final void convClear(View v) {
        final Activity main = getActivity();

        if (main != null) {
            TextView from = main.findViewById(R.id.fromNumConv);
            TextView to = main.findViewById(R.id.toNumConv);

            ImageButton clear = main.findViewById(R.id.toBackspace);

            if (clear != null)
                clear.setVisibility(View.GONE);

            if (from != null)
                from.setText("\0");

            if (to != null)
                to.setText("\0");

            isConvDec = false;
            isError = false;
            eqConv = "\0";
            num1 = "\0";
        }
    }

    public final void fromInputHandler(View v){
        try {
            Button keyNum = (Button) v;
            TextView tv = main.findViewById(R.id.fromNumConv);

            ImageButton clear = main.findViewById(R.id.toBackspace);

            String fromStr = tv.getText().toString();

            int max = 23;

            if (isSmol)
                max = 21;

            if (clear != null && clear.getVisibility() == View.VISIBLE)
                convClear(clear);

            if (isConvDec)
                max--;

            if (fromStr.length() < max || (fromStr.length() < max + 1 && fromStr.contains(",") && !fromStr.contains("."))) {
                String pressed = keyNum.getText().toString();

                if (pressed.equals(".")) {
                    if (!isConvDec) {
                        isConvDec = true;
                    }
                    else
                        pressed = "";
                }

                if (eqConv != null && eqConv.length() > 0 && eqConv.startsWith("\0"))
                    eqConv = eqConv.substring(1);

                eqConv += pressed;

                if (eqConv.length() > 3 && !isConvDec && selectedType != 7)
                    eqConv = Ax.checkCommas(eqConv);

                tv.setText(eqConv);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Ax.saveStack(e);
            MainActivity.mainActivity.finishAffinity();
        }
    }

    @SuppressLint("SetTextI18n")
    public final void convEquals(View v) {
        final Activity main = getActivity();

        final TextView from = main.findViewById(R.id.fromNumConv);
        final TextView to = main.findViewById(R.id.toNumConv);

        final Converter converter = new Converter();

        try {
            if (from.getText().toString().equals(".") || from.getText().toString().equals("\0."))
                return;
        }
        catch (NullPointerException ignored) {}

        if (!Ax.isNull(from)) {
            String fromStr = "";

            try {
                fromStr = from.getText().toString();
            }
            catch (NullPointerException e) {
                e.printStackTrace();
                return;
            }

            if (fromStr.startsWith("\0")){
                fromStr = fromStr.substring(1);
            }

            if (fromStr.length() > 3)
                fromStr = fromStr.replace(",", "");

            if (selectedType == 7) {
                int input, initBase, finalBase;

                if (fromStr.contains(".") || (fromStr.contains("-") && !fromStr.startsWith("-")) || fromStr.length() > 10 || Double.parseDouble(fromStr) > Integer.MAX_VALUE - 1 || Double.parseDouble(fromStr) < ((Integer.MAX_VALUE - 1) * -1) + 1){
                    to.setText("Error");
                }
                else {
                    if (!Ax.isNull(fromStr) && fromStr.length() > 0) {
                        input = Integer.parseInt(fromStr);

                        try {
                            initBase = Integer.parseInt(fromBase.getText().toString());
                            finalBase = Integer.parseInt(toBase.getText().toString());
                        }
                        catch (NullPointerException | NumberFormatException e){
                            e.printStackTrace();
                            return;
                        }

                        to.setText(converter.bases(input, initBase, finalBase));
                    }
                }
            }
            else if (!from.getText().toString().equals("\0.") && !from.getText().toString().equals(".")) {
                    if (from.length() > 0 && !Ax.isNull(from)) {
                        try {
                            fromDub = Double.parseDouble(fromStr);
                        }
                        catch (NullPointerException | NumberFormatException e) {
                            e.printStackTrace();
                            return;
                        }

                        if (fromDub == 0 && selectedType == 4){
                            result = 0;
                            to.setText("0");

                            ImageButton clear = main.findViewById(R.id.toBackspace);

                            if (clear != null) {
                                clear.setVisibility(View.VISIBLE);

                                clear.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        convClear(v);

                                        v.setVisibility(View.GONE);
                                    }
                                });

                                clear.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        convClear(v);

                                        final Vibrator vibe = (Vibrator) MainActivity.mainActivity.getSystemService(Context.VIBRATOR_SERVICE);

                                        vibe.vibrate(25);

                                        v.setVisibility(View.GONE);

                                        return true;
                                    }
                                });
                            }

                            return;
                        }

                        result = converter.convert(fromDub, selectedType, selectedFrom, selectedTo);

                        if (selectedType == 4) {
                            if (internetChecked || Ax.ratesChecked || internetIsConnected()) {
                                internetChecked = true;

                                if (result == -1 || (result == 0 && fromDub != 0)) {
                                    c++;

                                    if (c < 19800) {
                                        convEquals(v);
                                    }
                                    else {
                                        to.setText(getString(R.string.currency_fetch_error));
                                        isError = true;
                                    }

                                    internetChecked = false;
                                    return;
                                }
                                else if (result == -4) {
                                    to.setText(getString(R.string.currency_fetch_error));
                                    isError = true;
                                    internetChecked = false;
                                    return;
                                }
                            }
                            else {
                                to.setText(getString(R.string.error_no_internet));
                                isError = true;
                                internetChecked = false;
                                return;
                            }
                        }

                        if (selectedType == 4) {
                            final DecimalFormat outputFormat = new DecimalFormat("#,###.##");

                            if (Ax.newTrim(Double.toString(result), 1).endsWith("."))
                                to.setText(outputFormat.format(result) + "0");
                            else
                                to.setText(outputFormat.format(result));
                        }
                        else {
                            if (result > 0 && result < 0.000000001) {
                                to.setText("0.000000001");
                            }
                            else if (result > 0 && result < 0.001) {
                                to.setText(edf.format(result));
                            }
                            else if (result > 0 && result < 1) {
                                to.setText(pidf.format(result));
                            }
                            else {
                                to.setText(pidf.format(result));
                            }
                        }
                    }
            }

            ImageButton clear = main.findViewById(R.id.toBackspace);

            if (clear != null) {
                clear.setVisibility(View.VISIBLE);

                clear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        convClear(v);

                        v.setVisibility(View.GONE);
                    }
                });

                clear.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        convClear(v);

                        final Vibrator vibe = (Vibrator) MainActivity.mainActivity.getSystemService(Context.VIBRATOR_SERVICE);

                        vibe.vibrate(25);

                        v.setVisibility(View.GONE);

                        return true;
                    }
                });
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public boolean internetIsConnected() {
        try {
            //TODO: Maybe ping another domain if this fails. Also wait, wouldn't this loop endlessly if Google was down?
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        }
        catch (Exception e) {
            return false;
        }
    }

    int[] unitMenus = {R.menu.distance_units, R.menu.mass_units, R.menu.volume_units, R.menu.time_units, R.menu.currency_units, R.menu.speed_units, R.menu.temp_units};

    View.OnClickListener selectFromUnit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final PopupMenu popup = new PopupMenu(MainActivity.mainActivity, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(unitMenus[selectedType], popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    unitFrom.setText(item.getTitle().toString());

                    selectedFrom = item.getOrder();

                    return false;
                }
            });
            popup.show();
        }
    };

    View.OnClickListener selectToUnit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final PopupMenu popup = new PopupMenu(MainActivity.mainActivity, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(unitMenus[selectedType], popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    unitTo.setText(item.getTitle().toString());

                    selectedTo = item.getOrder();

                    return false;
                }
            });
            popup.show();
        }
    };
}
