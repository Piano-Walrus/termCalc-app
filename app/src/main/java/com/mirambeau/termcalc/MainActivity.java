package com.mirambeau.termcalc;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

import ch.obermuhlner.math.big.BigDecimalMath;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener{
    static final int androidVersion = Build.VERSION.SDK_INT;
    private DrawerLayout drawer;
    public static Activity mainActivity;
    static Toolbar toolbar;

    MathContext mc = new MathContext(45, RoundingMode.HALF_UP);

    int squarePrecision = 6, roundedPrecision = 8, maxPrecision = 20;

    static float barHeight = (float) 70.1;

    int initDay, finalDay, initMonth, finalMonth, initYear, finalYear, resultDay, resultMonth, resultYear;

    int vibeDuration = 25;

    int foldTextOffset = 0;

    String tvText, fullFrom, fullTo, eqConv, fromTo, fromSave, toSave, selectedFrom, selectedTo, selectedType;
    String bgColor, keypadColor, bTextColor, primary, secondary, tertiary, initSecondary;

    static final String[] subscripts = {"₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉"};

    static final String[] initConstantTitles = {"Avogadro's Number", "Atomic Mass Unit", "Planck's Constant", "Electron Charge", "Gas Constant", "Faraday Constant", "Acceleration of Gravity"};
    static final String[] initConstantNums = {"6.0221409×10²³", "1.67377×10⁻²⁷", "6.6260690×10⁻³⁴", "1.6021766×10⁻¹⁹", "8.3145", "96,485.337", "9.8"};
    static final String[] initConstantUnits = {"mol⁻¹", "kg", "J·s", "C", "J·K⁻¹·mol⁻¹", "C/mol", "m/s²"};

    static final String[] initFunctionTitles = {"Compound Interest", "Simple Interest", "Pythagorean Theorem"};
    static final String[] initFunctionTexts = {"P(1 + (r/n))^nt", "P(1 + rt)", Aux.sq + "(a^2 + b^2)"};
    static final String[] initFunctionVariables = {"P`r`n`t", "P`r`t", "a`b"};

    ArrayList<String> constantTitles, constantNums, constantUnits;
    ArrayList<String> functionTitles, functionTexts, functionVariables;

    Button[] nums, compBar, trigBar, mainOps;
    Button bDec, bParenthesisOpen, bParenthesisClose, bEquals, bMod;
    Button[][] allButtons;

    TextView previousExpression;
    EditText tv;

    Boolean isInv = false, isDec = false, isRad = true, equaled = false, deleted = false, isEquals = false;
    boolean isExpanded, hasExpanded, error, dError, didIntro, isBig, isCustomTheme, dontVibe, isFabExpanded;
    boolean isDate = false;
    boolean theme_boolean = true, isDynamic;
    boolean shouldRecord = true, isFocus = false;
    boolean isDarkTab = true;
    boolean roundedButtons = false;

    DecimalFormat arcdf;

    final int darkGray = Color.parseColor("#222222");
    final int monochromeTextColor = Color.parseColor("#303030");

    int primaryColor, secondaryColor, tertiaryColor;

    ArrayList<ConstantCard> constantCards = new ArrayList<>();
    ArrayList<FunctionCard> functionCards = new ArrayList<>();

    FloatingActionButton[] customFabs;
    TextView[] customLabels;

    ConstantsBottomSheet constantsSheet;
    FunctionsBottomSheet functionsSheet;

    Menu menu;

    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility", "CutPasteId"})
    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int i, j, l;

        try {
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            final TinyDB tinydb = new TinyDB(this);

            isCustomTheme = sp.getBoolean("custom", false);

            String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
            String theme = isCustomTheme ? tinydb.getString("customTheme") : tinydb.getString("basicTheme");

            try {
                if (!Aux.isDigit(theme) || Integer.parseInt(theme) < 1) {
                    if (isCustomTheme)
                        tinydb.putString("customTheme", "1");
                    else
                        tinydb.putString("basicTheme", "1");

                    theme = "1";
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

            tinydb.putString("theme", theme);

            isFocus = tinydb.getBoolean("isFocus");

            //TODO: Enable custom rounded buttons themes, possibly after merging basic and custom themes into one layout
            roundedButtons = !isCustomTheme && tinydb.getString("buttonShape").equals("2") && !theme.equals("5") && !theme.equals("4");

            if (!tinydb.getBoolean("mtIsSet"))
                tinydb.putString("-mt", "\0");

            if (!tinydb.getBoolean("clearedHistory")) {
                tinydb.putListString("equations", new ArrayList<>());
                tinydb.putListString("answers", new ArrayList<>());
                tinydb.putListInt("dayEntries", new ArrayList<>());
                tinydb.putListInt("monthEntries", new ArrayList<>());
                tinydb.putListInt("yearEntries", new ArrayList<>());

                tinydb.putBoolean("clearedHistory", true);
            }

            isDynamic = tinydb.getBoolean("isDynamic");
            tinydb.putBoolean("termButton", true);

            isDarkTab = tinydb.getBoolean("isDarkTab");

            //If basic theme style is not set, set it to Dark
            if (!Aux.isDigit(tinydb.getString("basicTheme")))
                tinydb.putString("basicTheme", "1");

            if (!isDynamic) {
                StringBuilder dfStr = new StringBuilder("#,###.");
                int precision = tinydb.getInt("precision");

                if (precision == 0) {
                    precision = 12;
                    tinydb.putInt("precision", 12);
                }

                for (i = 0; i < precision; i++) {
                    dfStr.append("#");
                }

                arcdf = new DecimalFormat(dfStr.toString());
            }
            else
                arcdf = new DecimalFormat("#,###.####");

            int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

            if (Aux.isNull(color))
                color = "1";
            else if (color.equals("18")) {
                color = "1";
                tinydb.putString("color", "1");
                tinydb.putBoolean("custom", true);
            }

            //Start handling theme
            int cursorInt = theme.equals("2") ? 1 : 0;
            Aux.cursorColors = new int[][]{{R.style.d_03DAC5, R.style.d_009688, R.style.d_54AF57, R.style.d_00C7E0, R.style.d_2196F3, R.style.d_0D2A89, R.style.d_3F51B5, R.style.d_6C42B6, R.style.d_E32765, R.style.d_F44336, R.style.d_E77369, R.style.d_FF9800, R.style.d_FFC107, R.style.d_FEF65B, R.style.d_66BB6A, R.style.d_873804, R.style.d_9BCEE9}, {R.style.l_03DAC5, R.style.l_009688, R.style.l_54AF57, R.style.l_00C7E0, R.style.l_2196F3, R.style.l_0D2A89, R.style.l_3F51B5, R.style.l_6C42B6, R.style.l_E32765, R.style.l_F44336, R.style.l_E77369, R.style.l_FF9800, R.style.l_FFC107, R.style.l_FEF65B, R.style.l_66BB6A, R.style.l_873804, R.style.l_9BCEE9}};
            Aux.switchColors = new int[]{R.style.ds_03DAC5, R.style.ds_009688, R.style.ds_54AF57, R.style.ds_00C7E0, R.style.ds_2196F3, R.style.ds_0D2A89, R.style.ds_3F51B5, R.style.ds_6C42B6, R.style.ds_E32765, R.style.ds_F44336, R.style.ds_E77369, R.style.ds_FF9800, R.style.ds_FFC107, R.style.ds_FEF65B, R.style.ds_66BB6A, R.style.ds_873804, R.style.ds_9BCEE9};


            final String[] primaryColors = {"#03DAC5", "#009688", "#54AF57", "#00C7E0", "#2196F3", "#0D2A89", "#3F51B5", "LILAC", "PINK", "#F44336", "#E77369", "#FF9800", "#FFC107", "#FEF65B", "#66BB6A", "#873804", "#B8E2F8"};
            final String[][] secondaryColors = {{"#53E2D4", "#4DB6AC", "#77C77B", "#51D6E8", "#64B5F6", "#1336A9", "#7986CB", "#8C6DCA", "#F06292", "#FF5956", "#EC8F87", "#FFB74D", "#FFD54F", "#FBF68D", "#EF5350", "#BD5E1E", "#B8E2F8"}, {"#00B5A3", "#00796B", "#388E3C", "#0097A7", "#1976D2", "#0A2068", "#303F9F", "#5E35B1", "#C2185B", "#D32F2F", "#D96459", "#F57C00", "#FFA000", "#F4E64B", "#EF5350", "#572300", "#9BCEE9"}};
            final String[][] tertiaryColors = {{"#3CDECE", "#26A69A", "#68B86E", "#39CFE3", "#42A5F5", "#0D2F9E", "#5C6BC0", "#7857BA", "#EC407A", "#FA4E4B", "#EB837A", "#FFA726", "#FFCB2E", "#F8F276", "#FF5754", "#A14D15", "#ABDBF4"}, {"#00C5B1", "#00897B", "#43A047", "#00ACC1", "#1E88E5", "#0A2373", "#3949AB", "#663ABD", "#D81B60", "#E33532", "#DE685D", "#FB8C00", "#FFB300", "#FCEE54", "#FF5754", "#612703", "#ABDBF4"}};

            tinydb.putString("cbEquals", "");
            tinydb.putString("cPlus", "");
            tinydb.putString("cMinus", "");
            tinydb.putString("cMulti", "");
            tinydb.putString("cDiv", "");

            boolean isCustom = false;
            boolean isCustomNav = tinydb.getBoolean("navTheme");
            boolean isAlwaysDarkNav = tinydb.getBoolean("isAlwaysDarkNav");

            String navBG;
            String tempTheme = theme;

            if (isAlwaysDarkNav)
                theme = "3";

            if (theme.equals("2"))
                navBG = "#FFFFFF";
            else if (theme.equals("1"))
                navBG = "#202227";
            else
                navBG = "#191919";

            if (isCustomTheme && isCustomNav && Aux.isColor(tinydb.getString("cMain")) && theme.equals("2"))
                navBG = tinydb.getString("cMain");

            theme = tempTheme;

            // ------------------- Set Content View ------------------- //
            setContentView(roundedButtons && !theme.equals("5") ? R.layout.activity_main_round : R.layout.activity_main);

            final SharedPreferences mPrefs = getSharedPreferences("THEME", 0);
            theme_boolean = mPrefs.getBoolean("theme_boolean", true);

            tinydb.putBoolean("theme_boolean", theme_boolean);

            mainActivity = this;
            drawer = findViewById(R.id.drawer_layout);

            bDec = findViewById(R.id.bDec);
            bParenthesisOpen = findViewById(R.id.bParenthesisOpen);
            bParenthesisClose = findViewById(R.id.bParenthesisClose);
            bEquals = findViewById(R.id.bEquals);
            bMod = findViewById(R.id.bMod);

            nums = new Button[]{findViewById(R.id.b0), findViewById(R.id.b1), findViewById(R.id.b2), findViewById(R.id.b3), findViewById(R.id.b4), findViewById(R.id.b5), findViewById(R.id.b6), findViewById(R.id.b7), findViewById(R.id.b8), findViewById(R.id.b9)};
            compBar = new Button[]{findViewById(R.id.bSqrt), findViewById(R.id.bExp), findViewById(R.id.bFact), findViewById(R.id.bPi), findViewById(R.id.bE), findViewById(R.id.bLog), findViewById(R.id.bLn), bMod};
            trigBar = new Button[]{findViewById(R.id.bSin), findViewById(R.id.bCos), findViewById(R.id.bTan), findViewById(R.id.bCsc), findViewById(R.id.bSec), findViewById(R.id.bCot), findViewById(R.id.bInv)};
            mainOps = new Button[]{findViewById(R.id.sPlus), findViewById(R.id.sMinus), findViewById(R.id.sMulti), findViewById(R.id.sDiv)};

            allButtons = new Button[][]{nums, compBar, trigBar, mainOps, {bDec, bParenthesisOpen, bParenthesisClose, bEquals, bMod}};

            tv = findViewById(R.id.equation);
            tv.setEnabled(true);

            if (tinydb.getBoolean("showPreviousExpression"))
                previousExpression = findViewById(R.id.previousExpression);

            final Button[] nums = {findViewById(R.id.b0), findViewById(R.id.b1), findViewById(R.id.b2), findViewById(R.id.b3), findViewById(R.id.b4), findViewById(R.id.b5), findViewById(R.id.b6), findViewById(R.id.b7), findViewById(R.id.b8), findViewById(R.id.b9)};

            final Button bgAnim = findViewById(R.id.bgAnim);

            final FloatingActionButton bDel;
            final ImageButton backspace;

            backspace = roundedButtons ? findViewById(R.id.delete) : null;
            bDel = findViewById(R.id.bDel);

            final Button bDegRad = findViewById(R.id.bDegRad);

            final ConstraintLayout main = findViewById(R.id.mainView);
            final ConstraintLayout keypad = findViewById(R.id.buttonView);

            final ImageButton expandBG = findViewById(R.id.expandBG);

            toolbar = findViewById(R.id.toolbar);

            try {
                setSupportActionBar(toolbar);
            } catch (Exception e) {
                Aux.saveStack(e);
            }

            try {
                getSupportActionBar().setTitle(getResources().getString(R.string.home_menu_item));
            } catch (NullPointerException except) {
                Aux.saveStack(except);
                finish();
            }

            // Set up nav drawer
            final NavigationView navigationView = findViewById(R.id.nav_view);

            try {
                navigationView.setNavigationItemSelectedListener(this);
            } catch (NullPointerException except) {
                Aux.saveStack(except);
                finish();
            }

            //Theme nav drawer background
            navigationView.setBackgroundColor(Color.parseColor(navBG));

            if (isAlwaysDarkNav)
                theme = "3";

            ColorStateList navItemCsl = AppCompatResources.getColorStateList(this, R.color.dark_drawer_item);

            if (theme.equals("2")) {
                navigationView.setItemBackground(ContextCompat.getDrawable(this, R.drawable.light_drawer_list_selector));

                navItemCsl = AppCompatResources.getColorStateList(this, R.color.light_drawer_item);
            }
            else if (theme.equals("1"))
                navigationView.setItemBackground(ContextCompat.getDrawable(this, R.drawable.drawer_selected_item));
            else
                navigationView.setItemBackground(ContextCompat.getDrawable(this, R.drawable.dark_drawer_list_selector));

            if (isCustomTheme && isCustomNav) {
                int unchecked = theme.equals("2") ? darkGray : Color.WHITE;
                int checked;

                if (Aux.isTinyColor("-mt") && Aux.isGray(tinydb.getString("-mt"))) {
                    if (theme.equals("2")) {
                        if (Aux.getAverageBrightness(tinydb.getString("-mt")) <= 9)
                            unchecked = Aux.getTinyColor("-mt");
                    }
                    else {
                        if (Aux.getAverageBrightness(tinydb.getString("-mt")) > 9)
                            unchecked = Aux.getTinyColor("-mt");
                    }
                }

                String navAccentColor = Aux.getNavAccentColor();

                if (theme.equals("2")) {
                    if (Aux.getAverageBrightness(navAccentColor) > 12)
                        navAccentColor = Aux.hexAdd(navAccentColor, -96);
                    else if (Aux.getAverageBrightness(navAccentColor) > 11)
                        navAccentColor = Aux.hexAdd(navAccentColor, -84);
                }
                else {
                    if (Aux.getAverageBrightness(navAccentColor) < 5)
                        navAccentColor = Aux.hexAdd(navAccentColor, 32);
                }

                checked = Color.parseColor(navAccentColor);

                navItemCsl = new ColorStateList(
                        new int[][]{
                                {-android.R.attr.state_checked}
                                , {android.R.attr.state_checked}
                        },

                        new int[]{unchecked, checked}
                );
            }

            navigationView.setItemTextColor(navItemCsl);
            navigationView.setItemIconTintList(navItemCsl);

            //Account for older devices that can't render png's for some reason
            final View header = navigationView.getHeaderView(0);

            if (header != null) {
                final ConstraintLayout navBg = header.findViewById(R.id.nav_header_bg);

                if (Build.VERSION.SDK_INT > 23 && navBg != null) {
                    navBg.setBackground(ContextCompat.getDrawable(this, R.drawable.termcalc_nav_header_kik_compressed));
                }
                else {
                    if (theme.equals("2")) {
                        ((TextView) header.findViewById(R.id.navHeaderTitle)).setTextColor(darkGray);
                        ((TextView) header.findViewById(R.id.navHeaderSubtitle)).setTextColor(darkGray);
                    }
                }
            }

            //Theme Nav Header
            if (header != null && isCustomTheme && isCustomNav) {
                final ConstraintLayout navBg = header.findViewById(R.id.nav_header_bg);

                ImageView left, leftText, right, rightText, bRight, bRightText;
                String cLeft, cLeftText, cRight = "", cRightText = "", cbRight = "", cbRightText = "";

                left = header.findViewById(R.id.navHeaderIcon);
                leftText = header.findViewById(R.id.navHeaderIconLeftText);
                right = header.findViewById(R.id.navHeaderIconRight);
                rightText = header.findViewById(R.id.navHeaderIconTopRightText);
                bRight = header.findViewById(R.id.navHeaderIconBottomRight);
                bRightText = header.findViewById(R.id.navHeaderIconBottomRightText);

                if (theme.equals("2"))
                    navBg.setBackground(ContextCompat.getDrawable(this, R.drawable.light_drawer_header_rounded));
                else if (theme.equals("1"))
                    navBg.setBackground(ContextCompat.getDrawable(this, R.drawable.drawer_header_rounded));
                else
                    navBg.setBackground(ContextCompat.getDrawable(this, R.drawable.dark_drawer_header_rounded));

                if (theme.equals("2")) {
                    if (Aux.isTinyColor("-mt") && Aux.isGray(tinydb.getString("-mt")))
                        ((TextView) header.findViewById(R.id.navHeaderTitle)).setTextColor(Aux.getTinyColor("-mt"));
                    else
                        ((TextView) header.findViewById(R.id.navHeaderTitle)).setTextColor(darkGray);

                    try {
                        if (Aux.isTinyColor("-mt") && Aux.isGray(tinydb.getString("-mt")))
                            ((TextView) header.findViewById(R.id.navHeaderSubtitle)).setTextColor(Aux.getTinyColor("-mt"));
                        else
                            ((TextView) header.findViewById(R.id.navHeaderSubtitle)).setTextColor(darkGray);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                boolean isDarkened = false;

                if (Aux.isTinyColor("cKeypad")) {
                    if (Aux.isTinyColor("cMain") && Aux.getTinyColor("cKeypad") == Aux.getTinyColor("cMain")) {
                        cLeft = Aux.hexAdd(tinydb.getString("cKeypad"), -12);
                        isDarkened = true;
                    }
                    else
                        cLeft = tinydb.getString("cKeypad");
                }
                else {
                    if (theme.equals("2"))
                        cLeft = "#FFFFFF";
                    else if (theme.equals("3") || theme.equals("4"))
                        cLeft = "#000000";
                    else if (theme.equals("5"))
                        cLeft = "#53E2D4";
                    else
                        cLeft = "#272C33";
                }

                if (Aux.isTinyColor("cPrimary")) {
                    int dimTop, dimBottom;
                    int primaryInt = Aux.getTinyColor("cPrimary");
                    String primaryStr = tinydb.getString("cPrimary");

                    if (Aux.isTinyColor("cKeypad")) {
                        if (Aux.getTinyColor("cKeypad") == primaryInt) {
                            if (isDarkened) {
                                dimTop = -12;
                                dimBottom = -12;
                            }
                            else {
                                dimTop = 0;
                                dimBottom = 0;
                            }
                        }
                        else {
                            dimTop = 1;
                            dimBottom = -9;
                        }
                    }
                    else {
                        if (theme.equals("1") && primaryInt == Color.parseColor("#272C33") ||
                                theme.equals("2") && primaryInt == Color.WHITE ||
                                (theme.equals("3") || theme.equals("4")) && primaryInt == Color.BLACK) {
                            dimTop = 0;
                            dimBottom = 0;
                        }
                        else {
                            dimTop = 1;
                            dimBottom = -9;
                        }
                    }

                    cbRight = Aux.hexAdd(primaryStr, dimBottom);
                    cRight = Aux.hexAdd(primaryStr, dimTop);
                }

                if (Aux.isTinyColor("cNum"))
                    cLeftText = tinydb.getString("cNum");
                else
                    cLeftText = theme.equals("2") ? "#222222" : "#FFFFFF";

                boolean isKeyText;
                String[] topOps = {"-b" + Aux.divi + "t", "-b" + Aux.multi + "t"};
                String[] bottomOps = {"-b+t", "-b-t"};

                for (i = 0; i < topOps.length; i++) {
                    if (Aux.isTinyColor(topOps[i]))
                        isKeyText = Aux.isTinyColor("cNum") ? Aux.getTinyColor(topOps[i]) == Aux.getTinyColor("cNum") : Aux.getTinyColor(topOps[i]) == Color.WHITE;
                    else
                        isKeyText = true;

                    if ((!isKeyText || i == topOps.length - 1) && Aux.isTinyColor(topOps[i])) {
                        cRightText = tinydb.getString(topOps[i]);

                        if (Aux.isTinyColor(Aux.newTrim(topOps[i], 1)))
                            cRight = tinydb.getString(Aux.newTrim(topOps[i], 1));

                        break;
                    }
                }

                if (Aux.isTinyColor("-bop")) {
                    String primaryTextColor = tinydb.getString("-bop");

                    cRightText = primaryTextColor;
                    cbRightText = primaryTextColor;
                }

                for (i = 0; i < bottomOps.length; i++) {
                    if (Aux.isTinyColor(bottomOps[i])) {
                        if (Aux.isTinyColor("cNum"))
                            isKeyText = Aux.getTinyColor(bottomOps[i]) == Aux.getTinyColor("cNum");
                        else
                            isKeyText = Aux.getTinyColor(bottomOps[i]) == Color.WHITE;
                    }
                    else
                        isKeyText = true;

                    if ((!isKeyText || i == bottomOps.length - 1) && Aux.isTinyColor(bottomOps[i])) {
                        cbRightText = tinydb.getString(bottomOps[i]);

                        if (Aux.isTinyColor(Aux.newTrim(bottomOps[i], 1)))
                            cbRight = tinydb.getString(Aux.newTrim(bottomOps[i], 1));

                        break;
                    }
                }

                boolean isAllGray = true;
                ImageView[] zones = {left, leftText, right, rightText, bRight, bRightText};
                String[] colors = {cLeft, cLeftText, cRight, cRightText, cbRight, cbRightText};

                for (i = 0; i < colors.length; i++) {
                    if (Aux.isColor(colors[i]) && !Aux.isGray(colors[i])) {
                        isAllGray = false;
                        break;
                    }
                }

                if (isAllGray && Aux.isTinyColor("cFab") && Aux.isTinyColor("cFabText") && (!Aux.isGray(tinydb.getString("cFab")) || !Aux.isGray(tinydb.getString("cFabText")))) {
                    colors[4] = tinydb.getString("cFab");
                    colors[5] = tinydb.getString("cFabText");
                }

                for (i = 0; i < zones.length; i++) {
                    if (Aux.isColor(colors[i]))
                        zones[i].setColorFilter(Color.parseColor(colors[i]));
                }
            }

            theme = tempTheme;

            if (isCustomTheme) {
                if (theme.equals("3") && tinydb.getBoolean("darkStatusBar")) {
                    theme = "1";

                    tinydb.putString("theme", "1");
                    tinydb.putString("customTheme", "1");

                    EditorActivity.THEME_DARK = "1";

                }
                else if (theme.equals("1") && !tinydb.getBoolean("darkStatusBar")) {
                    theme = "3";

                    tinydb.putString("theme", "3");
                    tinydb.putString("customTheme", "3");

                    EditorActivity.THEME_DARK = "3";
                }
            }

            constantTitles = tinydb.getListString("constantTitles");
            constantNums = tinydb.getListString("constantNums");
            constantUnits = tinydb.getListString("constantUnits");

            functionTitles = tinydb.getListString("functionTitles");
            functionTexts = tinydb.getListString("functionTexts");
            functionVariables = tinydb.getListString("functionVariables");

            if ((constantTitles.size() < 1 && !tinydb.getBoolean("hasDeletedConstant"))) {
                for (i = 0; i < initConstantTitles.length; i++) {
                    constantTitles.add(initConstantTitles[i]);
                    constantNums.add(initConstantNums[i]);
                    constantUnits.add(initConstantUnits[i]);
                }

                tinydb.putListString("constantTitles", constantTitles);
                tinydb.putListString("constantNums", constantNums);
                tinydb.putListString("constantUnits", constantUnits);
            }

            if ((functionTitles.size() < 1 && !tinydb.getBoolean("hasDeletedFunction"))) {
                for (i = 0; i < initFunctionTitles.length; i++) {
                    functionTitles.add(initFunctionTitles[i]);
                    functionTexts.add(initFunctionTexts[i]);
                    functionVariables.add(initFunctionVariables[i]);
                }

                tinydb.putListString("functionTitles", functionTitles);
                tinydb.putListString("functionTexts", functionTexts);
                tinydb.putListString("functionVariables", functionVariables);
            }

            for (i = 0; i < constantTitles.size(); i++) {
                constantCards.add(new ConstantCard(constantTitles.get(i), constantNums.get(i), constantUnits.get(i)));
            }

            ArrayList<VariablesAdapter> adapters = new ArrayList<>();

            for (i = 0; i < functionTitles.size(); i++) {
                functionCards.add(new FunctionCard(functionTitles.get(i), functionTexts.get(i), functionVariables.get(i)));

                adapters.add(new VariablesAdapter(functionVariables.get(i).split("`"), i));
            }

            try {
                Aux.adapter = new FunctionsAdapter(functionCards, adapters);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            final ConstraintLayout scrollBar = findViewById(R.id.scrollBar);

            //Debug Output
            bgAnim.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            bgAnim.setAllCaps(false);

            if (theme == null || theme.equals("\0"))
                theme = "1";

            if (theme != null && !theme.equals("\0")) {
                if (theme.equals("2"))
                    bgAnim.setTextColor(darkGray);
                else if (theme.equals("5"))
                    bgAnim.setTextColor(monochromeTextColor);
                else
                    bgAnim.setTextColor(Color.WHITE);
            }

            theme_boolean = theme.equals("1") || theme.equals("3");

            if (theme.equals("5"))
                tinydb.putBoolean("theme_boolean", theme_boolean);

            // - Change Button Colors According to Theme -
            if (color == null || color.equals("\0"))
                color = "1";

            //Get primary, secondary, and tertiary colors
            //Basic Theme
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

                switch (primary) {
                    case "LILAC":
                        if (!theme_boolean)
                            primary = "#6C42B6";
                        else
                            primary = "#7357C2";
                        break;

                    case "PINK":
                        if (!theme_boolean)
                            primary = "#E32765";
                        else
                            primary = "#E91E63";
                        break;

                    case "#B8E2F8":
                        if (!theme_boolean)
                            primary = "#9BCEE9";
                        else
                            primary = "#B8E2F8";
                        break;
                }
            }
            //Custom
            else {
                String cPrimary = tinydb.getString("cPrimary");
                String cSecondary = tinydb.getString("cSecondary");
                String cTertiary = tinydb.getString("cTertiary");

                //Primary
                if (Aux.isColor(cPrimary)) {
                    primary = cPrimary;
                }
                else {
                    primary = "#03DAC5";
                    cPrimary = "#03DAC5";
                    tinydb.putString("cPrimary", cPrimary);
                }

                //Secondary
                if (Aux.isColor(cSecondary)) {
                    secondary = cSecondary;
                }
                else {
                    secondary = "#00B5A3";
                    cSecondary = "#00B5A3";
                    tinydb.putString("cSecondary", cSecondary);
                }

                //Tertiary
                if (Aux.isColor(cTertiary)) {
                    tertiary = cTertiary;
                }
                else {
                    tertiary = "#00C5B1";
                    cTertiary = "#00C5B1";
                    tinydb.putString("cTertiary", cTertiary);
                }
            }

            //Light
            if (!theme_boolean)
                initSecondary = secondaryColors[0][Integer.parseInt(color) - 1];
            //Dark
            else
                initSecondary = secondaryColors[1][Integer.parseInt(color) - 1];

            primaryColor = Color.parseColor(primary);
            secondaryColor = Color.parseColor(secondary);
            tertiaryColor = Color.parseColor(tertiary);

            tinydb.putString("site", "none");

            final ConstraintLayout tertiaryButtons = findViewById(R.id.tertiaryButtons);
            FrameLayout frame = findViewById(R.id.fragment_container);

            //Set equals button text color to primary color
            bEquals.setTextColor(primaryColor);

            //Set the four main operator button background colors to primary color
            for (Button button : mainOps) {
                if (roundedButtons)
                    button.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
                else
                    button.setBackgroundColor(primaryColor);
            }

            try {
                bDel.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
            }
            catch (Exception e) {
                e.printStackTrace();

                if (roundedButtons)
                    backspace.setColorFilter(primaryColor);
            }

            if (tertiaryButtons != null) {
                if (roundedButtons)
                    tertiaryButtons.setBackgroundTintList(ColorStateList.valueOf(tertiaryColor));
                else
                    tertiaryButtons.setBackgroundColor(tertiaryColor);
            }

            if (!roundedButtons) {
                bParenthesisClose.setBackgroundColor(tertiaryColor);
                bParenthesisOpen.setBackgroundColor(tertiaryColor);
            }

            if (!roundedButtons)
                scrollBar.setBackgroundColor(theme.equals("5") ? Color.parseColor(isDarkTab ? Aux.hexAdd(secondary, -6) : secondary) : secondaryColor);

            if (!isBig) {
                if (theme.equals("2"))
                    setExpandBGColor(Aux.hexAdd(secondary, 0));
                else if (theme.equals("4"))
                    setExpandBGColor("#000000");
                else
                    setExpandBGColor(isDarkTab ? Aux.hexAdd(secondary, -6) : secondary);
            }

            final int orientation = this.getResources().getConfiguration().orientation;

            bgColor = "#FFFFFF";
            keypadColor = "#FFFFFF";
            bTextColor = "#21262B";

            DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
            float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
            float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
            float minimumWidth = (dpHeight == dpWidth) ? dpWidth : Math.min(dpHeight, dpWidth);

            if (minimumWidth >= 600)
                isBig = true;

            if (minimumWidth <= 360)
                tinydb.putBoolean("mightBeFold3", true);

            if (tinydb.getBoolean("mightBeFold3") && (minimumWidth >= 600)) {
                tinydb.putBoolean("isFold3", true);
            }

            if (theme != null && color != null && ((color.equals("14") && !theme.equals("4")) || (color.equals("17") && (theme.equals("3") || theme.equals("1")))) && !roundedButtons) {
                bDel.setColorFilter(darkGray);
            }

            if (isAlwaysDarkNav)
                Aux.cursorColor = Aux.cursorColors[0][Integer.parseInt(color)-1];
            else
                Aux.cursorColor = Aux.cursorColors[cursorInt][Integer.parseInt(color)-1];

            setTheme(Aux.cursorColor);

            //Light
            if (theme.equals("2")) {
                final ImageButton expand = findViewById(R.id.expand);

                //Color System Bars
                getWindow().setNavigationBarColor(Color.parseColor("#1A1A1B"));

                if (Build.VERSION.SDK_INT >= 23) {
                    main.setFitsSystemWindows(true);
                    frame.setFitsSystemWindows(true);

                    if (androidVersion >= 29)
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    else
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }

                setMTColor(darkGray);

                if (expand != null)
                    expand.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_expand_up_dark_24));
            }

            //Dark
            else if (theme.equals("1")) {
                toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Dark);

                bgColor = "#272C33";
                keypadColor = "#1C232B";
                bTextColor = "#FFFFFF";

                if (isCustomTheme) {
                    main.setFitsSystemWindows(true);
                    frame.setFitsSystemWindows(true);
                }

                getWindow().setNavigationBarColor(Color.parseColor(Aux.hexAdd(bgColor, -15)));

                setMTColor(Color.WHITE);
            }

            //AMOLED BLACK w/ Buttons
            else if (theme.equals("3")) {
                toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Dark);

                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setNavigationBarColor(Color.BLACK);
                }

                main.setFitsSystemWindows(true);
                frame.setFitsSystemWindows(true);

                setMTColor(Color.WHITE);

                bgColor = "#000000";
                keypadColor = "#000000";
                bTextColor = "#FFFFFF";
            }

            //AMOLED BLACK just text
            else if (theme.equals("4")) {
                toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Dark);

                getWindow().setNavigationBarColor(Color.BLACK);

                main.setFitsSystemWindows(true);
                frame.setFitsSystemWindows(true);

                setMTColor(Color.WHITE);

                bgColor = "#000000";
                keypadColor = "#000000";
                bTextColor = "#FFFFFF";

                bEquals.setTextColor(primaryColor);

                for (i = 0; i < 4; i++) {
                    mainOps[i].setTextColor(primaryColor);
                }

                if (bMod != null) {
                    bMod.setTextColor(secondaryColor);
                }

                for (i = 0; i < 7; i++) {
                    try {
                        compBar[i].setTextColor(secondaryColor);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (i = 0; i < 7; i++) {
                    try {
                        trigBar[i].setTextColor(secondaryColor);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                final Button inv2 = findViewById(R.id.bInv2);

                if (inv2 != null) {
                    inv2.setTextColor(Aux.isTinyColor("-bINV2t") && isCustomTheme ? Aux.getTinyColor("-bINV2t") : tertiaryColor);
                }

                bParenthesisOpen.setTextColor(tertiaryColor);
                bParenthesisClose.setTextColor(tertiaryColor);

                if (!roundedButtons)
                    scrollBar.setBackgroundColor(Color.BLACK);

                for (i = 0; i < 4; i++) {
                    mainOps[i].setBackgroundColor(Color.BLACK);
                }

                if (tertiaryButtons != null) {
                    tertiaryButtons.setBackgroundColor(Color.BLACK);
                }

                bParenthesisClose.setBackgroundColor(Color.BLACK);
                bParenthesisOpen.setBackgroundColor(Color.BLACK);

                if (!roundedButtons)
                    bDel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#191919")));
            }

            //Monochrome
            else if (theme.equals("5")) {
                bgColor = secondary;

                secondary = isDarkTab ? Aux.hexAdd(secondary, -6) : secondary;

                bTextColor = "#303030";
                keypadColor = secondary;

                toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Dark);

                getWindow().setNavigationBarColor(Color.BLACK);

                    if (Build.VERSION.SDK_INT >= 23) {
                        main.setFitsSystemWindows(true);
                        frame.setFitsSystemWindows(true);
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                        if (androidVersion >= 29) {
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                            getWindow().setNavigationBarColor(Color.parseColor(secondary));
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        }
                    }

                setMTColor(monochromeTextColor);

                bEquals.setTextColor(monochromeTextColor);

                for (i = 0; i < 4; i++) {
                    mainOps[i].setTextColor(monochromeTextColor);
                }

                if (bMod != null) {
                    bMod.setTextColor(monochromeTextColor);
                }

                for (i = 0; i < 7; i++) {
                    try {
                        compBar[i].setTextColor(monochromeTextColor);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (i = 0; i < 7; i++) {
                    try {
                        trigBar[i].setTextColor(monochromeTextColor);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                final Button inv2 = findViewById(R.id.bInv2);

                if (inv2 != null) {
                    inv2.setTextColor(monochromeTextColor);
                }

                bParenthesisOpen.setTextColor(monochromeTextColor);
                bParenthesisClose.setTextColor(monochromeTextColor);

                scrollBar.setBackgroundColor(Color.parseColor(secondary));

                for (i = 0; i < 4; i++) {
                    mainOps[i].setBackgroundColor(Color.parseColor(secondary));
                }

                if (tertiaryButtons != null) {
                    tertiaryButtons.setBackgroundColor(Color.parseColor(secondary));
                }

                bParenthesisClose.setBackgroundColor(Color.parseColor(secondary));
                bParenthesisOpen.setBackgroundColor(Color.parseColor(secondary));

                if (roundedButtons) {
                    backspace.setColorFilter(monochromeTextColor);
                }
                else {
                    bDel.setBackgroundTintList(ColorStateList.valueOf(monochromeTextColor));
                    bDel.setColorFilter(Color.parseColor(secondary));
                }

                for (i = 0; i < 4; i++) {
                    mainOps[i].setElevation(0);
                    mainOps[i].setStateListAnimator(null);
                }

                findViewById(R.id.buttonView).setElevation(0);
            }


            //Dodie Yellow Text Color
            if (!theme.equals("5") && !isCustomTheme) {
                if ((color.equals("14") && !theme.equals("4")) || (color.equals("17") && (theme.equals("3") || theme.equals("1")))) {
                    if (color.equals("14")) {
                        bEquals.setTextColor(Color.parseColor("#f4e64b"));
                    }
                    else {
                        bEquals.setTextColor(primaryColor);
                    }
                    for (i = 0; i < 4; i++) {
                        mainOps[i].setTextColor(darkGray);
                    }

                    if (bMod != null) {
                        bMod.setTextColor(darkGray);
                    }

                    for (i = 0; i < 7; i++) {
                        try {
                            compBar[i].setTextColor(darkGray);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    for (i = 0; i < 7; i++) {
                        try {
                            trigBar[i].setTextColor(darkGray);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        if (!isBig)
                            ((ImageButton) findViewById(R.id.expand)).setColorFilter(darkGray);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    bParenthesisOpen.setTextColor(darkGray);
                    bParenthesisClose.setTextColor(darkGray);

                    //Fixed the fab and animations with some booleans, idk
                    if (!roundedButtons)
                        bDel.setImageDrawable(ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.ic_arrow_dark_24));
                }
            }

            final String finalTheme = theme;

            if (theme.equals("2") || theme.equals("5")) {
                ActionBarDrawerToggle mcToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
                    private float last = 0;

                    @Override
                    public void onDrawerSlide(@NonNull View arg0, float arg1) {
                        super.onDrawerSlide(arg0, arg1);

                        final boolean opening = arg1 > last;
                        final boolean closing = arg1 < last;

                        if (opening) {
                            if (androidVersion >= 29) {
                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                                getWindow().setNavigationBarColor(Color.BLACK);
                            }
                        }
                        else if (closing) {
                            if (androidVersion >= 29) {
                                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                                if (finalTheme.equals("5")) {
                                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

                                    new Handler((Looper.myLooper())).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (bgColor == null || bgColor.equals("\0"))
                                                bgColor = secondary;

                                            getWindow().setNavigationBarColor(Color.parseColor(secondary));
                                        }
                                    }, 110);
                                }
                            }
                        }

                        last = arg1;
                    }
                };

                drawer.addDrawerListener(mcToggle);
            }
            else {
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                drawer.addDrawerListener(toggle);
                toggle.syncState();
            }

            int bTextColorInt = Color.parseColor(bTextColor);

            for (i = 0; i < 10; i++) {
                nums[i].setTextColor(bTextColorInt);
            }

            bDec.setTextColor(bTextColorInt);

            tv.setTextColor(bTextColorInt);

            main.setBackgroundColor(Color.parseColor(bgColor));
            bgAnim.setBackgroundColor(Color.parseColor(bgColor));

            if (!roundedButtons)
                keypad.setBackgroundColor(Color.parseColor(keypadColor));

            if (theme.equals("5"))
                toolbar.setTitleTextColor(monochromeTextColor);
            else
                toolbar.setTitleTextColor(bTextColorInt);

            bDegRad.setTextColor(bTextColorInt);

            parColorCheck();

            String cPlus = tinydb.getString("-b+t");
            String cMinus = tinydb.getString("-b-t");
            String cMulti = tinydb.getString("-b×t");
            String cDiv = tinydb.getString("-b÷t");

            if (isCustomTheme || (color.equals("1") && (!cPlus.equals("\0") || !cMinus.equals("\0") || !cMulti.equals("\0") || !cDiv.equals("\0")))) {
                if (isCustom) {
                    if (Aux.isColor(cPlus)) {
                        mainOps[0].setTextColor(Color.parseColor(cPlus));
                    }
                    if (Aux.isColor(cMinus)) {
                        mainOps[1].setTextColor(Color.parseColor(cMinus));
                    }
                    if (Aux.isColor(cMulti)) {
                        mainOps[2].setTextColor(Color.parseColor(cMulti));
                    }
                    if (Aux.isColor(cDiv)) {
                        mainOps[3].setTextColor(Color.parseColor(cDiv));
                    }
                }
            }

            if (!deleted) {
                clear(bDel);
                deleted = true;
            }

            if (isCustomTheme) {
                String cbEquals = tinydb.getString("cbEquals");
                String cKeypad = tinydb.getString("cKeypad");
                String cMain = tinydb.getString("cMain");
                String cNum = tinydb.getString("cNum");
                String cFab = tinydb.getString("cFab");

                bgColor = cMain;

                if (cbEquals != null && !cbEquals.equals("\0") && cbEquals.length() == 7) {
                    bEquals.setTextColor(Color.parseColor(cbEquals));
                }

                if (cKeypad != null && !cKeypad.equals("\0") && cKeypad.length() == 7) {
                    keypad.setBackgroundColor(Color.parseColor(cKeypad));
                }

                if (cMain != null && !cMain.equals("\0") && cMain.length() == 7) {
                    main.setBackgroundColor(Color.parseColor(cMain));
                    bgAnim.setBackgroundColor(Color.parseColor(cMain));
                }

                if (Aux.isTinyColor("cTop")) {
                    int cTop = Aux.getTinyColor("cTop");

                    for (l = 0; l < compBar.length; l++) {
                        if (compBar[l] != null)
                            compBar[l].setTextColor(cTop);

                        if (l < trigBar.length && trigBar[l] != null)
                            trigBar[l].setTextColor(cTop);
                    }

                    //Set landscape INV button
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        trigBar[6].setTextColor(bTextColorInt);

                        if (bMod != null) {
                            bMod.setTextColor(cTop);
                        }
                    }
                    else {
                        Button inv2 = findViewById(R.id.bInv2);

                        if (inv2 != null) {
                            if (Aux.isTinyColor("-bINV2t"))
                                inv2.setTextColor(Aux.getTinyColor("-bINV2t"));
                            else if (Aux.isTinyColor("-btt"))
                                inv2.setTextColor(Aux.getTinyColor("-btt"));
                        }
                    }
                }

                if (cNum != null && !cNum.equals("\0") && cNum.length() == 7) {
                    for (l = 0; l < 10; l++) {
                        nums[l].setTextColor(Color.parseColor(cNum));
                    }

                    bDec.setTextColor(Color.parseColor(cNum));
                }

                if (cFab != null && !cFab.equals("\0") && cFab.length() == 7) {
                    for (l = 0; l < 10; l++) {
                        bDel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(cFab)));
                    }
                }
            }

            Button inv2 = findViewById(R.id.bInv2);

            if (inv2 != null) {
                if (!isBig)
                    inv2.setVisibility(View.GONE);

                if (!isCustomTheme && (color.equals("14") || (color.equals("17") && (theme.equals("1") || theme.equals("3")))) && !theme.equals("4")) {
                    inv2.setTextColor(darkGray);
                }
            }

            if (isBig && orientation == Configuration.ORIENTATION_PORTRAIT)
                compBar[6].setVisibility(View.GONE);

            //Landscape button size adjustments depending on screen size
            if (orientation == Configuration.ORIENTATION_LANDSCAPE && !roundedButtons) {
                int mainWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
                int mainHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

                ViewGroup.LayoutParams[] scrollButtonParams = new ViewGroup.LayoutParams[16];
                ViewGroup.LayoutParams[] buttonParams = new ViewGroup.LayoutParams[16];

                for (i = 0; i < 16; i++) {
                    if (i < 10) {
                        buttonParams[i] = nums[i].getLayoutParams();
                    }
                    else if (i == 10) {
                        buttonParams[i] = bEquals.getLayoutParams();
                    }
                    else if (i == 11) {
                        buttonParams[i] = bDec.getLayoutParams();
                    }
                    else {
                        buttonParams[i] = mainOps[i - 12].getLayoutParams();
                    }
                }

                for (i = 0; i < 16; i++) {
                    if (i < 7 && compBar[i] != null) {
                        scrollButtonParams[i] = compBar[i].getLayoutParams();
                    }
                    else if (i - 7 < 6) {
                        scrollButtonParams[i] = trigBar[i - 7].getLayoutParams();
                    }
                    else if (i == 13) {
                        scrollButtonParams[i] = bParenthesisOpen.getLayoutParams();
                        i++;
                        scrollButtonParams[i] = bParenthesisClose.getLayoutParams();
                    }
                    else {
                        if (bMod != null) {
                            scrollButtonParams[i] = bMod.getLayoutParams();
                        }
                    }
                }

                double buttonZoneHeight = (mainHeight / 5.0) * 3;

                int zoneMod = (int) buttonZoneHeight % 4;

                if (zoneMod != 0) {
                    if (zoneMod < 3)
                        buttonZoneHeight -= zoneMod;
                    else
                        buttonZoneHeight += (4 - zoneMod);
                }

                double buttonHeight = buttonZoneHeight / 4.0;

                double scrollViewWidth = (mainWidth / 2.27609) + 1.5;
                double buttonViewWidth = (mainWidth - scrollViewWidth) + 3;

                double scrollButtonWidth = scrollViewWidth / 4;
                double buttonWidth = buttonViewWidth / 4;

                ViewGroup.LayoutParams[] buttonZoneParams = {scrollBar.getLayoutParams(), keypad.getLayoutParams()};
                ConstraintLayout[] buttonZones = {scrollBar, keypad};

                for (i = 0; i < 2; i++) {
                    buttonZoneParams[i].height = (int) buttonZoneHeight;
                    buttonZones[i].setLayoutParams(buttonZoneParams[i]);
                }

                for (i = 0; i < 16; i++) {
                    buttonParams[i].height = (int) buttonHeight;

                    if (i < 10) {
                        buttonParams[i].width = (int) buttonWidth;
                        nums[i].setLayoutParams(buttonParams[i]);
                    }
                    else if (i == 10) {
                        buttonParams[i].width = (int) buttonWidth;
                        bEquals.setLayoutParams(buttonParams[i]);
                    }
                    else if (i == 11) {
                        buttonParams[i].width = (int) buttonWidth;
                        bDec.setLayoutParams(buttonParams[i]);
                    }
                    else {
                        buttonParams[i].width = (int) buttonWidth;
                        mainOps[i - 12].setLayoutParams(buttonParams[i]);
                    }
                }

                for (i = 0; i < 16; i++) {
                    scrollButtonParams[i].width = (int) scrollButtonWidth;
                    scrollButtonParams[i].height = (int) buttonHeight;

                    if (i < 7) {
                        compBar[i].setLayoutParams(scrollButtonParams[i]);
                    }
                    else if (i - 7 < 6) {
                        trigBar[i - 7].setLayoutParams(scrollButtonParams[i]);
                    }
                    else if (i == 13) {
                        bParenthesisOpen.setLayoutParams(scrollButtonParams[i]);
                        i++;
                        bParenthesisClose.setLayoutParams(scrollButtonParams[i]);
                    }
                    else {
                        if (bMod != null) {
                            bMod.setLayoutParams(scrollButtonParams[i]);
                        }
                    }
                }
            }
            else if (orientation == Configuration.ORIENTATION_PORTRAIT && findViewById(R.id.bInv2) != null && findViewById(R.id.bInv2).getVisibility() == View.GONE) {
                int mainWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
                int mainHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

                ViewGroup.LayoutParams[] scrollButtonParams = new ViewGroup.LayoutParams[16];
                ViewGroup.LayoutParams scrollViewParams = scrollBar.getLayoutParams();
                ViewGroup.LayoutParams[] buttonParams = new ViewGroup.LayoutParams[16];
                ViewGroup.LayoutParams buttonViewParams = keypad.getLayoutParams();

                for (i = 0; i < 16; i++) {
                    if (i < 10) {
                        buttonParams[i] = nums[i].getLayoutParams();
                    }
                    else if (i == 10) {
                        buttonParams[i] = bEquals.getLayoutParams();
                    }
                    else if (i == 11) {
                        buttonParams[i] = bDec.getLayoutParams();
                    }
                    else {
                        buttonParams[i] = mainOps[i - 12].getLayoutParams();
                    }
                }

                for (i = 0; i < 16; i++) {
                    if (i < 7) {
                        scrollButtonParams[i] = compBar[i].getLayoutParams();
                    }
                    else if (i - 7 < 6) {
                        scrollButtonParams[i] = trigBar[i - 7].getLayoutParams();
                    }
                    else if (i == 13) {
                        scrollButtonParams[i] = bParenthesisOpen.getLayoutParams();
                        i++;
                        scrollButtonParams[i] = bParenthesisClose.getLayoutParams();
                    }
                    else {
                        if (bMod != null) {
                            scrollButtonParams[i] = bMod.getLayoutParams();
                        }
                    }
                }

                double buttonViewWidth = mainWidth + 2;
                double scrollViewWidth = mainWidth - (buttonViewWidth / 4.0) + 4;
                double buttonViewHeight = mainHeight / 2.215;

                Log.d("initHeight", "" + buttonViewHeight);

                buttonViewHeight = (int) buttonViewHeight - ((int) buttonViewHeight % 4) + 4;

                Log.d("finalHeight", "" + buttonViewHeight);

                double buttonWidth = buttonViewWidth / 4.0;
                double scrollButtonWidth = scrollViewWidth / 4.0;
                double buttonHeight = (buttonViewHeight / 4.0);

                if (isBig)
                    buttonWidth = 0;

                ViewGroup.LayoutParams bOpenParams = bParenthesisOpen.getLayoutParams();
                ViewGroup.LayoutParams bCloseParams = bParenthesisClose.getLayoutParams();

                ViewGroup.LayoutParams compScrollParams = findViewById(R.id.horizontalComplex).getLayoutParams();
                ViewGroup.LayoutParams trigScrollParams = !roundedButtons ? findViewById(R.id.horizontalTrig).getLayoutParams() : null;

                for (i = 0; i < 16; i++) {
                    buttonParams[i].width = (int) buttonWidth;

                    Log.d("buttonHeight", "" + buttonHeight);

                    if (!isBig) {
                        if (i == 13)
                            buttonParams[i].height = (int) buttonHeight;
                        else if (i >= 12)
                            buttonParams[i].height = (int) buttonHeight;
                        else if (i < 4 && i > 0)
                            buttonParams[i].height = (int) buttonHeight;
                        else
                            buttonParams[i].height = (int) buttonHeight;
                    }

                    if (i < 10)
                        nums[i].setLayoutParams(buttonParams[i]);
                    else if (i == 10)
                        bEquals.setLayoutParams(buttonParams[i]);
                    else if (i == 11)
                        bDec.setLayoutParams(buttonParams[i]);
                    else
                        mainOps[i - 12].setLayoutParams(buttonParams[i]);
                }

                for (i = 0; i < 16; i++) {
                    if (bMod != null && i == 15) {
                        scrollButtonParams[i].width = (int) scrollButtonWidth;
                    }
                    else if (i != 15) {
                        scrollButtonParams[i].width = (int) scrollButtonWidth;
                    }

                    if (i < 7) {
                        compBar[i].setLayoutParams(scrollButtonParams[i]);
                    }
                    else if (i - 7 < 6) {
                        trigBar[i - 7].setLayoutParams(scrollButtonParams[i]);
                    }
                    else if (i == 13) {
                        bOpenParams.width = (int) buttonWidth / 2;
                        bCloseParams.width = (int) buttonWidth / 2;
                        bParenthesisOpen.setLayoutParams(bOpenParams);
                        i++;
                        bParenthesisClose.setLayoutParams(bCloseParams);
                    }
                    else {
                        if (bMod != null) {
                            bMod.setLayoutParams(scrollButtonParams[i]);
                        }
                    }
                }

                if (!isBig)
                    buttonViewParams.height = (int) buttonViewHeight;

                scrollViewParams.width = (int) scrollViewWidth;
                compScrollParams.width = (int) scrollViewWidth;
                scrollBar.setLayoutParams(scrollViewParams);
                keypad.setLayoutParams(buttonViewParams);
                findViewById(R.id.horizontalComplex).setLayoutParams(compScrollParams);

                if (!roundedButtons) {
                    trigScrollParams.width = (int) scrollViewWidth;
                    findViewById(R.id.horizontalTrig).setLayoutParams(trigScrollParams);
                }
            }

            for (i = 0; i < allButtons.length; i++) {
                for (j = 0; j < allButtons[i].length; j++) {
                    try {
                        allButtons[i][j].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onButtonPressed(v);
                            }
                        });
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //Handle custom constants & custom functions fabs
            customFabs = new FloatingActionButton[]{findViewById(R.id.expandCustoms), findViewById(R.id.customConstants), findViewById(R.id.customFunctions)};
            customLabels = new TextView[]{null, findViewById(R.id.constantsLabel), findViewById(R.id.functionsLabel)};

            int fabTextColor;
            String fabTextColorStr;

            if (isCustomTheme) {
                if (Aux.isColor(tinydb.getString("cFabText")))
                    fabTextColorStr = tinydb.getString("cFabText");
                else
                    fabTextColorStr = "#FFFFFF";
            }
            else {
                if (theme.equals("5"))
                    fabTextColorStr = secondary;
                else if ((color.equals("14") || (color.equals("17") && !theme.equals("2"))) && !theme.equals("4"))
                    fabTextColorStr = "#222222";
                else
                    fabTextColorStr = "#FFFFFF";
            }

            fabTextColor = Color.parseColor(fabTextColorStr);

            final ColorStateList fabCsl;
            String fabColor;

            if (isCustomTheme) {
                if (Aux.isColor(tinydb.getString("cFab")))
                    fabColor = tinydb.getString("cFab");
                else if (!theme.equals("4") && Aux.isColor(tinydb.getString("cPrimary")))
                    fabColor = tinydb.getString("cPrimary");
                else if (theme.equals("4"))
                    fabColor = "#222222";
                else
                    fabColor = "#03DAC5";
            }
            else {
                if (theme.equals("4"))
                    fabColor = "#222222";
                else if (theme.equals("5"))
                    fabColor = "#303030";
                else
                    fabColor = primary;
            }

            fabCsl = ColorStateList.valueOf(Color.parseColor(fabColor));

            final FloatingActionButton addCustom = findViewById(R.id.addCustom);

            try {
                for (i = 0; i < customFabs.length; i++) {
                    if (customFabs[i] != null) {
                        customFabs[i].setColorFilter(fabTextColor);
                        customFabs[i].setBackgroundTintList(fabCsl);
                    }
                }

                addCustom.setColorFilter(fabTextColor);
                addCustom.setBackgroundTintList(fabCsl);

                try {
                    FloatingActionButton decFrac = findViewById(R.id.decFracButton);

                    decFrac.setColorFilter(fabTextColor);
                    decFrac.setBackgroundTintList(fabCsl);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                if (roundedButtons) {
                    String fTheme = theme;
                    String fColor = color;

                    final ImageButton expandCustomsNew = findViewById(R.id.expandCustomsNew);

                    expandCustomsNew.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View constants = findViewById(R.id.customConstantsNew);
                            View functions = findViewById(R.id.customFunctionsNew);

                            spin(expandCustomsNew, fTheme, fColor, constants.getVisibility() == View.VISIBLE ? R.drawable.ic_baseline_add_24 : R.drawable.ic_close_24);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    constants.setVisibility(Math.abs(constants.getVisibility() - 8));
                                }
                            }, constants.getVisibility() == View.VISIBLE ? 95 : 0);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    functions.setVisibility(Math.abs(functions.getVisibility() - 8));
                                }
                            }, functions.getVisibility() == View.VISIBLE ? 0 : 95);
                        }
                    });
                }
                else if (orientation != Configuration.ORIENTATION_LANDSCAPE)
                    customFabs[0].setOnClickListener(expandFabs);

                if (!roundedButtons) {
                    if (Aux.getThemeInt() != 2) {
                        for (i = 1; i < customLabels.length; i++) {
                            if (!isCustomTheme && Aux.getThemeInt() == 5)
                                customLabels[i].setBackgroundTintList(ColorStateList.valueOf(monochromeTextColor));
                            else if (isCustomTheme && Aux.isGray(fabColor) && Aux.isDigit(Aux.chat(fabColor, 1)) && Integer.parseInt(Aux.chat(fabColor, 1)) < 9)
                                customLabels[i].setBackgroundTintList(fabCsl);

                            if (isCustomTheme && Aux.isGray(fabTextColorStr) && Aux.isLetter(Aux.chat(fabTextColorStr, 1)))
                                customLabels[i].setTextColor(fabTextColor);
                            else
                                customLabels[i].setTextColor(Color.WHITE);
                        }
                    }
                    else {
                        for (i = 1; i < customLabels.length; i++) {
                            if (isCustomTheme && Aux.isGray(fabTextColorStr) && Aux.isDigit(Aux.chat(fabTextColorStr, 1)) && Integer.parseInt(Aux.chat(fabTextColorStr, 1)) < 9)
                                customLabels[i].setTextColor(fabTextColor);
                            else
                                customLabels[i].setTextColor(darkGray);

                            if (isCustomTheme && Aux.isGray(fabColor) && Aux.isLetter(Aux.chat(fabColor, 1)))
                                customLabels[i].setBackgroundTintList(fabCsl);
                            else {
                                try {
                                    int f;
                                    int labelColor;

                                    if (isCustomTheme)
                                        labelColor = Aux.safeParseColor(tinydb.getString("cMain"), null);
                                    else
                                        labelColor = Color.WHITE;

                                    for (f = 1; f < customLabels.length; f++) {
                                        customLabels[f].setBackgroundTintList(ColorStateList.valueOf(labelColor));
                                    }
                                }
                                catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                View functionsButton = roundedButtons ? findViewById(R.id.customFunctionsNew) : customFabs[2];
                View constantsButton = roundedButtons ? findViewById(R.id.customConstantsNew) : customFabs[1];

                //Functions
                functionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (roundedButtons)
                                findViewById(R.id.expandCustomsNew).performClick();
                            else
                                customFabs[0].performClick();

                            openFunctionsSheet();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                //Constants
                constantsButton.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {
                        try {
                            if (roundedButtons)
                                findViewById(R.id.expandCustomsNew).performClick();
                            else
                                customFabs[0].performClick();

                            openConstantsSheet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }

            Aux.adapter.setOnItemClickListener(functionItemClickListener);



            if (Build.VERSION.SDK_INT >= 23) {
                HorizontalScrollView scrollView = findViewById(R.id.equationScrollView);

                if (scrollView != null) {
                    scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                            if (!roundedButtons) {
                                if (bDel != null && !v.canScrollHorizontally(-1)) {
                                    if (orientation == Configuration.ORIENTATION_PORTRAIT)
                                        bDel.show();
                                    else
                                        bDel.setVisibility(View.VISIBLE);

                                    if (customFabs[0] != null)
                                        customFabs[0].setVisibility(View.VISIBLE);
                                }
                                else if (bDel != null) {
                                    if (orientation == Configuration.ORIENTATION_PORTRAIT)
                                        bDel.hide();
                                    else
                                        bDel.setVisibility(View.INVISIBLE);

                                    if (customFabs[0] != null) {
                                        if (customFabs[1] != null && customFabs[1].getVisibility() == View.VISIBLE)
                                            customFabs[0].performClick();

                                        customFabs[0].setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                        }
                    });
                }
            }

            for (i = 1; i <= 9; i++) {
                try {
                    final int fi = i;
                    nums[i].setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            String constant = tinydb.getString("shortcut" + fi);

                            if (constant != null && !constant.equals("") && (Aux.isDigit(Aux.chat(constant, 0)) || constant.startsWith(Aux.sq) ||
                                    constant.startsWith("(")) || constant.startsWith("a") || constant.startsWith("l") || constant.startsWith("s") ||
                                    constant.startsWith("c") || constant.startsWith("t")) {
                                int j;
                                boolean isExponent = false;

                                for (j = 0; j < constant.length(); j++) {
                                    if (Aux.isSuperscript(Aux.chat(constant, j))) {
                                        if (!isExponent) {
                                            constant = Aux.newReplace(j, constant, "^" + Aux.chat(constant, j));

                                            isExponent = true;
                                        }
                                        else {
                                            constant = Aux.newReplace(j, constant, Aux.fromSuper(Aux.chat(constant, j)));
                                        }
                                    }
                                    else {
                                        isExponent = false;
                                    }
                                }

                                if (!Aux.isFullNum(constant) && !Aux.isNum(constant))
                                    constant = "(" + constant + ")";

                                if (constant != null && !constant.equals("") && (Aux.isDigit(Aux.chat(constant, 0)) || constant.startsWith(Aux.sq) ||
                                        constant.startsWith("(")) || constant.startsWith("a") || constant.startsWith("l") || constant.startsWith("s") ||
                                        constant.startsWith("c") || constant.startsWith("t"))
                                    enterStr(Aux.parseEq(constant));
                            }

                            return true;
                        }
                    });
                } catch (Exception shortcutError) {
                    shortcutError.printStackTrace();
                }
            }

            if (tinydb.getBoolean("termShortcut")) {
                nums[0].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        startActivity(new Intent(MainActivity.mainActivity, TerminalActivity.class));

                        return false;
                    }
                });
            }

            trigBar[0].setOnLongClickListener(new View.OnLongClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onLongClick(View v) {
                    Button sin = (Button) v;

                    vibe(vibeDuration);

                    String pressed = sin.getText().toString();

                    sin.setText(pressed + "h");
                    operation(sin);
                    sin.setText(pressed);

                    return true;
                }
            });

            trigBar[1].setOnLongClickListener(new View.OnLongClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onLongClick(View v) {
                    Button cos = (Button) v;

                    vibe(vibeDuration);

                    String pressed = cos.getText().toString();

                    cos.setText(pressed + "h");
                    operation(cos);
                    cos.setText(pressed);

                    return true;
                }
            });

            trigBar[2].setOnLongClickListener(new View.OnLongClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onLongClick(View v) {
                    Button tan = (Button) v;

                    vibe(vibeDuration);

                    String pressed = tan.getText().toString();

                    tan.setText(pressed + "h");
                    operation(tan);
                    tan.setText(pressed);

                    return true;
                }
            });

            trigBar[3].setOnLongClickListener(new View.OnLongClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onLongClick(View v) {
                    Button csc = (Button) v;

                    vibe(vibeDuration);

                    String pressed = csc.getText().toString();

                    csc.setText(pressed + "h");
                    operation(csc);
                    csc.setText(pressed);

                    return true;
                }
            });

            trigBar[4].setOnLongClickListener(new View.OnLongClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onLongClick(View v) {
                    Button sec = (Button) v;

                    vibe(vibeDuration);

                    String pressed = sec.getText().toString();

                    sec.setText(pressed + "h");
                    operation(sec);
                    sec.setText(pressed);

                    return true;
                }
            });

            trigBar[5].setOnLongClickListener(new View.OnLongClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onLongClick(View v) {
                    Button cot = (Button) v;

                    vibe(vibeDuration);

                    String pressed = cot.getText().toString();

                    cot.setText(pressed + "h");
                    operation(cot);
                    cot.setText(pressed);

                    return true;
                }
            });

            //N-th Root
            compBar[0].setOnLongClickListener(new View.OnLongClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onLongClick(View v) {
                    int i;
                    int cursor = tv.getSelectionStart();
                    String eq3, converted = "";
                    String tvText = tv.getText().toString();

                    if (equaled)
                        getEqualed();

                    eq3 = tv.getSelectionStart() == getTvText().length() ? tvText : tvText.substring(0, cursor);

                    for (i = eq3.length(); i > 0; i--) {
                        String current = Aux.chat(eq3, i);

                        if (Aux.isDigit(current) || current.equals(".") || current.equals("-") || current.equals(Aux.emDash) || current.equals("e") || current.equals("(") || current.equals(")"))
                            converted = Aux.numToSuper(current) + converted;
                    }

                    tv.setText(tvText.replace(eq3, Aux.newTrim(eq3, converted.length()) + converted));

                    if (!dontVibe)
                        vibe(vibeDuration);

                    dontVibe = false;

                    return true;
                }
            });

            String modSymbol = tinydb.getString("modSymbol");

            if (!modSymbol.equals("mod") && !modSymbol.equals("%") && (modSymbol == null || modSymbol.equals("\0") || modSymbol.equals("") || modSymbol.equals(" "))) {
                tinydb.putString("modSymbol", "mod");
                modSymbol = "mod";
            }

            if (modSymbol != null && !modSymbol.equals("\0"))
                bMod.setText(tinydb.getString("modSymbol"));

            if (isCustomTheme) {
                if (Aux.isTinyColor("-b%"))
                    bMod.setBackgroundColor(Aux.getTinyColor("-b%"));
                if (Aux.isTinyColor("-b%t"))
                    bMod.setTextColor(Aux.getTinyColor("-b%t"));
            }

            //Mod
            compBar[7].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button button = (Button) v;

                    button.setText("%");
                    operation(button);
                    button.setText(tinydb.getString("modSymbol"));
                }
            });

            //Log
            compBar[5].setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    Button pressed = (Button) v;

                    try {
                        Aux.removeCommas(tv.getText().toString());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    operation(pressed);
                }
            });

            //Ln
            compBar[6].setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    Button pressed = (Button) v;

                    try {
                        Aux.removeCommas(tv.getText().toString());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    operation(pressed);
                    tv.append("(");
                }
            });

            //Log Base x
            compBar[5].setOnLongClickListener(new View.OnLongClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onLongClick(View v) {
                    Button pressed = (Button) v;
                    String eq3 = getTvText();

                    if (equaled)
                        getEqualed();

                    if (!Aux.isNull(eq3) && Aux.isNum(Aux.lastChar(eq3))) {
                        vibe(vibeDuration);

                        int n;
                        int numChars = Aux.countNums(eq3);
                        String number = Aux.getLast(eq3, numChars);

                        eq3 = Aux.newTrim(eq3, numChars);

                        pressed.setText("log");
                        operation(pressed);

                        eq3 = Aux.newTrim(eq3, 1);

                        for (n = 0; n < numChars; n++) {
                            if (Aux.isDigit(Aux.chat(number, n)))
                                eq3 += subscripts[Integer.parseInt(Aux.chat(number, n))];
                            else if (Aux.chat(number, n).equals("e"))
                                eq3 += "ₑ";
                            else if (Aux.chat(number, n).equals("π")) {
                                if (number.length() == 1)
                                    eq3 += subscripts[3] + "  ̣" + subscripts[1] + subscripts[4] + subscripts[1] + subscripts[5] + subscripts[9] + subscripts[2] + subscripts[6];
                                else
                                    eq3 += "₍" + subscripts[3] + "  ̣" + subscripts[1] + subscripts[4] + subscripts[1] + subscripts[5] + subscripts[9] + subscripts[2] + subscripts[6] + "₎";
                            }
                            else if (Aux.chat(number, n).equals("."))
                                eq3 += "  ̣";
                        }

                        eq3 += "(";

                        tv.setText(eq3);
                    }

                    return true;
                }
            });

            tv.addTextChangedListener(new TextValidator(tv) {
                @Override
                public void validate(TextView textView, String before, String after) {
                    wrapText(tv);

                    if (equaled && previousExpression != null) {
                        try {
                            previousExpression.setText("");
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    String tvText = Aux.updateCommas(getTvText().replace("\n", ""));

                    if (tvText.length() < 1) {
                        tv.setText(" ");
                        return;
                    }
                    else if (!getTvText().equals(" ")) {
                        tv.setText(tvText.replace(" ", "").trim());
                    }

                    if (!equaled && tinydb.getBoolean("showPreviousExpression") && previousExpression != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int precision = tinydb.getInt("precision");
                                int scale = tinydb.getBoolean("isDynamic") ? (roundedButtons ? roundedPrecision : squarePrecision) : precision;
                                final MathContext newMc = new MathContext(tinydb.getBoolean("isDynamic") ? maxPrecision : (Math.min(precision, (maxPrecision / 2)) * 2), RoundingMode.HALF_UP);

                                final String tvText = getTvText().trim();
                                final String eq = Aux.isBinaryOp(Aux.lastChar(tvText)) ? Aux.newTrim(tvText, 1) : tvText;

                                BigDecimal result;

                                try {
                                    previousExpression.setText("");
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {
                                    result = BetterMath.evaluate(eq, tinydb.getBoolean("prioritizeCoefficients"), isRad, newMc, scale, false);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }

                                String resultStr = BetterMath.formatResult(result, newMc, scale).trim();

                                while (resultStr.equals("0") && scale < maxPrecision)
                                    resultStr = BetterMath.formatResult(result, newMc, scale++).trim();

                                while ((resultStr.endsWith("0") && resultStr.contains(".")) || resultStr.endsWith(".") || resultStr.endsWith("0E"))
                                    resultStr = Aux.newTrim(resultStr, 1);

                                if (!equaled && getTvText().replace(",", "").trim().equals(tvText.replace(",", "").trim()) && !resultStr.equals(eq) && Aux.isFullSignedNumE(resultStr) && (!Aux.isFullNum(tvText) || tvText.equals("e") || tvText.equals(Aux.pi))) {
                                    try {
                                        previousExpression.setText(resultStr);
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, "BetterMathThread").start();

                        if (equaled && previousExpression != null) {
                            try {
                                previousExpression.setText("");
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

            if (roundedButtons)
                backspace.setOnLongClickListener(bDelLongClick);
            else
                bDel.setOnLongClickListener(bDelLongClick);

            if (roundedButtons) {
                try {
                    findViewById(R.id.decFracButtonNew).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            equalsDec(v);
                            findViewById(R.id.decFracButtonNew).setVisibility(View.GONE);
                        }
                    });
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    findViewById(R.id.decFracButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            equalsDec(v);
                            findViewById(R.id.decFracButton).setVisibility(View.INVISIBLE);
                        }
                    });
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (frame.getVisibility() == View.VISIBLE) {
                findViewById(R.id.bDegRad).setVisibility(View.INVISIBLE);
            }
            else {
                findViewById(R.id.bDegRad).setVisibility(View.VISIBLE);
            }

            if (theme.equals("2")) {
                toolbar.setNavigationIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu_dark_24, null));
            }

            String cSecondary = tinydb.getString("cSecondary");

            if (theme.equals("4") && isCustomTheme && orientation == Configuration.ORIENTATION_LANDSCAPE && (cSecondary != null && !cSecondary.equals("\0") && cSecondary.length() == 7 && cSecondary.startsWith("#"))) {
                bParenthesisOpen.setTextColor(Color.parseColor(cSecondary));
                bParenthesisClose.setTextColor(Color.parseColor(cSecondary));
            }

            // If cKeypad and cPrimary are the same, hide the shadow between them
            String cKeypad = tinydb.getString("cKeypad");
            String cPrimary = tinydb.getString("cPrimary");

            if (isCustomTheme) {
                if (Aux.isColor(cPrimary) && !cPrimary.equals("#reset0") && Aux.isColor(cKeypad) && !cKeypad.equals("#reset0") && Color.parseColor(cPrimary) == Color.parseColor(cKeypad)) {
                    final Button[] mainOps = {findViewById(R.id.sPlus), findViewById(R.id.sMinus), findViewById(R.id.sMulti), findViewById(R.id.sDiv)};

                    for (i = 0; i < mainOps.length; i++) {
                        mainOps[i].setElevation(0);
                        mainOps[i].setStateListAnimator(null);
                        mainOps[i].setBackground(null);
                    }

                    findViewById(R.id.buttonView).setElevation(0);

                    String eqStr = tinydb.getString("cbEquals");
                    String cNum = tinydb.getString("cNum");

                    if (Aux.isNull(eqStr) || eqStr.equals("#reset0")) {
                        if (!Aux.isNull(cNum) && Aux.isColor(cNum)) {
                            tinydb.putString("cbEquals", tinydb.getString("cNum"));
                            bEquals.setTextColor(Color.parseColor(cNum));
                        }
                        else {
                            if (theme.equals("2"))
                                bEquals.setTextColor(darkGray);
                            else
                                bEquals.setTextColor(Color.WHITE);
                        }
                    }
                }

                for (i = 0; i < nums.length; i++) {
                    if (nums[i] != null) {
                        nums[i].setElevation(0);
                        nums[i].setStateListAnimator(null);
                    }
                }

                bDec.setElevation(0);
                bDec.setStateListAnimator(null);
                bEquals.setElevation(0);
                bEquals.setStateListAnimator(null);
            }

            if (!roundedButtons) {
                if (isCustomTheme && Aux.isTinyColor("cFabText"))
                    bDel.setColorFilter(Aux.getTinyColor("cFabText"));
                else if (theme.equals("5"))
                    bDel.setColorFilter(secondaryColor);
                else if (theme != null && color != null && ((color.equals("14") && !theme.equals("4")) || (color.equals("17") && (theme.equals("3") || theme.equals("1")))))
                    bDel.setColorFilter(darkGray);
                else
                    bDel.setColorFilter(Color.WHITE);
            }

            didIntro = tinydb.getBoolean("didIntro");

            if (!didIntro) {
                didIntro = true;
                tinydb.putBoolean("didIntro", true);
                this.startActivity(new Intent(this, Intro.class));
            }

            String fragTag = tinydb.getString("fragTag");
            String cMain = tinydb.getString("cMain");

            if (fragTag != null && !fragTag.equals("\0")) {
                if (fragTag.equals("Splash")) {
                    fragTag = "Home";
                    navigationView.getMenu().getItem(0).setChecked(true);
                    bDegRad.setVisibility(View.VISIBLE);
                    frame.setVisibility(View.GONE);
                }
                else if (fragTag.equals("Conversions")) {
                    onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_conversions));
                    navigationView.getMenu().getItem(1).setChecked(true);
                }
                else if (fragTag.equals("Home")) {
                    onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_home));
                    navigationView.getMenu().getItem(0).setChecked(true);
                    bDegRad.setVisibility(View.VISIBLE);
                    frame.setVisibility(View.GONE);
                }
                else if (fragTag.equals("Date")) {
                    onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_date));
                    navigationView.getMenu().getItem(2).setChecked(true);
                }
                else if (fragTag.equals("Geometry")) {
                    if (theme.equals("2"))
                        drawer.setBackgroundColor(Color.WHITE);
                    else if (theme.equals("3") || theme.equals("4"))
                        drawer.setBackgroundColor(Color.BLACK);

                    onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_geo));
                    navigationView.getMenu().getItem(4).setChecked(true);
                }
                else {
                    onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_home));
                    navigationView.getMenu().getItem(0).setChecked(true);
                    bDegRad.setVisibility(View.VISIBLE);
                    frame.setVisibility(View.GONE);
                }

                if (isCustomTheme && fragTag.equals("Geometry")) {
                    if (!Aux.isNull(cMain) && Aux.isColor(cMain))
                        drawer.setBackgroundColor(Color.parseColor(cMain));
                    else if (Aux.isColor(bgColor))
                        drawer.setBackgroundColor(Color.parseColor(bgColor));
                    else
                        drawer.setBackgroundColor(Color.BLACK);
                }
            }
            else
                navigationView.getMenu().getItem(0).setChecked(true);

            if (isCustomTheme) {
                for (i = 0; i < 10; i++) {
                    if (Aux.isColor(tinydb.getString("-b" + i)))
                        nums[i].setTextColor(Aux.getTinyColor("-b" + i));
                }

                if (Aux.isColor(tinydb.getString("-bDec")))
                    bDec.setTextColor(Aux.getTinyColor("-bDec"));
            }

            //Individual Button Colors
            if (isCustomTheme) {
                int a, b;

                //Button Text Color
                String cbTextColor = bTextColor;

                //Main Ops Text Color
                String moTextColor;

                if (theme.equals("2") || Aux.isNull(cbTextColor))
                    cbTextColor = "#FFFFFF";
                else if (theme.equals("4"))
                    cbTextColor = secondary;
                else if (theme.equals("5"))
                    cbTextColor = "#303030";

                if (theme.equals("4"))
                    moTextColor = primary;
                else
                    moTextColor = cbTextColor;

                for (a = 0; a < allButtons.length; a++) {
                    for (b = 0; b < allButtons[a].length; b++) {
                        if (allButtons[a][b] != null) {
                            String buttonText = allButtons[a][b].getText().toString();

                            try {
                                if (inv2 == null && (buttonText.equals("(") || buttonText.equals(")")) && Aux.isTinyColor("cTop") && !Aux.tinyEquals("cSecondary", "cTertiary"))
                                    continue;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (buttonText.equals("%"))
                                buttonText = "ⁿ√";

                            //Background Colors
                            if (Aux.isTinyColor("-b" + buttonText)) {
                                if (!(orientation == Configuration.ORIENTATION_LANDSCAPE && a == 2 && b == allButtons[a].length - 1))
                                    allButtons[a][b].setBackgroundColor(Aux.getTinyColor("-b" + buttonText));

                                if (a == 2 && b == allButtons[a].length - 1 && inv2 != null && !Aux.isTinyColor("-bINV2"))
                                    inv2.setBackgroundColor(Aux.getTinyColor("-b" + buttonText));
                            }

                            //Text Colors
                            if (Aux.isTinyColor("-b" + buttonText + "t")) {
                                allButtons[a][b].setTextColor(Aux.getTinyColor("-b" + buttonText + "t"));

                                if (a == 2 && b == allButtons[a].length - 1 && inv2 != null && !Aux.isTinyColor("-bINV2t"))
                                    inv2.setTextColor(Aux.getTinyColor("-b" + buttonText + "t"));
                            }
                            else if (Aux.isTinyColor("cNum") && (a == 0 || (a == 4 && (b == 0 || b == 3)))) {
                                allButtons[a][b].setTextColor(Aux.getTinyColor("cNum"));
                            }
                            //Equals Button Default
                            else if (a == 4 && b == 3 && !theme.equals("5")) {
                                allButtons[a][b].setTextColor(primaryColor);
                            }
                            //Other Defaults
                            else {
                                //Top Bar (Left)
                                if ((a == 1 || a == 2)) {
                                    if (!Aux.isTinyColor("cTop") && Aux.isColor(cbTextColor))
                                        allButtons[a][b].setTextColor(Color.parseColor(cbTextColor));
                                }
                                //Top Bar (Right)
                                else if ((a == 4 && (b == 1 || b == 2 || b == 4))) {
                                    if (!Aux.isTinyColor("-btt") && Aux.isColor(cbTextColor))
                                        allButtons[a][b].setTextColor(Color.parseColor(cbTextColor));
                                }
                                //Main Ops
                                else if (a == 3 && Aux.isColor(moTextColor)) {
                                    allButtons[a][b].setTextColor(Color.parseColor(moTextColor));
                                }
                                else if (Aux.isColor(bTextColor))
                                    allButtons[a][b].setTextColor(bTextColorInt);
                            }
                        }
                    }
                }

                for (i = 0; i < nums.length; i++) {
                    nums[i].setElevation(0);
                    nums[i].setStateListAnimator(null);
                }

                if (Aux.isTinyColor("-mt")) {
                    setMTColor(Aux.getTinyColor("-mt"));

                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Button inv = findViewById(R.id.bInv);

                        inv.setTextColor(Aux.getTinyColor("-mt"));
                    }
                }

                try {
                    if (Aux.isTinyColor("cSecondary")) {
                        int secondary = Aux.getTinyColor("cSecondary");
                        int secondaryDarker;

                        if (Aux.isTinyColor("cMain") && secondary == Aux.getTinyColor("cMain"))
                            secondaryDarker = secondary;
                        else
                            secondaryDarker = Color.parseColor(Aux.hexAdd(tinydb.getString("cSecondary"), -6));

                        int ebgColor = isDarkTab ? secondaryDarker : secondary;

                        Drawable wrappedBG = DrawableCompat.wrap(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_expandbg, null));
                        DrawableCompat.setTint(wrappedBG, ebgColor);

                        expandBG.setBackground(wrappedBG);
                        findViewById(R.id.expandBG2).setBackgroundColor(ebgColor);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (Aux.isTinyColor("-bop")) {
                    for (i=0; i < mainOps.length; i++) {
                        String code = "-b" + mainOps[i].getText().toString() + "t";
                        mainOps[i].setTextColor(Aux.isTinyColor(code) ? Aux.getTinyColor(code) : Aux.getTinyColor("-bop"));
                    }
                }

                if (Aux.isTinyColor("-btt")) {
                    final Button[] tertiaryBtns = {findViewById(R.id.bInv2), findViewById(R.id.bParenthesisOpen), findViewById(R.id.bParenthesisClose)};

                    for (i=0; i < tertiaryBtns.length; i++) {
                        if (tertiaryBtns[i] != null) {
                            String code = ("-b" + tertiaryBtns[i].getText().toString() + "t").replace("-bINVt", "-bINV2t");

                            tertiaryBtns[i].setTextColor(Aux.isTinyColor(code) ? Aux.getTinyColor(code) : Aux.getTinyColor("-btt"));
                        }
                    }
                }

                try {
                    if (Aux.isTinyColor("-bINV2") && isCustomTheme)
                        inv2.setBackgroundColor(Aux.getTinyColor("-bINV2"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //Set tertiary buttons to 0 elevation when in portrait mode on tablets
            if (isBig && orientation == Configuration.ORIENTATION_PORTRAIT) {
                Button[] buttons = {compBar[3], findViewById(R.id.bInv), inv2, bParenthesisOpen, bParenthesisClose};

                for (i=0; i < buttons.length; i++) {
                    if (buttons[i] != null) {
                        try {
                            buttons[i].setElevation(0);
                            buttons[i].setStateListAnimator(null);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (isCustomTheme) {
                if (Aux.isColor(tinydb.getString("cDiv")) && !Aux.isColor(tinydb.getString("-b÷t"))) {
                    tinydb.putString("-b÷t", tinydb.getString("cDiv"));
                    mainOps[3].setTextColor(Color.parseColor(tinydb.getString("-b÷t")));
                }
                if (Aux.isColor(tinydb.getString("cMulti")) && !Aux.isColor(tinydb.getString("-b×t"))) {
                    tinydb.putString("-b×t", tinydb.getString("cMulti"));
                    mainOps[2].setTextColor(Color.parseColor(tinydb.getString("-b×t")));
                }
                if (Aux.isColor(tinydb.getString("cMinus")) && !Aux.isColor(tinydb.getString("-b-t"))) {
                    tinydb.putString("-b-t", tinydb.getString("cMinus"));
                    mainOps[1].setTextColor(Color.parseColor(tinydb.getString("-b-t")));
                }
                if (Aux.isColor(tinydb.getString("cPlus")) && !Aux.isColor(tinydb.getString("-b+t"))) {
                    tinydb.putString("-b+t", tinydb.getString("cPlus"));
                    mainOps[0].setTextColor(Color.parseColor(tinydb.getString("-b+t")));
                }

                if (theme.equals("1") && Aux.isColor(tinydb.getString("cMain")))
                    main.setBackgroundColor(Color.parseColor(Aux.hexAdd(tinydb.getString("cMain"), -12)));
            }

            tinydb.putString("accentPrimary", primary);
            tinydb.putString("accentSecondary", secondary);
            tinydb.putString("accentTertiary", tertiary);

            if (tinydb.getString("fetchRate").equals("1")) {
                final HandlerThread thread = new HandlerThread("SendRatesThread");
                thread.start();

                new Handler(thread.getLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        while (!Aux.checkRates()) {
                            CurrencyConverter.calculate(1.5, "USD", "EUR", new CurrencyConverter.Callback() {
                                @Override
                                public void onValueCalculated(Double value, Exception e) {

                                }
                            });

                            CurrencyConverter.sendRatesToAux(Aux.currencyCodes);
                        }

                        thread.quitSafely();
                    }
                }, 3);
            }

            tv.setBackgroundResource(android.R.color.transparent);

            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!tv.hasFocus())
                        tv.requestFocus();

                    snapCursor();
                }
            });

            tv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus)
                        snapCursor();
                }
            });

            tv.setShowSoftInputOnFocus(false);

            if (customFabs[0] != null && tinydb.getString("whereCustom").equals("2"))
                customFabs[0].setVisibility(View.GONE);

            //Set custom labels to -ft if -ft is equal to -mt and if -m is black
            if (isCustomTheme && Aux.isTinyColor("-mt") && Aux.isTinyColor("cFabText") && Aux.getTinyColor("-mt") == Aux.getTinyColor("cFabText") && Aux.isTinyColor("cMain") && tinydb.getString("cMain").equals("#000000")) {
                for (TextView label : customLabels) {
                    if (label != null)
                        label.setTextColor(Aux.getTinyColor("cFabText"));
                }
            }

            for (i = 0; i < nums.length; i++) {
                if (nums[i] != null) {
                    nums[i].setElevation(0);
                    nums[i].setStateListAnimator(null);
                }
            }

            bDec.setElevation(0);
            bDec.setStateListAnimator(null);
            bEquals.setElevation(0);
            bEquals.setStateListAnimator(null);

            if (isFocus) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            if (isBig)
                clear(bDel);

            if (isCustomTheme && tinydb.getBoolean("isGradMain")) {
                try {
                    final GradientDrawable.Orientation horizontal = GradientDrawable.Orientation.LEFT_RIGHT;
                    final GradientDrawable.Orientation vertical = GradientDrawable.Orientation.TOP_BOTTOM;

                    String direction = tinydb.getString("gradDirectMain");
                    int[] gradColors = {Aux.getTinyColor("gradStartMain"), Aux.getTinyColor("gradEndMain")};
                    GradientDrawable.Orientation gradDirection;

                    if (direction.equalsIgnoreCase("horizontal"))
                        gradDirection = horizontal;
                    else if (direction.equalsIgnoreCase("vertical"))
                        gradDirection = vertical;
                    else
                        throw new Exception("Error: No gradient direction provided.");

                    GradientDrawable gradient = new GradientDrawable(gradDirection, gradColors);
                    gradient.setCornerRadius(0f);

                    bgAnim.setBackground(gradient);

                    if (gradDirection == vertical) {
                        main.setBackgroundColor(gradColors[0]);
                    }
                    else {
                        main.setBackground(gradient);
                    }
                } catch (Exception gradError) {
                    gradError.printStackTrace();
                }
            }

            try {
                findViewById(R.id.bInv).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inv(v);
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (isCustomTheme) {
                try {
                    final FloatingActionButton[] fabs = {bDel, findViewById(R.id.expandCustoms), customFabs[1], customFabs[2]};

                    if (!Aux.isTinyColor("cFabText") && Aux.isTinyColor("-bop"))
                        fabs[0].setColorFilter(Aux.getTinyColor("-bop"));

                    for (i=1; i < fabs.length; i++) {
                        if (Aux.isTinyColor("-bfc"))
                            fabs[i].setBackgroundTintList(ColorStateList.valueOf(Aux.getTinyColor("-bfc")));

                        if (Aux.isTinyColor("-bfct"))
                            fabs[i].setColorFilter(Aux.getTinyColor("-bfct"));
                        else if (Aux.isTinyColor("cFabText"))
                            fabs[i].setColorFilter(Aux.getTinyColor("cFabText"));
                        else if (Aux.isTinyColor("-bop"))
                            fabs[i].setColorFilter(Aux.getTinyColor("-bop"));
                    }

                    try {
                        int previousColor = Aux.isTinyColor("-mt") ? Aux.getTinyColor("-mt") :
                                Aux.getThemeInt() == 2 ? Color.BLACK : Aux.getThemeInt() == 5 ? Color.parseColor("#303030") : Color.WHITE;

                        previousExpression.setTextColor(previousColor);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                if (orientation != Configuration.ORIENTATION_PORTRAIT) {
                    ViewGroup.LayoutParams bgAnimParams = bgAnim.getLayoutParams();

                    if (bgAnimParams.height <= screenHeight) {
                        bgAnimParams.height += 1.5 * (screenHeight - bgAnimParams.height);

                        bgAnim.setLayoutParams(bgAnimParams);
                    }
                    if (bgAnimParams.width <= screenWidth) {
                        bgAnimParams.width += 1.5 * (screenWidth - bgAnimParams.width);

                        bgAnim.setLayoutParams(bgAnimParams);
                    }
                }
            }
            catch (Exception bgAnimError) {
                bgAnimError.printStackTrace();
            }

            //Handle Rounded Button Theme Colors
            if (roundedButtons && !theme.equals("5")) {
                ImageButton swapTopBar = findViewById(R.id.swapTopBar);
                int textColor = theme.equals("2") ? darkGray : Color.parseColor(Aux.hexAdd(tertiary, 230));

                Button inv = findViewById(R.id.bInv);

                LinearLayout compLayout = findViewById(R.id.horizontalComplexLinearLayout);

                //SwapTopBar onClickListener
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    swapTopBar.setOnClickListener(v -> {
                        int a;

                        //Spin the swapTopBar button
                        ObjectAnimator.ofFloat(v, "rotation", 0f, inv.getVisibility() == View.VISIBLE ? -360f : 360f).setDuration(782).start();

                        inv.setVisibility(Math.abs(inv.getVisibility() - 8));

                        final int inVisibility = inv.getVisibility();
                        final boolean invIsVisible = inVisibility == View.VISIBLE;
                        final int childCount = compLayout.getChildCount();
                        int delay = 30;
                        int delayCount = 0;

                        for (a = invIsVisible ? 0 : childCount - 1; a < childCount && a > -1; a += invIsVisible ? 1 : -1) {
                            try {
                                int finalA = a;

                                new Handler((Looper.myLooper())).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            ((ConstraintLayout) compLayout.getChildAt(finalA)).getChildAt(1).setVisibility(inVisibility);
                                        }
                                        catch (Exception e) {
                                            e.printStackTrace();

                                            //Animate log and ln
                                            compBar[finalA - 1].setVisibility(Math.abs(inVisibility - 8));
                                        }
                                    }
                                }, (int) ((delayCount++ * delay) / (invIsVisible ? 1 : 2.6)));
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                    });
                }

                if (!isCustomTheme) {
                    for (i = 0; i < allButtons.length; i++) {
                        for (j = 0; j < allButtons[i].length; j++) {
                            try {
                                Button button = allButtons[i][j];
                                String buttonText = button.getText().toString();

                                if (!buttonText.equals("=")) {
                                    if (!theme.equals("2") && (color.equals("14") || color.equals("17")) && !Aux.isDigit(buttonText) && !buttonText.equals("."))
                                        button.setTextColor(darkGray);
                                    else
                                        button.setTextColor(textColor);
                                }
                                else if (theme.equals("2"))
                                    button.setTextColor(Color.parseColor(Aux.hexAdd(primary, -28)));

                                if (button.getParent() == keypad && theme.equals("2")) {
                                    button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(Aux.hexAdd(primary, 16))));

                                    Drawable background = button.getBackground();
                                    background.setAlpha(26);

                                    button.setBackground(background);
                                }

                                if (button.getParent().getParent() == compLayout || button.getParent() == compLayout || (orientation != Configuration.ORIENTATION_PORTRAIT && button.getParent() == findViewById(R.id.scrollBar)))
                                    button.setBackgroundTintList(ColorStateList.valueOf(secondaryColor));

                                button.setElevation(0f);
                                button.setStateListAnimator(null);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    setMTColor(textColor);
                }
            }

            try {
                if (getTvText().length() < 1)
                    tv.setText(" ");

                tv.setSelection(getTvText().length());
                tv.requestFocus();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            tinydb.putBoolean("recreating", false);
        }
        catch (Exception e) {
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }
    }

    @Override
    public final boolean onOptionsItemSelected(@NonNull MenuItem item){
        if (item.getItemId() == R.id.history) {
            HistoryBottomSheet.newInstance().show(getSupportFragmentManager(), "history_bottom_sheet");
        }
        else if (item.getItemId() == R.id.constants) {
            openConstantsSheet();
        }
        else if (item.getItemId() == R.id.functions) {
            openFunctionsSheet();
        }
        else if (item.getItemId() == R.id.help){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

            View viewInflated = LayoutInflater.from(this).inflate(R.layout.main_help_dialog, (ViewGroup) findViewById(R.id.editorBG), false);

            builder.setTitle("Help\n");

            TextView mainHelpText = viewInflated.findViewById(R.id.mainHelpText);

            mainHelpText.setTextColor(Color.WHITE);

            try {
                String toolbarTitle = toolbar.getTitle().toString();

                if (toolbarTitle.startsWith("Conv"))
                    mainHelpText.setText("This section converts your input between various different units of measurement.\n\nThe bar directly above the keypad scrolls horizontally. Select which unit type you'd like from this bar, then select your \"From\" and \"To\" units by tapping on the corresponding unit.");
                else if (toolbarTitle.startsWith("Date") || toolbarTitle.endsWith("Fecha"))
                    mainHelpText.setText("This section takes your input of two specific dates, and outputs the time between the two dates in the middle of the screen. Simply tap each text box on the bottom half of the screen, select your two dates, and tap the checkmark button at the bottom.\n\nTip: When selecting each date, in the date picker interface that appears after tapping a text box, you can tap the year at the top to change it.");
                else if (toolbarTitle.startsWith("Geo"))
                    mainHelpText.setText("This section lets you both view, and evaluate, various common geometric expressions, i.e. the area of a square or volume of a cube.\n\nTip: The button at the bottom right of the screen allows you to quickly jump to a specific shape in the list.");
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            builder.setView(viewInflated);

            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();
        }

        return true;
    }

    @Override
    public final void onDateSet(DatePicker view, int year, int month, int day){
        TextView from = findViewById(R.id.fromInput);
        TextView to = findViewById(R.id.toInput);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        String currentDateString = DateFormat.getDateInstance().format(cal.getTime());

        if (fromTo.equals("from")) {
            from.setText(currentDateString);
            initDay = day;
            initMonth = month + 1;
            initYear = year;
        }
        else {
            to.setText(currentDateString);
            finalDay = day;
            finalMonth = month + 1;
            finalYear = year;
        }
    }

    //OnClick methods for date pickers
    public final void openDatePicker(View v) {
        Button button = (Button) v;

        final FloatingActionButton equals = findViewById(R.id.dateEquals);
        TextView numYears = findViewById(R.id.numYears), numMonths = findViewById(R.id.numMonths), numDays = findViewById(R.id.numDays);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
        final String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
        final String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

        if (Objects.equals(button, findViewById(R.id.fromButton)))
            fromTo = "from";
        else
            fromTo = "to";

        DialogFragment datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(), "date picker");

        if (numMonths != null && !numMonths.getText().toString().equals("\0")) {
            numYears.setText("\0");
            numMonths.setText("\0");
            numDays.setText("\0");

            if (isEquals) {
                spin(equals, theme, color, R.drawable.ic_check_24);
                isEquals = false;
            }
        }
    }

    public final void parColorCheck(){
        Button bOpen = findViewById(R.id.bParenthesisOpen);
        Button bClose = findViewById(R.id.bParenthesisClose);

        int orientation = this.getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!roundedButtons) {
                bOpen.setBackground(null);
                bClose.setBackground(null);
            }

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");
            String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");

            Button inv = findViewById(R.id.bInv);

            if (theme != null && inv != null) {
                if (color != null && (color.equals("14") || color.equals("17")) && !theme.equals("2")) {
                    if (theme.equals("5"))
                        inv.setTextColor(Color.parseColor("#303030"));
                    else
                        inv.setTextColor(Color.WHITE);
                }
                else if (theme.equals("5")) {
                    inv.setTextColor(Color.parseColor("#303030"));
                }

                if (theme.equals("4"))
                    inv.setTextColor(Color.WHITE);
            }
        }
    }

    //Display "NaN" when the user attempts to square root a negative number
    @SuppressLint("SetTextI18n")
    public final void error() {
        final FloatingActionButton bDel = findViewById(R.id.bDel);

        Button bgAnim = findViewById(R.id.bgAnim);
        Button bEquals = findViewById(R.id.bEquals);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");
        final String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");

        boolean zeroFact = false;

        if (tv.getText() != null && !tv.getText().toString().equals("\0")) {
            if (tv.getText().toString().equals("0.0!") || tv.getText().toString().equals(" 0.0!"))
                zeroFact = true;
        }

        clear(bDel);

        if (zeroFact){
            tv.setText("1");
        }
        else if (dError) {
            tv.setText(R.string.domain_error);
            tv.setEnabled(false);
        }
        else {
            tv.setText("NaN");
        }

        equaled = true;

        showRippleAnimation(bgAnim);

        spin(bDel, theme, color, R.drawable.ic_close_24);

        tv.setEnabled(false);
        error = false;
        dError = false;

        bEquals.setText("=");
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public final boolean onNavigationItemSelected(@NonNull MenuItem item) {
        ConstraintLayout main = findViewById(R.id.mainView);
        FrameLayout frame = findViewById(R.id.fragment_container);
        Toolbar toolbar = findViewById(R.id.toolbar);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");
        String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");

        Button bDegRad = findViewById(R.id.bDegRad);

        final TinyDB tinydb = new TinyDB(this);

        String cMain = tinydb.getString("cMain");

        boolean isAlwaysDarkNav = tinydb.getBoolean("isAlwaysDarkNav");

        if (theme == null || !Aux.isFullNum(theme))
            theme = "1";

        // - Change Button Colors According to Theme -
        int cursorInt = theme.equals("2") ? 1 : 0;
        String[] primaryColors = {"#03DAC5", "#009688", "#54AF57", "#00C7E0", "#2196F3", "#0D2A89", "#3F51B5", "LILAC", "PINK", "#F44336", "#E77369", "#FF9800", "#FFC107", "#FEF65B", "#66BB6A", "#873804", "#B8E2F8"};
        String[][] secondaryColors = {{"#53E2D4", "#4DB6AC", "#77C77B", "#51D6E8", "#64B5F6", "#1336A9", "#7986CB", "#8C6DCA", "#F06292", "#FF5956", "#EC8F87", "#FFB74D", "#FFD54F", "#FBF68D", "#EF5350", "#BD5E1E", "#B8E2F8"}, {"#00B5A3", "#00796B", "#388E3C", "#0097A7", "#1976D2", "#0A2068", "#303F9F", "#5E35B1", "#C2185B", "#D32F2F", "#D96459", "#F57C00", "#FFA000", "#F4E64B", "#EF5350", "#572300", "#9BCEE9"}};
        String[][] tertiaryColors = {{"#3CDECE", "#26A69A", "#68B86E", "#39CFE3", "#42A5F5", "#0D2F9E", "#5C6BC0", "#7857BA", "#EC407A", "#FA4E4B", "#EB837A", "#FFA726", "#FFCB2E", "#F8F276", "#FF5754", "#A14D15", "#ABDBF4"}, {"#00C5B1", "#00897B", "#43A047", "#00ACC1", "#1E88E5", "#0A2373", "#3949AB", "#663ABD", "#D81B60", "#E33532", "#DE685D", "#FB8C00", "#FFB300", "#FCEE54", "#FF5754", "#612703", "#ABDBF4"}};

        if (color == null || !Aux.isFullNum(color))
            color = "1";

        if (isAlwaysDarkNav)
            setTheme(Aux.cursorColors[0][Integer.parseInt(color)-1]);
        else
            setTheme(Aux.cursorColors[cursorInt][Integer.parseInt(color)-1]);

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

            switch (primary) {
                case "LILAC":
                    if (!theme_boolean)
                        primary = "#6C42B6";
                    else
                        primary = "#7357C2";
                    break;
                case "PINK":
                    if (!theme_boolean)
                        primary = "#E32765";
                    else
                        primary = "#E91E63";
                    break;
                case "#B8E2F8":
                    if (!theme_boolean)
                        primary = "#9BCEE9";
                    else
                        primary = "#B8E2F8";
                    break;
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

        if (theme == null || theme.equals("\0"))
            theme = "1";

        final FloatingActionButton bDel = findViewById(R.id.bDel);

        final String finalTheme = theme;
        final String finalColor = color;

        if (isFabExpanded && !roundedButtons)
            findViewById(R.id.expandCustoms).performClick();

        if (item.getItemId() != R.id.nav_geo && theme.equals("1") && isCustomTheme && Aux.isColor(cMain))
            main.setBackgroundColor(Color.parseColor(Aux.hexAdd(cMain, -12)));

        if (equaled && !(toolbar.getTitle().equals("Home") && item.getItemId() == R.id.nav_home)) {
            if (!isCustomTheme && (finalColor.equals("14") && !finalTheme.equals("4") || finalColor.equals("17") && (finalTheme.equals("3") || finalTheme.equals("1")))) {
                bDel.setImageDrawable(ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.ic_arrow_dark_24));

                if (!finalTheme.equals("5"))
                    bDel.setColorFilter(darkGray);
            }
            else {
                bDel.setImageDrawable(ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.ic_baseline_arrow_back_24));

                if (!finalTheme.equals("5"))
                    bDel.setColorFilter(Color.WHITE);

                if (isCustomTheme && !Aux.isTinyColor("cFabText"))
                    bDel.setColorFilter(Color.WHITE);
            }
        }

        if (theme.equals("1") && !isCustomTheme)
            MainActivity.mainActivity.getWindow().setStatusBarColor(0);

        switch (item.getItemId()) {
            case R.id.nav_home:
                if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) != null)
                    getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).commit();

                if (isAlwaysDarkNav)
                    setTheme(Aux.cursorColors[0][Integer.parseInt(color)-1]);
                else
                    setTheme(Aux.cursorColors[cursorInt][Integer.parseInt(color)-1]);

                if (isCustomTheme) {
                    if (!Aux.isNull(cMain) && Aux.isColor(cMain))
                        drawer.setBackgroundColor(Color.parseColor(cMain));
                    else if (!Aux.isNull(bgColor))
                        drawer.setBackgroundColor(Color.parseColor(bgColor));
                }

                toolbar.setTitleTextColor(Color.parseColor(bTextColor));
                bDegRad.setTextColor(Color.parseColor(bTextColor));

                frame.setVisibility(View.INVISIBLE);
                main.setVisibility(View.VISIBLE);

                if (!toolbar.getTitle().equals("Home"))
                    clear(bDel);

                toolbar.setTitle(getResources().getString(R.string.home_menu_item));
                tinydb.putString("fragTag", "Home");

                break;

            //Unit Converter
            case R.id.nav_conversions:
                frame.setVisibility(View.VISIBLE);

                if (isCustomTheme) {
                    if (!Aux.isNull(cMain) && Aux.isColor(cMain))
                        drawer.setBackgroundColor(Color.parseColor(cMain));
                    else if (!Aux.isNull(bgColor))
                        drawer.setBackgroundColor(Color.parseColor(bgColor));
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ConversionsFragmentNew(), "conversions").commit();

                toolbar.setTitle("Conversions");
                tinydb.putString("fragTag", "Conversions");

                break;

            //Date Calculator
            case R.id.nav_date:
                frame.setVisibility(View.VISIBLE);

                if (isCustomTheme) {
                    if (!Aux.isNull(cMain) && Aux.isColor(cMain))
                        drawer.setBackgroundColor(Color.parseColor(cMain));
                    else if (!Aux.isNull(bgColor))
                        drawer.setBackgroundColor(Color.parseColor(bgColor));
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DateFragment(), "date").commit();

                toolbar.setTitle("Date");
                tinydb.putString("fragTag", "Date");

                break;

            case R.id.nav_geo:
                frame.setVisibility(View.VISIBLE);

                if (theme.equals("2"))
                    drawer.setBackgroundColor(Color.WHITE);
                else if (theme.equals("3") || theme.equals("4"))
                    drawer.setBackgroundColor(Color.BLACK);

                setTheme(theme.equals("2") ? R.style.ThemeOverlay_AppCompat_Light : R.style.ThemeOverlay_AppCompat_Dark);

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new GeometryFragmentNew(), "geometry").commit();

                toolbar.setTitle("Geometry");
                tinydb.putString("fragTag", "Geometry");

                break;

            //Settings
            case R.id.nav_theme:
                clear(bDel);

                tinydb.putBoolean("tempDynamic", tinydb.getBoolean("isDynamic"));
                tinydb.putBoolean("tempCustom", isCustomTheme);
                tinydb.putInt("tempPrecision", tinydb.getInt("precision"));
                tinydb.putString("tempTheme", theme);
                tinydb.putString("tempColor", color);
                tinydb.putString("tempTermTheme", tinydb.getString("termTheme"));
                tinydb.putString("tempWhereCustom", tinydb.getString("whereCustom"));
                tinydb.putBoolean("tempIsFocus", tinydb.getBoolean("isFocus"));

                Intent themeIntent = new Intent(this, SettingsActivity.class);
                startActivity(themeIntent);

                break;

            case R.id.nav_about:
                clear(bDel);

                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);

                deleted = false;

                break;

            //Theme Editor
            case R.id.nav_term:
                clear(bDel);

                tinydb.putBoolean("tempIsGradMain", tinydb.getBoolean("isGradMain"));

                Intent intent;
                intent = new Intent(this, ThemeActivity.class);

                startActivity(intent);

                deleted = false;

                break;
        }

        if (item.getItemId() == R.id.nav_geo && isCustomTheme && Aux.isColor(cMain)) {
            if (theme.equals("2"))
                main.setBackgroundColor(Color.WHITE);
            else if (theme.equals("5"))
                main.setBackgroundColor(Color.parseColor(cMain));
            else
                main.setBackgroundColor(Color.BLACK);
        }
        else if (isCustomTheme && Aux.isColor(cMain)) {
            if (theme.equals("1"))
                main.setBackgroundColor(Color.parseColor(Aux.hexAdd(cMain, -12)));
            else
                main.setBackgroundColor(Color.parseColor(cMain));
        }

        drawer.closeDrawer(GravityCompat.START);

        if (frame.getVisibility() == View.VISIBLE)
            findViewById(R.id.bDegRad).setVisibility(View.INVISIBLE);
        else
            findViewById(R.id.bDegRad).setVisibility(View.VISIBLE);

        if (item.getItemId() != R.id.nav_geo && isCustomTheme && Aux.isTinyColor("-mt"))
            setMTColor(Aux.getTinyColor("-mt"));
        else
            setMTColor(Color.parseColor(bTextColor));

        return true;
    }

    public void setMTColor(int color){
        ImageButton swapTopBar = findViewById(R.id.swapTopBar);
        ImageButton backspace = findViewById(R.id.delete);
        Button inv = findViewById(R.id.bInv);

        toolbar.setTitleTextColor(color);
        tv.setTextColor(color);
        ((Button) findViewById(R.id.bDegRad)).setTextColor(color);
        ((Button) findViewById(R.id.bgAnim)).setTextColor(color);

        TextView numView = findViewById(R.id.num);
        TextView denView = findViewById(R.id.den);

        View numDivider = findViewById(R.id.numeratorDivider);
        View denDivider = findViewById(R.id.denominatorDivider);

        if (Aux.getThemeInt() == 2 && !isCustomTheme && Aux.getOrientation(MainActivity.mainActivity) == Configuration.ORIENTATION_LANDSCAPE)
            ((Button) findViewById(R.id.bInv)).setTextColor(color);

        try {
            numView.setTextColor(color);
            denView.setTextColor(color);

            numDivider.setBackgroundColor(color);
            denDivider.setBackgroundColor(color);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        if (!isBig) {
            try {
                ((ImageButton) findViewById(R.id.expand)).setColorFilter(color);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            try {
                previousExpression.setTextColor(color);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (roundedButtons) {
            try {
                swapTopBar.setColorFilter(color);
                backspace.setColorFilter(color);

                ImageButton expandCustomsNew = findViewById(R.id.expandCustomsNew);
                ImageButton customFunctionsNew = findViewById(R.id.customFunctionsNew);
                ImageButton customConstantsNew = findViewById(R.id.customConstantsNew);
                ImageButton decFracNew = findViewById(R.id.decFracButtonNew);

                expandCustomsNew.setColorFilter(color);
                customFunctionsNew.setColorFilter(color);
                customConstantsNew.setColorFilter(color);
                decFracNew.setColorFilter(color);
                inv.setTextColor(color);

                TinyDB tinydb = new TinyDB(MainActivity.mainActivity);
                String theme = tinydb.getString("theme");

                ColorStateList bgColor;

                if (isCustomTheme && Aux.isTinyColor("cMain"))
                    bgColor = ColorStateList.valueOf(Aux.getTinyColor("cMain"));
                else if (theme.equals("2"))
                    bgColor = ColorStateList.valueOf(Color.WHITE);
                else if (theme.equals("1"))
                    bgColor = ColorStateList.valueOf(Color.parseColor("#272C33"));
                else
                    bgColor = ColorStateList.valueOf(Color.BLACK);

                swapTopBar.setBackgroundTintList(bgColor);
                backspace.setBackgroundTintList(bgColor);

                expandCustomsNew.setBackgroundTintList(bgColor);
                customFunctionsNew.setBackgroundTintList(bgColor);
                customConstantsNew.setBackgroundTintList(bgColor);
                decFracNew.setBackgroundTintList(bgColor);
                inv.setBackgroundTintList(bgColor);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            inv.setTextColor(color);
        }

        if (!isBig) {
            if (isCustomTheme && Aux.isTinyColor("cTop")) {
                try {
                    ((ImageButton) findViewById(R.id.expand)).setColorFilter(Aux.getTinyColor("cTop"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (isCustomTheme) {
                try {
                    int theme = Aux.getThemeInt();

                    if (theme == 5)
                        ((ImageButton) findViewById(R.id.expand)).setColorFilter(monochromeTextColor);
                    else
                        ((ImageButton) findViewById(R.id.expand)).setColorFilter(Color.WHITE);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    TinyDB tinydb = new TinyDB(MainActivity.mainActivity);
                    String tinyColor = tinydb.getString("color");
                    String theme = tinydb.getString("theme");

                    if (theme.equals("5"))
                        ((ImageButton) findViewById(R.id.expand)).setColorFilter(monochromeTextColor);
                    else if (theme.equals("4")) {
                        ((ImageButton) findViewById(R.id.expand)).setColorFilter(secondaryColor);
                    }
                    else if (tinyColor != null && (tinyColor.equals("14") || tinyColor.equals("17") && (theme.equals("3") || theme.equals("1")))) {
                        ((ImageButton) findViewById(R.id.expand)).setColorFilter(darkGray);
                    }
                    else
                        ((ImageButton) findViewById(R.id.expand)).setColorFilter(Color.WHITE);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            Drawable wrappedOverflow = DrawableCompat.wrap(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_more_vert_24, null));
            DrawableCompat.setTint(wrappedOverflow, color);

            toolbar.setOverflowIcon(wrappedOverflow);
        }
        catch (Exception e){
            e.printStackTrace();

            try {
                Drawable wrappedOverflow = DrawableCompat.wrap(toolbar.getOverflowIcon());
                DrawableCompat.setTint(wrappedOverflow, color);

                toolbar.setOverflowIcon(wrappedOverflow);
            }
            catch (Exception e2){
                e.printStackTrace();
            }
        }

        toolbar.setOverflowIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_more_vert_24, null));
        toolbar.setNavigationIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu_dark_24, null));

        try {
            Drawable wrappedHMB = DrawableCompat.wrap(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu_dark_24, null));
            DrawableCompat.setTint(wrappedHMB, color);

            toolbar.setNavigationIcon(wrappedHMB);
        }
        catch (Exception e){
            e.printStackTrace();

            try {
                Drawable wrappedHMB = DrawableCompat.wrap(toolbar.getNavigationIcon());
                DrawableCompat.setTint(wrappedHMB, color);

                toolbar.setNavigationIcon(wrappedHMB);
            }
            catch (Exception e2){
                e.printStackTrace();
            }
        }

        try {
            toolbar.getNavigationIcon().setTint(color);
            toolbar.getOverflowIcon().setTint(color);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setExpandBGColor(String color) {
        if (Aux.isColor(color) && !isBig && !roundedButtons) {
            int ebgColor = Color.parseColor(color);

            try {
                Drawable wrappedBG = DrawableCompat.wrap(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_expandbg, null));
                DrawableCompat.setTint(wrappedBG, ebgColor);

                if (Integer.parseInt(findViewById(R.id.mainView).getTag().toString()) < 600) {
                    findViewById(R.id.expandBG).setBackground(wrappedBG);
                    findViewById(R.id.expandBG2).setBackgroundColor(ebgColor);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public final void expandScrollbar(View v){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

        final ConstraintLayout scrollbar = findViewById(R.id.scrollBar);
        ImageButton expand = findViewById(R.id.expand);

        final Button inv1 = findViewById(R.id.bInv);
        final Button inv2 = findViewById(R.id.bInv2);

        final Button bOpen = findViewById(R.id.bParenthesisOpen);
        final Button bClose = findViewById(R.id.bParenthesisClose);
        final ConstraintLayout tertiaryButtons = findViewById(R.id.tertiaryButtons);

        final float parElevation = bClose.getElevation();
        final float layoutElevation = tertiaryButtons.getElevation();

        inv2.setElevation(0);

        bClose.setElevation(0);
        bOpen.setElevation(0);

        ObjectAnimator.ofFloat(expand, "rotation", 0, 180f).setDuration(250).start();
        new Handler((Looper.myLooper())).postDelayed(new Runnable() {
            @Override
            public void run() {
                //Closing
                if (isExpanded) {
                    ViewGroup.LayoutParams mainParams = scrollbar.getLayoutParams();
                    mainParams.height = (int) barHeight;

                    if (!isBig) {
                        scrollbar.getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);

                        ((ViewGroup) findViewById(R.id.expandLayout)).getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);

                        try {
                            ((ViewGroup) findViewById(R.id.customFabLayout)).getLayoutTransition()
                                    .enableTransitionType(LayoutTransition.CHANGING);
                        }
                        catch (NullPointerException e){
                            e.printStackTrace();
                        }

                        ((ViewGroup) findViewById(R.id.deleteLayout)).getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);

                        ((ViewGroup) findViewById(R.id.equationLayout)).getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);

                        ((ViewGroup) findViewById(R.id.equationScrollView)).getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);

                        ((ViewGroup) findViewById(R.id.expandBGLayout)).getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);
                    }

                    scrollbar.setLayoutParams(mainParams);

                    inv1.setVisibility(View.VISIBLE);
                    inv2.setVisibility(View.GONE);

                    if (theme != null) {
                        if (theme.equals("5")) {
                            int tertiaryColor = Color.parseColor(isDarkTab ? Aux.hexAdd(secondary, -6) : secondary);

                            bOpen.setBackgroundColor(tertiaryColor);
                            bClose.setBackgroundColor(tertiaryColor);
                        }
                        else if (!theme.equals("4")) {
                            bOpen.setBackgroundColor(tertiaryColor);
                            bClose.setBackgroundColor(tertiaryColor);
                        }
                    }

                    isExpanded = false;
                }
                //Opening
                else {
                    if (isFabExpanded)
                        findViewById(R.id.expandCustoms).performClick();

                    ViewGroup.LayoutParams mainParams = scrollbar.getLayoutParams();
                    barHeight = mainParams.height;
                    mainParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                    if (!isBig) {
                        scrollbar.getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);

                        ((ViewGroup) findViewById(R.id.expandLayout)).getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);

                        try {
                            ((ViewGroup) findViewById(R.id.customFabLayout)).getLayoutTransition()
                                    .enableTransitionType(LayoutTransition.CHANGING);
                        }
                        catch (NullPointerException e){
                            e.printStackTrace();
                        }

                        ((ViewGroup) findViewById(R.id.deleteLayout)).getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);

                        ((ViewGroup) findViewById(R.id.equationLayout)).getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);

                        ((ViewGroup) findViewById(R.id.equationScrollView)).getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);

                        ((ViewGroup) findViewById(R.id.expandBGLayout)).getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);
                    }

                    scrollbar.setLayoutParams(mainParams);

                    inv1.setVisibility(View.GONE);
                    inv2.setVisibility(View.VISIBLE);

                    bOpen.setBackground(null);
                    bClose.setBackground(null);

                    hasExpanded = true;

                    isExpanded = true;
                }
            }
        }, 10);

        new Handler((Looper.myLooper())).postDelayed(new Runnable() {
            @Override
            public void run() {
                bClose.setElevation(parElevation);
                bOpen.setElevation(parElevation);
                tertiaryButtons.setElevation(layoutElevation);
                scrollbar.setElevation(layoutElevation);
            }
        }, 200);

        if (hasExpanded)
            expand.setRotationX(expand.getRotationX()+180f);
    }

    @Override
    public final void onBackPressed() {
        FrameLayout frame = findViewById(R.id.fragment_container);
        Toolbar toolbar = findViewById(R.id.toolbar);

        TinyDB tinydb = new TinyDB(this);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (frame.getVisibility() == View.VISIBLE && !toolbar.getTitle().equals("Home")){
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_home));

            toolbar.setTitle(getResources().getString(R.string.home_menu_item));
            tinydb.putString("fragTag", "Home");

            navigationView.getMenu().getItem(0).setChecked(true);
        }
        else {
            super.onBackPressed();
        }

        if (frame.getVisibility() == View.VISIBLE && !toolbar.getTitle().equals("Home"))
            findViewById(R.id.bDegRad).setVisibility(View.INVISIBLE);
        else
            findViewById(R.id.bDegRad).setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        this.menu = menu;

        try {
            if (!Aux.tinydb().getString("whereCustom").equals("1") || this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                inflater.inflate(R.menu.main_customs, menu);
            else
                inflater.inflate(R.menu.main, menu);
        }
        catch (Exception e){
            e.printStackTrace();
            inflater.inflate(R.menu.main, menu);
        }

        return true;
    }

    @Override
    public final void onConfigurationChanged(@NonNull Configuration newConfig) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

        TinyDB tinydb = new TinyDB(this);

        try {
            functionsSheet.dismiss();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            constantsSheet.dismiss();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (isDate){
            EditText from = findViewById(R.id.fromInput);
            EditText to = findViewById(R.id.toInput);
            fromSave = from.getText().toString();
            toSave = to.getText().toString();
        }

        if (drawer.isDrawerOpen(GravityCompat.START))
            tinydb.putBoolean("closeDrawer", false);

        super.onConfigurationChanged(newConfig);

        recreate();

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(roundedButtons && Aux.getThemeInt() != 5 ? R.layout.activity_main_round : R.layout.activity_main);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(roundedButtons && Aux.getThemeInt() != 5 ? R.layout.activity_main_round : R.layout.activity_main);

            if (theme != null && theme.equals("2")){
                Button inv = findViewById(R.id.bInv);
                inv.setTextColor(darkGray);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);

        //Strings
        outState.putString("eq3", getTvText());
        outState.putString("eqConv", eqConv);
        outState.putString("fromSave", fromSave);
        outState.putString("toSave", toSave);
        outState.putString("fullFrom", fullFrom);
        outState.putString("fullTo", fullTo);

        try {
            outState.putString("result", tv.getText().toString());
        }
        catch (Exception ignored) {}

        //Booleans
        outState.putBoolean("isInv", isInv);
        outState.putBoolean("isDec", isDec);
        outState.putBoolean("isRad", isRad);
        outState.putBoolean("equaled", equaled);
        outState.putBoolean("deleted", deleted);
        outState.putBoolean("isEquals", isEquals);
        outState.putBoolean("isDrawerOpened", drawer.isDrawerOpen(GravityCompat.START));

        //Date Ints
        outState.putInt("initDay", initDay);
        outState.putInt("initMonth", initMonth);
        outState.putInt("initYear", initYear);

        outState.putInt("finalDay", finalDay);
        outState.putInt("finalMonth", finalMonth);
        outState.putInt("finalYear", finalYear);

        outState.putInt("resultDay", resultDay);
        outState.putInt("resultMonth", resultMonth);
        outState.putInt("resultYear", resultYear);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int i;
        String[] trig = {"sin", "cos", "tan", "csc", "sec", "cot"};
        Button[] trigButtons = {findViewById(R.id.bSin), findViewById(R.id.bCos), findViewById(R.id.bTan), findViewById(R.id.bCsc), findViewById(R.id.bSec), findViewById(R.id.bCot)};

        Button degRad = findViewById(R.id.bDegRad);

        String eq3 = savedInstanceState.getString("eq3");
        isInv = savedInstanceState.getBoolean("isInv");
        isDec = savedInstanceState.getBoolean("isDec");
        isRad = savedInstanceState.getBoolean("isRad");
        equaled = savedInstanceState.getBoolean("equaled");
        deleted = savedInstanceState.getBoolean("deleted");
        isEquals = savedInstanceState.getBoolean("isEquals");
        fromSave = savedInstanceState.getString("fromSave");
        toSave = savedInstanceState.getString("toSave");
        fullFrom = savedInstanceState.getString("fullFrom");
        fullTo = savedInstanceState.getString("fullTo");
        eqConv = savedInstanceState.getString("eqConv");

        selectedType = savedInstanceState.getString("selectedType");
        selectedFrom = savedInstanceState.getString("selectedFrom");
        selectedTo = savedInstanceState.getString("selectedTo");

        boolean isDrawerOpened = savedInstanceState.getBoolean("isDrawerOpened");

        initDay = savedInstanceState.getInt("initDay");
        initMonth = savedInstanceState.getInt("initMonth");
        initYear = savedInstanceState.getInt("initYear");

        finalDay = savedInstanceState.getInt("finalDay");
        finalMonth = savedInstanceState.getInt("finalMonth");
        finalYear = savedInstanceState.getInt("finalYear");

        resultDay = savedInstanceState.getInt("resultDay");
        resultMonth = savedInstanceState.getInt("resultMonth");
        resultYear = savedInstanceState.getInt("resultYear");

        final TinyDB tinydb = new TinyDB(this);
        boolean closeDrawer = tinydb.getBoolean("closeDrawer");

        if (isDrawerOpened && !closeDrawer) {
            drawer.open();
            tinydb.putBoolean("closeDrawer", true);
        }

        degRad.setText(isRad ? "RAD" : "DEG");

        try {
            for (i = 0; i < trig.length; i++) {
                trigButtons[i].setText(isInv ? trig[i] + Aux.superMinus + Aux.superscripts[1] : trig[i]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (equaled)
            ((FloatingActionButton) findViewById(R.id.bDel)).setImageDrawable(ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.ic_close_24));

        String result;

        try {
            result = savedInstanceState.getString("result");
        }
        catch (Exception e) {
            e.printStackTrace();

            result = "0";
        }

        if (getTvText().equals("\0") && equaled)
            eq3 = result;

        tv.setText(eq3);

        try {
            String tvText = getTvText();

            if (tvText.length() > 1 && equaled && tvText.contains(".") && !tvText.contains("E")) {
                if (roundedButtons)
                    findViewById(R.id.decFracButtonNew).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.decFracButton).setVisibility(View.VISIBLE);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Wrap Text
    public final void wrapText(EditText tv) {
        final TinyDB tinydb = new TinyDB(this);

        int orientation = this.getResources().getConfiguration().orientation;

        int length = getTvText().replace(",", "").length();

        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float minimumWidth = (dpHeight == dpWidth) ? dpWidth : Math.min(dpHeight, dpWidth);

        int foldShrinkOffset = 0;
        boolean textSizeChanged = tinydb.getBoolean("tvSizeChanged") && tinydb.getInt("tvSize") > 0;

        if (tinydb.getBoolean("isFold3") && minimumWidth <= 345) {
            foldTextOffset = 8;
            foldShrinkOffset = 3;
        }

        ArrayList<Integer> lengths = new ArrayList<>(Arrays.asList(0, (roundedButtons ? 12 : 10) - foldShrinkOffset, (roundedButtons ? 15 : 12) - foldShrinkOffset, (roundedButtons ? 18 : 14) - foldShrinkOffset));
        ArrayList<Integer> sizes = new ArrayList<>(Arrays.asList((textSizeChanged ? tinydb.getInt("tvSize") + 8 : 38) - foldTextOffset, (textSizeChanged ? tinydb.getInt("tvSize") + 4 : 34) - foldTextOffset, (textSizeChanged ? tinydb.getInt("tvSize") : 32) - (foldTextOffset + 2), (textSizeChanged ? tinydb.getInt("tvSize") : 30) - (foldTextOffset + 2)));

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (lengths.contains(length - 1) && length > 2) {
                ((ViewGroup) findViewById(R.id.equationLayout)).getLayoutTransition()
                        .enableTransitionType(LayoutTransition.CHANGING);

                ((ViewGroup) findViewById(R.id.equationScrollView)).getLayoutTransition()
                        .enableTransitionType(LayoutTransition.CHANGING);
            }

            for (int i=lengths.size()-1; i >= 0; i--) {
                if (length > lengths.get(i)) {
                    tv.setTextSize(sizes.get(i));
                    break;
                }
            }
        }
        else
            tv.setTextSize(40 - (length > 22 ? (length > 31 ? 10 : (int) ((length - 22) / 2) * 2) : 0));
    }

    public final void swapDate(){
        int temp;

        temp = initDay;
        initDay = finalDay;
        finalDay = temp;

        temp = initMonth;
        initMonth = finalMonth;
        finalMonth = temp;

        temp = initYear;
        initYear = finalYear;
        finalYear = temp;
    }

    public final void dateEquals(View v) {
        final FloatingActionButton equals = findViewById(R.id.dateEquals);
        EditText from = findViewById(R.id.fromInput);
        EditText to = findViewById(R.id.toInput);

        TextView days = findViewById(R.id.numDays);
        TextView months = findViewById(R.id.numMonths);
        TextView years = findViewById(R.id.numYears);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
        final String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
        final String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

        int i;

        int[] calendar = new int[12];

        if (isEquals) {
            dateClear(equals);
        }
        else if (from != null && to != null && from.getText() != null && to.getText() != null && from.getText().toString().contains(", ") && to.getText().toString().contains(", ")){
            if (initYear > finalYear || (initYear == finalYear && initMonth > finalMonth) || (initYear == finalYear && initMonth == finalMonth && initDay > finalDay))
                swapDate();

            for (i = 0; i < 12; i++) {
                if (i == 0 || i == 2 || i == 4 || i == 6 || i == 7 || i == 9 || i == 11) {
                    calendar[i] = 31;
                }
                else if (i == 1) {
                    if (initYear % 400 == 0)
                        calendar[i] = 29;
                    else if (initYear % 100 == 0)
                        calendar[i] = 28;
                    else if (initYear % 4 == 0)
                        calendar[i] = 29;
                    else
                        calendar[i] = 28;
                }
                else {
                    calendar[i] = 30;
                }
            }

            if (initDay != 0 && finalDay != 0) {
                fullFrom = from.getText().toString();
                fullTo = to.getText().toString();

                resultYear = finalYear - initYear;

                if (finalMonth >= initMonth) {
                    resultMonth = finalMonth - initMonth;
                }
                else if (finalMonth < initMonth) {
                    resultMonth = 12 - initMonth;
                    resultMonth += finalMonth;
                    resultYear -= 1;

                    if (resultYear < 0)
                        resultYear = 0;
                }

                if (finalMonth == initMonth && finalDay < initDay)
                    resultYear -= 1;

                if (finalDay >= initDay) {
                    resultDay = finalDay - initDay;
                }
                else if (finalDay < initDay) {
                    resultDay = calendar[initMonth - 1] - initDay;
                    resultDay += finalDay;
                    resultMonth -= 1;

                    if (resultMonth < 0) {
                        resultMonth += 12;
                    }
                }

                days.setText("" + resultDay);
                months.setText("" + resultMonth);
                years.setText("" + resultYear);

                spin(equals, theme, color, isEquals ? R.drawable.ic_check_24 : R.drawable.ic_extra_close_light_24);
                isEquals = !isEquals;
            }
        }
    }

    public final void dateClear(View v){
        final FloatingActionButton equals = (FloatingActionButton) v;
        EditText from = findViewById(R.id.fromInput);
        EditText to = findViewById(R.id.toInput);

        TextView days = findViewById(R.id.numDays);
        TextView months = findViewById(R.id.numMonths);
        TextView years = findViewById(R.id.numYears);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
        final String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
        final String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

        from.setText("\0");
        to.setText("\0");
        days.setText("\0");
        months.setText("\0");
        years.setText("\0");

        initDay = 0;
        initMonth = 0;
        initYear = 0;
        finalDay = 0;
        finalMonth = 0;
        finalYear = 0;

        spin(equals, theme, color, isEquals ? R.drawable.ic_check_24 : R.drawable.ic_extra_close_light_24);
        isEquals = !isEquals;
    }

    public final void switchMode(View v){
        final Button keyNum = (Button) v;
        TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

        if (keyNum.getText().toString().equals("RAD"))
            keyNum.setText("DEG");
        else
            keyNum.setText("RAD");

        isRad = keyNum.getText().toString().equals("RAD");

        tinydb.putBoolean("isRad", isRad);

        try {
            if (Aux.isFullSignedNumE(previousExpression.getText().toString()))
                previousExpression.setText(evaluate(tv.getText().toString()));
        }
        catch (Exception ignored) {}
    }

    //Cosecant
    public double arccsc(double n1) {
        double n = 0, sin, arcsin;

        if (n1 >= 1) {
            arcsin = 2000000000;
            n = 0.00001;

            while (arcsin > n1) {
                sin = Math.sin(Math.toRadians(n));
                arcsin = 1.0 / sin;
                n = n + 0.000024;
            }
        }
        else if (n1 <= 1) {
            arcsin = -2000000000;
            n = -0.00001;

            while (arcsin < n1) {
                sin = Math.sin(Math.toRadians(n));
                arcsin = 1.0 / sin;
                n = n - 0.000024;
            }
        }

        return n;
    }

    //Secant
    public double arcsec(double n1) {
        double n = 0, cos, sec;

        if (n1 > 0) {
            sec = 0;
            n = 0.00001;

            while (sec < n1) {
                cos = Math.cos(Math.toRadians(n));
                sec = 1.0 / cos;
                n += 0.000024;
            }
        }
        else if (n1 < 0) {
            n1 = 1.0 / n1;
            cos = -2;
            n = 180;

            while (cos < n1) {
                cos = Math.cos(Math.toRadians(n));
                n -= 0.000024;
            }
        }

        return n;
    }

    //Cotangent
    public double arccot(double n1) {
        double n = 0;
        double tan, arctan;

        if (n1 >= 0) {
            arctan = 2000000000;
            n = 0.00001;

            while (arctan > n1) {
                tan = Math.tan(Math.toRadians(n));
                arctan = 1.0 / (tan);
                n += 0.000024;
            }
        }
        else if (n1 < 0) {
            n1 = n1 * -1.0;
            arctan = 2000000000;
            n = 0.00001;

            while (arctan > n1) {
                tan = Math.tan(Math.toRadians(n));
                arctan = 1.0 / (tan);
                n += 0.000024;
            }

            n *= -1.0;
        }

        return n;
    }

    //INV
    public final void inv(View v) {
        Button sin = findViewById(R.id.bSin), cos = findViewById(R.id.bCos), tan = findViewById(R.id.bTan), csc = findViewById(R.id.bCsc), sec = findViewById(R.id.bSec), cot = findViewById(R.id.bCot);

        int orientation;

        try {
            orientation = Aux.getOrientation(this);
        }
        catch (Exception e) {
            e.printStackTrace();

            orientation = isBig ? Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
        }

        if (!isInv) {
            sin.setText("sin⁻¹"); cos.setText("cos⁻¹"); tan.setText("tan⁻¹"); csc.setText("csc⁻¹"); sec.setText("sec⁻¹"); cot.setText("cot⁻¹");

            if (isBig && orientation == Configuration.ORIENTATION_PORTRAIT) {
                compBar[5].setVisibility(View.GONE);
                compBar[6].setVisibility(View.VISIBLE);
            }

            isInv = true;
        }
        else {
            sin.setText("sin"); cos.setText("cos"); tan.setText("tan"); csc.setText("csc"); sec.setText("sec"); cot.setText("cot");

            if (isBig && orientation == Configuration.ORIENTATION_PORTRAIT) {
                compBar[5].setVisibility(View.VISIBLE);
                compBar[6].setVisibility(View.GONE);
            }

            isInv = false;
        }
    }

    @SuppressLint("SetTextI18n")
    public final void removeLast() {
        FloatingActionButton bDel = findViewById(R.id.bDel);

        int i;
        int cursor = tv.getSelectionStart();
        int selectionRange = tv.getSelectionEnd() - cursor;
        int endOffset = getTvText().length() - cursor;
        int deleteAmt = 1;

        String tvText = getTvText();
        String before = "~";

        try {
           before = tvText.substring(0, cursor);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (selectionRange == tvText.length()) {
            clear(bDel);
            return;
        }
        else if (selectionRange != 0) {
            cursor = tv.getSelectionEnd();
            deleteAmt = selectionRange;
        }
        else if (!before.equals("~") && before.endsWith("(")) {
            for (i=before.length()-2; i >= 0; i--) {
                if ((Aux.isLetter(Aux.chat(before, i)) && !Aux.chat(before, i).equals("e")) || (i > 0 && (Aux.chat(before, i-1) + Aux.chat(before, i)).equals(Aux.superMinus + Aux.superscripts[1])))
                    deleteAmt++;
                else
                    break;
            }
        }

        try {
            if (cursor < tvText.length() && tvText.substring(0, cursor + 1).endsWith(",") && Aux.isDigit(Aux.chat(tvText, cursor-1)) && (cursor < 2 || !Aux.isDigit(Aux.chat(tvText, cursor-2))))
                endOffset--;
            else if (tvText.substring(0, cursor).endsWith(","))
                deleteAmt++;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (tv.getSelectionStart() == getTvText().length()) {
            if (getTvText().length() <= 1)
                clear(bDel);
            else {
                if (getTvText().length() > 0) {
                    //N-th root
                    if (tvText.endsWith(Aux.sq) && Aux.isSuperscript(Aux.lastChar(Aux.newTrim(tvText, 1)))) {
                        //TODO: Backspacing n-th root
                    }
                    //Log base n
                    else if (tvText.endsWith("(") && Aux.isSubscript(Aux.lastChar(Aux.newTrim(tvText, 1)))) {
                        //TODO: Backspacing log base n
                    }
                    else {
                        //Delete trig, log, or ln
                        if (getTvText().endsWith("(")) {
                            tv.setText(Aux.newTrim(getTvText(), 1));

                            if (getTvText().endsWith(Aux.superMinus + Aux.superscripts[1]) && Aux.isLetter(Aux.lastChar(Aux.newTrim(getTvText(), 2))))
                                tv.setText(Aux.newTrim(getTvText(), 2));

                            while (Aux.isLetter(Aux.lastChar(getTvText()))) {
                                tv.setText(Aux.newTrim(getTvText(), 1));
                            }
                        }
                        //Normal backspace
                        else
                            tv.setText(Aux.newTrim(getTvText(), 1));
                    }
                }

                tv.setSelection(getTvText().length());

                if (!Aux.isNull(getTvText()) && getTvText().endsWith("."))
                    isDec = true;
            }
        }
        else {
            try {
                if (cursor > 1 && Aux.chat(getTvText(), cursor - 1).equals(Aux.sq) && Aux.isSuperscript(Aux.chat(tv.getText().toString(), cursor - 2))) {
                    tv.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));

                    //TODO: Add a for loop that goes through Aux.countNum() superscripts and sets them back to regular numbers
                }
                else if (cursor > 0){
                    tv.setText(getTvText().substring(0, cursor - deleteAmt) + getTvText().substring(cursor));
                }

                tv.setText(Aux.updateCommas(getTvText()));

                cursor = getTvText().length() - endOffset;

                if (cursor < 0)
                    cursor = 0;
                else if (cursor > getTvText().length())
                    cursor = getTvText().length();

                if (!tv.hasFocus())
                    tv.requestFocus();

                tv.setSelection(cursor);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //Backspace (Normal Click)
    public final void backspace(View v) {
        final FloatingActionButton bDel = findViewById(R.id.bDel);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
        final String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
        final String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

        if (!isBig) {
            ((ViewGroup) findViewById(R.id.equationLayout)).getLayoutTransition()
                    .disableTransitionType(LayoutTransition.CHANGING);

            ((ViewGroup) findViewById(R.id.equationScrollView)).getLayoutTransition()
                    .disableTransitionType(LayoutTransition.CHANGING);
        }

        try {
            tv.requestFocus();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String tvText = getTvText();

            if (equaled) {
                clear(bDel);
                equaled = false;

                spin(bDel, theme, color, R.drawable.ic_baseline_arrow_back_24);
            }
            else if (tvText.equals("\0") && (tv.getSelectionStart() == getTvText().length() || (tv.getSelectionStart() < 1)))
                clear(bDel);
            else
                removeLast();

            if ((getTvText().equals("\0") || getTvText().equals("") || getTvText().equals(" ")) && tv.getSelectionStart() == getTvText().length())
                clear(bDel);

            if (tvText.equals(getTvText()) && tv.getSelectionStart() > 0)
                clear(bDel);

            try {
                previousExpression.setText("");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try {
            tv.requestFocus();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Clear (Long Press)
    public final void clear(View v) {
        tv.setText(" ");
        tv.setSelection(getTvText().length());
        tv.requestFocus();

        try {
            if (roundedButtons)
                findViewById(R.id.decFracButtonNew).setVisibility(View.GONE);
            else
                findViewById(R.id.decFracButton).setVisibility(View.INVISIBLE);

            findViewById(R.id.numeratorDivider).setVisibility(View.GONE);
            findViewById(R.id.denominatorDivider).setVisibility(View.GONE);

            TextView num = findViewById(R.id.num), den = findViewById(R.id.den);

            num.setText("");
            den.setText("");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            previousExpression.setText("");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        isDec = false;
        equaled = false;
    }

    @SuppressLint("SetTextI18n")
    public final void parenthesis(View v) {
        int cursor = tv.getSelectionStart();
        String buttonText = ((Button) v).getText().toString();

        if (cursor == getTvText().length()) {
            if (equaled && buttonText.equals("(")) {
                if (getTvText().contains("E"))
                    tv.setText("(" + getTvText() + ")");

                getEqualed();

                tv.append(buttonText);
            }
            else if (buttonText.equals("(") || (!equaled && (buttonText.equals(")") && Aux.countChars(getTvText(), "(") > Aux.countChars(getTvText(), ")")) && !(getTvText().trim().replace("\0", "").equals(".") || getTvText().endsWith("(") || Aux.isBinaryOp(Aux.lastChar(getTvText())) || getTvText().endsWith("√")))) {
                tv.append(buttonText);
            }
        }
        else {
            String tvText = getTvText();

            if (equaled)
                getEqualed();

            try {
                tv.setSelection(cursor + (getTvText().length() - tvText.length()));
            }
            catch (Exception e) {
                e.printStackTrace();
                tv.setSelection(cursor);
            }

            tv.requestFocus();

            tvText = getTvText();

            if (cursor > 0)
                tv.setText(Aux.newReplace(cursor - 1, tvText, Aux.chat(tvText, cursor - 1) + buttonText));
            else if (buttonText.equals("("))
                tv.setText(buttonText + tvText.trim().replace("\0", "").replace(" ", "").trim());

            tv.setSelection(cursor + 1);
            wrapText(tv);
        }
    }

    @SuppressLint("SetTextI18n")
    public final void newerEquals(View v) {
        try {
            final FloatingActionButton clear = findViewById(R.id.bDel);

            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
            final String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
            final String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

            final TinyDB tinydb = new TinyDB(this);

            int i;

            String resultStr = "\0";

            if (tv.getText() == null)
                return;

            if (Aux.isBinaryOp(Aux.lastChar(getTvText())))
                tv.setText(Aux.newTrim(getTvText(), 1));

            if (getTvText().equals("\0") || getTvText().equals("") || getTvText().equals(" "))
                return;

            if (getTvText().endsWith("\0") || getTvText().endsWith(" "))
                tv.setText(Aux.newTrim(tv.getText().toString(), 1));

            if (tv.getText() == null || getTvText().equals("\0") || getTvText().equals("") || getTvText().equals(" "))
                return;

            //Error if tv contains negative square root
            if (getTvText().contains(Aux.sq + "-") || getTvText().contains(Aux.sq + Aux.emDash))
                error = true;

            if (!(tv.getText().toString().equals(".") || tv.getText().toString().equals(" .") || tv.getText().toString().equals("\0.") ||
                    tv.getText().toString().endsWith("(") || tv.getText().toString().endsWith("÷") || tv.getText().toString().endsWith("×") || tv.getText().toString().endsWith("+") ||
                    tv.getText().toString().endsWith("-") || tv.getText().toString().endsWith("^") || tv.getText().toString().endsWith("√") || tv.getText().toString().endsWith("÷.") ||
                    tv.getText().toString().endsWith("+.") || tv.getText().toString().endsWith("-.") || tv.getText().toString().endsWith("^.") || tv.getText().toString().endsWith("×.") ||
                    tv.getText().toString().endsWith("√.") || tv.getText().toString().endsWith("log(.") || tv.getText().toString().endsWith("ln(."))) {

                if (tv.getText().toString().equals(" (0)!") || tv.getText().toString().equals("(0.0)!") || tv.getText().toString().equals(" (0.0)!") || tv.getText().toString().equals("(0.)!") || tv.getText().toString().equals(" (0.)!") || tv.getText().toString().equals("(.0)!") || tv.getText().toString().equals(" (.0)!")) {
                    tv.setText("0!");
                    findViewById(R.id.bEquals).performClick();
                }
                else {
                    String tvText = getTvText();
                    String logTest = tvText.replace("ln(", "log(");

                    if (logTest.contains("log(0)") || logTest.contains("log(0.0)") || logTest.endsWith("log(0") || logTest.endsWith("log(0.") || logTest.endsWith("log(0.0"))
                        error = true;

                    if (tv.getText().toString().equals("0÷0") || tv.getText().toString().equals(" 0÷0") || tv.getText().toString().equals("0.0÷0.0") || tv.getText().toString().equals(" 0.0÷0.0"))
                        error = true;

                    if (error || dError) {
                        error();

                        showRippleAnimation(findViewById(R.id.bgAnim));
                    }
                    else {
                        if (!equaled) {
                            String historyTemp = getTvText();

                            // Add remaining parenthesis
                            int missing = Aux.countChars(tvText, "(") - Aux.countChars(tvText, ")");

                            for (i = 0; i < missing; i++) {
                                tv.append(")");
                            }

                            try {
                                if (previousExpression != null && Aux.isFullSignedNumE(previousExpression.getText().toString()))
                                    resultStr = previousExpression.getText().toString();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }

                            isDec = false;

                            String lastChar = Aux.lastChar(getTvText());

                            if (lastChar != null && ((lastChar.equals("!") || lastChar.equals(")") || lastChar.equals("π") || lastChar.equals("e") || lastChar.equals("∞") || Aux.isDigit(lastChar)))) {
                                if (error || dError)
                                    error();
                                else {
                                    if (!isBig) {
                                        ((ViewGroup) findViewById(R.id.equationLayout)).getLayoutTransition()
                                                .enableTransitionType(LayoutTransition.CHANGING);

                                        ((ViewGroup) findViewById(R.id.equationScrollView)).getLayoutTransition()
                                                .enableTransitionType(LayoutTransition.CHANGING);
                                    }

                                    if (shouldRecord) {
                                        ArrayList<String> equations = tinydb.getListString("equations");

                                        //Add equation to history
                                        if (tv.getText() != null && !tv.getText().toString().equals("\0") && !tv.getText().toString().contains("NaN")) {
                                            equations.add(0, historyTemp);
                                            tinydb.putListString("equations", equations);
                                        }
                                    }

                                    if (!equaled)
                                        spin(clear, theme, color, R.drawable.ic_close_24);

                                    equaled = true;

                                    //Set text of previous expression TextView
                                    try {
                                        if (previousExpression != null && tinydb.getBoolean("showPreviousExpression") && !resultStr.equals(" ") && !resultStr.equals("") && !resultStr.equals("\0") && Aux.isFullSignedNumE(resultStr)) {
                                            if (!isBig) {
                                                ((ViewGroup) findViewById(R.id.equationLayout)).getLayoutTransition()
                                                        .disableTransitionType(LayoutTransition.CHANGING);

                                                ((ViewGroup) findViewById(R.id.equationScrollView)).getLayoutTransition()
                                                        .disableTransitionType(LayoutTransition.CHANGING);
                                            }

                                            tv.setText(resultStr);
                                        }
                                        else {
                                            int precision = tinydb.getInt("precision");
                                            int scale = tinydb.getBoolean("isDynamic") ? (roundedButtons ? roundedPrecision : squarePrecision) : precision;
                                            MathContext newMc = new MathContext(tinydb.getBoolean("isDynamic") ? maxPrecision : (Math.min(precision, (maxPrecision / 2)) * 2), RoundingMode.HALF_UP);

                                            BigDecimal result = BetterMath.evaluate(getTvText().trim(), tinydb.getBoolean("prioritizeCoefficients"), isRad, newMc, scale, false);
                                            resultStr = BetterMath.formatResult(result, newMc, scale).trim();

                                            while (resultStr.equals("0") && scale < 26)
                                                resultStr = BetterMath.formatResult(result, newMc, scale++).trim();

                                            while ((resultStr.endsWith("0") && resultStr.contains(".")) || resultStr.endsWith(".") || resultStr.endsWith("0E"))
                                                resultStr = Aux.newTrim(resultStr, 1);

                                            if (!resultStr.equals(getTvText().trim()) && Aux.isFullSignedNumE(resultStr)) {
                                                tv.setText(resultStr);
                                            }
                                        }

                                        tv.clearFocus();

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                tv.selectAll();
                                                tv.setSelection(tv.getSelectionEnd());
                                                tv.requestFocus();
                                            }
                                        }, 500);

                                        if (!isBig) {
                                            ((ViewGroup) findViewById(R.id.equationLayout)).getLayoutTransition()
                                                    .enableTransitionType(LayoutTransition.CHANGING);

                                            ((ViewGroup) findViewById(R.id.equationScrollView)).getLayoutTransition()
                                                    .enableTransitionType(LayoutTransition.CHANGING);
                                        }

                                        try {
                                            if (previousExpression != null)
                                                previousExpression.setText("");
                                        }
                                        catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        showRippleAnimation(findViewById(R.id.bgAnim));
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();

                                        if (e.getMessage() != null && (e.getMessage().equals("NaN") || e.getMessage().equalsIgnoreCase("Division by zero")))
                                            tv.setText("NaN");
                                    }

                                    if (!Aux.isFullSignedNumE(getTvText())) {
                                        if (!getTvText().equals("NaN"))
                                            tv.setText(R.string.parse_error);

                                        showRippleAnimation(findViewById(R.id.bgAnim));
                                    }

                                    if (shouldRecord) {
                                        ArrayList<String> answers = tinydb.getListString("answers");

                                        //Add answer to history
                                        if (tv.getText() != null && !tv.getText().toString().equals("\0") && !tv.getText().toString().contains("NaN")) {
                                            answers.add(0, tv.getText().toString());
                                            tinydb.putListString("answers", answers);

                                            Calendar cal = Calendar.getInstance();
                                            ArrayList<Integer> days, months, years;

                                            days = tinydb.getListInt("dayEntries");
                                            months = tinydb.getListInt("monthEntries");
                                            years = tinydb.getListInt("yearEntries");

                                            days.add(0, cal.get(Calendar.DAY_OF_MONTH));
                                            months.add(0, cal.get(Calendar.MONTH));
                                            years.add(0, cal.get(Calendar.YEAR));

                                            tinydb.putListInt("dayEntries", days);
                                            tinydb.putListInt("monthEntries", months);
                                            tinydb.putListInt("yearEntries", years);
                                        }
                                    }

                                    try {
                                        if (tv.getText().toString().contains(".") && !tv.getText().toString().contains("E")) {
                                            if (roundedButtons)
                                                findViewById(R.id.decFracButtonNew).setVisibility(View.VISIBLE);
                                            else
                                                findViewById(R.id.decFracButton).setVisibility(View.VISIBLE);
                                        }
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (tv.getText().toString().contains("Error") || tv.getText().toString().contains("NaN")) {
                tv.setEnabled(false);
                tv.clearFocus();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }
    }

    public final void decimal(View v) {
        try {
            Button keyNum = (Button) v;
            final FloatingActionButton clear = findViewById(R.id.bDel);

            if (tv.getSelectionStart() == getTvText().length()) {
                if (!(!tv.getText().toString().equals("\0") && (getTvText().endsWith("π") || getTvText().endsWith("e") || getTvText().endsWith("!")))) {
                    if (equaled) {
                        equaled = false;

                        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
                        final String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
                        final String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

                        clear(findViewById(R.id.delete));
                        spin(clear, theme, color, R.drawable.ic_baseline_arrow_back_24);
                    }

                    if (!isDec) {
                        isDec = true;

                        tv.append(".");
                        tv.setSelection(getTvText().length());
                    }
                }
            }
            else {
                int cursor = tv.getSelectionStart();

                if (equaled)
                    getEqualed();

                tv.requestFocus();
                tv.setSelection(cursor);

                String tvText = tv.getText().toString();

                if (cursor > 0) {
                    tv.setText(Aux.newReplace(cursor - 1, tv.getText().toString(), Aux.chat(tv.getText().toString(), cursor - 1) + keyNum.getText().toString()));
                    tv.setSelection(cursor + 1);
                }
                else {
                    tv.setText((keyNum.getText().toString() + tvText.trim()).replace("\0", "").replace(" ", "").trim());
                    tv.setSelection(cursor + 1);
                }

                if (tv.getText().toString().contains("..")) {
                    tv.setText(tv.getText().toString().replace("..", "."));
                    tv.setSelection(cursor);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }
    }

    @SuppressLint("SetTextI18n")
    public final void number(View v) {
        try {
            int cursor = tv.getSelectionStart();
            Button keyNum = (Button) v;
            final FloatingActionButton clear = findViewById(R.id.bDel);

            if (cursor == getTvText().length()) {
                if (!(!getTvText().equals("\0") && (getTvText().endsWith(".") && (keyNum.getText().toString().equals("π") || keyNum.getText().toString().equals("e") || keyNum.getText().toString().equals("∞"))))) {
                    //Clear previous result upon pressing a number key
                    if (equaled) {
                        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
                        final String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
                        final String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

                        equaled = false;

                        clear(findViewById(R.id.delete));
                        spin(clear, theme, color, R.drawable.ic_baseline_arrow_back_24);
                    }

                    tv.append(keyNum.getText().toString());
                    tv.setSelection(getTvText().length());
                }
            }
            else {
                String tvText = getTvText();

                if (equaled)
                    getEqualed();

                try {
                    tv.setSelection(cursor + (getTvText().length() - tvText.length()));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    tv.setSelection(cursor);
                }

                tv.requestFocus();

                tvText = getTvText();

                if (cursor > 0)
                    tv.setText(Aux.newReplace(cursor - 1, tv.getText().toString(), Aux.chat(tv.getText().toString(), cursor - 1) + ((Button) v).getText().toString()));
                else
                    tv.setText((keyNum.getText().toString() + tvText.trim()).replace("\0", "").replace(" ", "").trim());

                tv.setSelection(cursor + 1);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }
    }

    @SuppressLint("SetTextI18n")
    public final void getEqualed() {
        try {
            final FloatingActionButton clear = findViewById(R.id.bDel);

            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
            final String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
            final String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

            if (getTvText().contains("NaN") || getTvText().contains("Error") || getTvText().contains("∞")) {
                clear(clear);
            }
            else {
                if (tv.getText() != null && getTvText().length() > 0) {
                    String tvText = getTvText();

                    tv.setText(tvText.replace("E", Aux.multi + "10^"));

                    clear(clear);

                    tv.setText(tvText);
                }
                else
                    clear(clear);
            }

            equaled = false;

            spin(clear, theme, color, R.drawable.ic_baseline_arrow_back_24);
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }
    }

    public final void onButtonPressed(View v) {
        Button button = (Button) v;
        String buttonText = button.getText().toString();

        if (buttonText.equals("=")) {
            newerEquals(v);
            return;
        }

        if (!isBig) {
            ((ViewGroup) findViewById(R.id.equationLayout)).getLayoutTransition()
                    .disableTransitionType(LayoutTransition.CHANGING);

            ((ViewGroup) findViewById(R.id.equationScrollView)).getLayoutTransition()
                    .disableTransitionType(LayoutTransition.CHANGING);
        }

        int resultStrLength = previousExpression != null && previousExpression.getText() != null ? previousExpression.getText().toString().length() : getTvText().length();
        int cursor = tv.getSelectionStart();
        int initCursor = cursor;
        int endOffset = getTvText().length() - cursor;
        int initLength = getTvText().length();
        boolean beforeComma = false;

        try {
            if (getTvText().length() > 1 && Aux.chat(getTvText(), cursor).equals(","))
                endOffset--;
            else if (getTvText().length() > 1 && Aux.chat(getTvText(), cursor-1).equals(","))
                beforeComma = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (Aux.isNum(buttonText))
            number(v);
        else if (buttonText.equals("."))
            decimal(v);
        else if (buttonText.equals("(") || buttonText.equals(")"))
            parenthesis(v);
        else
            operation(v);

        cursor = getTvText().length() - endOffset;

        try {
            if (!beforeComma && getTvText().length() > 1 && Aux.chat(getTvText(), cursor - 1).equals(","))
                cursor--;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (initLength == getTvText().length())
            cursor = initCursor;

        tv.setSelection(cursor);

        try {
            tv.requestFocus();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    public final void operation(View v) {
        try {
            int cursor = tv.getSelectionStart();
            String pressed = ((Button) v).getText().toString();

            if (getTvText().contains("NaN") || getTvText().contains("Error") || getTvText().contains("∞")) {
                getEqualed();
                return;
            }

            if (cursor == getTvText().length()) {
                boolean dont = false;

                //If tv is empty, and the character can logically be placed first, then just type it
                if (getTvText().trim().replace("\0", "").replace(" ", "").length() < 1) {
                    isDec = false;

                    if (!pressed.equals("!") && !pressed.equals(")") && (!Aux.isBinaryOp(pressed) || pressed.equals("-"))) {
                        dont = true;

                        tv.append(pressed);

                        if (Aux.isTrig(pressed))
                            tv.append("(");
                    }
                    else
                        return;
                }

                //Check for too many negative signs
                if (!(pressed.equals("-") && getTvText().endsWith("-") && Aux.newTrim(getTvText(), 1).endsWith("(")) && !dont) {
                    if (equaled) {
                        if (getTvText().contains("E"))
                            tv.setText("(" + getTvText() + ")");

                        getEqualed();
                    }

                    if (!(pressed.equals("!") && (Aux.lastChar(getTvText()).equals("!") || Aux.lastChar(getTvText()).equals("-")))) {
                        if (Aux.isOp(pressed) || pressed.equals("log") || pressed.equals("ln") || Aux.isTrig(pressed)) {
                            if (Aux.isBinaryOp(pressed) && !pressed.equals("-") && !pressed.equals(Aux.emDash) && Aux.isBinaryOp(Aux.lastChar(getTvText())))
                                removeLast();

                            if (!getTvText().equals("\0") && (getTvText().endsWith(".") && pressed.equals("(")))
                                dont = true;
                            else if (!getTvText().equals("\0") && (getTvText().endsWith("(") && (pressed.equals("!") || (Aux.isBinaryOp(pressed) && !pressed.equals("-") && !pressed.equals(Aux.emDash)))))
                                dont = true;
                            else if (!getTvText().equals("\0") && (getTvText().equals("-") || getTvText().equals(" -") || getTvText().equals("- ")) && (Aux.isBinaryOp(pressed) || pressed.equals(")")))
                                dont = true;
                            else if (!getTvText().equals("\0") && getTvText().endsWith("√") && ((Aux.isBinaryOp(pressed) && !pressed.equals("-")) || pressed.equals("!")))
                                dont = true;
                            else if (getTvText().endsWith("--") && pressed.equals("-"))
                                dont = true;

                            if (!dont) {
                                isDec = false;

                                tv.append(pressed);

                                if (Aux.isTrig(pressed))
                                    tv.append("(");
                            }
                        }
                    }
                }

                if (!dont) {
                    isDec = false;
                    tv.setSelection(getTvText().length());
                }
            }
            else {
                String tvText = getTvText();

                if (equaled)
                    getEqualed();

                try {
                    tv.setSelection(cursor + (getTvText().length() - tvText.length()));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    tv.setSelection(cursor);
                }

                tv.requestFocus();

                tvText = getTvText();

                if (pressed.startsWith("a") || pressed.startsWith("s") || pressed.startsWith("c") || pressed.startsWith("t") || pressed.startsWith("l"))
                    pressed += "(";

                //Cursor is not at the beginning
                if (cursor > 0)
                    tv.setText(Aux.newReplace(cursor - 1, tvText, Aux.chat(tvText, cursor - 1) + pressed));
                //Cursor is at the beginning, AND pressed is something that can exist in the beginning of an expression
                else if ((!Aux.isBinaryOp(pressed) || pressed.equals("-")) && !pressed.equals("!") && !pressed.equals(")"))
                    tv.setText((pressed + tvText.trim()).replace("\0", "").replace(" ", "").trim());
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }
    }

    public void enterStr(String[] presses){
        try {
            if (presses != null) {
                int i, j;
                int k = 0;

                for (String current : presses) {
                    if (current != null && !current.equals("\0") && !current.equals(" ") && !Aux.isNull(current)) {
                        if (Aux.isSuperscript(current))
                            nums[Integer.parseInt(Aux.superNum)].performClick();
                        else if (Aux.isDigit(current))
                            nums[Integer.parseInt(current)].performClick();
                        else if (current.equals(Aux.pi))
                            compBar[3].performClick();
                        else if (current.equals("e"))
                            compBar[4].performClick();
                        else if (current.equals("E"))
                            compBar[1].performClick();
                        else if (current.equals("√")) {
                            if (k > 0 && Aux.isSuperscript(presses[k - 1]))
                                findViewById(R.id.bSqrt).performLongClick();
                            else
                                findViewById(R.id.bSqrt).performClick();
                        }
                        else if (current.equals("."))
                            bDec.performClick();
                        else if (current.equals("-"))
                            findViewById(R.id.sMinus).performClick();
                            //Trig
                        else if (current.startsWith("arc") || current.startsWith("s") || current.startsWith("c") || current.startsWith("t")) {
                            boolean isHyper = false;

                            // Handle inverse trig
                            if (current.startsWith("arc") ^ current.endsWith("⁻¹")) {
                                if (trigBar[0].getText().toString().equals("sin"))
                                    findViewById(R.id.bInv).performClick();

                                if (current.endsWith("⁻¹"))
                                    current = "arc" + Aux.newTrim(current, 2);
                            }

                            if (current.endsWith("h")) {
                                isHyper = true;
                                current = Aux.newTrim(current, 1);
                            }

                            for (i = 0; i < trigBar.length; i++) {
                                if (trigBar[i].getText().toString().equals(current)) {
                                    if (isHyper) {
                                        trigBar[i].performLongClick();
                                        break;
                                    }
                                    else {
                                        trigBar[i].performClick();
                                        break;
                                    }
                                }
                            }
                        }
                        else {
                            boolean found = false;

                            all:
                            for (i = 1; i < allButtons.length; i++) {
                                for (j = 0; j < allButtons[i].length; j++) {
                                    try {
                                        if (allButtons[i][j].getText().toString().equals(current)) {
                                            allButtons[i][j].performClick();
                                            found = true;
                                            break all;
                                        }
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            if (!found) {
                                try {
                                    Button button = new Button(this);

                                    button.setText(current);
                                    operation(button);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    k++;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }
    }

    public void openConstantsSheet() {
        findViewById(R.id.addCustom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TinyDB tinydb = Aux.tinydb();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mainActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

                builder.setTitle("New Constant");
                View viewInflated = LayoutInflater.from(MainActivity.mainActivity).inflate(R.layout.new_constant, (ViewGroup) findViewById(R.id.mainBackup), false);

                final EditText titleInput = viewInflated.findViewById(R.id.constantTitleInput);
                final EditText numInput = viewInflated.findViewById(R.id.constantNumInput);
                final EditText unitInput = viewInflated.findViewById(R.id.constantUnitInput);

                builder.setView(viewInflated);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = null;

                        try {
                            if (numInput != null)
                                input = numInput.getText().toString();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        int i;
                        boolean inputExists = false;

                        for (i=0; i < constantNums.size(); i++) {
                            if (constantNums.get(i).equals(numInput.getText().toString())) {
                                inputExists = true;
                                break;
                            }
                        }

                        if (!inputExists && (input != null && (Aux.isFullNum(input) || Aux.containsBinaryOperator(input))) && titleInput.getText() != null) {
                            dialog.dismiss();

                            constantTitles.add(0, titleInput.getText().toString());
                            constantNums.add(0, numInput.getText().toString());
                            constantUnits.add(0, unitInput.getText().toString());

                            constantCards.add(0, new ConstantCard(constantTitles.get(0), constantNums.get(0), constantUnits.get(0)));

                            tinydb.putListString("constantTitles", constantTitles);
                            tinydb.putListString("constantNums", constantNums);
                            tinydb.putListString("constantUnits", constantUnits);

                            try {
                                constantsSheet.constantTitles = constantTitles;
                                constantsSheet.constantNums = constantNums;
                                constantsSheet.constantUnits = constantUnits;

                                constantsSheet.cards = constantCards;
                                constantsSheet.adapter.cards = constantCards;
                                constantsSheet.adapter.notifyItemInserted(0);
                                constantsSheet.recyclerView.scrollToPosition(0);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if (inputExists) {
                            Aux.makeLongToast("Error: Constant value has already been saved.");
                        }
                        else if (titleInput.getText() == null) {
                            Toast.makeText(MainActivity.mainActivity, "Error: Constant title cannot be blank.", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(MainActivity.mainActivity, "Error: Constant value must be a number.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        constantsSheet = ConstantsBottomSheet.newInstance();
        constantsSheet.show(getSupportFragmentManager(), "constants_bottom_sheet");
    }

    public void openFunctionsSheet() {
        findViewById(R.id.addCustom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TinyDB tinydb = Aux.tinydb();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mainActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

                builder.setTitle("New Function");
                View viewInflated = LayoutInflater.from(MainActivity.mainActivity).inflate(R.layout.new_function, (ViewGroup) findViewById(R.id.mainBackup), false);

                final EditText titleInput = viewInflated.findViewById(R.id.functionTitleInput);
                final EditText expressionInput = viewInflated.findViewById(R.id.eqRight);

                builder.setView(viewInflated);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (expressionInput.getText() != null && titleInput.getText() != null && titleInput.getText().toString().length() > 0 && !Aux.parseVars(expressionInput.getText().toString()).equals("0")) {
                            dialog.dismiss();

                            functionTitles.add(0, titleInput.getText().toString());
                            functionTexts.add(0, expressionInput.getText().toString());
                            functionVariables.add(0, Aux.parseVars(expressionInput.getText().toString()));
                            functionCards.add(0, new FunctionCard(functionTitles.get(0), functionTexts.get(0), functionVariables.get(0)));

                            tinydb.putListString("functionTitles", functionTitles);
                            tinydb.putListString("functionTexts", functionTexts);
                            tinydb.putListString("functionVariables", functionVariables);

                            try {
                                functionsSheet.functionTitles = functionTitles;
                                functionsSheet.functionTexts = functionTexts;
                                functionsSheet.functionVariables = functionVariables;

                                functionsSheet.cards = functionCards;
                                functionsSheet.adapter.cards = functionCards;
                                functionsSheet.adapter.notifyItemInserted(0);
                                functionsSheet.recyclerView.scrollToPosition(0);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if (expressionInput.getText() != null && expressionInput.getText().toString().length() > 0 && Aux.parseVars(expressionInput.getText().toString()).equals("0"))
                            Aux.makeLongToast("Error: Function must contain at least one variable.");
                        else
                            Toast.makeText(MainActivity.mainActivity, "Error: All fields must be filled to add a new function.", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        functionsSheet = FunctionsBottomSheet.newInstance();
        functionsSheet.show(getSupportFragmentManager(), "functions_bottom_sheet");
    }

    View.OnClickListener expandFabs = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int f;

            //Closing
            if (isFabExpanded){
                for (f = 1; f < customLabels.length; f++) {
                    customFabs[f].setVisibility(View.INVISIBLE);
                    customLabels[f].setVisibility(View.INVISIBLE);
                }

                ObjectAnimator.ofFloat(customFabs[0], "rotation", 0f, 360f).setDuration(600).start();
                try {
                    new Handler((Looper.myLooper())).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            customFabs[0].setImageDrawable(ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.ic_baseline_add_24));
                        }
                    }, 50);
                }
                catch (NullPointerException e){
                    e.printStackTrace();

                    customFabs[0].setImageDrawable(ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.ic_baseline_add_24));
                }
            }
            //Opening
            else {
                //If the scrollbar is expanded, close it
                if (isExpanded)
                    findViewById(R.id.expand).performClick();

                customFabs[1].show();
                customLabels[1].setVisibility(View.VISIBLE);

                ObjectAnimator.ofFloat(customFabs[0], "rotation", 0f, 360f).setDuration(600).start();
                try {
                    new Handler((Looper.myLooper())).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            customFabs[0].setImageDrawable(ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.ic_close_24));

                            customFabs[2].show();
                            customLabels[2].setVisibility(View.VISIBLE);
                        }
                    }, 100);
                }
                catch (NullPointerException e){
                    e.printStackTrace();

                    customFabs[0].setImageDrawable(ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.ic_close_24));

                    customFabs[2].show();
                    customLabels[2].setVisibility(View.VISIBLE);
                }
            }

            isFabExpanded = !isFabExpanded;
        }
    };

    public FunctionsAdapter.OnItemClickListener functionItemClickListener = new FunctionsAdapter.OnItemClickListener() {
        @Override
        public void onOverflowClick(final int position, View anchor) {
            final androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(MainActivity.mainActivity, anchor);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.function_overflow, popup.getMenu());

            final TinyDB tinydb = Aux.tinydb();

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getOrder() == 1) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mainActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

                        builder.setTitle("Are you sure you want to delete \"" + functionTitles.get(position) + "\"?\n");

                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                try {
                                    tinydb.putBoolean("hasDeletedFunction", true);

                                    functionTitles.remove(position);
                                    functionTexts.remove(position);
                                    functionVariables.remove(position);
                                    functionCards.remove(position);

                                    tinydb.putListString("functionTitles", functionTitles);
                                    tinydb.putListString("functionTexts", functionTexts);
                                    tinydb.putListString("functionVariables", functionVariables);

                                    functionsSheet.functionTitles = functionTitles;
                                    functionsSheet.functionTexts = functionTexts;
                                    functionsSheet.functionVariables = functionVariables;

                                    functionsSheet.cards = functionCards;
                                    functionsSheet.adapter.cards = functionCards;
                                    functionsSheet.adapter.notifyItemRemoved(position);
                                } catch (NullPointerException | IndexOutOfBoundsException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                    else if (item.getOrder() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mainActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

                        builder.setTitle("Edit Function");
                        View viewInflated = LayoutInflater.from(MainActivity.mainActivity).inflate(R.layout.new_function, (ViewGroup) findViewById(R.id.mainBackup), false);

                        final EditText titleInput = viewInflated.findViewById(R.id.functionTitleInput);
                        final EditText expressionInput = viewInflated.findViewById(R.id.eqRight);

                        builder.setView(viewInflated);

                        if (titleInput != null)
                            titleInput.setText(functionTitles.get(position));

                        if (expressionInput != null)
                            expressionInput.setText(functionTexts.get(position));

                        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (expressionInput.getText() != null && titleInput.getText() != null && titleInput.getText().toString().length() > 0 && !Aux.parseVars(expressionInput.getText().toString()).equals("0")) {
                                    dialog.dismiss();

                                    functionTitles.set(position, titleInput.getText().toString());
                                    functionTexts.set(position, expressionInput.getText().toString());
                                    functionVariables.set(position, Aux.parseVars(expressionInput.getText().toString()));

                                    functionCards.set(position, new FunctionCard(functionTitles.get(position), functionTexts.get(position), functionVariables.get(position)));

                                    tinydb.putListString("functionTitles", functionTitles);
                                    tinydb.putListString("functionTexts", functionTexts);
                                    tinydb.putListString("functionVariables", functionVariables);

                                    functionsSheet.functionTitles = functionTitles;
                                    functionsSheet.functionTexts = functionTexts;
                                    functionsSheet.functionVariables = functionVariables;

                                    functionsSheet.cards = functionCards;
                                    functionsSheet.adapter.cards = functionCards;
                                    functionsSheet.adapter.notifyItemChanged(position);
                                }
                                else if (expressionInput.getText() != null && expressionInput.getText().toString().length() > 0 && Aux.parseVars(expressionInput.getText().toString()).equals("0"))
                                    Aux.makeLongToast("Error: Function must contain at least one variable.");
                                else
                                    Toast.makeText(MainActivity.mainActivity, "Error: All fields must be filled.", Toast.LENGTH_LONG).show();
                            }
                        });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                    return false;
                }
            });

            popup.show();
        }

        @Override
        public void onCardClick(final int position, final RecyclerView recyclerView, final CardView card, final TextView functionTv, final Button insert, final ImageButton copy, final ImageButton expand) {
            try {
                final VariablesAdapter adapter = Aux.adapter.adapters.get(position);
                final String initFunctionText = functionTv.getText().toString();

                final int cardPosition = position;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            functionsSheet.recyclerView.scrollToPosition(position + 2);
                        }
                        catch (Exception e) {
                            e.printStackTrace();

                            try {
                                functionsSheet.recyclerView.scrollToPosition(position + 1);
                            }
                            catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
                }, 200);

                adapter.setOnItemTextChangedListener(new VariablesAdapter.OnItemTextChangedListener() {
                    @Override
                    public void onTextChanged(int position, EditText editText) {
                        int i;

                        adapter.values[position] = editText.getText().toString();

                        //Check that all variable fields are filled
                        for (i=0; i < adapter.values.length; i++) {
                            String value = adapter.values[i];

                            if (value == null || value.equals("\0"))
                                break;
                            else if (value.startsWith("-") && !Aux.isFullNum(value.substring(1)))
                                break;
                            else if (!Aux.isFullNum(value))
                                break;
                        }

                        //Replace variables and calculate function result
                        if (i == adapter.values.length) {
                            String functionText = Aux.adapter.cards.get(cardPosition).function;

                            insert.setAlpha(1.0f);
                            insert.setEnabled(true);

                            for (i=0; i < adapter.values.length; i++) {
                                functionText = functionText.replace(adapter.variables[i], adapter.values[i]);
                            }

                            try {
                                functionText = evaluate(functionText);
                                functionTv.setText(functionText);

                                copy.setVisibility(View.VISIBLE);

                                final String finalFunctionText = functionText;
                                copy.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            Aux.copy(finalFunctionText, MainActivity.mainActivity);

                                            Aux.makeToast("Copied \"" + finalFunctionText + "\" to clipboard", 0);
                                        }
                                        catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                insert.setOnClickListener(new View.OnClickListener() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void onClick(View v) {
                                        if (tv.getText() != null && !getTvText().equals("\0")) {
                                            tv.setText(getTvText() + finalFunctionText);

                                            try {
                                                tv.setSelection(getTvText().length());
                                            }
                                            catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            tv.requestFocus();

                                            try {
                                                functionsSheet.dismiss();
                                                hideKeyboard();
                                            }
                                            catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        else {
                                            tv.setText(finalFunctionText);

                                            try {
                                                tv.setSelection(getTvText().length());
                                            }
                                            catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            tv.requestFocus();

                                            try {
                                                functionsSheet.dismiss();
                                                hideKeyboard();
                                            }
                                            catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            }
                            catch (NaNException e) {
                                functionTv.setText(e.getMessage());

                                copy.setVisibility(View.GONE);

                                insert.setAlpha(0.5f);
                                insert.setEnabled(false);
                            }
                        }
                        else {
                            functionTv.setText(initFunctionText);

                            copy.setVisibility(View.GONE);

                            insert.setAlpha(0.5f);
                            insert.setEnabled(false);
                        }
                    }
                });

                //Insert the calculated function result if the enter key is pressed while the last variable's EditText is focused
                adapter.setOnItemEditorActionListener(new VariablesAdapter.OnItemEditorActionListener() {
                    @Override
                    public void onItemEditorAction(int position, EditText editText, int actionId) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            if (position == adapter.getItemCount() - 1 && copy.getVisibility() == View.VISIBLE)
                                insert.performClick();
                        }
                    }
                });

                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.mainActivity) {
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                });
                recyclerView.setAdapter(adapter);

                //Toggle visibility of elements, and rotate the expand icon. This works because View.VISIBLE and View.GONE both represent integers, and
                // View.VISIBLE is 0, and View.GONE is 8.
                recyclerView.setVisibility(Math.abs(recyclerView.getVisibility() - 8));
                insert.setVisibility(Math.abs(insert.getVisibility() - 8));

                expand.setRotation(Math.abs(expand.getRotation() - 180));

                if (recyclerView.getVisibility() == View.GONE) {
                    hideKeyboard();
                    copy.setVisibility(View.GONE);

                    try {
                        functionTv.setText(Aux.adapter.cards.get(cardPosition).function);
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
    };

    public final void equalsDec(View v){
        try {
            int i, j = 0;
            BigDecimal piTest;

            TextView numView = findViewById(R.id.num);
            TextView denView = findViewById(R.id.den);

            View numDivider = findViewById(R.id.numeratorDivider);
            View denDivider = findViewById(R.id.denominatorDivider);

            final DecimalFormat decdf = new DecimalFormat("#,###");

            String eq2 = tv.getText().toString().trim();

            if (!eq2.equals("\0")) {
                if (!eq2.equals(".") && !eq2.equals(" .") && !eq2.equals("\0.")) {
                    String outNum, outDen;

                    if (eq2.length() > 16 && eq2.contains(",") && Aux.countChars(eq2, ",") > 3) {
                        outNum = "Error:";
                        outDen = "Number too large";

                        outNum = "  " + outNum + "  ";
                        outDen = "  " + outDen + "  ";

                        if (numView != null)
                            numView.setText(outNum);

                        if (denView != null)
                            denView.setText(outDen);

                        if (denDivider != null)
                            denDivider.setVisibility(View.VISIBLE);

                        return;
                    }

                    String dec = eq2;

                    for (i = 0; i < eq2.length(); i++) {
                        if (dec.length() > 1 && dec.startsWith(".")) {
                            dec = dec.substring(1);
                            break;
                        }
                        else
                            dec = dec.substring(1);
                    }

                    BigDecimal[] fraction = new BigDecimal[]{BigDecimal.ONE, BigDecimal.ONE};
                    String tempNum1;

                    if (i > 0)
                        tempNum1 = eq2.substring(0, i);
                    else
                        tempNum1 = "0";

                    if (dec.length() > 1) {
                        boolean isRepeating = false;

                        if (Aux.onlyContains(dec, "3")) {
                            fraction = new BigDecimal[]{new BigDecimal("1"), new BigDecimal("3")};
                            isRepeating = true;
                        }
                        else if (Aux.onlyContains(dec, "6") || (dec.endsWith("7") && Aux.onlyContains(Aux.newTrim(dec, 1), "6"))) {
                            fraction = new BigDecimal[]{new BigDecimal("2"), new BigDecimal("3")};
                            isRepeating = true;
                        }

                        if (isRepeating) {
                            try {
                                fraction[0] = fraction[0].add(new BigDecimal(tempNum1).multiply(new BigDecimal("3")));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();

                                if (!eq2.startsWith("0") && !eq2.startsWith("."))
                                    fraction = decToFrac(eq2);
                            }
                        }
                    }
                    else
                        fraction = decToFrac(eq2);

                    if (fraction == null || fraction[0] == null || fraction[1] == null)
                        fraction = decToFrac(eq2);
                    else if ((fraction[0].toPlainString().equals("1") && fraction[1].toPlainString().equals("1")) || (fraction[1].toPlainString().equals("1") && eq2.contains(".")))
                        fraction = decToFrac(eq2);

                    try {
                        if (fraction == null || fraction[0] == null || fraction[1] == null) {
                            Log.d("Error", "decToFrac returned null everytime it was called.");
                            return;
                        }
                    }
                    catch (NullPointerException e) {
                        e.printStackTrace();
                        return;
                    }

                    if (fraction[0].compareTo(BigDecimal.valueOf(-1.0)) == 0 && fraction[1].compareTo(BigDecimal.valueOf(-1.0)) == 0)
                        return;

                    BigDecimal numerator = fraction[0];
                    BigDecimal denominator = fraction[1];

                    BigDecimal result = fraction[0].divide(fraction[1], mc);
                    BigDecimal piDec = BigDecimalMath.toBigDecimal("3.14159265358979323846");

                    boolean piCheck = false;

                    for (i = 2; i < 7; i++) {
                        piTest = piDec.divide(BigDecimal.valueOf(i), mc);

                        if (result.compareTo(piTest.add(BigDecimal.valueOf(0.0005))) < 0 && result.compareTo(piTest.subtract(BigDecimal.valueOf(0.0005))) > 0) {
                            piCheck = true;
                            break;
                        }
                    }

                    if (!piCheck) {
                        for (i = 1; i < 7; i++) {
                            for (j = 2; j < 12; j++) {
                                if (j == i)
                                    continue;

                                piTest = piDec.multiply(BigDecimal.valueOf(j)).divide(BigDecimal.valueOf(i), mc);

                                if (result.compareTo(piTest.add(BigDecimal.valueOf(0.0005))) < 0 && result.compareTo(piTest.subtract(BigDecimal.valueOf(0.0005))) > 0) {
                                    piCheck = true;
                                    break;
                                }
                            }

                            if (piCheck)
                                break;
                        }
                    }

                    if (piCheck) {
                        if (j != 0)
                            outNum = j + "π";
                        else
                            outNum = "π";

                        outDen = Integer.toString(i);
                    }
                    else {
                        outNum = decdf.format(numerator);
                        outDen = decdf.format(denominator);
                    }

                    outNum = "  " + outNum + "  ";
                    outDen = "  " + outDen + "  ";

                    findViewById(R.id.decLayout).setVisibility(View.VISIBLE);

                    if (numView != null)
                        numView.setText(outNum);

                    if (denView != null)
                        denView.setText(outDen);

                    if (outNum.length() >= outDen.length()) {
                        if (numDivider != null)
                            numDivider.setVisibility(View.VISIBLE);
                    }
                    else {
                        if (denDivider != null)
                            denDivider.setVisibility(View.VISIBLE);
                    }
                }
            }

            if (!isBig) {
                ConstraintLayout outputLayout = findViewById(R.id.decLayout);

                ConstraintLayout numLayout = findViewById(R.id.numeratorLayout);
                ConstraintLayout denLayout = findViewById(R.id.denominatorLayout);

                if (outputLayout != null)
                    outputLayout.getLayoutTransition()
                            .enableTransitionType(LayoutTransition.CHANGING);

                if (numLayout != null)
                    numLayout.getLayoutTransition()
                            .enableTransitionType(LayoutTransition.CHANGING);

                if (denLayout != null)
                    denLayout.getLayoutTransition()
                            .enableTransitionType(LayoutTransition.CHANGING);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            MainActivity.mainActivity.finishAffinity();
        }
    }

    public final BigDecimal[] decToFrac(String num1) {
        int i, j;

        BigDecimal dec, dec1, frac;
        BigDecimal num, den;

        if (num1 == null)
            return null;

        if (num1.equals("0.8333"))
            return new BigDecimal[]{new BigDecimal("5"), new BigDecimal("6")};
        if (num1.equals("0.2222"))
            return new BigDecimal[]{new BigDecimal("2"), new BigDecimal("9")};

        if (num1.length() > 3)
            num1 = num1.replace(",", "");

        try {
            //If input is a whole number
            if (!num1.contains("."))
                return new BigDecimal[]{new BigDecimal(num1), BigDecimal.ONE};
            if (num1.contains(".") && !num1.startsWith(".") && Aux.onlyContains(num1.substring(Aux.searchFor(num1, ".") + 1), "0")) {
                return new BigDecimal[]{new BigDecimal(num1.substring(0, Aux.searchFor(num1, "."))), BigDecimal.ONE};
            }
        }
        catch (NullPointerException | NumberFormatException e){
            e.printStackTrace();
            return null;
        }

        final BigDecimal maxNum = BigDecimal.valueOf(2000000);

        for (i = 3; i < 7; i += 3) {
            String iStr = Integer.toString(i);
            if (num1.length() > 2 && num1.contains(iStr + iStr) && num1.replace(iStr, "").endsWith(".")) {
                for (j=0; j < 5; j++){
                    num1 += iStr;
                }

                break;
            }
        }

        try {
            dec = new BigDecimal(num1);
        }
        catch (NullPointerException | NumberFormatException e){
            e.printStackTrace();
            return null;
        }

        dec1 = dec;

        if (dec.compareTo(BigDecimal.ZERO) == 0)
            return null;

        BigDecimal lr = new BigDecimal("-1");

        //Modulus
        while (dec1.compareTo(BigDecimal.ONE) > 0) {
            int subAmt = -1;
            String input = dec1.toString();

            if (input.contains(".")) {
                input = input.substring(0, Aux.searchFor(input, "."));

                if (Aux.isFullNum(input) && !input.contains("."))
                    subAmt = Integer.parseInt(input);
            }

            if (subAmt < 0) {
                double[] intervals = {1, 5, 10, 50, 100, 500, 1000, 5000, 10000, 50000, 100000, 500000};

                for (i = intervals.length - 1; i > 0; i--) {
                    while (dec1.compareTo(BigDecimal.valueOf(intervals[i])) > 0)
                        dec1 = dec1.subtract(BigDecimal.valueOf(intervals[i]));
                }
            }
            else {
                lr = new BigDecimal(subAmt);
                dec1 = dec1.subtract(lr);
            }
        }

        while (dec1.compareTo(BigDecimal.ZERO) < 0) {
            dec1 = dec1.add(BigDecimal.ONE);
        }

        //Find the two whole integers the decimal is between
        if (lr.compareTo(BigDecimal.ZERO) < 0) {
            if ((dec.subtract(dec1)).compareTo(BigDecimal.ZERO) == 0)
                lr = dec1;
            else
                lr = dec.subtract(dec1);

            if (dec.compareTo(BigDecimal.ONE) < 0) {
                lr = BigDecimal.ZERO;
            }
        }

        dec = dec1;

        num = BigDecimal.ONE;
        den = BigDecimal.ONE;

        boolean answerFound = false;
        int roundAmt = -1;

        final BigDecimal smolRange = BigDecimalMath.toBigDecimal("475");

        //Decimal is between 0 and 1
        if (dec.abs().compareTo(BigDecimal.ONE) < 0) {
            bigLoop : while (den.compareTo(smolRange) < 0) {
                frac = num.divide(den, mc);

                if (frac.compareTo(dec) != 0) {
                    num = BigDecimal.ONE;
                    den = BigDecimal.ONE;

                    while (dec.compareTo(frac) != 0 && num.compareTo(maxNum) < 0) {
                        frac = num.divide(den, mc);

                        if (frac.toString().length() > num1.length() + 1 && Aux.isDigit(frac.toString().substring(num1.length(), num1.length() + 1))) {
                            if (Integer.parseInt(frac.toString().substring(num1.length(), num1.length() + 1)) > 4)
                                roundAmt = -1;
                            else
                                roundAmt = 1;
                        }

                        if (frac.compareTo(dec) == 0) {
                            answerFound = true;
                            break bigLoop;
                        }
                        else if (num1.length() > 4 && (frac.toString().startsWith(num1 + "0") || (Aux.isDigit(Aux.lastChar(num1)) && (frac.toString().length() > num1.length() && frac.toString().startsWith(Aux.newTrim(num1, 1) + (Integer.parseInt(Aux.lastChar(num1)) + roundAmt)))))) {
                            try {
                                if (frac.toString().startsWith(num1)) {
                                    if (frac.toString().length() > num1.length()) {
                                        String last = Aux.lastChar(frac.toString().substring(0, num1.length() + 1));

                                        if ((Aux.isDigit(last) && Integer.parseInt(last) > 4) || !Aux.lastChar(frac.toString().substring(0, num1.length())).equals(Aux.lastChar(num1))) {
                                            num = BigDecimal.ONE;
                                            den = den.add(BigDecimal.ONE);
                                            continue;
                                        }
                                    }
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }

                            answerFound = true;
                            break bigLoop;
                        }
                        else if (den.compareTo(smolRange) > 0)
                            break bigLoop;
                        else if (frac.compareTo(dec) > 0) {
                            num = BigDecimal.ONE;
                            den = den.add(BigDecimal.ONE);
                        }
                        else {
                            BigDecimal dubAmt = (dec.subtract(frac)).divide((BigDecimal.ONE.divide(den, mc)), mc);
                            int addAmt = (int) Double.parseDouble(dubAmt.toPlainString());

                            if (addAmt > 2 || addAmt == 2 && dubAmt.compareTo(BigDecimal.valueOf(2)) < 0)
                                addAmt--;

                            if (addAmt < 2)
                                addAmt = 1;

                            num = num.add(BigDecimal.valueOf(addAmt));
                        }
                    }
                }
            }

            if (!answerFound){
                num = new BigDecimal(dec.toPlainString().replace(".", ""));
                den = BigDecimal.TEN.pow(num.toPlainString().length());

                frac = num.divide(den, mc);

                if (frac.compareTo(dec) != 0 && frac.compareTo(new BigDecimal(num1)) != 0) {
                    num = BigDecimal.ONE;
                    den = BigDecimal.ONE;

                    while (dec.compareTo(frac) != 0 && num.compareTo(maxNum) < 0) {
                        frac = num.divide(den, mc);

                        if (frac.compareTo(dec) == 0)
                            break;
                        else if (num1.length() > 4 && frac.toString().startsWith(num1))
                            break;
                        else if (frac.compareTo(dec) > 0) {
                            num = BigDecimal.ONE;
                            den = den.add(BigDecimal.ONE);
                        }
                        else {
                            BigDecimal dubAmt = (dec.subtract(frac)).divide((BigDecimal.ONE.divide(den, mc)), mc);
                            int addAmt = (int) Double.parseDouble(dubAmt.toPlainString());

                            if (addAmt > 2 || addAmt == 2 && dubAmt.compareTo(BigDecimal.valueOf(2)) < 0)
                                addAmt--;

                            if (addAmt < 2)
                                addAmt = 1;

                            num = num.add(BigDecimal.valueOf(addAmt));
                        }
                    }
                }
            }
        }
        else
            return null;

        num = num.add(den.multiply(lr));

        //Check to see if fraction is in simplest form
        for (i = 10; i > 1; i--){
            String numSimplified = num.divide(BigDecimal.valueOf(i), mc).toPlainString();
            String denSimplified = den.divide(BigDecimal.valueOf(i), mc).toPlainString();

            boolean isIntNum = false, isIntDen = false;

            if (numSimplified.contains(".")) {
                for (j = numSimplified.length() - 1; j > 0; j--) {
                    if (Aux.chat(numSimplified, j) != null && Aux.chat(numSimplified, j).equals("0"))
                        numSimplified = Aux.newTrim(numSimplified, 1);
                    else if (Aux.chat(numSimplified, j) != null && Aux.chat(numSimplified, j).equals(".")) {
                        isIntNum = true;
                        break;
                    }
                    else
                        break;
                }
            }
            else
                isIntNum = true;

            if (denSimplified.contains(".") && isIntNum) {
                for (j = denSimplified.length() - 1; j > 0; j--) {
                    if (Aux.chat(denSimplified, j) != null && Aux.chat(denSimplified, j).equals("0"))
                        denSimplified = Aux.newTrim(denSimplified, 1);
                    else if (Aux.chat(denSimplified, j) != null && Aux.chat(denSimplified, j).equals(".")) {
                        isIntDen = true;
                        break;
                    }
                    else
                        break;
                }
            }
            else
                isIntDen = true;

            if (isIntNum && isIntDen) {
                num = num.divide(BigDecimal.valueOf(i), mc);
                den = den.divide(BigDecimal.valueOf(i), mc);

                i = 10;
            }
        }

        return new BigDecimal[]{num, den};
    }

    public void spin(final ImageButton view, final String theme, final String color, final int icon) {
        ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).setDuration(800).start();
        new Handler((Looper.myLooper())).postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setImageDrawable(ContextCompat.getDrawable(MainActivity.mainActivity, icon));
            }
        }, 300);
    }

    public String evaluate(String str) throws NaNException {
        TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

        int precision = tinydb.getInt("precision");
        int scale = tinydb.getBoolean("isDynamic") ? (roundedButtons ? roundedPrecision : squarePrecision) : precision;
        MathContext newMc = new MathContext(tinydb.getBoolean("isDynamic") ? maxPrecision : (Math.min(precision, (maxPrecision / 2)) * 2), RoundingMode.HALF_UP);

        BigDecimal result = BetterMath.evaluate(str, tinydb.getBoolean("prioritizeCoefficients"), isRad, newMc, scale, false);
        String resultStr = BetterMath.formatResult(result, newMc, scale).trim();

        while (resultStr.equals("0") && scale < 26)
            resultStr = BetterMath.formatResult(result, newMc, scale++).trim();

        while ((resultStr.endsWith("0") && resultStr.contains(".")) || resultStr.endsWith(".") || resultStr.endsWith("0E"))
            resultStr = Aux.newTrim(resultStr, 1);

        return resultStr;
    }

    //Show the keyboard from a dialog
    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    //Hide the keyboard from a dialog
    public void hideKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    View.OnLongClickListener bDelLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            final FloatingActionButton bDel = findViewById(R.id.bDel);
            Button bgAnim = findViewById(R.id.bgAnim);

            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
            final String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
            final String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

            final Vibrator vibe = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);

            try {
                Aux.removeCommas(tv.getText().toString());
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            vibe(vibeDuration);

            showRippleAnimation(bgAnim);

            try {
                previousExpression.setText("");
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (equaled)
                spin(bDel, theme, color, R.drawable.ic_baseline_arrow_back_24);

            clear(bDel);

            return true;
        }
    };

    public void showRippleAnimation(View view) {
        ImageButton swapTopBar = findViewById(R.id.swapTopBar);
        ImageButton backspace = findViewById(R.id.delete);
        Button inv = findViewById(R.id.bInv);

        ImageButton expandCustomsNew = findViewById(R.id.expandCustomsNew);
        ImageButton customFunctionsNew = findViewById(R.id.customFunctionsNew);
        ImageButton customConstantsNew = findViewById(R.id.customConstantsNew);
        ImageButton decFracNew = findViewById(R.id.decFracButtonNew);

        View[] MTButtons = {backspace, swapTopBar, expandCustomsNew, customConstantsNew, customFunctionsNew, decFracNew, inv};
        final Drawable[] backgrounds = roundedButtons ? new Drawable[]{backspace.getBackground(), swapTopBar.getBackground(), expandCustomsNew.getBackground(),
                customConstantsNew.getBackground(), customFunctionsNew.getBackground(), decFracNew.getBackground(), inv.getBackground()} : null;

        try {
            if (roundedButtons) {
                for (View button : MTButtons) {
                    button.setBackground(null);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        view.setClickable(true);
        view.performClick();
        view.setPressed(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.invalidate();
                view.setPressed(false);
                view.invalidate();
                view.setClickable(false);
            }
        }, 112);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (roundedButtons) {
                    for (int i=0; i < MTButtons.length; i++) {
                        try {
                            if (backgrounds != null)
                                MTButtons[i].setBackground(backgrounds[i]);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 368);
    }

    public void snapCursor() {
        int cursor = tv.getSelectionStart();
        String tvText = getTvText();

        System.out.println("initCursor = " + cursor);

        if (tvText.contains("Error") || tvText.contains("NaN"))
            return;

        if (cursor < tvText.length() && cursor > 0) {
            int right = 0, left = 0;

            String previous = tvText.substring(cursor-1, cursor);
            String next = tvText.substring(cursor, cursor+1);

            if ((Aux.isLetter(previous) && !previous.equals("e")) && ((Aux.isLetter(next) || next.equals("(")) && !next.equals("e"))) {
                if (next.equals("("))
                    cursor++;
                else {
                    int i;

                    for (i = cursor + 1; i < tvText.length(); i++) {
                        if (!Aux.chat(tvText, i).equals("("))
                            right++;
                    }

                    for (i = cursor-1; i >= 0; i--) {
                        String current = Aux.chat(tvText, i);

                        if (Aux.isLetter(current) && !current.equals("e"))
                            left--;
                    }

                    cursor += Math.abs(left) < right ? left : right;
                }
            }
            else if (Aux.isSuperscript(previous) || Aux.isSuperscript(next))
                cursor += tvText.substring(cursor).indexOf("(") + 1;
        }
        else if (cursor != 0)
            cursor = tvText.length();

        System.out.println("finalCursor = " + cursor);

        try {
            tv.setSelection(cursor);
        }
        catch (Exception e) {
            e.printStackTrace();
            tv.setSelection(tvText.length() - 1);
        }
    }

    public String getTvText() {
        try {
            tvText = tv.getText().toString();
            return tvText;
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void vibe(int duration) {
        final Vibrator vibe = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);

        vibe.vibrate(duration);
    }
}