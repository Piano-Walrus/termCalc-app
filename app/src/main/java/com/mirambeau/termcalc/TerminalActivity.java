package com.mirambeau.termcalc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class TerminalActivity extends AppCompatActivity {
    final Activity main = MainActivity.mainActivity;
    static Activity terminalActivity;

    Button[] nums, compBar, trigBar, mainOps;
    Button bDec, bParenthesisOpen, bParenthesisClose, bEquals, bMod;
    Button[][] allButtons, smolButtons;

    int cycleIndex = 0;
    boolean cycleStarted;

    final int darkGray = Color.parseColor("#222222");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        Toolbar toolbar = findViewById(R.id.terminalToolbar);

        terminalActivity = this;

        try {
            toolbar.setTitle("Terminal");
            toolbar.setTitleTextColor(Color.WHITE);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String termTheme = sp.getString("termTheme", "1");

            if (termTheme == null || termTheme.equals("\0"))
                termTheme = "1";

            int termThemeInt = Integer.parseInt(termTheme) - 1;

            if (main != null) {
                nums = new Button[]{main.findViewById(R.id.b0), main.findViewById(R.id.b1), main.findViewById(R.id.b2), main.findViewById(R.id.b3), main.findViewById(R.id.b4), main.findViewById(R.id.b5), main.findViewById(R.id.b6), main.findViewById(R.id.b7), main.findViewById(R.id.b8), main.findViewById(R.id.b9)};
                compBar = new Button[]{main.findViewById(R.id.bSqrt), main.findViewById(R.id.bExp), main.findViewById(R.id.bFact), main.findViewById(R.id.bPi), main.findViewById(R.id.bE), main.findViewById(R.id.bLog), main.findViewById(R.id.bLn)};
                trigBar = new Button[]{main.findViewById(R.id.bSin), main.findViewById(R.id.bCos), main.findViewById(R.id.bTan), main.findViewById(R.id.bCsc), main.findViewById(R.id.bSec), main.findViewById(R.id.bCot), main.findViewById(R.id.bInv)};
                mainOps = new Button[]{main.findViewById(R.id.sPlus), main.findViewById(R.id.sMinus), main.findViewById(R.id.sMulti), main.findViewById(R.id.sDiv)};

                bDec = main.findViewById(R.id.bDec);
                bParenthesisOpen = main.findViewById(R.id.bParenthesisOpen);
                bParenthesisClose = main.findViewById(R.id.bParenthesisClose);
                bEquals = main.findViewById(R.id.bEquals);
                bMod = main.findViewById(R.id.bMod);

                allButtons = new Button[][]{nums, compBar, trigBar, mainOps, {bDec, bParenthesisOpen, bParenthesisClose, bEquals, bMod}};
                smolButtons = new Button[][]{compBar, trigBar, {mainOps[0], mainOps[2], mainOps[3]}, {bDec, bParenthesisOpen, bParenthesisClose, bEquals}};
            }

            String[] backgrounds = {"#000000", "#202227", "#FFFFFF", "#2D0A22", "#1C1E20", "#000000", "#002456"};
            String[] outputColors = {"#FFFFFF", "#FFFFFF", "#222222", "#FFFFFF", "#FFFFFF", "#65F436", "#FFFFFF"};
            String[] inputColors = {"#FFFFFF", "#FFFFFF", "#222222", "#FFFFFF", "#FFFFFF", "#65F436", "#FFFFFF"};
            String[] iconColors = {"#FFFFFF", "#FFFFFF", "#222222", "#97D955", "#34CC66", "#65F436", "#FFFFFF"};
            String[] fabTextColors = {"#FFFFFF", "#FFFFFF", "#FFFFFF", "#7292BD", "#729FCF", "#65F436", "#FFFFFF"};

            setTheme(R.style.DarkTheme);

            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {
                if (termTheme.equals("3"))
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_dark);
                else if (termTheme.equals("6"))
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_green);
                else
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_light);

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(Color.BLACK);
                getWindow().setNavigationBarColor(Color.BLACK);
            }

            final int mainWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

            //Modify the widths of both text boxes according to screen size
            final double inputWidth = mainWidth * 0.7;
            final double outputWidth = mainWidth * 0.90;

            ViewGroup.LayoutParams cmdIn = findViewById(R.id.cmdInput).getLayoutParams();
            ViewGroup.LayoutParams cmdOut = findViewById(R.id.cmdOut).getLayoutParams();

            cmdIn.width = (int) inputWidth;
            cmdOut.width = (int) outputWidth;
            findViewById(R.id.cmdInput).setLayoutParams(cmdIn);
            findViewById(R.id.cmdOut).setLayoutParams(cmdOut);

            // Allow the keyboard's enter key to be used to run a command
            final EditText edittext = (EditText) findViewById(R.id.cmdInput);
            edittext.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // If the event is a key-down event on the "enter" button
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
                        // Perform action on key press
                        enterCmd(findViewById(R.id.enterCmd));

                        return true;
                    }
                    else if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        // Perform action on key press
                        enterCmd(findViewById(R.id.enterCmd));

                        return true;
                    }
                    return false;
                }
            });

            TinyDB tinydb;

            if (main != null)
                tinydb = new TinyDB(MainActivity.mainActivity);
            else
                tinydb = new TinyDB(this);

            //Set terminal theme
            final ConstraintLayout main = findViewById(R.id.termBG);
            final TextView output = findViewById(R.id.cmdOut);
            final EditText input = findViewById(R.id.cmdInput);
            final TextView inputIcon = findViewById(R.id.hash);
            final FloatingActionButton enter = findViewById(R.id.enterCmd);

            main.setBackgroundColor(Color.parseColor(backgrounds[termThemeInt]));
            output.setTextColor(Color.parseColor(outputColors[termThemeInt]));
            input.setTextColor(Color.parseColor(inputColors[termThemeInt]));
            input.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(inputColors[termThemeInt])));
            inputIcon.setTextColor(Color.parseColor(iconColors[termThemeInt]));
            enter.setColorFilter(Color.parseColor(fabTextColors[termThemeInt]));
            toolbar.setBackgroundColor(Color.parseColor(backgrounds[termThemeInt]));

            if (termTheme.equals("3")) {
                toolbar.setTitleTextColor(darkGray);

                if (Build.VERSION.SDK_INT >= 23) {
                    main.setFitsSystemWindows(true);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }
            }
            else {
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setStatusBarColor(Color.parseColor(backgrounds[termThemeInt]));
                }
            }

            if (termTheme.equals("6")) {
                toolbar.setTitleTextColor(Color.parseColor("#65F436"));
                enter.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            }

            if (!tinydb.getBoolean("custom")) {
                printf("Note: You must first switch to \"Custom\" in Theme Settings before any changes to your theme become visible.\n");
            }

            try {
                MainActivity.mainActivity.recreate();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Ax.saveStack(e, false);
            printf("Error: Something went wrong while loading the terminal (however, some functions may still work properly). If you'd like to report this to the developer, run \"debug stack\" and attach the command's output to a bug report.\n\nError message: \"" + e.getMessage() + "\"");
        }
    }

    String cmd = "\0";
    String cPrimary, cSecondary, cTertiary, cbEquals, cPlus, cMinus, cMulti, cDiv, cKeypad, cMain, cTop, cNum, cFab, cFabText;

    String[] cmdCodes = {"-p", "-s", "-t", "-k", "-m", "-tt", "-kt", "-ft", "-bp", "-bs", "-bm", "-bd", "-f", "-e"};
    String[] cmdKeys = {"cPrimary", "cSecondary", "cTertiary", "cKeypad", "cMain", "cTop", "cNum", "cFabText", "cPlus", "cMinus", "cMulti", "cDiv", "cFab", "cbEquals"};
    String[] cmdColorWords = {"Primary", "Secondary", "Tertiary", "Keypad", "Main background", "Top bar text", "Keypad text", "Delete button text", "Plus button", "Minus button", "Multiply button", "Divide button", "Delete button", "Equals button"};

    int i, j, k;

    boolean isHex = false;
    boolean isAll = false, isRestore = false, ftIsSecondary;

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (main == null) {
            try {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(TerminalActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 10);
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void enterCmd(View v) {
        EditText tv = findViewById(R.id.cmdInput);

        if (!Ax.isNull(tv)) {
            if (!Ax.isNull(tv.getText()) && !Ax.isNull(tv.getText().toString())) {
                cmd = tv.getText().toString();

                try {
                    run(cmd);
                } catch (Exception except) {
                    except.printStackTrace();
                    Ax.saveStack(except);
                    printf("Error: Could not execute command.\nStack trace logged.");
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void run(String cmd) {
        TinyDB tinydb = Ax.tinydb();

        //String[] cmdColors = {tinydb.getString("cPrimary"), tinydb.getString("cSecondary"), tinydb.getString("cTertiary"), tinydb.getString("cKeypad"), tinydb.getString("cMain"), tinydb.getString("cTop"), tinydb.getString("cNum"), tinydb.getString("cFabText"), tinydb.getString("cPlus"), tinydb.getString("cMinus"), tinydb.getString("cMulti"), tinydb.getString("cDiv"), tinydb.getString("cFab"), tinydb.getString("cbEquals")};
        String setError;
        String hex = "none";
        String[] splitCmd = cmd.split(" ");

        if (isRestore)
            setError = "Error: Invalid hex code";
        else
            setError = "\nUsages:\n• set <button code> <hex code>\n• set <button code> gui\n• set <button code> #reset0\n\nType \"help set\" or \"help get\" for more information about button codes, and how these commands work.";

        String getError = "\nUsage: get <button code>\n\nType \"help set\" or \"help get\" for more information about button codes, and how these commands work.";

        if (!cmd.equals("\0")) {
            Log.d("cmd", cmd);
            if (cmd.length() > 2) {
                Log.d("init cmd", cmd.substring(0, 3));
            }
        }

        if (!cmd.equals("\0")) {
            if (cmd.endsWith(" ") && cmd.length() > 1)
                cmd = Ax.newTrim(cmd, 1);

            //Set color
            if (cmd.length() > 2 && cmd.startsWith("set") || (cmd.startsWith("mode ") && cmd.length() == 6)) {
                if (cmd.length() > 8) {
                    hex = Ax.getLast(cmd, 7);

                    if (!cmd.contains("mode") && !cmd.contains("reset0")) {
                        hex = Ax.colorToUpper(hex);
                    }

                    isHex = Ax.isColor(hex);

                    if (cmd.contains("-ft") && hex.substring(1).equalsIgnoreCase(tinydb.getString("cSecondary").substring(1))) {
                        hex = Ax.hexAdd(hex, -1);
                        ftIsSecondary = true;
                    }
                }
                else
                    isHex = false;

                String cmdEnd = cmd.substring(4);


                //ColorPicker
                if (cmd.length() > 8 && Ax.chat(cmd, 3).equals(" ") && cmd.endsWith("gui")){
                    for (i=0; i < 14; i++){
                        if (Ax.newTrim(cmd, 4).endsWith(cmdCodes[i])){
                            openColorPicker(i);
                            break;
                        }
                        else if (Ax.buttonExists(Ax.newTrim(cmd, 4).substring(6))){
                            openColorPicker(15);
                            break;
                        }
                    }
                }
                //Buttons
                else if (cmd.length() > 12 && (cmd.substring(4).startsWith("buttons") || cmd.substring(4).startsWith("-buttons")) && cmd.endsWith("#reset0")){
                    int a, b, c;

                    int testLength = 0;

                    for (a=0; a < allButtons.length; a++) {
                        for (b = 0; b < allButtons[a].length; b++) {
                            testLength++;
                        }
                    }

                    testLength += 1;

                    String[] extraTexts = new String[testLength];

                    c = 0;

                    for (a=0; a < allButtons.length; a++) {
                        for (b = 0; b < allButtons[a].length; b++) {
                            if (allButtons[a][b] != null)
                                extraTexts[c] = allButtons[a][b].getText().toString();

                            c++;
                        }
                    }

                    String[] extraColors = new String[extraTexts.length];
                    String[] extraTextColors = new String[extraTexts.length];

                    for (b=0; b < extraTexts.length; b++){
                        extraColors[b] = tinydb.getString("-b" + extraTexts[b]);
                        extraTextColors[b] = tinydb.getString("-b" + extraTexts[b] + "t");
                    }

                    for (a = 0; a < extraColors.length; a++) {
                        tinydb.putString("-b" + extraTexts[a], "\0");
                        tinydb.putString("-b" + extraTexts[a] + "t", "\0");
                    }

                    tinydb.putString("-bⁿ√", "\0");
                    tinydb.putString("-bⁿ√t", "\0");

                    printf("All individual button background and text colors have been reset.");

                    try {
                        MainActivity.mainActivity.recreate();
                    }
                    catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }
                //Mode
                else if ((cmd.length() == 6 && Ax.newTrim(cmd, 1).equals("mode ")) || (cmd.length() == 7 && Ax.newTrim(cmd, 1).equals("theme "))){
                    if (Ax.isDigit(Ax.lastChar(cmd)) && (Ax.getLast(Ax.newTrim(cmd, 1), 5).equals("mode ") || Ax.getLast(Ax.newTrim(cmd, 1), 6).equals("theme "))){
                        if (Integer.parseInt(Ax.lastChar(cmd)) < 6 && Integer.parseInt(Ax.lastChar(cmd)) > 0) {
                            String newTheme = Ax.lastChar(cmd);
                            String[] themeNames = {"Dark", "Light", "AMOLED Black (Colored Buttons)", "AMOLED Black (Black Buttons)", "Monochrome"};
                            int themeInt = Integer.parseInt(newTheme);

                            tinydb.putString("theme", newTheme);
                            tinydb.putString("basicTheme", newTheme);
                            tinydb.putString("customTheme", newTheme);
                            printf("Theme set to " + themeNames[themeInt - 1]);

                            try {
                                MainActivity.mainActivity.recreate();
                            }
                            catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }
                        else {
                            printf("Error: There is no base theme associated with that number. Please enter a number from 1-5.");
                        }
                    }
                    else {
                        printf("Error: There is no base theme associated with that number. Please enter a number from 1-5.");
                    }
                }
                //Plus
                else if (cmd.length() > 6 && cmd.substring(4, 7).equals("-bp")) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            printf("Plus button text color set to " + hex);
                            cPlus = hex;
                            tinydb.putString("-b+t", cPlus);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (hex.equals("#reset0")){
                            printf("Plus button text color reset");
                            cPlus = "\0";
                            tinydb.putString("-b+t", cPlus);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }

                    }
                    else {
                        printf(setError);
                    }
                }
                //Minus
                else if (cmd.length() > 9 && cmd.startsWith("-bs", 4) && !cmd.startsWith("-bsin", 4) && !cmd.startsWith("-bsec", 4)) {
                    if (isHex) {
                        printf("Minus button text color set to " + hex);
                        cMinus = hex;
                        tinydb.putString("-b-t", cMinus);

                        if (!isAll) {
                            try {
                                MainActivity.mainActivity.recreate();
                            }
                            catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    else if (hex.equals("#reset0")) {
                        printf("Minus button text color reset");
                        cMinus = "\0";
                        tinydb.putString("-b-t", cMinus);

                        if (!isAll) {
                            try {
                                MainActivity.mainActivity.recreate();
                            }
                            catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    else {
                        printf(setError);
                    }
                }
                //Multi
                else if (cmd.length() > 6 && cmd.startsWith("-bm", 4)) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            printf("Multiply button text color set to " + hex);
                            cMulti = hex;
                            tinydb.putString("-b×t", cMulti);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")){
                            printf("Multiply button text color reset");
                            cMulti = "\0";
                            tinydb.putString("-b×t", cMulti);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }
                    }
                    else {
                        printf(setError);
                    }
                }
                //Div
                else if (cmd.length() > 6 && cmd.substring(4, 7).equals("-bd")) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            printf("Divide button color set to " + hex);
                            cDiv = hex;
                            tinydb.putString("-b÷t", cDiv);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")){
                            printf("Divide button color reset");
                            cDiv = "\0";
                            tinydb.putString("-b÷t", cDiv);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }

                    }
                    else {
                        printf(setError);
                    }
                }
                else if (cmdEnd.startsWith("-b")) {
                    int c;
                    int codeLength = 0;
                    boolean isReset = false;

                    if (!isHex) {
                        if (cmd.endsWith("#reset0")) {
                            hex = "#reset0";
                            isReset = true;
                        }
                    }

                    for (c=0; c < cmdEnd.length(); c++){
                        if (Ax.chat(cmdEnd, c).equals(" "))
                            break;

                        codeLength++;
                    }

                    String buttonCode = cmdEnd.substring(0, codeLength);
                    String buttonText;

                    if (buttonCode.endsWith("t") && !buttonCode.endsWith("ot")) {
                        buttonText = Ax.newTrim(buttonCode.substring(2), 1);

                        if (Ax.buttonExists(buttonText)) {
                            if (isHex) {
                                printf("Button " + buttonText + " text color set to " + hex);
                                tinydb.putString(buttonCode, hex);
                            }
                            else if (isReset) {
                                printf("Button " + buttonText + " text color reset");
                                tinydb.putString(buttonCode, "\0");
                            }
                        }
                        else{
                            printf("Error: Unknown button code.\nTry running the \"help set\" or \"help get\" commands.");
                        }
                    }
                    else {
                        buttonText = buttonCode.substring(2);

                        if (Ax.buttonExists(buttonText)) {
                            if (isHex) {
                                printf("Button " + buttonText + " color set to " + hex);
                                tinydb.putString(buttonCode, hex);
                            }
                            else if (isReset) {
                                printf("Button " + buttonText + " color reset");
                                tinydb.putString(buttonCode, "\0");
                            }
                        }
                        else{
                            printf("Error: Unknown button code.\nTry running the \"help set\" or \"help get\" commands.");
                        }
                    }

                    if (!isAll && (isHex || isReset)) {
                        try {
                            MainActivity.mainActivity.recreate();
                        }
                        catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                }
                //Top Bar Text Color
                else if (cmd.length() > 6 && cmd.substring(4, 7).equals("-tt")) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            printf("Top bar text color set to " + hex);
                            cTop = hex;
                            tinydb.putString("cTop", cTop);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")){
                            printf("Top bar text color reset");
                            cTop = "\0";
                            tinydb.putString("cTop", cTop);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }

                    }
                    else {
                        printf(setError);
                    }
                }
                //Toolbar/UI Text Color
                else if (cmd.length() > 6 && cmd.startsWith("-mt", 4)) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            printf("Toolbar/UI text color set to " + hex);
                            tinydb.putString("-mt", hex);
                            tinydb.putBoolean("mtIsSet", true);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")){
                            printf("Toolbar/UI text color reset");
                            tinydb.putString("-mt", "\0");
                            tinydb.putBoolean("mtIsSet", false);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }

                    }
                    else {
                        printf(setError);
                    }
                }
                //Keypad Text Color
                else if (cmd.length() > 6 && cmd.substring(4, 7).equals("-kt")) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            printf("Keypad text color set to " + hex);
                            cNum = hex;
                            tinydb.putString("cNum", cNum);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")){
                            printf("Keypad text color reset");
                            cNum = "\0";
                            tinydb.putString("cNum", cNum);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }

                    }
                    else {
                        printf(setError);
                    }
                }
                //Fab Icon Color
                else if (cmd.length() > 6 && cmd.substring(4, 7).equals("-ft")) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            String newHex;

                            if (ftIsSecondary)
                                newHex = Ax.hexAdd(hex, 1);
                            else
                                newHex = hex;

                            printf("Delete button icon color set to " + newHex);
                            cFabText = hex;
                            tinydb.putString("cFabText", cFabText);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")){
                            printf("Delete button icon color reset");
                            cFabText = "\0";
                            tinydb.putString("cFabText", "\0");

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }
                    }
                    else {
                        printf(setError);
                    }

                    if (cFabText.equals("#reset0")) {
                        cFabText = "\0";
                        tinydb.putString("cFabText", "\0");
                    }
                }



                //All
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-a")) {
                    if (cmd.length() > 8) {
                        if (isHex) {
                            isAll = true;

                            for (k=0; k < 14; k++) {
                                run("set " + cmdCodes[k] + " " + hex);
                            }

                            printf("\nAll colors set to " + hex);

                            Ax.tinydb().putBoolean("isSetSecondary", true);

                            isAll = false;

                            try {
                                MainActivity.mainActivity.recreate();
                            }
                            catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }
                        else if (hex.equals("#reset0")){
                            isAll = true;

                            for (k=0; k < 14; k++) {
                                run("set " + cmdCodes[k] + " " + hex);
                            }

                            printf("\nAll colors reset");

                            isAll = false;

                            try {
                                MainActivity.mainActivity.recreate();
                            }
                            catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }
                        else {
                            printf(setError);
                        }

                    }
                    else {
                        printf(setError);
                    }
                }
                //Fab Color
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-f")) {
                    if (cmd.length() > 8) {
                        if (isHex) {
                            printf("Delete button color set to " + hex);
                            cFab = hex;
                            tinydb.putString("cFab", cFab);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            printf("Delete button color reset");
                            cFab = "\0";
                            tinydb.putString("cFab", cFab);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }

                    }
                    else {
                        printf(setError);
                    }
                }
                //Primary
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-p")) {
                    if (cmd.length() > 8 && cmd.substring(7).length() == 7 && cmd.charAt(7) == '#') {
                        if (isHex) {
                            printf("Primary color set to " + hex);
                            cPrimary = hex;
                            tinydb.putString("cPrimary", cPrimary);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            printf("Primary color reset");
                            cPrimary = "\0";
                            tinydb.putString("cPrimary", cPrimary);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }
                    }
                    else {
                        printf(setError);
                    }
                }
                //Secondary
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-s")) {
                    if (cmd.length() > 8 && cmd.substring(7).length() == 7 && cmd.charAt(7) == '#') {
                        if (isHex) {
                            printf("Secondary color set to " + hex);
                            cSecondary = hex;
                            tinydb.putString("cSecondary", cSecondary);

                            Ax.tinydb().putBoolean("isSetSecondary", true);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            printf("Secondary color reset");
                            cSecondary = "\0";
                            tinydb.putString("cSecondary", cSecondary);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }
                    }
                    else {
                        printf(setError);
                    }
                }
                //Tertiary
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-t")) {
                    if (cmd.length() > 8 && cmd.substring(7).length() == 7 && cmd.charAt(7) == '#') {
                        if (isHex) {
                            printf("Tertiary color set to " + hex);
                            cTertiary = hex;
                            tinydb.putString("cTertiary", cTertiary);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            printf("Tertiary color reset");
                            cTertiary = "\0";
                            tinydb.putString("cTertiary", cTertiary);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }

                    }
                    else {
                        printf(setError);
                    }
                }
                //Keypad
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-k")) {
                    if (cmd.length() > 8 && cmd.substring(7).length() == 7 && cmd.charAt(7) == '#') {
                        if (isHex) {
                            printf("Keypad color set to " + hex);
                            cKeypad = hex;
                            tinydb.putString("cKeypad", cKeypad);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            printf("Keypad color reset");
                            cKeypad = "\0";
                            tinydb.putString("cKeypad", cKeypad);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }
                    }
                    else {
                        printf(setError);
                    }
                }
                //Main Background
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-m")) {
                    if (cmd.length() > 8 && cmd.substring(7).length() == 7 && cmd.charAt(7) == '#') {
                        if (isHex) {
                            printf("Main background color set to " + hex);
                            cMain = hex;
                            tinydb.putString("cMain", cMain);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            printf("Main background color reset");
                            cMain = "\0";
                            tinydb.putString("cMain", cMain);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }
                    }
                    else {
                        printf(setError);
                    }
                }
                //Equals
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-e")) {
                    if (cmd.length() > 8 && cmd.substring(7).length() == 7 && cmd.charAt(7) == '#') {
                        if (isHex) {
                            printf("Equals button text color set to " + hex);
                            cbEquals = hex;
                            tinydb.putString("-b=t", cbEquals);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            printf("Equals button text color reset");
                            cbEquals = "\0";
                            tinydb.putString("-b=t", cbEquals);

                            if (!isAll) {
                                try {
                                    MainActivity.mainActivity.recreate();
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            printf(setError);
                        }
                    }
                }
                else if (cmd.startsWith("set") || cmd.startsWith("Set") || cmd.startsWith("SET")){
                    printf(setError);
                }

                if (!Ax.isNull(cFabText)) {
                    if (cFabText.equals("#reset0")) {
                        cFabText = "\0";
                        tinydb.putString("cFabText", "\0");
                    }
                }
            }




            //Delete
            else if (cmd.startsWith("delete ") && cmd.length() > 7){
                String filename = cmd.substring(7);

                if (filename.endsWith(".txt"))
                    filename = Ax.newTrim(filename, 4);

                File path = new File(this.getFilesDir(), "themes");
                File file = new File(path, filename + ".txt");
                boolean deleted = file.delete();

                if (deleted)
                    printf("\"" + filename + "\" successfully deleted.");
                else
                    printf("Error: \"" + filename + "\" could not be deleted.\nPlease check that you have typed the name correctly, and try again.");
            }





            //Share
            else if (cmd.startsWith("share ") && cmd.length() > 6) {
                String filename = cmd.substring(6);

                if (filename.endsWith(".txt"))
                    filename = Ax.newTrim(filename, 4);

                File path = new File(this.getFilesDir(), "themes");
                File file = new File(path, filename + ".txt");

                if (file.exists()) {
                    Uri textUri = FileProvider.getUriForFile(
                            this,
                            "com.mirambeau.termcalc.fileprovider",
                            file);

                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("application/text");
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, textUri);
                    startActivity(Intent.createChooser(sharingIntent, "Share Theme"));
                }
            }




            //Clear History
            else if (cmd.replace(" ", "").equalsIgnoreCase("clearhistory")) {
                tinydb.putListString("equations", new ArrayList<String>());
                tinydb.putListString("answers", new ArrayList<String>());
                tinydb.putListInt("dayEntries", new ArrayList<Integer>());
                tinydb.putListInt("monthEntries", new ArrayList<Integer>());
                tinydb.putListInt("yearEntries", new ArrayList<Integer>());

                printf("History cleared");
            }




            //Accent Color
            else if (splitCmd[0].equalsIgnoreCase("accent") && Ax.isFullNum(splitCmd[1])) {
                try {
                    int colorInt = Integer.parseInt(splitCmd[1]);

                    String[] colors = {"Mint Green", "Teal", "Green", "Cyan", "Blue", "Navy Blue", "Indigo", "Lilac", "Pink", "Red", "Coral", "Orange", "Honey",
                            "Dodie Yellow", "Festive", "Brown", "Baby Blue"};
                    if (colorInt > 0 && colorInt < 18) {
                        tinydb.putString("color", splitCmd[1]);
                        MainActivity.mainActivity.recreate();
                        printf("Accent color set to \"" + colors[colorInt-1] + "\"");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }




            //TinyDB
            if (splitCmd[0].equalsIgnoreCase("tinydb") && splitCmd.length > 1) {
                String dataType = splitCmd[1];
                String key = splitCmd[2].replace("\"", "").replace("'", "");
                String value = "";

                if (splitCmd.length > 3)
                    value = splitCmd[3].replace("\"", "").replace("'", "");

                boolean isGet = splitCmd.length == 3;
                boolean isSet = splitCmd.length == 4;

                String method = "";

                if (dataType.equalsIgnoreCase("str") || dataType.equalsIgnoreCase("string") || dataType.equalsIgnoreCase("getString")) {
                    if (isGet) {
                        smolPrintf("value: " + tinydb.getString(key));
                        method = "getString";
                    }
                    else if (isSet) {
                        tinydb.putString(key, value);
                        method = "putString";
                    }
                }
                else if (dataType.equalsIgnoreCase("int") || dataType.equalsIgnoreCase("getInt")) {
                    if (isGet) {
                        smolPrintf("value: " + tinydb.getInt(key));
                        method = "getInt";
                    }
                    else if (isSet) {
                        try {
                            tinydb.putInt(key, Integer.parseInt(value));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        method = "putInt";
                    }
                }
                else if (dataType.equalsIgnoreCase("bool") || dataType.equalsIgnoreCase("boolean") || dataType.equalsIgnoreCase("getBoolean")) {
                    if (isGet) {
                        smolPrintf("value: " + tinydb.getBoolean(key));
                        method = "getBoolean";
                    }
                    else if (isSet) {
                        tinydb.putBoolean(key, Boolean.parseBoolean(value));
                        method = "putBoolean";
                    }
                }

                if (Ax.isColor(value) || value.equals("#reset0") || (splitCmd.length == 4 && (splitCmd[3].equals("\"\"") || key.equals("custom")))) {
                    try {
                        MainActivity.mainActivity.recreate();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                String hasQuotes = ((dataType.equalsIgnoreCase("str") || dataType.equalsIgnoreCase("string")) && isSet) ? "\"" : "";

                smolPrintf("tinydb." + method + "(\"" + key + "\"" + (isGet ? "" : ", ") + hasQuotes + value + hasQuotes + ") executed successfully");
            }




            //TextSize
            else if (splitCmd[0].equalsIgnoreCase("textSize") || splitCmd[0].equalsIgnoreCase("txtSize") && splitCmd.length > 1) {
                try {
                    if (Ax.isFullNum(splitCmd[1])) {
                        try {
                            tinydb.putBoolean("tvSizeChanged", true);
                            tinydb.putInt("tvSize", Integer.parseInt(splitCmd[1]));

                            smolPrintf("Main output text size set to " + splitCmd[1] + "sp");
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }




            //Minimum Width
            else if (splitCmd[0].equalsIgnoreCase("getMinimumWidth") || splitCmd[0].equalsIgnoreCase("minWidth")) {
                try {
                    smolPrintf("Current minimum width is: " + Ax.getMinimumWidth(MainActivity.mainActivity));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }




            //Reset
            else if ((cmd.length() > 6 && cmd.startsWith("reset ") && !cmd.equals("reset buttons"))){
                String end = cmd.substring(6);
                String[] editorCodes = {"-p", "-s", "-t", "-m", "-k", "-kt", "-tt"};

                int e;

                if (end.equals("-s"))
                    Ax.tinydb().putBoolean("isSetSecondary", false);

                if (end.equalsIgnoreCase("all") || end.equals("-a")){
                    run("set -a #reset0");
                    run("reset buttons");
                    tinydb.putString("-mt", "\0");

                    tinydb.putBoolean("mtIsSet", false);

                    Ax.tinydb().putBoolean("isSetSecondary", false);

                    for (e=0; e < editorCodes.length; e++)
                        tinydb.putString(editorCodes[e], "\0");
                }
                else if (end.equals("-mt")) {
                    run("set -mt #reset0");
                    tinydb.putBoolean("mtIsSet", false);
                }
                else if (end.equalsIgnoreCase("button") || end.equalsIgnoreCase("-buttons")){
                    run("reset buttons");
                }
                else if (Ax.isButtonCode(end)){
                    run("set " + end + " #reset0");
                }
                else {
                    String[] extraCodes = {"-bop", "-btt", "-bINV2", "-bINV2t"};

                    for (i=0; i < extraCodes.length; i++) {
                        if (extraCodes[i].equals(end)) {
                            tinydb.putString(extraCodes[i], "");
                            smolPrintf(extraCodes[i] + " reset");

                            try {
                                MainActivity.mainActivity.recreate();
                            }
                            catch (Exception e2) {
                                e2.printStackTrace();
                            }

                            break;
                        }
                    }
                }
            }



            //Help
            else if (((cmd.length() > 3 && cmd.startsWith("help")) || (cmd.length() > 0 && cmd.startsWith("?"))) && splitCmd.length <= 2) {
                ArrayList<String> helps = new ArrayList<>(Arrays.asList(getString(R.string.help_delete), getString(R.string.help_reset), getString(R.string.help_help), getString(R.string.help_set),
                        getString(R.string.help_get), getString(R.string.help_copy), getString(R.string.help_share), getString(R.string.help_mode),
                        getString(R.string.help_recreate), getString(R.string.help_themes), getString(R.string.help_sym)));
                ArrayList<String> commands = new ArrayList<>(Arrays.asList("delete", "reset", "help", "set", "get", "copy", "share", "mode", "recreate", "themes", "sym"));

                if (cmd.endsWith(" ") && cmd.length() > 1)
                    Ax.newTrim(cmd, 1);

                if (splitCmd.length == 2 && splitCmd[1].equals("debug") && Ax.isTinyColor("-mt") && (Ax.getTinyColor("-mt") == Color.parseColor("#010101") || Ax.getTinyColor("-mt") == Color.parseColor("#FEFEFE")))
                    printf(getString(R.string.help_debug));
                else if (cmd.equalsIgnoreCase("help") || cmd.equals("?"))
                    printf(getString(R.string.help_text));
                else if (splitCmd.length == 2 && commands.contains(splitCmd[1]))
                    printf(helps.get(commands.indexOf(splitCmd[1])));
            }




            //Debug Mode & Debug Last
            else if (splitCmd.length == 2 && splitCmd[0].equalsIgnoreCase("debug") && (splitCmd[1].equalsIgnoreCase("last") || Ax.isDigit(splitCmd[1]))) {
                String[] onOff = {"disabled", "enabled"};

                if (splitCmd[1].equalsIgnoreCase("last")) {
                    printf("\n\n ~ Debug Parameters ~ \n\n");
                    smolPrintf("buttonPresses: " + tinydb.getString("buttonPresses"));
                    smolPrintf("\n - numbers - \n" + tinydb.getString("numbers"));
                    smolPrintf(" - ops - \n" + tinydb.getString("ops"));
                }
                else if (splitCmd[1].equals("1") || splitCmd[1].equals("0")){
                    int mode = Integer.parseInt(splitCmd[1]);

                    printf("Debug mode " + onOff[mode]);
                    tinydb.putBoolean("debug", mode == 1);

                    MainActivity.mainActivity.recreate();
                }
            }




            //ModSymbol
            else if (splitCmd.length == 3 && (splitCmd[0].equals("symbol") || splitCmd[0].equals("sym")) && splitCmd[1].startsWith("-") && splitCmd[2].length() > 2
                    && splitCmd[2].startsWith("\"") && splitCmd[2].endsWith("\"")) {
                String newSymbol = splitCmd[2].replace("\"", "");

                tinydb.putString("modSymbol", newSymbol);
                smolPrintf(splitCmd[1].substring(1) + " symbol set to \"" + newSymbol + "\"");

                MainActivity.mainActivity.recreate();
            }




            //BMI
            else if (splitCmd.length == 3 && splitCmd[0].equalsIgnoreCase("bmi") && cmd.contains("'")) {
                double feet = Double.parseDouble(splitCmd[1].split("'")[0]);
                double inches = (Double.parseDouble(splitCmd[1].split("'")[1].replace("\"", "").replace("'", "")) / 12);
                double height = (feet + inches) * 0.3048;

                double weight = Double.parseDouble(splitCmd[2]) * 0.45359;

                double bmi = weight / Math.pow(height, 2.0);

                String health;
                DecimalFormat df = new DecimalFormat("#,###.##");

                if (bmi < 18.5)
                    health = "Underweight";
                else if (bmi <= 24.9)
                    health = "Healthy";
                else if (bmi <= 29.9)
                    health = "Overweight";
                else if (bmi <= 39)
                    health = "Obese";
                else
                    health = "Severely Obese";

                smolPrintf("BMI: " + df.format(bmi));
                smolPrintf("Your weight is: " + health);

                printf("A healthy BMI for adults ranges from 18.5 to 24.9");
            }




            //New EQ
            else if (splitCmd.length > 1 && (splitCmd[0].equalsIgnoreCase("newEq") || splitCmd[0].equalsIgnoreCase("newEquals")) && Ax.isDigit(splitCmd[1])) {
                try {
                    int option = Integer.parseInt(splitCmd[1]);

                    String[] toggleStates = {"disabled", "enabled"};

                    if (option == 0 || option == 1) {
                        tinydb.putBoolean("newEqToast", option == 1);
                        smolPrintf("newEq Toast " + toggleStates[option]);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }




            //Reset Buttons
            else if (cmd.equalsIgnoreCase("reset buttons")){
                try {
                    int a, b, c;

                    int testLength = 0;

                    for (a = 0; a < allButtons.length; a++) {
                        for (b = 0; b < allButtons[a].length; b++) {
                            testLength++;
                        }
                    }

                    testLength += 1;

                    String[] extraTexts = new String[testLength];

                    c = 0;

                    for (a = 0; a < allButtons.length; a++) {
                        for (b = 0; b < allButtons[a].length; b++) {
                            if (allButtons[a][b] != null)
                                extraTexts[c] = allButtons[a][b].getText().toString();

                            c++;
                        }
                    }

                    for (a = 0; a < extraTexts.length; a++) {
                        tinydb.putString("-b" + extraTexts[a], "\0");
                        tinydb.putString("-b" + extraTexts[a] + "t", "\0");
                    }

                    tinydb.putString("-bⁿ√", "\0");
                    tinydb.putString("-bⁿ√t", "\0");

                    printf("All individual button background and text colors have been reset.");

                    try {
                        MainActivity.mainActivity.recreate();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }




            //Themes
            else if (cmd.equals("themes")){
                int f;

                File directory = new File(this.getFilesDir(), "themes");
                File[] files = directory.listFiles();

                if (files != null) {
                    if (files.length > 0) {

                        printf("\nCurrently backed up themes:");
                        for (f = 0; f < files.length; f++) {
                            if (files[f].getName().endsWith(".txt"))
                                smolPrintf(" - " + files[f].getName());
                        }
                    }
                    else {
                        printf("\nError: No theme backups currently exist.");
                    }
                }
                else {
                    printf("\nError: No theme backups currently exist.");
                }
            }




            //isCustom
            else if (cmd.startsWith("isCustom")){
                if (cmd.equals("isCustom 0"))
                    tinydb.putBoolean("custom", false);
                else if (cmd.equals("isCustom 1"))
                    tinydb.putBoolean("custom", true);
            }





            //Copy
            else if (cmd.length() > 3 && cmd.startsWith("copy") && cmd.length() < 11){
                int c;
                String[] colors = new String[cmdCodes.length];
                String buttonCode = cmd.substring(5);
                String buttonText;
                boolean copied = false;

                String[] old = {"-e", "-bd", "-bm", "-bs", "-bp"};
                String[] updated = {"-b=t", "-b÷t", "-b×t", "-b-t", "-b+t"};

                if (cmd.endsWith(" "))
                    cmd = Ax.newTrim(cmd, 1);
                else if (buttonCode.startsWith(" "))
                    buttonCode = buttonCode.substring(1);

                for (c=0; c < cmdCodes.length; c++){
                    colors[c] = tinydb.getString(cmdKeys[c]);
                }

                for (c=0; c < old.length; c++) {
                    if (buttonCode.equals(old[c])) {
                        buttonCode = updated[c];
                        cmd = Ax.newTrim(cmd, old[c].length()) + updated[c];
                        break;
                    }
                }

                if (!cmd.endsWith("-b-t")) {
                    for (c = 0; c < cmdCodes.length; c++) {
                        if (cmd.endsWith(cmdCodes[c])) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("copyColor", colors[c]);
                            clipboard.setPrimaryClip(clip);

                            printf("Copied " + cmdColorWords[c] + " button color (" + colors[c] + ") to clipboard");

                            copied = true;
                            break;
                        }
                    }
                }

                if (!copied && Ax.isButtonCode(buttonCode) && Ax.isColor(tinydb.getString(buttonCode))){
                    String color = tinydb.getString(buttonCode);

                    Log.d("buttonCode and color", buttonCode + "    " + color);

                    if (!buttonCode.equals("\0") && !buttonCode.equals("")) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("copyColor", color);
                        clipboard.setPrimaryClip(clip);

                        if (buttonCode.endsWith("t") && !buttonCode.endsWith("ot")) {
                            buttonText = Ax.newTrim(buttonCode.substring(2), 1);

                            printf("Copied " + buttonText + " button text color (" + color + ") to clipboard");
                        }
                        else {
                            buttonText = buttonCode.substring(2);

                            printf("Copied " + buttonText + " button color (" + color + ") to clipboard");
                        }
                    }
                    else
                        printf("Error: No color found.\nEither this color has not been set, or an incorrect button code was entered.\n");
                }
                else if (!copied){
                    printf("Error: No color found.\nEither this color has not been set, or an incorrect button code was entered.\n");
                }
            }





            //Recreate
            else if (cmd.length() > 7 && cmd.contains("recreate")) {
                printf("MainActivity recreated");

                tinydb.putBoolean("closeDrawer", true);

                try {
                    MainActivity.mainActivity.recreate();
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
            }




            //Throw Exception
            else if (splitCmd[0].equalsIgnoreCase("debug") && splitCmd[1].equalsIgnoreCase("throw")) {
                try {
                    throw new Exception("Test exception manually thrown using the terminal");
                }
                catch (Exception e) {
                    Ax.saveStack(e);
                }
            }




            //StackTrace
            else if (cmd.length() > 7 && (cmd.contains("printStackTrace") || cmd.contains("debug stack") || cmd.contains("debug crash") || cmd.contains("debug anr"))) {
                printf(tinydb.getString("stackTrace"));
            }




            //Reason
            else if (cmd.length() > 9 && cmd.contains("debug reason")) {
                printf(tinydb.getString("reason"));
            }




            //Gradient Colors
            else if (cmd.length() > 10 && cmd.startsWith("gradient ")) {
                if (cmd.contains("-m")) {
                    String gradHex = Ax.getLast(cmd, 7);

                    if (Ax.isColor(gradHex)) {
                        if (Ax.newTrim(cmd, 8).endsWith("start")) {
                            tinydb.putString("gradStartMain", gradHex);

                            smolPrintf("\n\nGradient start color set to " + gradHex);
                            MainActivity.mainActivity.recreate();
                        }
                        else if (Ax.newTrim(cmd, 8).endsWith("end")) {
                            tinydb.putString("gradEndMain", gradHex);

                            smolPrintf("\n\nGradient end color set to " + gradHex);
                            MainActivity.mainActivity.recreate();
                        }
                    }
                    else if (cmd.contains("direction")) {
                        if (cmd.contains("horizontal") || cmd.contains("rtl") || cmd.contains("ltr") || cmd.contains("right") || cmd.contains("left")) {
                            tinydb.putString("gradDirectMain", "horizontal");

                            smolPrintf("\n\nGradient direction set to \"Horizontal\"");
                            MainActivity.mainActivity.recreate();
                        }
                        else if (cmd.contains("vertical") || cmd.contains("up") || cmd.contains("down")) {
                            tinydb.putString("gradDirectMain", "vertical");

                            smolPrintf("\n\nGradient direction set to \"Vertical\"");
                            MainActivity.mainActivity.recreate();
                        }
                    }
                    else if (cmd.endsWith("enable") || cmd.endsWith("1")) {
                        tinydb.putBoolean("isGradMain", true);

                        smolPrintf("\n\nMain background gradient enabled");
                        MainActivity.mainActivity.recreate();
                    }
                    else if (cmd.endsWith("disable") || cmd.endsWith("0")) {
                        tinydb.putBoolean("isGradMain", false);

                        smolPrintf("\n\nMain background gradient disabled");
                        MainActivity.mainActivity.recreate();
                    }
                    else if (cmd.endsWith("get")) {
                        smolPrintf("\n\nGradient starts at " + tinydb.getString("gradStartMain") + " (" + Ax.getTinyColor("gradStartMain") + ") and ends at " + tinydb.getString("gradEndMain") + " (" + Ax.getTinyColor("gradEndMain") + ")");
                    }
                }
            }




            //Terminal Shortcut
            else if (cmd.startsWith("debug shortcut") || cmd.startsWith("debug sc")) {
                if (cmd.endsWith("1")) {
                    tinydb.putBoolean("termShortcut", true);

                    smolPrintf("\n\nTerminal shortcut enabled. Please long-press the 0 key on the Home layout to open the terminal");
                    MainActivity.mainActivity.recreate();
                }
                else if (cmd.endsWith("0")) {
                    tinydb.putBoolean("termShortcut", false);

                    smolPrintf("\n\nTerminal shortcut disabled");
                    MainActivity.mainActivity.recreate();
                }
            }





            //Get
            else if (cmd.length() > 2 && cmd.startsWith("get") && cmd.length() < 14) {
                //Primary
                if (cmd.length() > 5 && cmd.endsWith("-p")) {
                    cPrimary = tinydb.getString("cPrimary");

                    if (cPrimary != null && !cPrimary.equals("\0")) {
                        printf("Current primary color is: " + cPrimary);
                    }
                    else {
                        printf("Primary color not set");
                    }
                }
                //Secondary
                else if (cmd.length() > 5 && cmd.endsWith("-s")) {
                    cSecondary = tinydb.getString("cSecondary");

                    if (cSecondary != null && !cSecondary.equals("\0")) {
                        printf("Current secondary color is: " + cSecondary);
                    }
                    else {
                        printf("Secondary color not set");
                    }
                }
                //Tertiary
                else if (cmd.length() > 5 && cmd.endsWith("-t")) {
                    cTertiary = tinydb.getString("cTertiary");

                    if (cTertiary != null && !cTertiary.equals("\0")) {
                        printf("Current tertiary color is: " + cTertiary);
                    }
                    else {
                        printf("Tertiary color not set");
                    }
                }
                //Keypad
                else if (cmd.length() > 5 && cmd.endsWith("-k")) {
                    cKeypad = tinydb.getString("cKeypad");

                    if (cKeypad != null && !cKeypad.equals("\0")) {
                        printf("Current keypad color is: " + cKeypad);
                    }
                    else {
                        printf("Keypad color not set");
                    }
                }
                //Main
                else if (cmd.length() > 5 && cmd.endsWith("-m")) {
                    cMain = tinydb.getString("cMain");

                    if (cMain != null && !cMain.equals("\0")) {
                        printf("Current main background color is: " + cMain);
                    }
                    else {
                        printf("Main background color not set");
                    }
                }
                //Equals
                else if (cmd.length() > 5 && cmd.endsWith("-e")) {
                    cbEquals = tinydb.getString("-b=t");

                    if (cbEquals != null && !cbEquals.equals("\0")) {
                        printf("Current equals button color is: " + cbEquals);
                    }
                    else {
                        printf("Equals button text color not set");
                    }
                }
                //Fab
                else if (cmd.length() > 5 && cmd.endsWith("-f")) {
                    cFab = tinydb.getString("cFab");

                    if (cFab != null && !cFab.equals("\0")) {
                        printf("Current delete button color is: " + cFab);
                    }
                    else {
                        printf("Delete button color not set");
                    }
                }
                //FabText
                else if (cmd.length() > 6 && cmd.endsWith("-ft")) {
                    cFabText = tinydb.getString("cFabText");

                    if (cFabText != null && !cFabText.equals("\0")) {
                        printf("Current delete button text color is: " + cFabText);
                    }
                    else {
                        printf("Delete button text color not set");
                    }
                }
                //Top Bar
                else if (cmd.length() > 6 && cmd.endsWith("-tt")) {
                    cTop = tinydb.getString("cTop");

                    if (cTop != null && !cTop.equals("\0")) {
                        printf("Current top bar text color is: " + cTop);
                    }
                    else {
                        printf("Top bar text color not set");
                    }
                }
                //Plus
                else if (cmd.length() > 6 && cmd.endsWith("-bp")) {
                    cPlus = tinydb.getString("-b+t");

                    if (cPlus != null && !cPlus.equals("\0")) {
                        printf("Current plus button text color is: " + cPlus);
                    }
                    else {
                        printf("Plus button text color not set");
                    }
                }
                //Minus
                else if (cmd.length() > 6 && cmd.endsWith("-bs")) {
                    cMinus = tinydb.getString("-b-t");

                    if (cMinus != null && !cMinus.equals("\0")) {
                        printf("Current minus button text color is: " + cMinus);
                    }
                    else {
                        printf("Minus button text color not set");
                    }
                }
                //Multi
                else if (cmd.length() > 6 && cmd.endsWith("-bm")) {
                    cMulti = tinydb.getString("-b×t");

                    if (cMulti != null && !cMulti.equals("\0")) {
                        printf("Current multiply button text color is: " + cMulti);
                    }
                    else {
                        printf("Multiply button text color not set");
                    }
                }
                //Div
                else if (cmd.length() > 6 && cmd.endsWith("-bd")) {
                    cDiv = tinydb.getString("-b÷t");

                    if (cDiv != null && !cDiv.equals("\0")) {
                        printf("Current divide button text color is: " + cDiv);
                    }
                    else {
                        printf("Divide button text color not set");
                    }
                }
                //KeyText
                else if (cmd.length() > 6 && cmd.endsWith("-kt")) {
                    cNum = tinydb.getString("cNum");

                    if (cNum != null && !cNum.equals("\0")) {
                        printf("Current keypad text color is: " + cNum);
                    }
                    else {
                        printf("Keypad text color not set");
                    }
                }
                //All
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-a")) {
                    cPrimary = tinydb.getString("cPrimary");
                    cSecondary = tinydb.getString("cSecondary");
                    cTertiary = tinydb.getString("cTertiary");
                    cbEquals = tinydb.getString("-b=t");
                    cPlus = tinydb.getString("-b+t");
                    cMinus = tinydb.getString("-b-t");
                    cMulti = tinydb.getString("-b×t");
                    cDiv = tinydb.getString("-b÷t");
                    cKeypad = tinydb.getString("cKeypad");
                    cMain = tinydb.getString("cMain");
                    cTop = tinydb.getString("cTop");
                    cNum = tinydb.getString("cNum");
                    cFab = tinydb.getString("cFab");
                    cFabText = tinydb.getString("cFabText");

                    if (cFabText.equals("#reset0")) {
                        cFabText = "\0";
                        tinydb.putString("cFabText", "\0");
                    }

                    printf("\n\nCurrent primary color is: " + cPrimary + "\nCurrent secondary color is: " + cSecondary + "\nCurrent tertiary color is: " + cTertiary + "\n\nCurrent equals button text color is: " + cbEquals + "\nCurrent delete button color is: " + cFab + "\nCurrent plus button text color is: " + cPlus + "\nCurrent minus button text color is: " + cMinus + "\nCurrent multiply button text color is: " + cMulti + "\nCurrent divide button text color is: " + cDiv + "\n\nCurrent main background color is: " + cMain + "\nCurrent keypad color is: " + cKeypad + "\n\nCurrent top bar text color is: " + cTop + "\nCurrent keypad text color is: " + cNum + "\nCurrent delete button icon color is: " + cFabText);
                }
                else if (cmd.substring(4).startsWith("-b")) {
                    boolean isReset = false;

                    String buttonText;
                    String buttonCode = cmd.substring(4);
                    String color = tinydb.getString(buttonCode);

                    if (buttonCode.endsWith("t") && !buttonCode.endsWith("ot")) {
                        buttonText = Ax.newTrim(buttonCode.substring(2), 1);
                        if (Ax.buttonExists(buttonText)) {
                            if (Ax.isColor(color)) {
                                printf("Current " + buttonText + " button text color is " + color);
                            }
                            else {
                                printf(buttonText + " button text color not set");
                            }
                        }
                        else{
                            printf("Error: Unknown button code.\nTry running the \"help set\" or \"help get\" commands.");
                        }
                    }
                    else {
                        buttonText = buttonCode.substring(2);

                        if (Ax.buttonExists(buttonText)) {
                            if (Ax.isColor(color)) {
                                printf("Current " + buttonText + " button color is " + color);
                            }
                            else {
                                printf(buttonText + " button color not set");
                            }
                        }
                        else{
                            printf("Error: Unknown button code.\nTry running the \"help set\" or \"help get\" commands.");
                        }
                    }

                    if (!isAll && (isHex || isReset)) {
                        try {
                            MainActivity.mainActivity.recreate();
                        }
                        catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    printf(getError);
                }
            }




            else if (cmd.startsWith("sampleCalc ") && cmd.length() > 11) {
                int i;
                String k1 = "", k2 = "";

                for (i=11; i < cmd.length(); i++) {
                    if (Ax.chat(cmd, i).equals(" ")) {
                        k1 = cmd.substring(11, i);
                        k2 = cmd.substring(i + 1);
                    }
                }

                sampleCalc(k1, k2);
            }
        }

        try {
            if (!isAll) {
                final ScrollView stdoutScroll = findViewById(R.id.outputScroll);
                stdoutScroll.post(new Runnable() {
                    @Override
                    public void run() {
                        stdoutScroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    public final void printf(String output){
        try {
            TextView stdout = findViewById(R.id.cmdOut);

            stdout.append("\n\n" + output);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    public final void smolPrintf(String output){
        try {
            TextView stdout = findViewById(R.id.cmdOut);

            stdout.append("\n" + output);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public final void writeTheme(Context mcoContext, String body, String filename){
        File dir = new File(mcoContext.getFilesDir(), "themes");
        if(!dir.exists()){
            dir.mkdir();
        }

        try {
            File gpxfile = new File(dir, filename);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(body);
            writer.flush();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public final void openColorPicker(final int key) {
        final TinyDB tinydb = new TinyDB(this);

        ColorPickerDialog colorPickerDialog = null;

        colorPickerDialog = ColorPickerDialog.createColorPickerDialog(this,ColorPickerDialog.DARK_THEME);

        colorPickerDialog.show();
        colorPickerDialog.hideOpacityBar();

        colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
            @Override
            public void onColorPicked(int color, String hexVal) {
                String buttonCode = Ax.newTrim(cmd, 4).substring(4);

                if (hexVal != null && !hexVal.equals("\0")) {
                    if (hexVal.length() > 0) {
                        hexVal = "#" + hexVal.substring(3);
                    }

                    if (hexVal.length() > 7)
                        hexVal = "#" + Ax.getLast(hexVal, 6);
                    else if (hexVal.length() < 7)
                        hexVal = "#FFFFFF";

                    hexVal = Ax.colorToUpper(hexVal);

                    if (key < 15) {
                        if (Ax.isColor(hexVal))
                            tinydb.putString(cmdKeys[key], hexVal);

                        printf(cmdColorWords[i] + " color set to " + tinydb.getString(cmdKeys[i]));
                    }
                    else if (key == 15){
                        if (Ax.isColor(hexVal))
                            tinydb.putString(buttonCode, hexVal);

                        if (buttonCode.endsWith("t") && !buttonCode.endsWith("ot"))
                            printf("Button " + Ax.newTrim(buttonCode.substring(2), 1) + " color set to " + hexVal);
                        else
                            printf("Button " + buttonCode.substring(2) + " color set to " + hexVal);
                    }

                    try {
                        MainActivity.mainActivity.recreate();
                    }
                    catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);

        Ax Ax = new Ax();

        TextView cmdOut = findViewById(R.id.cmdOut);

        //Ints
        outState.putInt("i", i);
        outState.putInt("j", j);
        outState.putInt("k", k);

        //Strings
        if (!Ax.isNull(cmdOut))
            outState.putString("cmdOutText", cmdOut.getText().toString());

        //Booleans

        //Doubles

        //Longs

        //Arrays

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");
        String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");

        TextView cmdOut = findViewById(R.id.cmdOut);

        String cmdOutText = savedInstanceState.getString("cmdOutText");

        cmdOut.setText(cmdOutText);

        i = savedInstanceState.getInt("i");
        j = savedInstanceState.getInt("j");
        k = savedInstanceState.getInt("k");
    }

    public void cycleSymbols(View v){
        int a, b, c;
        int testLength = 0;

        EditText input = findViewById(R.id.cmdInput);

        for (a=0; a < smolButtons.length; a++) {
            for (b = 0; b < smolButtons[a].length; b++) {
                testLength++;
            }
        }

        testLength += 1;

        String[] extraTexts = new String[testLength];

        c = 0;

        for (a=0; a < smolButtons.length; a++) {
            for (b = 0; b < smolButtons[a].length; b++) {
                if (smolButtons[a][b] != null)
                    extraTexts[c] = smolButtons[a][b].getText().toString();

                if (extraTexts[c].equals("e"))
                    extraTexts[c] = "ⁿ√";

                c++;
            }
        }

        String currentText = input.getText().toString();

        if (!cycleStarted){
            cycleStarted = true;

            input.setText(currentText + extraTexts[cycleIndex]);
            cycleIndex++;
        }
        else{
            if (cycleIndex > 0 && currentText.endsWith(extraTexts[cycleIndex - 1])) {
                if (cycleIndex == extraTexts.length || extraTexts[cycleIndex] == null) {
                    currentText = currentText.replace(extraTexts[cycleIndex - 1], "");
                    input.setText(currentText);
                    cycleIndex = 0;
                    cycleStarted = false;
                }
                else {
                    currentText = currentText.replace(extraTexts[cycleIndex - 1], extraTexts[cycleIndex]);
                    input.setText(currentText);
                    cycleIndex++;
                }
            }
            else {
                cycleIndex = 0;
                input.setText(currentText + extraTexts[cycleIndex]);
                cycleIndex++;
            }
        }
    }

    public void sampleCalc(String k1, String k2) {
        int keyInt, key2Int = 0, n1, n2, up, down, minor;
        String majMin, key, key2;

        String[] keys = {"A", "Bb", "B", "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab"};

        if (k1.contains("#") && (k1.contains("b") && !k1.startsWith("b "))) {
            key = k1.substring(0, 2);
            majMin = k1.substring(2);
        }
        else {
            key = Ax.chat(k1, 0);
            majMin = k1.substring(1);
        }

        if (key == null)
            key = "A";

        if (key.equals("A") || key.equals("a")) {
            keyInt = 1;
        }
        else if (key.equals("A#") || key.equals("a#") || key.equals("Bb") || key.equals("bb")) {
            keyInt = 2;
        }
        else if (key.equals("B") || key.equals("b")) {
            keyInt = 3;
        }
        else if (key.equals("Cb") || key.equals("cb")) {
            keyInt = 3;
        }
        else if (key.equals("C") || key.equals("c") || key.equals("B#") || key.equals("b#")) {
            keyInt = 4;
        }
        else if (key.equals("C#") || key.equals("c#") || key.equals("Db") || key.equals("db")) {
            keyInt = 5;
        }
        else if (key.equals("D") || key.equals("d")) {
            keyInt = 6;
        }
        else if (key.equals("D#") || key.equals("d#") || key.equals("Eb") || key.equals("eb")) {
            keyInt = 7;
        }
        else if (key.equals("E") || key.equals("e") || key.equals("Fb") || key.equals("fb")) {
            keyInt = 8;
        }
        else if (key.equals("F") || key.equals("f") || key.equals("E#") || key.equals("e#")) {
            keyInt = 9;
        }
        else if (key.equals("F#") || key.equals("f#") || key.equals("Gb") || key.equals("gb")) {
            keyInt = 10;
        }
        else if (key.equals("G") || key.equals("g")) {
            keyInt = 11;
        }
        else if (key.equals("G#") || key.equals("g#") || key.equals("Ab") || key.equals("ab")) {
            keyInt = 12;
        }
        else {
            printf("\nError: The first key you entered could not be understood. Please check your inputs, and try again.\n");
            return;
        }

        //Major Key
        if (majMin.equals("major") || majMin.equals("Major") || majMin.equals("maj") || majMin.equals("Maj")) {
            minor = keyInt - 3;

            if (minor < 1) {
                minor = minor + 12;
            }

            n1 = minor;

            printf("The relative minor of your first key is " + keys[n1 - 1] + " minor.");
        }

        //Minor Key
        else if (majMin.equals("minor") || majMin.equals("Minor") || majMin.equals("min") || majMin.equals("Min")) {
            keyInt = keyInt + 3;

            if (keyInt > 12) {
                keyInt = keyInt - 12;
            }

            n1 = keyInt;

            printf("The relative major of your first key is " + keys[n1 - 1] + " major.");
        }

        if (k2.contains("#") || (k2.contains("b") && !k2.startsWith("b "))) {
            key2 = k2.substring(0, 2);
            majMin = k2.substring(2);
        }
        else {
            key2 = Ax.chat(k2, 0);
            majMin = k2.substring(1);
        }

        if (key2 == null)
            key2 = "A";

        if (key2.equalsIgnoreCase("A")) {
            key2Int = 1;
        }
        else if (key2.equals("A#") || key2.equals("a#") || key2.equalsIgnoreCase("Bb")) {
            key2Int = 2;
        }
        else if (key2.equalsIgnoreCase("B") || key2.equalsIgnoreCase("Cb")) {
            key2Int = 3;
        }
        else if (key2.equalsIgnoreCase("C") || key2.equals("B#") || key2.equals("b#")) {
            key2Int = 4;
        }
        else if (key2.equals("C#") || key2.equals("c#") || key2.equalsIgnoreCase("Db")) {
            key2Int = 5;
        }
        else if (key2.equalsIgnoreCase("D")) {
            key2Int = 6;
        }
        else if (key2.equals("D#") || key2.equals("d#") || key2.equalsIgnoreCase("Eb")) {
            key2Int = 7;
        }
        else if (key2.equals("E") || key2.equals("e") || key2.equals("Fb") || key2.equals("fb")) {
            key2Int = 8;
        }
        else if (key2.equals("F") || key2.equals("f") || key2.equals("E#") || key2.equals("e#")) {
            key2Int = 9;
        }
        else if (key2.equals("F#") || key2.equals("f#") || key2.equals("Gb") || key2.equals("gb")) {
            key2Int = 10;
        }
        else if (key2.equals("G") || key2.equals("g")) {
            key2Int = 11;
        }
        else if (key2.equals("G#") || key2.equals("g#") || key2.equals("Ab") || key2.equals("ab")) {
            key2Int = 12;
        }
        else {
            printf("\nError: The second key you entered could not be understood. Please check your inputs, and try again.\n");
        }

        //Major Key
        if (majMin.equalsIgnoreCase("major") || majMin.equalsIgnoreCase("maj")) {
            minor = key2Int - 3;

            if (minor < 1) {
                minor = minor + 12;
            }

            n2 = minor;

            printf("\nThe relative minor of your second key is " + keys[n2 - 1] + " minor.");
        }

        //Minor Key
        else if (majMin.equalsIgnoreCase("minor") || majMin.equalsIgnoreCase("min")) {
            key2Int = key2Int + 3;

            if (key2Int > 12) {
                key2Int = key2Int - 12;
            }

            n2 = key2Int;

            printf("\nThe relative major of your second key is " + keys[n2 - 1] + " major.");
        }

        if (keyInt != key2Int) {
            if (keyInt > key2Int) {
                down = keyInt - key2Int;
                up = 12 - down;
            }
            else {
                up = key2Int - keyInt;
                down = 12 - up;
            }

            printf("\nYou can either raise the first key by " + up + " semitones, or lower it by " + down + " semitones, to get to your second key.\n");
        }
        else {
            printf("\n\nThese two keys are the same.\n");
        }
    }
}

