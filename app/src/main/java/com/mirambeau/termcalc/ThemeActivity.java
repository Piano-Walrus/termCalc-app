package com.mirambeau.termcalc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;

public class ThemeActivity extends AppCompatActivity {
    static Activity themeActivity;
    static boolean shouldRecreateMain = false;

    boolean initState = false;

    String[] bgCodes = {"cKeypad", "cPrimary", "cTertiary", "cTertiary", "cSecondary"};
    String[] textCodes = {"-b=t", "-bop", "-btt", "-btt", "cTop"};

    String buttonShape;

    final String[] primaryColors = {"#03DAC5", "#009688", "#54AF57", "#00C7E0", "#2196F3", "#0D2A89", "#3F51B5", "#7357C2", "#E91E63", "#F44336", "#E77369", "#FF9800", "#FFC107", "#FEF65B", "#66BB6A", "#873804", "#B8E2F8"};

    int basicTheme, customTheme, accentColor;
    int darkGray, monochromeTextColor;
    int primary, secondary, tertiary;

    int themeTypeClicks = 0;

    boolean isMonochrome;

    float minimumWidth = 411;

    ConstraintLayout standardLayout, customLayout;

    View[] themeStyleButtons;
    ConstraintLayout[] themeColorButtons;

    Button[] standardButtons, customButtons;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            int i;

            setContentView(R.layout.activity_theme_settings);

            final TinyDB tinydb = new TinyDB(this);
            themeActivity = this;

