package com.mirambeau.termcalc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ImportActivity extends AppCompatActivity {
    String themeName;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_import);

            // Get intent, action and MIME type
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();

            if (type != null && (type.startsWith("text/") || (type.startsWith("application/t") && type.endsWith("xt")))) {
                if (action != null && (action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_SEND))) {
                    try {
                        handleSendText(intent); // Handle text being sent
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                    try {
                        handleSendMultipleFiles(intent); // Handle multiple text files being sent
                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
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
    public void handleSendText(Intent intent) throws IOException {
        if (intent != null) {
            Uri textUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

            if (textUri != null)
                process(textUri);
        }
    }

    public void handleSendMultipleFiles(Intent intent) throws IOException {
        ArrayList<Uri> textUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (textUris != null) {
            for (Uri theme : textUris)
                process(theme);
        }
    }

    public void process(Uri inputTheme) throws IOException {
        if (inputTheme != null) {
            int i;

            themeName = "temp-" + System.currentTimeMillis();
            String filename;

            String line;

            InputStreamReader isr = new InputStreamReader(getContentResolver().openInputStream(inputTheme));
            BufferedReader br = new BufferedReader(isr);

            for (i=0; i < 200; i++) {
                line = br.readLine();

                if (line != null) {
                    if (line.startsWith("name:")) {
                        themeName = line.substring(5);
                        break;
                    }
                }
                else
                    break;
            }

            filename = themeName + ".txt";

            isr = new InputStreamReader(getContentResolver().openInputStream(inputTheme));
            br = new BufferedReader(isr);

            File dir = new File(this.getFilesDir(), "themes");

            if (!dir.exists()){
                dir.mkdir();
            }

            try {
                File theme = new File(dir, filename);
                FileWriter writer = new FileWriter(theme);

                for (i=0; i < 200; i++) {
                    line = br.readLine();

                    if (line != null) {
                        line += "\n";

                        writer.append(line);
                    }
                    else
                        break;
                }

                writer.flush();
                writer.close();

                //run("restore " + themeName);

                try {
                    Toast.makeText(ImportActivity.this, "Successfully imported \"" + themeName + "\"", Toast.LENGTH_SHORT).show();

                    Intent homeIntent = new Intent(ImportActivity.this, Backups.class);
                    startActivity(homeIntent);
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    boolean isRestore = false, isAll = false, isHex = false, ftIsSecondary = false;
    String cmd;

    final Activity main = MainActivity.mainActivity;

    final Button[] nums = {main.findViewById(R.id.b0), main.findViewById(R.id.b1), main.findViewById(R.id.b2), main.findViewById(R.id.b3), main.findViewById(R.id.b4), main.findViewById(R.id.b5), main.findViewById(R.id.b6), main.findViewById(R.id.b7), main.findViewById(R.id.b8), main.findViewById(R.id.b9)};
    final Button[] compBar = {main.findViewById(R.id.bSqrt), main.findViewById(R.id.bExp), main.findViewById(R.id.bFact), main.findViewById(R.id.bPi), main.findViewById(R.id.bE), main.findViewById(R.id.bLog), main.findViewById(R.id.bLn)};
    final Button[] trigBar = {main.findViewById(R.id.bSin), main.findViewById(R.id.bCos), main.findViewById(R.id.bTan), main.findViewById(R.id.bCsc), main.findViewById(R.id.bSec), main.findViewById(R.id.bCot), main.findViewById(R.id.bInv)};
    final Button[] mainOps = {main.findViewById(R.id.sPlus), main.findViewById(R.id.sMinus), main.findViewById(R.id.sMulti), main.findViewById(R.id.sDiv)};

    final Button bDec = main.findViewById(R.id.bDec);
    final Button bParenthesisOpen = main.findViewById(R.id.bParenthesisOpen);
    final Button bParenthesisClose = main.findViewById(R.id.bParenthesisClose);
    final Button bEquals = main.findViewById(R.id.bEquals);
    final Button bMod = main.findViewById(R.id.bMod);

    final Button[][] allButtons = {nums, compBar, trigBar, mainOps, {bDec, bParenthesisOpen, bParenthesisClose, bEquals, bMod}};

    @SuppressLint("SetTextI18n")
    public String run(String cmd) throws IOException {
        TinyDB tinydb = new TinyDB(this);

        String setError;
        String hex = "none";
        String output = "";

        String cPrimary, cSecondary, cTertiary, cbEquals, cPlus, cMinus, cMulti, cDiv, cKeypad, cMain, cTop, cNum, cFab, cFabText = null;

        int i, k;

        if (isRestore)
            setError = "Error: Invalid hex code";
        else
            setError = "\nUsages:\n• set <button code> <hex code>\n• set <button code> gui\n• set <button code> #reset0\n\nType \"help set\" or \"help get\" for more information about button codes, and how these commands work.";

        if (!cmd.equals("\0")) {
            Log.d("cmd", cmd);
        }

        if (!cmd.equals("\0")) {
            this.cmd = cmd;

            if (cmd.endsWith(" ") && cmd.length() > 1)
                cmd = Aux.newTrim(cmd, 1);

            //Set color
            if (cmd.length() > 2 && cmd.startsWith("set") || (cmd.startsWith("mode ") && cmd.length() == 6)) {
                if (cmd.length() > 8) {
                    hex = Aux.getLast(cmd, 7);

                    if (!cmd.contains("mode") && !cmd.contains("reset0")) {
                        hex = Aux.colorToUpper(hex);
                    }

                    isHex = Aux.isColor(hex);

                    if (cmd.contains("-ft") && hex.substring(1).equalsIgnoreCase(tinydb.getString("cSecondary").substring(1))) {
                        hex = Aux.hexAdd(hex, -1);
                        ftIsSecondary = true;
                    }
                }
                else
                    isHex = false;

                String cmdEnd = cmd.substring(4);

                //Buttons
                if (cmd.length() > 12 && (cmd.substring(4).startsWith("buttons") || cmd.substring(4).startsWith("-buttons")) && cmd.endsWith("#reset0")){
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

                    Log.d("printf", "All individual button background and text colors have been reset.");

                    MainActivity.mainActivity.recreate();
                }
                //Mode
                else if ((cmd.length() == 6 && Aux.newTrim(cmd, 1).equals("mode ")) || (cmd.length() == 7 && Aux.newTrim(cmd, 1).equals("theme "))){
                    if (Aux.isDigit(Aux.lastChar(cmd)) && (Aux.getLast(Aux.newTrim(cmd, 1), 5).equals("mode ") || Aux.getLast(Aux.newTrim(cmd, 1), 6).equals("theme "))){
                        if (Integer.parseInt(Aux.lastChar(cmd)) < 6 && Integer.parseInt(Aux.lastChar(cmd)) > 0) {
                            String newTheme = Aux.lastChar(cmd);
                            String[] themeNames = {"Dark", "Light", "AMOLED Black (Colored Buttons)", "AMOLED Black (Black Buttons)", "Monochrome"};
                            int themeInt = Integer.parseInt(newTheme);

                            tinydb.putString("theme", newTheme);
                            Log.d("printf", "Theme set to " + themeNames[themeInt - 1]);

                            MainActivity.mainActivity.recreate();
                        }
                        else {
                            Log.d("printf", "Error: There is no base theme associated with that number. Please enter a number from 1-5.");
                        }
                    }
                    else {
                        Log.d("printf", "Error: There is no base theme associated with that number. Please enter a number from 1-5.");
                    }
                }
                //Plus
                else if (cmd.length() > 6 && cmd.substring(4, 7).equals("-bp")) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            Log.d("printf", "Plus button text color set to " + hex);
                            cPlus = hex;
                            tinydb.putString("-b+t", cPlus);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (hex.equals("#reset0")){
                            Log.d("printf", "Plus button text color reset");
                            cPlus = "\0";
                            tinydb.putString("-b+t", cPlus);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                    else {
                        Log.d("printf", setError);
                    }
                }
                //Minus
                else if (cmd.length() > 6 && cmd.substring(4, 7).equals("-bs") && !cmd.substring(4, 9).equals("-bsin")) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            Log.d("printf", "Minus button text color set to " + hex);
                            cMinus = hex;
                            tinydb.putString("-b-t", cMinus);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (hex.equals("#reset0")){
                            Log.d("printf", "Minus button text color reset");
                            cMinus = "\0";
                            tinydb.putString("-b-t", cMinus);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                    else {
                        Log.d("printf", setError);
                    }
                }
                //Multi
                else if (cmd.length() > 6 && cmd.substring(4, 7).equals("-bm")) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            Log.d("printf", "Multiply button text color set to " + hex);
                            cMulti = hex;
                            tinydb.putString("-b×t", cMulti);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")){
                            Log.d("printf", "Multiply button text color reset");
                            cMulti = "\0";
                            tinydb.putString("-b×t", cMulti);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                    else {
                        Log.d("printf", setError);
                    }
                }
                //Div
                else if (cmd.length() > 6 && cmd.substring(4, 7).equals("-bd")) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            Log.d("printf", "Divide button color set to " + hex);
                            cDiv = hex;
                            tinydb.putString("-b÷t", cDiv);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")){
                            Log.d("printf", "Divide button color reset");
                            cDiv = "\0";
                            tinydb.putString("-b÷t", cDiv);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                    else {
                        Log.d("printf", setError);
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
                        if (Aux.chat(cmdEnd, c).equals(" "))
                            break;

                        codeLength++;
                    }

                    String buttonCode = cmdEnd.substring(0, codeLength);
                    String buttonText;

                    if (buttonCode.endsWith("t") && !buttonCode.endsWith("ot")) {
                        buttonText = Aux.newTrim(buttonCode.substring(2), 1);

                        if (Aux.buttonExists(buttonText)) {
                            if (isHex) {
                                Log.d("printf", "Button " + buttonText + " text color set to " + hex);
                                tinydb.putString(buttonCode, hex);
                            }
                            else if (isReset) {
                                Log.d("printf", "Button " + buttonText + " text color reset");
                                tinydb.putString(buttonCode, "\0");
                            }
                        }
                        else{
                            Log.d("printf", "Error: Unknown button code.\nTry running the \"help set\" or \"help get\" commands.");
                        }
                    }
                    else {
                        buttonText = buttonCode.substring(2);

                        if (Aux.buttonExists(buttonText)) {
                            if (isHex) {
                                Log.d("printf", "Button " + buttonText + " color set to " + hex);
                                tinydb.putString(buttonCode, hex);
                            }
                            else if (isReset) {
                                Log.d("printf", "Button " + buttonText + " color reset");
                                tinydb.putString(buttonCode, "\0");
                            }
                        }
                        else{
                            Log.d("printf", "Error: Unknown button code.\nTry running the \"help set\" or \"help get\" commands.");
                        }
                    }

                    if (!isAll && (isHex || isReset)) {
                        MainActivity.mainActivity.recreate();
                    }
                }
                //Top Bar Text Color
                else if (cmd.length() > 6 && cmd.substring(4, 7).equals("-tt")) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            Log.d("printf", "Top bar text color set to " + hex);
                            cTop = hex;
                            tinydb.putString("cTop", cTop);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")){
                            Log.d("printf", "Top bar text color reset");
                            cTop = "\0";
                            tinydb.putString("cTop", cTop);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                    else {
                        Log.d("printf", setError);
                    }
                }
                //Keypad Text Color
                else if (cmd.length() > 6 && cmd.substring(4, 7).equals("-kt")) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            Log.d("printf", "Keypad text color set to " + hex);
                            cNum = hex;
                            tinydb.putString("cNum", cNum);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")){
                            Log.d("printf", "Keypad text color reset");
                            cNum = "\0";
                            tinydb.putString("cNum", cNum);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                    else {
                        Log.d("printf", setError);
                    }
                }
                //Fab Icon Color
                else if (cmd.length() > 6 && cmd.substring(4, 7).equals("-ft")) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            String newHex;

                            if (ftIsSecondary)
                                newHex = Aux.hexAdd(hex, 1);
                            else
                                newHex = hex;

                            Log.d("printf", "Delete button icon color set to " + newHex);
                            cFabText = hex;
                            tinydb.putString("cFabText", cFabText);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")){
                            Log.d("printf", "Delete button icon color reset");
                            cFabText = "\0";
                            tinydb.putString("cFabText", "\0");

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }
                    }
                    else {
                        Log.d("printf", setError);
                    }

                    if (cFabText != null && cFabText.equals("#reset0")) {
                        cFabText = "\0";
                        tinydb.putString("cFabText", "\0");
                    }
                }
                //Fab Color
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-f")) {
                    if (cmd.length() > 8) {
                        if (isHex) {
                            Log.d("printf", "Delete button color set to " + hex);
                            cFab = hex;
                            tinydb.putString("cFab", cFab);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            Log.d("printf", "Delete button color reset");
                            cFab = "\0";
                            tinydb.putString("cFab", cFab);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                    else {
                        Log.d("printf", setError);
                    }
                }
                //Primary
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-p")) {
                    if (cmd.length() > 8 && cmd.substring(7).length() == 7 && cmd.charAt(7) == '#') {
                        if (isHex) {
                            Log.d("printf", "Primary color set to " + hex);
                            cPrimary = hex;
                            tinydb.putString("cPrimary", cPrimary);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            Log.d("printf", "Primary color reset");
                            cPrimary = "\0";
                            tinydb.putString("cPrimary", cPrimary);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                    else {
                        Log.d("printf", setError);
                    }
                }
                //Secondary
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-s")) {
                    if (cmd.length() > 8 && cmd.substring(7).length() == 7 && cmd.charAt(7) == '#') {
                        if (isHex) {
                            Log.d("printf", "Secondary color set to " + hex);
                            cSecondary = hex;
                            tinydb.putString("cSecondary", cSecondary);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            Log.d("printf", "Secondary color reset");
                            cSecondary = "\0";
                            tinydb.putString("cSecondary", cSecondary);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                    else {
                        Log.d("printf", setError);
                    }
                }
                //Tertiary
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-t")) {
                    if (cmd.length() > 8 && cmd.substring(7).length() == 7 && cmd.charAt(7) == '#') {
                        if (isHex) {
                            Log.d("printf", "Tertiary color set to " + hex);
                            cTertiary = hex;
                            tinydb.putString("cTertiary", cTertiary);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            Log.d("printf", "Tertiary color reset");
                            cTertiary = "\0";
                            tinydb.putString("cTertiary", cTertiary);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                    else {
                        Log.d("printf", setError);
                    }
                }
                //Keypad
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-k")) {
                    if (cmd.length() > 8 && cmd.substring(7).length() == 7 && cmd.charAt(7) == '#') {
                        if (isHex) {
                            Log.d("printf", "Keypad color set to " + hex);
                            cKeypad = hex;
                            tinydb.putString("cKeypad", cKeypad);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            Log.d("printf", "Keypad color reset");
                            cKeypad = "\0";
                            tinydb.putString("cKeypad", cKeypad);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                    else {
                        Log.d("printf", setError);
                    }
                }
                //Main Background
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-m")) {
                    if (cmd.length() > 8 && cmd.substring(7).length() == 7 && cmd.charAt(7) == '#') {
                        if (isHex) {
                            Log.d("printf", "Main background color set to " + hex);
                            cMain = hex;
                            tinydb.putString("cMain", cMain);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            Log.d("printf", "Main background color reset");
                            cMain = "\0";
                            tinydb.putString("cMain", cMain);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                    else {
                        Log.d("printf", setError);
                    }
                }
                //Equals
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-e")) {
                    if (cmd.length() > 8 && cmd.substring(7).length() == 7 && cmd.charAt(7) == '#') {
                        if (isHex) {
                            Log.d("printf", "Equals button text color set to " + hex);
                            cbEquals = hex;
                            tinydb.putString("-b=t", cbEquals);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")){
                            Log.d("printf", "Equals button text color reset");
                            cbEquals = "\0";
                            tinydb.putString("-b=t", cbEquals);

                            if (!isAll) {
                                MainActivity.mainActivity.recreate();
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                }
                else if (cmd.startsWith("set") || cmd.startsWith("Set") || cmd.startsWith("SET")){
                    Log.d("printf", setError);
                }

                if (!Aux.isNull(cFabText)) {
                    if (cFabText.equals("#reset0")) {
                        cFabText = "\0";
                        tinydb.putString("cFabText", "\0");
                    }
                }
            }





            //Share
            else if (cmd.startsWith("share ") && cmd.length() > 6){
                String filename = cmd.substring(6);

                if (filename.endsWith(".txt"))
                    filename = Aux.newTrim(filename, 4);

                File path = new File(this.getFilesDir(), "themes");

                File file = new File(path, filename + ".txt");

                Uri textUri = FileProvider.getUriForFile(
                        this,
                        "com.mirambeau.termcalc.fileprovider",
                        file);

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("application/text");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, textUri);
                startActivity(Intent.createChooser(sharingIntent, "Share Theme"));
            }





            //Backup
            else if (cmd.startsWith("backup") && cmd.length() > 6 && Character.toString(cmd.charAt(6)).equals(" ")){
                int a, b, c;

                String[] colors = {tinydb.getString("cPrimary"), tinydb.getString("cSecondary"), tinydb.getString("cTertiary"), tinydb.getString("cbEquals"), tinydb.getString("cFab"), tinydb.getString("cPlus"), tinydb.getString("cMinus"), tinydb.getString("cMulti"), tinydb.getString("cDiv"), tinydb.getString("cMain"), tinydb.getString("cKeypad"), tinydb.getString("cTop"), tinydb.getString("cNum"), tinydb.getString("cFabText")};

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

                String fileText, filename;

                int numColors = colors.length;

                if (cmd.length() > 7 && !cmd.substring(7).equals("\0")) {
                    if (cmd.substring(7).endsWith(".txt"))
                        filename = cmd.substring(7);
                    else
                        filename = cmd.substring(7) + ".txt";

                    if (colors[0] == null || colors[0].equals("\0") || colors[0].equals("0"))
                        colors[0] = "#reset0";

                    fileText = colors[0] + "\n";

                    for (i = 1; i < numColors; i++) {
                        if (colors[i] == null || colors[i].equals("\0") || colors[i].equals("") || colors[i].equals("0") || !Aux.isColor(colors[i]))
                            colors[i] = "#reset0";

                        fileText += colors[i] + "\n";
                    }

                    boolean hasAddedButton = false;

                    for (a=0; a < extraColors.length; a++){
                        if (extraColors[a] != null){
                            if (Aux.isColor(extraColors[a])) {
                                if (!hasAddedButton) {
                                    fileText += "\n";
                                    hasAddedButton = true;
                                }

                                fileText += extraColors[a] + "-b" + extraTexts[a] + "\n";
                            }
                        }

                        if (extraTextColors[a] != null){
                            if (Aux.isColor(extraTextColors[a])) {
                                if (!hasAddedButton) {
                                    fileText += "\n";
                                    hasAddedButton = true;
                                }

                                fileText += extraTextColors[a] + "-b" + extraTexts[a] + "t" + "\n";
                            }
                        }
                    }

                    fileText += tinydb.getString("theme");

                    writeTheme(this, fileText, filename);
                }
            }





            //Restore
            else if (cmd.startsWith("restore") && cmd.length() > 7 && Character.toString(cmd.charAt(7)).equals(" ")) {
                int f = 0;
                boolean exists = false;
                String[] colorKeys = {"cPrimary", "cSecondary", "cTertiary", "cbEquals", "cFab", "cPlus", "cMinus", "cMulti", "cDiv", "cMain", "cKeypad", "cTop", "cNum", "cFabText"};
                String[] colorWords = {"Primary", "Secondary", "Tertiary", "Equals button", "Delete button", "Plus button", "Minus button", "Multiply button", "Divide button", "Main background", "Keypad", "Top bar text", "Keypad text", "Delete button text"};

                int a, b, c;
                int testLength = 0;

                final String theme = tinydb.getString("theme");

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

                if (cmd.length() > 8 && !cmd.substring(8).equals("\0")) {
                    isAll = true;
                    isRestore = true;

                    String themeName = cmd.substring(8);

                    File directory = new File(this.getFilesDir(), "themes");
                    File[] files = directory.listFiles();

                    if (files != null) {
                        files = directory.listFiles();

                        if (files != null) {
                            if (files.length > 0) {
                                for (f = 0; f < files.length; f++) {
                                    if (files[f].getName().equals(themeName + ".txt")) {
                                        exists = true;
                                        break;
                                    }
                                }
                            }
                            else {
                                Log.d("printf", "\nError: No theme backups currently exist.");
                            }
                        }

                        boolean isValid = true;

                        if (exists) {
                            FileInputStream fis = new FileInputStream(files[f]);
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader bufferedReader = new BufferedReader(isr);

                            String line;

                            for (i=0; (line = bufferedReader.readLine()) != null; i++) {
                                if (!((i > 12 && Aux.isDigit(line)) || Aux.isColor(line) || line.equals("#reset0") || (line.length() >= 6 && (Aux.isColor(line.substring(0, 6)) || Aux.isColor(line.substring(0, 7))) && line.contains("-b")))) {
                                    if (i != 14 && i != 19)
                                        isValid = false;

                                    Log.d("printf", "Error restoring theme:\n" + i + " color hex code (" + line + ") is invalid.\n\nPlease check the file and try again.");
                                }
                            }

                            if (isValid) {
                                fis = new FileInputStream(files[f]);
                                isr = new InputStreamReader(fis);
                                bufferedReader = new BufferedReader(isr);

                                for (a=0; a < extraTexts.length; a++){
                                    tinydb.putString("-b" + extraTexts[a], "\0");
                                    tinydb.putString("-b" + extraTexts[a] + "t", "\0");
                                }

                                for (i=0; (line = bufferedReader.readLine()) != null; i++) {
                                    if (Aux.isColor(line)) {
                                        if (i == 3)
                                            tinydb.putString("-b=t", line);
                                        else if (i >= 5 && i <= 8){
                                            String[] codes = {"-b+t", "-b-t", "-b×t", "-b÷t"};

                                            tinydb.putString(codes[i - 5], line);
                                        }
                                        else
                                            tinydb.putString(colorKeys[i], line);
                                    }
                                    else if (line.startsWith("name:")) {
                                        continue;
                                    }
                                    else if (line.equals("#reset0")) {
                                        tinydb.putString(colorKeys[i], "\0");
                                    }
                                    else if (line.contains("-b")){
                                        String buttonHex, buttonCode;

                                        buttonHex = line.substring(0, 7);
                                        buttonCode = Aux.getLast(line, line.length() - buttonHex.length());

                                        if (Aux.isColor(buttonHex)) {
                                            run("set " + buttonCode + " " + buttonHex);
                                        }
                                    }
                                    else if (Aux.isDigit(line) && Integer.parseInt(line) > 0 && Integer.parseInt(line) <= 5)
                                        tinydb.putString("theme", line);
                                }

                                isAll = false;

                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }

                            isAll = false;
                        }
                        else {
                            Log.d("printf", "Error: File does not exist. Please verify that you have entered the correct theme name.");
                        }
                    }
                    else {
                        Log.d("printf", "\nError: No theme backups currently exist.");
                    }
                }

                isAll = false;
                isRestore = false;
            }
        }

        return output;
    }

    public final void writeTheme(Context mcoContext, String body, String filename){
        File dir = new File(mcoContext.getFilesDir(), "themes");

        if (!dir.exists()){
            dir.mkdir();
        }

        try {
            File theme = new File(dir, filename);
            FileWriter writer = new FileWriter(theme);
            writer.append(body);
            writer.flush();
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
