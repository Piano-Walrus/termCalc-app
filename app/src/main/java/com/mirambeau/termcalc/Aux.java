package com.mirambeau.termcalc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class Aux {
    static String superNum, subNum;

    static final String[] superscripts = {"⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"};
    static final String[] subscripts = {"₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉"};

    static final String[] superLowerLetters = {"ᵃ", "ᵇ", "ᶜ", "ᵈ", "ᵉ", "ᶠ", "ᵍ", "ʰ", "ᶦ", "ʲ", "ᵏ", "ˡ", "ᵐ", "ⁿ", "ᵒ", "ᵖ", "ᑫ", "ʳ", "ˢ", "ᵗ", "ᵘ", "ᵛ", "ʷ", "ˣ", "ʸ", "ᶻ"};
    static final String[] superUpperLetters = {"ᴬ", "ᴮ", "ᶜ", "ᴰ", "ᴱ", "ᶠ", "ᴳ", "ᴴ", "ᴵ", "ᴶ", "ᴷ", "ᴸ", "ᴹ", "ᴺ", "ᴼ", "ᴾ", "Q", "ᴿ", "ˢ", "ᵀ", "ᵁ", "ⱽ", "ᵂ", "ˣ", "ʸ", "ᶻ"};

    public static final ArrayList<String> superlist = new ArrayList<String>(Arrays.asList("⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"));
    public static final ArrayList<String> sublist = new ArrayList<String>(Arrays.asList("₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉"));

    static final String[] trigIn = {"sin", "cos", "tan", "csc", "sec", "cot", "sinh", "cosh", "tanh", "csch", "sech", "coth", "arcsin", "arccos", "arctan", "arccsc", "arcsec", "arccot", "arcsinh", "arccosh", "arctanh", "arccsch", "arcsech", "arccoth"};

    static final String[] currencyCodes = {"USD", "EUR", "GBP", "CAD", "AUD", "MXN"};
    static double[] rates = {-1.0, -1.0, -1.0, -1.0, -1.0, -1.0};

    public static final String divi = "÷";
    public static final String multi = "×";
    public static final String pi = "π";
    public static final String sq = "√";
    public static final String bulletDot = "•";
    public static final String multiDot = "⋅";
    public static final String superDot = "‧";
    public static final String emDash = "—";
    public static final String superMinus = "⁻";

    public static final String piStr = superscripts[3] + superDot + superscripts[1] + superscripts[4] + superscripts[1] + superscripts[5] + superscripts[9];

    public static ArrayList<String> ops = new ArrayList<String>(Arrays.asList("+", "-", multi, divi, sq, "^", "(", ")", "!", "%", bulletDot, multiDot, "*"));

    public static boolean ratesChecked = false;
    static boolean isUpper = false;
    static FunctionsAdapter adapter;
    static boolean restored = false;

    public static String getTheme(){
        String theme = tinydb().getString("theme");

        if (theme == null || isNull(theme) || !isDigit(theme))
            return "1";

        return theme;
    }

    public static String getTheme(String key){
        String theme = tinydb().getString(key);

        if (theme == null || isNull(theme) || !isDigit(theme))
            return "1";

        return theme;
    }

    public static int getThemeInt(){
        try {
            return Integer.parseInt(getTheme());
        }
        catch (Exception e) {
            e.printStackTrace();

            return 1;
        }
    }

    public static int getThemeInt(String key){
        int defaultTheme = 1;

        try {
            if (key.equals("customTheme"))
                defaultTheme = 3;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            return Integer.parseInt(getTheme(key));
        }
        catch (Exception e) {
            e.printStackTrace();

            return defaultTheme;
        }
    }

    public static TinyDB tinydb(Context context) {
        try {
            try {
                if (MainActivity.mainActivity != null)
                    return new TinyDB(MainActivity.mainActivity);
            }
            catch (Exception e2) {
                e2.printStackTrace();

                try {
                    return new TinyDB(context);
                }
                catch (Exception e3) {
                    e3.printStackTrace();
                }
            }

            try {
                if (TerminalActivity.terminalActivity != null)
                    return new TinyDB(TerminalActivity.terminalActivity);
            }
            catch (Exception e2) {
                e2.printStackTrace();

                try {
                if (ThemeActivity.themeActivity != null)
                    return new TinyDB(ThemeActivity.themeActivity);

                if (EditorActivity.editorActivity != null)
                    return new TinyDB(EditorActivity.editorActivity);
                }
                catch (Exception e3) {
                    e3.printStackTrace();
                }
            }

            if (ThemeActivity.themeActivity != null)
                return new TinyDB(ThemeActivity.themeActivity);

            if (EditorActivity.editorActivity != null)
                return new TinyDB(EditorActivity.editorActivity);
        }
        catch (Exception e) {
            e.printStackTrace();

            return new TinyDB(new Activity().getApplicationContext());
        }

        return new TinyDB(new Activity().getApplicationContext());
    }

    public static TinyDB tinydb() {
        try {
            if (MainActivity.mainActivity != null)
                return new TinyDB(MainActivity.mainActivity);

            if (TerminalActivity.terminalActivity != null)
                return new TinyDB(TerminalActivity.terminalActivity);

            if (ThemeActivity.themeActivity != null)
                return new TinyDB(ThemeActivity.themeActivity);

            if (EditorActivity.editorActivity != null)
                return new TinyDB(EditorActivity.editorActivity);
        }
        catch (Exception e) {
            e.printStackTrace();

            return new TinyDB(new Activity().getApplicationContext());
        }

        return new TinyDB(new Activity().getApplicationContext());
    }

    public ArrayList<String> getRange(ArrayList<String> array, int start, int end) {
        int i;
        ArrayList<String> result = new ArrayList<>();

        if (end > start) {
            int temp = start;
            start = end;
            end = temp;
        }
        else if (end == start) {
            result.add(array.get(start));
            return result;
        }

        for (i=start; i < end; i++){
            result.add(array.get(i));
        }

        return result;
    }

    public static void toastHistorySteps (ArrayList<ThemeHistoryStep> steps, int currentEntry) {
        int i;
        try {
            String output = "currentEntry: " + currentEntry + "\n\n";

            for (i = 0; i < steps.size(); i++) {
                ThemeHistoryStep current = steps.get(i);

                output += "~~ index = " + i + " ~~\ncode: " + current.code + "  initial: " + current.initColor + "  final: " + current.finalColor + "\n\n";
            }

            makeToast(output, 1);
        }
        catch (Exception e) {
            saveStack(e);
        }
    }

    public static Drawable getDrawable(int id) {
        return ResourcesCompat.getDrawable(MainActivity.mainActivity.getResources(), id, null);
    }

    public static Drawable getDrawable(int id, Context context) {
        try {
            return ResourcesCompat.getDrawable(context.getResources(), id, null);
        }
        catch (Exception e) {
            try {
                return ResourcesCompat.getDrawable(MainActivity.mainActivity.getResources(), id, null);
            }
            catch (Exception e2) {
                e2.printStackTrace();

                return ResourcesCompat.getDrawable(EditorActivity.editorActivity.getResources(), id, null);
            }
        }
    }

    public static void copy(String str, Context context) {
        try {
            ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Copied text", str));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String toColorString(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static int getBackgroundColor(View v) {
        ColorDrawable bg;

        if (v.getClass() == androidx.appcompat.widget.AppCompatButton.class) {
            Button button = (Button) v;
            bg = (ColorDrawable) button.getBackground();
        }
        else if (v.getClass() == androidx.appcompat.widget.AppCompatImageButton.class) {
            try {
                ImageButton button = (ImageButton) v;
                bg = (ColorDrawable) button.getBackground();
            }
            catch (Exception e) {
                ImageButton button = (ImageButton) v;

                try {
                    return button.getBackgroundTintList().getDefaultColor();
                }
                catch (Exception e2) {
                    e.printStackTrace();

                    bg = null;
                }
            }
        }
        else {
            bg = (ColorDrawable) v.getBackground();
        }

        try {
            return bg.getColor();
        }
        catch (Exception e) {
            e.printStackTrace();

            int i;
            View parent = (View) v.getParent();

            for (i=0; i < 4; i++) {
                try {
                    return ((ColorDrawable) parent.getBackground()).getColor();
                }
                catch (Exception e2) {
                    e2.printStackTrace();

                    parent = (View) parent.getParent();
                }
            }
        }

        return Color.BLACK;
    }

    public static String getNavAccentColor() {
        TinyDB tinydb = tinydb();

        int i;
        boolean mainIsSet = isTinyColor("cMain"), keyTextIsSet = isTinyColor("cNum");
        int cMain;
        String[] colorTexts = {"-b=t", "cFabText", "-bop", "-b+t", "-b-t", "-b"+ multi + "t", "-b"+ divi + "t", "cFab", "-b=", "cSecondary", "cTop", "cTertiary", "cPrimary", "-b(t", "-b)t", "-b+", "-b-", "-b" + multi, "-b" + divi};

        if (mainIsSet)
            cMain = getTinyColor("cMain");
        else
            cMain = 0;

        for (i=0; i < colorTexts.length; i++){
            if (isTinyColor(colorTexts[i])) {
                if (tinydb.getBoolean("isAlwaysDarkNav") && getAverageBrightness(tinydb.getString(colorTexts[i])) < 8)
                    continue;

                if (i == 0 && keyTextIsSet && getTinyColor("cNum") == getTinyColor("-b=t"))
                    continue;

                String color = tinydb.getString(colorTexts[i]);

                boolean isMain;

                if (mainIsSet)
                    isMain = cMain == Color.parseColor(color);
                else
                    isMain = false;

                if ((getThemeInt() == 2 && (isDigit(chat(color, 1)) || isDigit(chat(color, 3)) || isDigit(chat(color, 5)))) ||
                    (getThemeInt() != 2 && (isLetter(chat(color, 1)) || isLetter(chat(color, 3)) || isLetter(chat(color, 5))))) {

                    Log.d("grayTest", "#" + color.substring(2) + "7");

                    if (isTinyColor("-mt")){
                        if (!(isGray(color) && isGray(tinydb.getString("-mt"))) && !isMain)
                            return color;
                    }
                    else if ((isGray(color) && !isMain) || (i == 0 && !isGray(color))) {
                        String tempMt;

                        if (getThemeInt() == 2)
                            tempMt = "#222222";
                        else if (getThemeInt() == 5)
                            tempMt = "#303030";
                        else
                            tempMt = "#FFFFFF";

                        if (!isGray("#" + color.substring(2) + "7", 8) && !color.equals(tempMt))
                            return color;
                    }
                }
            }
        }

        for (i=0; i < colorTexts.length; i++){
            if (isTinyColor(colorTexts[i])) {
                if (i == 0 && keyTextIsSet && getTinyColor("cNum") == getTinyColor("-b=t"))
                    continue;

                String color = tinydb.getString(colorTexts[i]);

                boolean isMain;

                if (mainIsSet)
                    isMain = cMain == Color.parseColor(color);
                else
                    isMain = false;

                if ((getThemeInt() == 2 && (isDigit(chat(color, 1)) || isDigit(chat(color, 3)) || isDigit(chat(color, 5)))) ||
                        (getThemeInt() != 2 && (isLetter(chat(color, 1)) || isLetter(chat(color, 3)) || isLetter(chat(color, 5))))) {

                    Log.d("grayTest", "#" + color.substring(2) + "7");

                    if (!isMain) {
                        String tempMt;

                        if (getThemeInt() == 2)
                            tempMt = "#222222";
                        else if (getThemeInt() == 5)
                            tempMt = "#303030";
                        else
                            tempMt = "#FFFFFF";

                        if (isGray(color) && !isGray("#" + color.substring(2) + "7", 6) && !color.equals(tempMt))
                            return color;
                        else if (!isGray(color) && !color.equals(tempMt))
                            return color;
                    }
                }
            }
        }

        if (isTinyColor("cPrimary")) {
            String cPrimary = tinydb.getString("cPrimary");

            if (getThemeInt() == 2) {
                if (getAverageBrightness(cPrimary) > 11)
                    return hexAdd(tinydb.getString("cPrimary"), -72);
                else
                    return tinydb.getString("cPrimary");
            }
            else {
                if (getAverageBrightness(cPrimary) < 5)
                    return hexAdd(tinydb.getString("cPrimary"), 32);
                else
                    return tinydb.getString("cPrimary");
            }
        }
        else
            return "#03DAC5";
    }

    public static int getAverageBrightness(String color){
        if (!isColor(color))
            return -1;

        int i, j = 0;
        double[] colorInts = new double[3];

        for (i=1; i < 6; i += 2) {
            if (isDigit(color.charAt(i)))
                colorInts[j] = Integer.parseInt(chat(color, i));
            else if (isLetter(color.charAt(i)) && letterToDigit(color.charAt(i)) < 7)
                colorInts[j] = letterToDigit(color.charAt(i)) + 10;

            j++;
        }

        Log.d("avg brightness", "color = " + color + "     average brightness = " + ((int) Math.round((colorInts[0] + colorInts[1] + colorInts[2]) / colorInts.length)));

        return (int) Math.round((colorInts[0] + colorInts[1] + colorInts[2]) / colorInts.length);
    }

    public static String addCommas (String inputStr){
        if (inputStr == null || isNull(inputStr) || inputStr.length() < 1)
            return inputStr;

        int i, offset = 0, length = inputStr.length(), nullCount = 0;
        int a = 0;
        String str = inputStr;
        int[][] coords = new int[2500][2];
        boolean isDec = false;

        str = str.replace(" ", "");

        for (i=0; i < length; i++) {
            if (isDigit(chat(str, i)) && !isDec) {
                if (i == 0)
                    coords[a][0] = 0;
                else if (coords[a][0] == 0 && a != 0)
                    coords[a][0] = i;

                coords[a][1] = i+1;
            }
            else if (chat(str, i) != null && chat(str, i).equals("."))
                isDec = true;
            else if (i == 0 && (str.startsWith("-") || str.startsWith("√")))
                coords[0][0] = 1;
            else {
                if (isDigit(chat(str, i)) && isDec)
                    continue;

                a++;
                isDec = false;
            }
        }

        /*
        for (i=0; i < 6; i++) {
            Log.d("" + i, "[" + coords[i][0] + "][" + coords[i][1] + "]");
        }
        */

        for (i=0; i < coords.length && nullCount < 10; i++) {
            if (coords[i][0] == 0 && coords[i][1] == 0) {
                nullCount++;
                continue;
            }

            nullCount = 0;

            Log.d("str", ""+ i + " = " + str);

            try {
                if (coords[i][1] - coords[i][0] > 3) {
                    int numCommas = countChars(str, ",");

                    str = str.substring(0, coords[i][0] + offset) + checkCommas(str.substring(coords[i][0] + offset, coords[i][1] + offset)) + str.substring(coords[i][1] + offset);

                    offset += countChars(str, ",") - numCommas;
                }
            }
            catch (NumberFormatException e) {
                e.printStackTrace();

                offset++;
                i--;
            }
            catch (StringIndexOutOfBoundsException e) {
                if (coords[i][1] - coords[i][0] > 3) {
                    str = str.substring(0, coords[i][0] + offset) + checkCommas(str.substring(coords[i][0] + offset, coords[i][1] + offset)) + str.substring(coords[i][1] + offset);

                    offset++;
                }
            }

            //Log.d("str", ""+ i + " = " + str);
        }

        if (str.length() < inputStr.length())
            return inputStr;

        return str;
    }

    public static String removeCommas (String str) {
        return str.replace(",", "");
    }

    public static String updateCommas (String str) {
        return addCommas(removeCommas(str));
    }

    public static int letterToDigit (char character){
        if (!isLetter(character))
            return -1;

        if (character - 65 > 26){
            return character % 97;
        }
        else
            return character % 65;
    }

    public static boolean containsBinaryOperator(String str){
        if (isNull(str))
            return false;

        if (str.length() == 1 && (isNum(str) || isLetter(str)))
            return false;

        int i;
        String[] operators = {"+", "-", multi, divi, "^", "%", ""};

        for (i=0; i < operators.length; i++){
            if (str.contains(operators[i]))
                return true;
        }

        return false;
    }

    public static boolean isTrig(String str){
        if (isNull(str) || str.length() < 3)
            return false;

        for (String current : trigIn){
            if (str.equals(current) || str.equals(current + "("))
                return true;
        }

        return true;
    }

    public static int getTrigLength(String str) {
        int i;
        str = str.trim().replace(" ", "");

        if (str.length() > 7)
            str = str.substring(0, 6);
        if (!str.startsWith("arc"))
            str = str.substring(3);

        while (str.length() > 3) {
            if (isTrig(str))
                return str.length();
            else
                str = newTrim(str, 1);
        }

        return isTrig(str) ? str.length() : -1;
    }

    public static int safeParseColor(String color, String[] backups){
        if (color == null && backups == null)
            return Color.WHITE;

        int i;

        if (!isColor(color) && backups != null) {
            for (i=0; i < backups.length; i++) {
                if (isColor(backups[i]))
                    return Color.parseColor(backups[i]);
            }

            return Color.WHITE;
        }
        else if (isColor(color))
            return Color.parseColor(color);
        else
            return Color.WHITE;
    }

    /**
     * Returns the last history entry pertaining to a particular button code in a given ArrayList of history entries.
     * @return -1 if no entries found
     */
    public static int getLastHistoryEntry(ArrayList<ThemeHistoryStep> entries, String code) {
        int i;
        int entry = -1;

        for (i=0; i < entries.size(); i++) {
            if (entries.get(i).code.equals(code))
                entry = i;
        }

        return entry;
    }

    /**
     * Returns the first history entry pertaining to a particular button code in a given ArrayList of history entries.
     * @return -1 if no entries found
     */
    public static int getFirstHistoryEntry(ArrayList<ThemeHistoryStep> entries, String code) {
        int i;

        for (i=0; i < entries.size(); i++) {
            if (entries.get(i).code.equals(code))
                return i;
        }

        return -1;
    }

    public static void makeLongToast(String str){
        if (!isNull(str))
            Toast.makeText(MainActivity.mainActivity, str, Toast.LENGTH_LONG).show();
    }

    /**
     * Short toast: length = 0
     * Long toast: length = 1
     */
    public static void makeToast(String str, int length){
        Context context = MainActivity.mainActivity;

        int[] lengths = {Toast.LENGTH_SHORT, Toast.LENGTH_LONG};

        if (length != 0 && length != 1)
            length = 0;

        try {
            context = context.getApplicationContext();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (!isNull(str))
                Toast.makeText(context, str, lengths[length]).show();
        }
        catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(MainActivity.mainActivity, str, lengths[length]).show();
        }
    }

    public static void makeToast(String str, Context context, int length){
        int[] lengths = {Toast.LENGTH_SHORT, Toast.LENGTH_LONG};

        if (length != 0 && length != 1)
            length = 0;

        try {
            if (!isNull(str))
                Toast.makeText(context, str, lengths[length]).show();
        }
        catch (Exception e) {
            e.printStackTrace();

            try {
                Toast.makeText(MainActivity.mainActivity, str, lengths[length]).show();
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public static AlertDialog.Builder createAlertDialog(String title, Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

        builder.setTitle(title);

        return builder;
    }

    public static void saveStack(Exception e){
        try {
            e.printStackTrace();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            tinydb().putString("stackTrace", sw.toString());
            tinydb().putString("reason", e.getMessage());
            makeLongToast("Crash recorded. Run \"debug stack\" in the terminal, and attach the output to a bug report.");
        }
        catch (Exception ignored) {}
    }

    public static void saveStack(Exception e, boolean shouldMakeToast){
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            tinydb().putString("stackTrace", sw.toString());
            tinydb().putString("reason", e.getMessage());

            if (shouldMakeToast)
                makeLongToast("Crash recorded. Run \"debug stack\" in the terminal, and attach the output to a bug report.");
        }
        catch (Exception ignored) {}
    }

    @SuppressLint("SetTextI18n")
    public static String printStack(Exception e){
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            return sw.toString();
        }
        catch (Exception ignored){
            return " ";
        }
    }

    /**
     * Converts the passed string into a string array containing individual button presses. If this function fails, an empty string array is returned.
     * @return String array containing the texts of buttons in MainActivity
     */
    public static String[] parseEq(String str){
        int i, j;

        if (str == null)
            return new String[0];

        str = str.trim().replace(" ", "").replace(",", "").replace("\0", "").replace("�", "");

        int length = str.length();
        ArrayList<String> output = new ArrayList<>();

        for (i=0; i < length; i++){
            if (isNum(chat(str, i)))
                output.add(chat(str, i));
            else if (chat(str, i).equals("*"))
                output.add(multi);
            else if (chat(str, i).equals("/"))
                output.add(divi);
            else if (chat(str, i).equals("−") || chat(str, i).equals("-"))
                output.add("-");
            else {
                boolean isTrig = (i < length - 4 && chat(str, i + 3).equals("h")) || (i < length - 7 && chat(str, i + 6).equals("h")) ||
                        (i < length - 3 && (chat(str, i).equals("s") || chat(str, i).equals("c") || chat(str, i).equals("t")) ||
                        (i < length - 6 && chat(str, i).equals("a")));

                if (isTrig) {
                    for (j = 0; j < trigIn.length; j++) {
                        if (chat(8, str, i).startsWith(trigIn[j])) {
                            output.add(trigIn[j]);
                            i += trigIn[j].length();
                            break;
                        }
                    }
                }
                else if (i < str.length() - 3 && chat(4, str, i).equals("sqrt")){
                    output.add("√");
                    i += 4;
                }
                else if (i < str.length() - 3 && chat(3, str, i).equals("log")){
                    output.add("log");
                    i += 3;
                }
                else if (i < str.length() - 2 && chat(2, str, i).equals("ln")){
                    output.add("ln");
                    i += 2;
                }
                else
                    output.add(chat(str, i));
            }
        }

        Log.d("parseTest", output + "     " + str);

        return output.toArray(new String[0]);
    }

    public static int findInArray(String[] array, String target){
        if (array == null || array[0] == null || isNull(target))
            return -1;

        int i;

        for (i=0; i < array.length; i++){
            if (array[i].equals(target))
                return i;
        }

        return -1;
    }

    public static String parseCoefficients(String functionText, ArrayList<String> vars) {
        int i, j;

        if (functionText != null) {
            for (i = 0; i < functionText.length()-1; i++) {
                boolean isCoefficient = (isDigit(chat(functionText, i)) && isLetter(chat(functionText, i+1)) && vars.contains(chat(functionText, i+1))) ||
                        (isLetter(chat(functionText, i)) && vars.contains(chat(functionText, i)) && isLetter(chat(functionText, i+1)) && vars.contains(chat(functionText, i+1))) ||
                        (isLetter(chat(functionText, i)) && vars.contains(chat(functionText, i)) && isDigit(chat(functionText, i+1))) ||
                        (isDigit(chat(functionText, i)) && chat(functionText, i+1).equals("(")) ||
                        (isLetter(chat(functionText, i)) && vars.contains(chat(functionText, i)) && chat(functionText, i+1).equals("(")) ||
                        (chat(functionText, i).equals(")") && isDigit(chat(functionText, i+1))) ||
                        (chat(functionText, i).equals(")") && isLetter(chat(functionText, i+1)) && vars.contains(chat(functionText, i+1)));

                if (isCoefficient) {
                    if (isLetter(chat(functionText, i)) && vars.contains(chat(functionText, i)) && isLetter(chat(functionText, i+1)) && vars.contains(chat(functionText, i+1))){
                        try {
                            functionText = functionText.replace(chat(2, functionText, i), "(" + chat(functionText, i) + "*" + chat(functionText, i + 1) + ")");
                            i += 3;
                        }
                        catch (Exception e){
                            e.printStackTrace();

                            functionText = newReplace(i, functionText, chat(functionText, i) + "*");
                            i++;
                        }
                    }
                    else {
                        functionText = newReplace(i, functionText, chat(functionText, i) + "*");
                        i++;
                    }
                }
            }
        }

        return functionText;
    }

    public static boolean checkRates() {
        int i;

        for (i=0; i < rates.length; i++){
            if (rates[i] < 0)
                return false;
        }

        ratesChecked = true;
        return true;
    }

    public static String fromSuper(String str){
        if (isNull(str) || str.length() != 1)
            return "";

        int i;

        for (i=0; i < superscripts.length; i++){
            if (str.equals(superscripts[i]))
                return Integer.toString(i);
        }

        return "";
    }

    public static String toSuper(String str){
        if (isNull(str) || str.length() != 1)
            return "";

        if (isDigit(str))
            return superscripts[Integer.parseInt(str)];
        else if (isLetter(str)) {
            if (isUpper) {
                isUpper = false;
                return superUpperLetters[str.charAt(0) - 65];
            }
            else
                return superLowerLetters[str.charAt(0) - 97];
        }

        return "";
    }

    public static boolean isNum(String str) {
        if (isNull(str))
            return false;

        if (!isDigit(str)){
            if (str.equals("e") || str.equals("π")) {
                return true;
            }
        }

        return isDigit(str);
    }

    public static boolean isFullNum(String str) {
        int i, length;
        if (isNull(str))
            return false;

        length = str.length();

        if (length == 1) {
            if (isDigit(str))
                return true;
            if (str.equals("."))
                return false;
        }

        for (i=0; i < length; i++) {
            if (!isDigit(chat(str, i))) {
                if (chat(str, i).equals("e") || chat(str, i).equals("π") || chat(str, i).equals(".")) {
                    continue;
                }
                else
                    return false;
            }
        }

        return countChars(str, ".") <= 1;
    }

    public static boolean onlyContains(String str, String target){
        if (str == null || target == null)
            return false;

        int i;
        int length = str.length();

        for (i=0; i < length; i++){
            if (chat(str, i) != null && !chat(str, i).equals(target))
                return false;
        }

        return true;
    }

    public static boolean isLetter(char character){
        return (character >= 65 && character <= 90) || (character >= 97 && character <= 122);
    }

    public static boolean isLetter(String str){
        if (str == null || str.length() != 1)
            return false;

        char character = str.charAt(0);

        if (character >= 65 && character <= 90)
            isUpper = true;
        else if (character >= 97 && character <= 122)
            isUpper = false;

        return (character >= 65 && character <= 90) || (character >= 97 && character <= 122);
    }

    public static boolean isLetter(char character, int end){
        return isLetter(character) && (((int) (character)) % 65 < 27) ? ((int) (character)) % 65 < end : ((int) (character)) % 97 < end;
    }

    public static boolean isLetter(String str, int end){
        return isLetter(str) && (((int) (str.charAt(0))) % 65 < 27) ? ((int) (str.charAt(0))) % 65 < end : ((int) (str.charAt(0))) % 97 < end;
    }

    public static String parseVars(String expression){
        if (expression == null)
            return "";

        int i, j;
        String vars = "0";
        String parsed = "";

        mainExpressionLoop : for (i=0; i < expression.length(); i++){
            if (vars != null && isLetter(chat(expression, i))) {
                if (chat(expression, i) != null && !parsed.contains(chat(expression, i))) {
                    if (expression.substring(i).length() > 3 && (expression.startsWith("sqrt", i) || expression.startsWith("cbrt", i))) {
                        i += 3;
                        continue;
                    }
                    else if (expression.substring(i).length() > 2) {
                        if (expression.startsWith("a", i) || expression.startsWith("s", i) || expression.startsWith("c", i) || expression.startsWith("t", i)) {
                            for (j = 0; j < trigIn.length; j++) {
                                if (expression.substring(i).length() >= trigIn[j].length() && expression.startsWith(trigIn[j], i)) {
                                    i += trigIn[j].length() - 1;
                                    continue mainExpressionLoop;
                                }
                            }
                        }
                        else if (expression.startsWith("log", i)) {
                            i += 2;
                            continue;
                        }
                        else if (expression.startsWith("ln", i)) {
                            i++;
                            continue;
                        }
                    }
                    else if (expression.substring(i).length() > 1) {
                        if (expression.startsWith("ln", i)) {
                            i++;
                            continue;
                        }
                    }

                    if (vars.equals("0"))
                        vars = chat(expression, i);
                    else
                        vars += "`" + chat(expression, i);

                    parsed += chat(expression, i);
                }
            }
        }

        try {
            return vars.replace("`l`o`g", "").replace("l`o`g`", "").replace("`l`n", "").replace("l`n`", "");
        }
        catch (Exception e){
            return "0";
        }
    }

    public static String checkCommas(String str){
        int i;
        int count = 0, threshold = 3;

        if (str == null || str.length() < 1)
            return "";
        if (str.length() < 4)
            return str;

        str = str.replace(",", "");

        for (i = str.length() - 1; i >= 0; i--){
            if (count == threshold){
                count = 0;

                if (threshold == 3)
                    threshold = 2;

                str = newReplace(i, str,  chat(str, i) + ",");
            }
            else
                count++;
        }

        return str;
    }

    public static int countChars(String str, String input){
        if (isNull(str) || isNull(input) || input.length() < 1)
            return 0;

        int i;
        int numChars = 0;
        int length = str.length();
        int inputLength = input.length();

        try {
            for (i = 0; i < length; i += inputLength) {
                if (chat(inputLength, str, i).equals(input))
                    numChars++;
            }
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }

        return numChars;
    }

    public static int countNums(String numCheck) {
        if (isNull(numCheck))
            return 0;
        if (numCheck.endsWith("-"))
            return 0;

        int numNumbers;

        for (numNumbers = 0; isNum(lastChar(numCheck)) || (numCheck.length() > 2 && getLast(numCheck, 3).equals("  ̣")) || isSuperscript(lastChar(numCheck)) || isSubscript(lastChar(numCheck)) || (numCheck.endsWith(".") && isDigit(lastChar(newTrim(numCheck, 1)))) || numCheck.endsWith(","); numNumbers++) {
            if (numCheck.length() > 2 && getLast(numCheck, 3).equals("  ̣")) {
                numCheck = newTrim(numCheck, 2);
                numNumbers += 2;
            }

            numCheck = newTrim(numCheck, 1);
        }

        return numNumbers;
    }

    public static String convSuper(String str){
        int i;

        if (isNull(str) || str.length() != 1)
            return str;

        if (!isSuperscript(str))
            return str;

        for (i=0; i < superscripts.length; i++){
            if (str.equals(superscripts[i]))
                return superscripts[i];
        }

        if (str.equals("⋅"))
            return ".";
        else if (str.equals("ᵉ"))
            return "e";
        else if (str.equals("⁻"))
            return "-";

        return str;
    }

    public static String newReplace(int i, String full, String str){
        try {
            if (isNull(full) || full.length() < 1 || i >= full.length())
                return full;

            if (i == -1)
                return str + full;

            if (str == null || str.equals("") || str.equals("\0"))
                return full.substring(0, i) + full.substring(i + 1);

            return full.substring(0, i) + str + full.substring(i + 1);
        }
        catch (IndexOutOfBoundsException | NullPointerException e){
            e.printStackTrace();

            if (!isNull(full))
                return full;
            else
                return "";
        }
    }

    public static String newTrim(String str, int numChars) {
        int s;

        if (isNull(str))
            return "";

        for (s=0; s < numChars; s++) {
            if (str != null && !str.equals("\0") && str.length() == 1)
                return "";

            if (str != null && !str.equals("\0") && str.length() > 1)
                str = str.substring(0, str.length() - 1);
        }

        return str;
    }

    public static String getLast(String str, int numChars){
        int s;
        String result = lastChar(str);

        if (str == null || str.equals("\0"))
            return null;
        if (numChars >= str.length())
            return str;

        str = newTrim(str, 1);

        for (s=0; s < numChars - 1; s++) {
            if (!isNull(str)) {
                if (str.length() == 1)
                    return lastChar(str) + result;

                if (str.length() > 1)
                    result = lastChar(str) + result;

                if (str.length() > 0)
                    str = str.substring(0, str.length() - 1);
            }
        }

        return result;
    }

    public static String chat(int numChars, String str, int index) {
        if (numChars == 0)
            return null;
        if (numChars == 1)
            return chat(str, index);

        if (isNull(str))
            return null;
        if (!isNull(str) && (index >= str.length() || index < 0))
            return null;

        if (numChars > str.length() - index) {
            try {
                return str.substring(index);
            }
            catch (IndexOutOfBoundsException except){
                return null;
            }
        }
        else {
            try {
                return str.substring(index, index + numChars);
            }
            catch (IndexOutOfBoundsException except) {
                return null;
            }
        }
    }

    public static String chat(String str, int index) {
        if (isNull(str))
            return null;
        if (!isNull(str) && (index >= str.length() || index < 0))
            return null;

        try {
            str = Character.toString(str.charAt(index));
        }
        catch (IndexOutOfBoundsException except){
            return null;
        }

        return str;
    }

    public static String lastChar(String str) {
        return (!isNull(str) && str.length() > 1) ? str.substring(str.length() - 1) : str;
    }

    public static boolean isDigit(char character) {
        return (int) character >= 48 && (int) character <= 57;
    }

    public static boolean isDigit(String str) {
        return !isNull(str) && str.length() == 1 && (int) str.charAt(0) >= 48 && (int) str.charAt(0) <= 57;
    }

    public static boolean isOp(String str) {
        if (isNull(str))
            return false;

        if (ops.contains(str))
            return true;

        return false;
    }

    static int extD = 0;

    public static int geti(){
        return extD;
    }

    public static boolean isSuperscript(String str) {
        int d;

        if (!isNull(str) && str.length() == 1) {
            for (d = 0; d < 10; d++) {
                if (str.equals(superscripts[d])) {
                    superNum = Integer.toString(d);
                    extD = d;
                    return true;
                }
            }

            if (str.equals("⋅") || str.equals(superDot)){
                superNum = ".";
                return true;
            }
            if (str.equals("⁻")){
                superNum = "-";
                return true;
            }
            if (str.equals("ᵉ")){
                superNum = "e";
                return true;
            }
        }

        return false;
    }

    public static String getSuperNum() {
        return superNum;
    }

    public static boolean isSubscript(String str) {
        int d;

        if (str == null)
            return false;

        if (!str.equals("\0") && str.length() == 1) {
            for (d = 0; d < 10; d++) {
                if (str.equals(subscripts[d])) {
                    subNum = Integer.toString(d);
                    return true;
                }
            }

            if (str.equals(".")){
                subNum = ".";
                return true;
            }

            if (str.equals("₍")){
                subNum = "(";
                return true;
            }

           if (str.equals("₎")) {
                subNum = ")";
                return true;
            }

            if (str.equals("ₑ")){
                subNum = "e";
                return true;
            }
        }

        return false;
    }

    public static int returnSub(String str) {
        int d;

        if (isNull(str))
            return -2;

        if (!isNull(str) && str.length() == 1) {
            if (str.equals("π") || str.equals("ₙ"))
                return -3;
            if (str.equals("ₑ"))
                return -4;

            for (d = 0; d < 10; d++) {
                if (str.equals(subscripts[d])) {
                    return d;
                }
            }
        }

        return -1;
    }

    public static float getMinimumWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        return (dpHeight == dpWidth) ? dpWidth : Math.min(dpHeight, dpWidth);
    }

    public static int getOrientation(Context context) {
        try {
            return context.getResources().getConfiguration().orientation;
        }
        catch (Exception e) {
            e.printStackTrace();

            return MainActivity.mainActivity.getResources().getConfiguration().orientation;
        }
    }

    public static boolean isExtraButtonCode(String code) {
        int i;

        if (isNull(code) || code.length() < 1)
            return false;

        String[] extraCodes = {"-bop", "-btt", "-bINV2", "-bINV2t"};

        for (i=0; i < extraCodes.length; i++) {
            if (extraCodes[i].equals(code))
                return true;
        }

        return buttonExists(code.replace("-b", ""));
    }

    public static boolean buttonExists(String buttonText){
        int i, j;

        if (isNull(buttonText) || buttonText.length() < 1)
            return false;

        if (isNum(buttonText))
            return true;

        final Activity main = MainActivity.mainActivity;

        final Button[] compBar = {main.findViewById(R.id.bSqrt), main.findViewById(R.id.bExp), main.findViewById(R.id.bFact), main.findViewById(R.id.bLog), main.findViewById(R.id.bLn)};
        final Button[] trigBar = {main.findViewById(R.id.bSin), main.findViewById(R.id.bCos), main.findViewById(R.id.bTan), main.findViewById(R.id.bCsc), main.findViewById(R.id.bSec), main.findViewById(R.id.bCot), main.findViewById(R.id.bInv)};
        final Button[] mainOps = {main.findViewById(R.id.sPlus), main.findViewById(R.id.sMinus), main.findViewById(R.id.sMulti), main.findViewById(R.id.sDiv)};

        final Button bDec = main.findViewById(R.id.bDec);
        final Button bParenthesisOpen = main.findViewById(R.id.bParenthesisOpen);
        final Button bParenthesisClose = main.findViewById(R.id.bParenthesisClose);
        final Button bEquals = main.findViewById(R.id.bEquals);
        final Button bMod = main.findViewById(R.id.bMod);

        final Button[][] allButtons = {compBar, trigBar, mainOps, {bDec, bParenthesisOpen, bParenthesisClose, bEquals, bMod}};

        final String[] topBarCodes = {"-b√", "-b%", "-b^", "-b!", "-bπ", "-be", "-blog", "-bln", "-bsin", "-bcos", "-btan", "-bcsc", "-bsec", "-bcot", "-bINV"};

        if (buttonText.endsWith("t") && !buttonText.endsWith("ot")) {
            buttonText = newTrim(buttonText, 1);

            if (isNull(buttonText) || buttonText.length() < 1)
                return false;

            if (isNum(buttonText))
                return true;
        }

        for (i=0; i < topBarCodes.length; i++){
            if (buttonText.equals(topBarCodes[i]))
                return true;
        }

        for (i=0; i < allButtons.length; i++){
            for (j=0; j < allButtons[i].length; j++){
                if (i == 3 && j == 4 && buttonText.equals("ⁿ√"))
                    return true;

                if (allButtons[i][j] != null) {
                    if (allButtons[i][j].getText().toString().equals(buttonText))
                        return true;
                }
            }
        }

        return false;
    }

    public static boolean isLegacyCode(String str){
        String[] cmdCodes = {"-p", "-s", "-t", "-k", "-m", "-tt", "-kt", "-ft", "-bp", "-bs", "-bm", "-bd", "-f", "-e"};

        if (isNull(str) || str.length() < 2)
            return false;

        if (!str.startsWith("-"))
            return false;

        int i;

        for (i=0; i < cmdCodes.length; i++){
            if (str.equals(cmdCodes[i]))
                return true;
        }

        return false;
    }

    public static boolean isButtonCode(String str){
        if (isNull(str))
            return false;

        if (!isLegacyCode(str)) {
            if (str.equals("-mt"))
                return true;

            if (!str.startsWith("-b"))
                return false;

            String buttonText = str.substring(2);

            if (buttonText.endsWith("t") && !buttonText.endsWith("ot"))
                buttonText = newTrim(buttonText, 1);

            return buttonExists(buttonText);
        }
        else
            return true;
    }

    public static boolean isNull(Object input){
        if (input == null)
            return true;

        if (input.getClass() == String.class) {
            String str = (String) input;

            return str.equals("\0") || str.equals("");
        }
        else if (input.getClass() == Button.class){
            Button button = (Button) input;

            return button.getText() == null || button.getText().toString().equals("\0");
        }
        else if (input.getClass() == TextView.class){
            TextView tv = (TextView) input;

            return tv.getText() == null || tv.getText().toString().equals("\0");
        }
        else if (input.getClass() == EditText.class){
            EditText tv = (EditText) input;

            return tv.getText() == null || tv.getText().toString().equals("\0");
        }
        else if (input.getClass() == SpannableStringBuilder.class){
            SpannableStringBuilder cs = (SpannableStringBuilder) input;

            if (cs.length() == 0)
                return true;

            return cs.toString().equals("\0");
        }

        return false;
    }

    //TODO: Make this throw an IllegalArgumentException
    public static boolean isGray(String str){
        if (!isColor(str))
            return false;

        if (chat(str, 1).equals(chat(str, 3)) && chat(str, 3).equals(chat(str, 5)))
            return true;

        int i, j;

        String[] values = {chat(str, 1), chat(str, 3), chat(str, 5)};
        String extraValues = "ABCDEF";
        int[] valueInts = new int[values.length];
        final int range = 1;

        for (i=0; i < values.length; i++) {
            if (isDigit(values[i]))
                valueInts[i] = Integer.parseInt(values[i]);
            else {
                for (j=0; j < extraValues.length(); j++){
                    if (chat(extraValues, j).equalsIgnoreCase(values[i])) {
                        valueInts[i] = 9 + j + 1;
                        break;
                    }
                }
            }
        }

        int max = valueInts[0], min = valueInts[0];

        for (i=1; i < valueInts.length; i++){
            if (valueInts[i] > max)
                max = valueInts[i];
            else if (valueInts[i] < min)
                min = valueInts[i];
        }

        //Log.d("hex", str + "\n");
        //Log.d("valueInts", valueInts[0] + "  " + valueInts[1] + "  " + valueInts[2]);
        //Log.d("max/min", "max: " + max + ", min: " + min);

        return Math.abs(max - min) <= range;
    }

    public static boolean isGray(String str, int tolerance){
        if (!isColor(str))
            return false;

        if (chat(str, 1).equals(chat(str, 3)) && chat(str, 3).equals(chat(str, 5)))
            return true;

        int i, j;

        String[] values = {chat(str, 1), chat(str, 3), chat(str, 5)};
        String extraValues = "ABCDEF";
        int[] valueInts = new int[values.length];

        for (i=0; i < values.length; i++) {
            if (isDigit(values[i]))
                valueInts[i] = Integer.parseInt(values[i]);
            else {
                for (j=0; j < extraValues.length(); j++){
                    if (chat(extraValues, j).equalsIgnoreCase(values[i])) {
                        valueInts[i] = 9 + j + 1;
                        break;
                    }
                }
            }
        }

        int max = valueInts[0], min = valueInts[0];

        for (i=1; i < valueInts.length; i++){
            if (valueInts[i] > max)
                max = valueInts[i];
            else if (valueInts[i] < min)
                min = valueInts[i];
        }

        Log.d("hex", str + "\n");
        Log.d("valueInts", valueInts[0] + "  " + valueInts[1] + "  " + valueInts[2]);
        Log.d("max/min", "max: " + max + ", min: " + min);

        return Math.abs(max - min) <= tolerance;
    }

    public static boolean tinyEquals(String s1, String s2){
        if (isNull(s1) || isNull(s2))
            return false;

        s1 = tinydb().getString(s1);
        s2 = tinydb().getString(s2);

        if (isNull(s1) || isNull(s2))
            return false;

        return s1.equals(s2);
    }

    public static boolean isTinyColor(String key){
        return isColor(tinydb().getString(key));
    }

    public static int getTinyColor(String key){
        if (key == null)
            return Color.WHITE;

        return Color.parseColor(tinydb().getString(key));
    }

    public static boolean isTinyColor(String key, TinyDB tinydb){
        if (tinydb == null)
            tinydb = tinydb();

        return isColor(tinydb.getString(key));
    }

    public static int getTinyColor(String key, TinyDB tinydb){
        if (key == null)
            return Color.WHITE;

        if (tinydb == null)
            tinydb = tinydb();

        return Color.parseColor(tinydb.getString(key));
    }

    public static boolean isColor(String str){
        int i;

        if (isNull(str))
            return false;
        if (!str.startsWith("#"))
            return false;
        if (str.length() != 7)
            return false;

        str = str.substring(1);

        for (i=0; i < str.length(); i++) {
            if (i > 0 && chat(str, i).equals(chat(str, i-1)))
                continue;

            if (!isLetter(chat(str, i), 6) && !isDigit(chat(str, i)))
                return false;
        }

        return true;
    }

    public static String colorToUpper(String hex){
        int h;

        if (isNull(hex))
            return null;
        if (!isColor(hex))
            return hex;

        for (h = 1; h < hex.length(); h++) {
            if (!isDigit(chat(hex, h)) && !chat(hex, h).equals("#"))
                hex = hex.replace(hex.charAt(h), chat(hex, h).toUpperCase().charAt(0));
        }

        return hex;
    }

    public static String hexAdd(String hex, int n) {
        int i, j, k;
        int initN;

        String[] hexCharsCaps = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

        if (isNull(hex) || !isColor(hex))
            return "";
        if (n == 0)
            return hex;

        try {
            if (n < 0 && n > -10 && chat(hex, 1).equals("0") && chat(hex, 3).equals("0") && chat(hex, 5).equals("0") && (isDigit(chat(hex, 2)))) {
                if (Integer.parseInt(chat(hex, 2)) < Math.abs(n))
                    return "#000000";
            }
        }
        catch (Exception ignored) {}

        if (n > 15) {
            initN = n;

            for (i=0; i < initN / 15; i++) {
                hex = hexAdd(hex, 15);
                n -= 15;
            }
        }
        else if (n < -15) {
            initN = n;

            for (i=0; i < (initN * -1) / 15; i++) {
                hex = hexAdd(hex, -15);
                n += 15;
            }
        }

        //Log.d("initHex", hex);

        for (i = 1; i < hex.length(); i += 2) {
            for (j = 0; j < hexCharsCaps.length; j++) {
                if (chat(hex, i + 1) != null && chat(hex, i + 1).equalsIgnoreCase(hexCharsCaps[j])) {
                    if (Math.abs(j + n) <= hexCharsCaps.length - 1 && j + n >= 0) {
                        hex = replaceChar(hex, i + 1, hexCharsCaps[j + n]);
                    }
                    else {
                        if (n > 0) {
                            int n1, addAmt = 1;

                            if (n <= 16)
                                n1 = n - (hexCharsCaps.length - j);
                            else {
                                n1 = (n % hexCharsCaps.length) + j;

                                if (n1 > 15)
                                    n1 = n1 % hexCharsCaps.length;

                                addAmt = (n / hexCharsCaps.length);

                                if (n % hexCharsCaps.length + j >= hexCharsCaps.length)
                                    addAmt++;
                            }

                            hex = replaceChar(hex, i + 1, hexCharsCaps[n1]);

                            for (k = 0; k < hexCharsCaps.length; k++) {
                                if (chat(hex, i).equalsIgnoreCase(hexCharsCaps[k]) && !chat(hex, i).equalsIgnoreCase("F")) {
                                    if (k + addAmt > 15)
                                        hex = replaceChar(hex, i, "F");
                                    else
                                        hex = replaceChar(hex, i, hexCharsCaps[k + addAmt]);

                                    break;
                                }
                            }
                        }
                        else {
                            int n1, subAmt = 1;

                            if (Math.abs(n) <= 16)
                                n1 = hexCharsCaps.length + (n + j);
                            else {
                                n1 = (-1 * (Math.abs(n) % hexCharsCaps.length)) + j;

                                if (Math.abs(n1) > 15)
                                    n1 = (Math.abs(n1) % hexCharsCaps.length) * -1;

                                subAmt = (n / hexCharsCaps.length) * -1;
                            }

                            if (n1 < 0)
                                n1 *= -1;

                            //Log.d("n", Integer.toString(n));

                            hex = replaceChar(hex, i + 1, hexCharsCaps[n1]);

                            for (k = 0; k < hexCharsCaps.length; k++) {
                                if (chat(hex, i).equalsIgnoreCase(hexCharsCaps[k]) && !chat(hex, i).equals("0")) {
                                    if (k - subAmt < 0)
                                        hex = replaceChar(hex, i, "0");
                                    else
                                        hex = replaceChar(hex, i, hexCharsCaps[k - subAmt]);

                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }

        //Log.d("finalHex", hex);

        return hex;
    }

    private static String replaceChar(String str, int index, String newChar){
        if (str == null || newChar == null || index >= str.length() || str.length() < 1)
            return "";

        return str.substring(0, index) + newChar + str.substring(index + 1);
    }

    public static String getStrArray (String[] array){
        int i;
        String str = "";

        if (array == null || (array[0] == null || array[0].equals("\0")) && (array[1] == null || array[1].equals("\0")))
            return " ";

        if (array.length > 100) {
            for (i = 0; i < array.length; i++) {
                if (!isNull(array[i])) {
                    str += array[i];
                    str += " ";
                }
                else if (!isNull(array[i + 1]) && isNull(array[i]) && i != 0) {
                    str += "� ";
                }

                try {
                    if ((array[i + 1] == null || array[i + 1].equals("\0")) && (isNull(array[i + 2])))
                        break;
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        else {
            try {
                str = Arrays.toString(array).replace("[", "").replace("]", "").replace(",", "");
            }
            catch (Exception e){
                e.printStackTrace();
                str = " ";
            }
        }

        return str;
    }

    public static String getDubArray (double[] array) {
        int i;
        String str = "";

        if (array == null)
            return "0";

        for (i=0; i < array.length; i++){
            str += Double.toString(array[i]);
            str += " ";

            if (array[i+1] == 0 && array[i+2] == 0 && array[i+3] == 0 && array[i+4] == 0 && array[i+5] == 0)
                break;
        }

        return str;
    }

    /**
     * Returns the index of the first instance of the target string found in the full string. For example, Aux.searchFor("3.14", ".") will return 1.
     * @return Index of first instance of target string in full string
     */
    public static int searchFor(String full, String target){
        int i, length;

        if (full == null || !full.contains(target))
            return -1;

        if (full.endsWith(target))
            return full.length() - target.length();

        length = full.length();

        for (i=0; i < length; i++){
            if (chat(full, i).equals(target)){
                return i;
            }
        }

        return -1;
    }

    public static Drawable getSheetBackground(int theme){
        if (theme == 1)
            return ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.rounded_dialog);
        else if (theme == 2)
            return ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.rounded_dialog_light);
        else
            return ContextCompat.getDrawable(MainActivity.mainActivity, R.drawable.rounded_dialog_black);
    }
}