            buttonShape = tinydb.getString("buttonShape");

            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(Color.parseColor("#16181B"));
                getWindow().setNavigationBarColor(Color.parseColor("#16181B"));
            }

            Toolbar toolbar = findViewById(R.id.themeToolbar);

            toolbar.setTitle("Theme Editor");
            toolbar.showOverflowMenu();
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_light);

            setTheme(R.style.Theme_MaterialComponents);

            toolbar.setOverflowIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_overflow_menu, null));

            try {
                minimumWidth = Aux.getMinimumWidth(this);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            //Establish Predefined Colors
            updateColors();

            basicTheme = Aux.getThemeInt("basicTheme");
            customTheme = Aux.getThemeInt("customTheme");

            darkGray = Color.parseColor("#222222");
            monochromeTextColor = Color.parseColor("#303030");

            initState = tinydb.getBoolean("custom");

            tinydb.putString("tempTheme", tinydb.getString("basicTheme"));
            tinydb.putString("tempColor", tinydb.getString("color"));

            themeStyleButtons = new View[]{null, findViewById(R.id.themeStyleDark), findViewById(R.id.themeStyleLight), findViewById(R.id.themeStyleBlack),
                    findViewById(R.id.themeStyleBlackButtons), findViewById(R.id.themeStyleMonochrome)};
            themeColorButtons = new ConstraintLayout[]{null, findViewById(R.id.mintLayout), findViewById(R.id.tealLayout), findViewById(R.id.greenLayout), findViewById(R.id.cyanLayout), findViewById(R.id.babyBlueLayout),
                    findViewById(R.id.blueLayout), findViewById(R.id.navyBlueLayout), findViewById(R.id.indigoLayout), findViewById(R.id.lilacLayout), findViewById(R.id.pinkLayout), findViewById(R.id.redLayout),
                    findViewById(R.id.coralLayout), findViewById(R.id.orangeLayout), findViewById(R.id.honeyLayout), findViewById(R.id.dodieYellowLayout), findViewById(R.id.brownLayout)};

            //Handle Theme Style Buttons
            for (i=0; i < themeStyleButtons.length; i++) {
                if (themeStyleButtons[i] != null) {
                    final int fi = i;

                    ((ConstraintLayout) themeStyleButtons[i].getParent()).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int j;

                            tinydb.putString("basicTheme", Integer.toString(fi));

                            basicTheme = fi;
                            isMonochrome = (fi == 5);
                            applyTheme();

                            shouldRecreateMain = true;

                            ((ConstraintLayout) themeStyleButtons[basicTheme].getParent()).setBackground(Aux.getDrawable(R.drawable.theme_toggle_selected));

                            for (j=0; j < themeStyleButtons.length; j++) {
                                if (j != fi && themeStyleButtons[j] != null)
                                    ((ConstraintLayout) themeStyleButtons[j].getParent()).setBackground(null);
                            }
                        }
                    });
                }
            }

            //Handle Accent Color Buttons
            for (i=0; i < themeColorButtons.length; i++) {
                if (themeColorButtons[i] != null) {
                    themeColorButtons[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int j;

                            tinydb.putString("color", v.getTag().toString());

                            v.setBackground(Aux.getDrawable(R.drawable.theme_style_selected));

                            for (j=0; j < themeColorButtons.length; j++) {
                                if (themeColorButtons[j] != null && themeColorButtons[j] != v)
                                    themeColorButtons[j].setBackground(null);
                            }

                            shouldRecreateMain = true;

                            updateColors();
                            applyTheme();
                        }
                    });
                }
            }

            for (i=0; i < themeColorButtons.length; i++) {
                String color = tinydb.getString("color");

                if (themeColorButtons[i] != null) {
                    if (color.equals(themeColorButtons[i].getTag().toString())) {
                        themeColorButtons[i].setBackground(Aux.getDrawable(R.drawable.theme_style_selected, ThemeActivity.this));
                        break;
                    }
                }
            }

            //Handle Standard/Custom Toggle Preview Colors
            standardLayout = findViewById(R.id.standardLayout);
            customLayout = findViewById(R.id.customLayout);

            getSupportFragmentManager().beginTransaction().replace(R.id.buttonShapeLayout,
                    new ButtonShapePreferenceActivity.SettingsFragment(), "buttonShape").commit();

            standardButtons = new Button[]{findViewById(R.id.standardEquals), findViewById(R.id.standardPlus), findViewById(R.id.standardParenthesisClose),
                    findViewById(R.id.standardParenthesisOpen), findViewById(R.id.standardPi)};
            customButtons = new Button[]{findViewById(R.id.customEquals), findViewById(R.id.customPlus), findViewById(R.id.customParenthesisClose),
                    findViewById(R.id.customParenthesisOpen), findViewById(R.id.customPi)};

            isMonochrome = basicTheme == 5;

            ConstraintLayout themeColorLayout = findViewById(R.id.themeColorLayout);

            //Wrap Brown & Indigo if screen is too narrow
            if (minimumWidth <= 400) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(themeColorLayout);

                constraintSet.connect(R.id.brownLayout, ConstraintSet.START, R.id.lilacLayout, ConstraintSet.START, 0);
                constraintSet.connect(R.id.brownLayout, ConstraintSet.END, R.id.lilacLayout, ConstraintSet.END, 0);
                constraintSet.connect(R.id.brownLayout, ConstraintSet.TOP, R.id.lilacLayout, ConstraintSet.BOTTOM, 16);
                constraintSet.clear(R.id.brownLayout, ConstraintSet.BOTTOM);

                constraintSet.connect(R.id.indigoLayout, ConstraintSet.START, R.id.pinkLayout, ConstraintSet.START, 0);
                constraintSet.connect(R.id.indigoLayout, ConstraintSet.END, R.id.pinkLayout, ConstraintSet.END, 0);
                constraintSet.connect(R.id.indigoLayout, ConstraintSet.TOP, R.id.pinkLayout, ConstraintSet.BOTTOM, 16);
                constraintSet.clear(R.id.indigoLayout, ConstraintSet.BOTTOM);

                constraintSet.applyTo(themeColorLayout);
            }

            applyTheme();

            onThemeTypeClick(tinydb.getBoolean("custom") ? customLayout : standardLayout);

            shouldRecreateMain = false;

            tinydb.putBoolean("closeDrawer", true);
        }
        catch (Exception e){
            Aux.saveStack(e);
            finish();
        }

        try {
            if (Aux.restored) {
                Aux.restored = false;

                if (!Aux.tinydb().getBoolean("wasCustom")) {
                    super.onBackPressed();
                    startActivity(new Intent(MainActivity.mainActivity, ThemeActivity.class));
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            MenuInflater inflater = getMenuInflater();

            inflater.inflate(R.menu.theme_settings_menu, menu);
        }
        catch (Exception e){
            Aux.saveStack(e);
            finish();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        try {
            if (item.getItemId() == R.id.advancedOptions){
                Intent intent = new Intent(this, AdvancedThemeOptionsActivity.class);
                startActivity(intent);
            }
            else if (item.getItemId() == android.R.id.home) {
                onBackPressed();
                return true;
            }
        }
        catch (Exception e){
            Aux.saveStack(e);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        try {
            TinyDB tinydb = new TinyDB(this);

            boolean initGrad = tinydb.getBoolean("tempIsGradMain");

            String initTheme = tinydb.getString("tempTheme");
            String initColor = tinydb.getString("tempColor");

            String theme = tinydb.getString("basicTheme");
            String color = tinydb.getString("color");

            if (shouldRecreateMain || (!initTheme.equals(theme) || !initColor.equals(color) || (initState != tinydb.getBoolean("custom")) || (tinydb.getBoolean("isGradMain") != initGrad)) || !buttonShape.equals(tinydb.getString("buttonShape"))) {
                MainActivity.mainActivity.recreate();
                shouldRecreateMain = false;
            }

            tinydb.putBoolean("closeDrawer", true);
        }
        catch (Exception e){
            e.printStackTrace();

            try {
                MainActivity.mainActivity.recreate();
                shouldRecreateMain = false;
            }
            catch (Exception e2) {
                Aux.saveStack(e2);
                finish();
            }
        }

        super.onBackPressed();
    }

    public void openEditor(View v) {
        try {
            TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

            try {
                MainActivity.mainActivity.recreate();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String theme = tinydb.getString("theme");
            String customTheme = tinydb.getString("customTheme");

            if (Aux.isDigit(customTheme)) {
                if (customTheme.equals("5"))
                    customTheme = "2";
                else if (customTheme.equals("1") || customTheme.equals("4"))
                    customTheme = "3";

                tinydb.putString("theme", customTheme);
            }
            else if (theme.equals("5")) {
                tinydb.putString("theme", "2");
                tinydb.putString("customTheme", "2");
            }
            else if (theme.equals("1") || theme.equals("4")) {
                tinydb.putString("theme", "3");
                tinydb.putString("customTheme", "3");
            }

            startActivity(new Intent(this, EditorActivity.class));
        }
        catch (Exception e2) {
            Aux.saveStack(e2);
            finish();
        }
    }

    public void openBackups(View v) {
        try {
            Aux.tinydb().putBoolean("wasCustom", Aux.tinydb().getBoolean("custom"));
            startActivity(new Intent(this, Backups.class));
        }
        catch (Exception e) {
            Aux.saveStack(e);
            finish();
        }
    }

    public void onThemeTypeClick (View v) {
        try {
            int i;

            String tag = v.getTag().toString();

            ConstraintLayout mt = findViewById(R.id.themeStyleLayout);
            ConstraintLayout ac = findViewById(R.id.themeColorLayout);

            FrameLayout buttonShapeLayout = findViewById(R.id.buttonShapeLayout);

            ConstraintLayout editor = findViewById(R.id.themeEditorLayout);
            ConstraintLayout savedThemes = findViewById(R.id.backupsLayout);

            ConstraintLayout[] toggleButtons = {findViewById(R.id.standardLayout), findViewById(R.id.customLayout)};
            RadioButton[] toggleRadioButtons = {findViewById(R.id.standardRadioButton), findViewById(R.id.customRadioButton)};
            boolean[] toggleStates = {false, true};
            int[] visibilities = {View.GONE, View.VISIBLE};

            if(themeTypeClicks >= 2)
                shouldRecreateMain = true;

            for (i=0; i < toggleButtons.length; i++) {
                if (toggleButtons[i] != null && toggleButtons[i].getTag().toString().equals(tag)) {
                    toggleButtons[i].setBackground(Aux.getDrawable(R.drawable.theme_toggle_selected));
                    toggleButtons[Math.abs(i-1)].setBackground(null);

                    toggleRadioButtons[i].setChecked(true);
                    toggleRadioButtons[Math.abs(i-1)].setChecked(false);

                    mt.setVisibility(visibilities[Math.abs(i-1)]);
                    ac.setVisibility(visibilities[Math.abs(i-1)]);
                    buttonShapeLayout.setVisibility(visibilities[Math.abs(i-1)]);

                    editor.setVisibility(visibilities[i]);
                    savedThemes.setVisibility(visibilities[i]);

                    Aux.tinydb().putBoolean("custom", toggleStates[i]);

                    shouldRecreateMain = true;
                }
            }

            themeTypeClicks++;
        }
        catch (Exception e) {
            Aux.saveStack(e);
            finish();
        }
    }

    public void applyTheme() {
        try {
            int i;

            TinyDB tinydb = new TinyDB(MainActivity.mainActivity);
            ((ImageButton) themeStyleButtons[4]).setColorFilter(primary);
            ((ImageButton) ((ConstraintLayout) themeStyleButtons[5]).getChildAt(0)).setColorFilter(tertiary);

            ((ConstraintLayout) themeStyleButtons[basicTheme].getParent()).setBackground(Aux.getDrawable(R.drawable.theme_toggle_selected));

            //Handle Standard Buttons
            if (basicTheme == 2) {
                standardButtons[0].setBackgroundColor(Color.WHITE);
            }
            else if ((basicTheme == 3 && !tinydb.getBoolean("custom")) || basicTheme == 4) {
                standardButtons[0].setBackgroundColor(Color.BLACK);
            }
            else if (isMonochrome) {
                standardButtons[0].setBackgroundColor(tertiary);
            }
            else if (basicTheme == 1){
                standardButtons[0].setBackgroundColor(Color.parseColor("#202227"));
            }

            standardButtons[0].setTextColor(isMonochrome ? Color.parseColor("#303030") : primary);

            for (i = 1; i < standardButtons.length; i++) {
                standardButtons[i].setTextColor(isMonochrome ? Color.parseColor("#303030") : Color.WHITE);
            }

            standardButtons[1].setBackgroundColor(isMonochrome ? tertiary : primary);
            standardButtons[2].setBackgroundColor(tertiary);
            standardButtons[3].setBackgroundColor(tertiary);
            standardButtons[4].setBackgroundColor(isMonochrome ? tertiary : secondary);

            if (basicTheme == 4) {
                for (i=1; i < standardButtons.length; i++) {
                    standardButtons[i].setBackgroundColor(Color.BLACK);
                }

                standardButtons[1].setTextColor(primary);
                standardButtons[2].setTextColor(tertiary);
                standardButtons[3].setTextColor(tertiary);
                standardButtons[4].setTextColor(secondary);
            }
            else if (((basicTheme == 1 || basicTheme == 3) && (accentColor + 1 == 14 || accentColor + 1 == 17)) || (basicTheme == 2 && accentColor + 1 == 14)) {
                for (i=1; i < standardButtons.length; i++) {
                    standardButtons[i].setTextColor(darkGray);
                }
            }

            for (i=0; i < standardButtons.length; i++) {
                standardButtons[i].setClickable(false);
                standardButtons[i].setFocusable(false);
            }

            //Handle Equals Custom Button Semi-Defaults
            if (customTheme != 1)
                customButtons[0].setBackgroundColor(Aux.isTinyColor("cKeypad") ? Aux.getTinyColor("cKeypad") : customTheme == 2 ? Color.WHITE : Color.BLACK);

            customButtons[0].setTextColor(Aux.isTinyColor("cNum") ? Aux.getTinyColor("cNum") :
                    (Aux.isTinyColor("cPrimary") ? Aux.getTinyColor("cPrimary") : customTheme == 2 ? darkGray : Color.WHITE));

            //Handle Custom Buttons
            for (i=0; i < customButtons.length; i++) {
                customButtons[i].setClickable(false);
                customButtons[i].setFocusable(false);

                if (Aux.isTinyColor(bgCodes[i]))
                    customButtons[i].setBackgroundColor(Aux.getTinyColor(bgCodes[i]));
                if (Aux.isTinyColor(textCodes[i]))
                    customButtons[i].setTextColor(Aux.getTinyColor(textCodes[i]));
            }

            if (Aux.isTinyColor("-b="))
                customButtons[0].setBackgroundColor(Aux.getTinyColor("-b="));

            //Handle Accent Color Buttons
            for (i=0; i < themeColorButtons.length; i++) {
                try {
                    if (themeColorButtons[i] != null) {
                        ImageButton button = (ImageButton) themeColorButtons[i].getChildAt(0);

                        button.setColorFilter(Color.parseColor(primaryColors[Integer.parseInt(themeColorButtons[i].getTag().toString()) - 1]));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            Aux.saveStack(e);
            finish();
        }
    }

    public void updateColors() {
        TinyDB tinydb;

        try {
            tinydb = new TinyDB(MainActivity.mainActivity);
        }
        catch (Exception e) {
            tinydb = Aux.tinydb(ThemeActivity.themeActivity);
        }

        try {
            accentColor = Integer.parseInt(tinydb.getString("color")) - 1;
        }
        catch (Exception e) {
            accentColor = 1;

            if (!Aux.isFullNum(tinydb.getString("color")))
                tinydb.putString("color", "1");
        }

        primary = Color.parseColor(primaryColors[accentColor]);
        secondary = Color.parseColor(Aux.hexAdd(primaryColors[accentColor], -16));
        tertiary = Color.parseColor(Aux.hexAdd(primaryColors[accentColor], -8));
    }
}