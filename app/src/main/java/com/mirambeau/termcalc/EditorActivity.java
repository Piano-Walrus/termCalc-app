package com.mirambeau.termcalc;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

public class EditorActivity extends AppCompatActivity {
    final Activity main = MainActivity.mainActivity;
    static Activity editorActivity;

    public static String THEME_DARK = "3";
    public static String THEME_LIGHT = "2";

    ArrayList<BackupCard> cards = new ArrayList<>();

    String cmd;

    ArrayList<String> setButtons = new ArrayList<>();
    ArrayList<String> setZones = new ArrayList<>();

    ArrayList<ThemeHistoryStep> entries = new ArrayList<>();
    int currentEntry = -1;
    final int darkGray = Color.parseColor("#222222");

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

    ViewGroup[] containers;
    Button[] zoneButtons;

    //Keypad, Primary, Secondary, Secondary, Tertiary, Main
    String[][] defaultBGByTheme = {{}, {"#202227", "#03DAC5", "#00B5A3", "#00B5A3", "#00C5B1", "#272C33"}, {"#FFFFFF", "#03DAC5", "#53E2D4", "#53E2D4", "#3CDECE", "#FFFFFF"},
            {"#000000", "#03DAC5", "#00B5A3", "#00B5A3", "#00C5B1", "#000000"}};
    String[][] defaultTextByTheme = {{}, {"#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF"}, {"#222222", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#222222"},
            {"#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF"}};

    final String[][] allCodes = {{"cPrimary", "cSecondary", "cTertiary", "cMain", "cKeypad", "-bop", "cTop", "-btt", "-mt", "cNum"}, {"-b÷", "-b×", "-b-", "-b+"}, {"-b0", "-b1", "-b2", "-b3", "-b4", "-b5", "-b6", "-b7", "-b8", "-b9"},
            {"-b=", "-b.", "-b(", "-b)", "-bfc", "cFab", "cFabText"}, {"-b√", "-b^", "-b!", "-bπ", "-be", "-b%", "-blog", "-bln"}, {"-bsin", "-bcos", "-btan", "-bcsc", "-bsec", "-bcot", "-bINV", "-bINV2"}};
    ArrayList<String> zoneCodes = new ArrayList<>();

    ViewGroup[] bigContainers;
    ViewGroup[] bigZones;

    boolean isAll, isRestore, isHex, ftIsSecondary;
    boolean themeChanged = false;

    int newTheme;
    int bigPosition;

    String bigTheme;

    private BackupAdapter adapter;
    RecyclerView backupsRv;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_editor);

            final Vibrator vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

            final TinyDB tinydb = new TinyDB(this);
            editorActivity = this;

            int i, j;

            THEME_DARK = tinydb.getBoolean("darkStatusBar") ? "1" : "3";

            try {
                String[] zoneCodesStr = {"cKeypad", "cPrimary", "cSecondary", "cTertiary", "cMain"};

                for (i=0; i < zoneCodesStr.length; i++) {
                    zoneCodes.add(zoneCodesStr[i]);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            try {
                bigContainers = new ViewGroup[]{findViewById(R.id.editorKeypad), findViewById(R.id.editorOps), findViewById(R.id.editorScrollBar), findViewById(R.id.editorTertiaryButtons), findViewById(R.id.previewLayout)};
                bigZones = new ViewGroup[]{findViewById(R.id.editorKeypadZone), findViewById(R.id.editorPrimaryZone), findViewById(R.id.editorSecondaryZone), findViewById(R.id.editorTertiaryZone), findViewById(R.id.editorZonesLayout)};
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(Color.parseColor("#16181B"));
                getWindow().setNavigationBarColor(Color.parseColor("#16181B"));
            }

            final Toolbar toolbar = findViewById(R.id.editorToolbar);

            toolbar.setTitle("Theme Editor");
            toolbar.showOverflowMenu();
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_light);

            setTheme(R.style.ThemeOverlay_AppCompat_Dark);

            toolbar.setOverflowIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_overflow_menu, null));

            final int theme = Aux.getThemeInt();

            newTheme = Aux.getThemeInt();

            //Initialize BackupCards
            File directory = new File(this.getFilesDir(), "themes");
            File[] files = directory.listFiles();

            if (files != null) {
                if (files.length > 0) {
                    int f;

                    for (f = 0; f < files.length; f++) {
                        String themeName = files[f].getName();

                        if (themeName.endsWith(".txt")) {
                            themeName = themeName.replace(".txt", "");

                            String[] colors = new String[0];

                            try {
                                colors = getColorsFromFile(files[f]);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            String[] newColors = new String[6];

                            if (colors.length > 6) {
                                for (i = 0; i < 6; i++)
                                    newColors[i] = colors[i];
                            }

                            boolean isFavorite = tinydb.getBoolean(themeName + "-favorite");

                            if (isFavorite) {
                                if (colors.length > 6)
                                    cards.add(0, new BackupCard(files[f].getName().replace(".txt", ""), newColors, colors[6]));
                                else if (colors.length == 6)
                                    cards.add(0, new BackupCard(files[f].getName().replace(".txt", ""), colors));
                                else
                                    cards.add(0, new BackupCard(files[f].getName().replace(".txt", "")));

                                cards.get(0).setFavorite(isFavorite);
                            }
                            else {
                                if (colors.length > 6)
                                    cards.add(new BackupCard(files[f].getName().replace(".txt", ""), newColors, colors[6]));
                                else if (colors.length == 6)
                                    cards.add(new BackupCard(files[f].getName().replace(".txt", ""), colors));
                                else
                                    cards.add(new BackupCard(files[f].getName().replace(".txt", "")));

                                cards.get(cards.size() - 1).setFavorite(isFavorite);
                            }
                        }
                    }
                }
                else {
                    Log.d("printf", "\nError: No theme backups currently exist.");
                }
            }
            else {
                Log.d("printf", "\nError: No theme backups currently exist.");
            }

            adapter = new BackupAdapter(cards);

            backupsRv = findViewById(R.id.editorBackupsRv);
            backupsRv.setHasFixedSize(true);
            backupsRv.setLayoutManager(new LinearLayoutManager(this));
            backupsRv.setAdapter(adapter);

            adapter.setOnItemClickListener(new BackupAdapter.OnItemClickListener() {
                @Override
                public void onDeleteClick(final int position) {
                    final String title = cards.get(position).getThemeName();

                    AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                    builder.setTitle("Are you sure you want to delete \"" + title + "\"?\n");

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            try {
                                run("delete " + title);
                                removeItem(position);
                                Toast.makeText(EditorActivity.this, "Successfully deleted \"" + title + "\"", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
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

                @Override
                public void onApplyClick(int position) {
                    final String title = cards.get(position).getThemeName();

                    tinydb.putBoolean("custom", true);

                    Toast.makeText(EditorActivity.this, "Successfully restored \"" + title + "\"", Toast.LENGTH_SHORT).show();

                    HandlerThread thread = new HandlerThread("MyHandlerThread");
                    thread.start();

                    new Handler(thread.getLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            newRun("restore " + title);

                            applyTheme();
                            closeDrawer();
                        }
                    }, 5);
                }

                @Override
                public void onShareClick(int position) {
                    final String title = cards.get(position).getThemeName();

                    try {
                        run("share " + title);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRenameClick(int position) {
                    final String title = cards.get(position).getThemeName();

                    bigPosition = position;
                    rename(title);
                }

                @Override
                public void onFavoriteClick(int position) {
                    cards.get(position).setFavorite(!cards.get(position).getFavorite());
                    tinydb.putBoolean(cards.get(position).getThemeName() + "-favorite", cards.get(position).getFavorite());

                    adapter.notifyItemChanged(position);
                }
            });

            //end of new stuff

            //Primary, Secondary, Tertiary, Main, Keypad
            String[][] defaultByTheme = {{}, {"#03DAC5", "#00B5A3", "#00C5B1", "#272C33", "#202227"}, {"#03DAC5", "#53E2D4", "#3CDECE", "#FFFFFF", "#FFFFFF"},
                    {"#03DAC5", "#00B5A3", "#00C5B1", "#000000", "#000000"}};

            String[] defaultMisc = {"#202227", "#202227", "#03DAC5", "#3CDECE", "#3CDECE"};

            // ------ ~Zones~ ------
            final ConstraintLayout zonesLayout = findViewById(R.id.editorZonesLayout);

            final ConstraintLayout eMain = findViewById(R.id.previewLayout);
            final ConstraintLayout eKeypad = findViewById(R.id.editorKeypad);

            final ConstraintLayout primaryButtons = findViewById(R.id.editorOps);
            final ConstraintLayout primaryZone = findViewById(R.id.editorPrimaryZone);
            final ConstraintLayout secondaryButtons = findViewById(R.id.editorScrollBar);
            final ConstraintLayout secondaryZone = findViewById(R.id.editorSecondaryZone);
            final ConstraintLayout tertiaryButtons = findViewById(R.id.editorTertiaryButtons);
            final ConstraintLayout tertiaryZone = findViewById(R.id.editorTertiaryZone);

            // ------ ~Editor Buttons~ ------
            final FloatingActionButton delete = findViewById(R.id.editorDelete);
            final FloatingActionButton customs = findViewById(R.id.editorExpandCustoms);

            final ConstraintLayout zoneToggleLayout = findViewById(R.id.zoneToggleLayout);

            final ImageButton undo = findViewById(R.id.undo);
            final ImageButton redo = findViewById(R.id.redo);
            final FloatingActionButton apply = findViewById(R.id.editorApply);

            final Button[] tabs = {findViewById(R.id.individualTab), findViewById(R.id.zonesTab)};
            final ConstraintLayout selectedTab = findViewById(R.id.editorSelectedTab);

            final View keypadDivider = findViewById(R.id.keypadDivider);
            final View scrollbarDivider = findViewById(R.id.scrollbarDivider);

            final Button bMod = findViewById(R.id.eBMod);

            final String modSymbol = tinydb.getString("modSymbol");

            bMod.setText(modSymbol != null && !modSymbol.equals("\0") && modSymbol.length() > 0 ? modSymbol : "mod");

            zonesLayout.setVisibility(View.GONE);

            for (i=0; i < tabs.length; i++) {
                final int fi = i;

                tabs[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Move the tab selection drawable to the selected tab
                        ((ViewGroup) selectedTab).getLayoutTransition()
                                .enableTransitionType(LayoutTransition.CHANGING);

                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(zoneToggleLayout);

                        constraintSet.connect(R.id.editorSelectedTab, ConstraintSet.START, tabs[fi].getId(), ConstraintSet.START, 0);
                        constraintSet.connect(R.id.editorSelectedTab, ConstraintSet.END, tabs[fi].getId(), ConstraintSet.END, 0);
                        constraintSet.applyTo(zoneToggleLayout);

                        View[] elementsToHide = {findViewById(R.id.editorDelete), findViewById(R.id.editorExpand), findViewById(R.id.editorExpandCustoms), findViewById(R.id.editorHMB),
                                findViewById(R.id.editorDegRad), findViewById(R.id.editorOverflow), findViewById(R.id.editorPreviewTitle)};

                        int[] visibilities = {View.GONE, View.VISIBLE};

                        zonesLayout.setVisibility(visibilities[fi]);

                        if (Aux.isTinyColor("cPrimary") && Aux.isTinyColor("cKeypad") && Aux.getTinyColor("cPrimary") == Aux.getTinyColor("cKeypad"))
                            keypadDivider.setVisibility(visibilities[fi]);

                        if (Aux.isTinyColor("cSecondary") && Aux.isTinyColor("cTertiary") && Aux.getTinyColor("cSecondary") == Aux.getTinyColor("cTertiary"))
                            scrollbarDivider.setVisibility(visibilities[fi]);

                        //Sets visibility of each element depending on which radio button is selected.
                        for (View element : elementsToHide) {
                            element.setVisibility(visibilities[Math.abs(fi-1)]);
                        }
                    }
                });
            }

            ImageButton drawerButton = findViewById(R.id.drawerButton);
            ConstraintLayout drawerBG = findViewById(R.id.editorDrawerBG);

            drawerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDrawer();
                }
            });

            drawerBG.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeDrawer();
                }
            });



            undoCheck();

            undo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        undo();
                    }
                    catch (Exception e) {
                        Aux.saveStack(e);
                    }
                }
            });

            redo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        redo();
                    }
                    catch (Exception e) {
                        Aux.saveStack(e);
                    }
                }
            });

            //Set colors in current theme
            applyTheme();

            containers = new ViewGroup[]{eKeypad, primaryButtons, findViewById(R.id.editorSecondaryLinear1), findViewById(R.id.editorSecondaryLinear2), tertiaryButtons};

            zoneButtons = new Button[]{findViewById(R.id.keypadZoneButton), findViewById(R.id.primaryZoneButton), findViewById(R.id.secondaryZoneButton),
                    findViewById(R.id.tertiaryZoneButton), findViewById(R.id.mainZoneButton)};

            final String[] zoneTextCodes = {"cNum", "-bop", "cTop", "cTop", Aux.isTinyColor("-btt") ? "-btt" : "cTop"};

            //Set zone onClick listeners
            for (i=0; i < zoneButtons.length; i++) {
                final int fi = i;
                zoneButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ThemeActivity.shouldRecreateMain = true;

                        final Button button = (Button) view;

                        int o;
                        int defaultTextColor = fi == 0 && theme == 2 ? darkGray : Color.WHITE;

                        final AlertDialog.Builder builder = createAlertDialog("Select Color");

                        final View viewInflated = LayoutInflater.from(EditorActivity.this).inflate(R.layout.bg_text_dialog, (ViewGroup) findViewById(R.id.editorBG), false);

                        builder.setView(viewInflated);

                        final String[] codes = view.getTag().toString().split(" ");
                        final String[] titles = {"Background Color", "Text Color"};

                        final ImageButton bgCircle = viewInflated.findViewById(R.id.bg);
                        final ImageButton bgIcon = viewInflated.findViewById(R.id.bgIcon);
                        final ImageButton textIcon = viewInflated.findViewById(R.id.txtIcon);

                        final ConstraintLayout[] dialogOptions = {viewInflated.findViewById(R.id.bgLayout), viewInflated.findViewById(R.id.txtLayout)};

                        final AlertDialog alertDialog = builder.create();

                        bgCircle.setColorFilter(Aux.getBackgroundColor(fi == 4 ? findViewById(R.id.previewLayout) : (View) button.getParent()));

                        try {
                            textIcon.setColorFilter(fi == 1 ? ((TextView) findViewById(R.id.primaryZoneButtonText)).getCurrentTextColor() : button.getCurrentTextColor());
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            textIcon.setColorFilter(Aux.isTinyColor(codes[1]) ? Aux.getTinyColor(codes[1]) : defaultTextColor);
                        }

                        for (o=0; o < dialogOptions.length; o++) {
                            final int fo = o;

                            dialogOptions[o].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();

                                    openCPAlertDialog(R.layout.new_cpdialog, titles[fo], "Set Color", "Cancel", codes[fo], button, fo);
                                }
                            });
                        }

                        alertDialog.show();
                    }
                });
            }

            //Set button onClick listeners
            for (i=0; i < containers.length; i++) {
                for (j = 0; j < containers[i].getChildCount(); j++) {
                    if (containers[i].getChildAt(j).getClass() == androidx.appcompat.widget.AppCompatButton.class) {
                        final Button button = (Button) containers[i].getChildAt(j);

                        if (button != null) {
                            String text = button.getText().toString();
                            final String bgCode = (button == findViewById(R.id.eBInv2)) ? "-bINV2" : "-b" + text;
                            final String textCode = (button == findViewById(R.id.eBInv2)) ? "-bINV2t" : "-b" + text + "t";

                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ThemeActivity.shouldRecreateMain = true;

                                    int o;

                                    final AlertDialog.Builder builder = createAlertDialog("Set Color");

                                    final View viewInflated = LayoutInflater.from(EditorActivity.this).inflate(R.layout.bg_text_dialog, (ViewGroup) findViewById(R.id.editorBG), false);

                                    builder.setView(viewInflated);

                                    final String[] codes = {bgCode, textCode};
                                    final String[] titles = {"Background Color", "Text Color"};

                                    final ImageButton bgCircle = viewInflated.findViewById(R.id.bg);
                                    final ImageButton textIcon = viewInflated.findViewById(R.id.txtIcon);

                                    final ConstraintLayout[] dialogOptions = {viewInflated.findViewById(R.id.bgLayout), viewInflated.findViewById(R.id.txtLayout)};

                                    final AlertDialog alertDialog = builder.create();

                                    bgCircle.setColorFilter(Aux.getBackgroundColor(button));
                                    textIcon.setColorFilter(button.getCurrentTextColor());

                                    for (o=0; o < dialogOptions.length; o++) {
                                        final int fo = o;

                                        dialogOptions[o].setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                alertDialog.dismiss();

                                                openCPAlertDialog(R.layout.new_cpdialog, titles[fo], "Set Color", "Cancel", codes[fo], button, fo);
                                            }
                                        });
                                    }

                                    alertDialog.show();
                                }
                            });
                        }
                    }
                }
            }

            final FloatingActionButton[] fabs = {delete, customs};
            final String[] fabBGCodes = {"cFab", "-bfc"};
            final String[] fabTextCodes = {"cFabText", "-bfct"};

            for (i=0; i < fabs.length; i++) {
                int f;
                final int fi = i;
                String backupPrimary, backupText;

                if (i == 0)
                    backupPrimary = Aux.isTinyColor("cPrimary") ? tinydb.getString("cPrimary") : "#03DAC5";
                else
                    backupPrimary = Aux.isTinyColor("cFab") ? tinydb.getString("cFab") : (Aux.isTinyColor("cPrimary") ? tinydb.getString("cPrimary") : "#03DAC5");

                try {
                    backupPrimary = Aux.toColorString(fabs[i].getBackgroundTintList().getDefaultColor());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                backupText = (Aux.isTinyColor(fabTextCodes[i]) ? tinydb.getString(fabTextCodes[i]) : Aux.isTinyColor("cFabText") ? tinydb.getString("cFabText") : (Aux.isTinyColor("-bop") ? tinydb.getString("-bop") : "#FFFFFF"));

                try {
                    for (f = entries.size() - 1; f > 0; f--) {
                        if (entries.get(f).code.equals(fabTextCodes[i])) {
                            backupText = entries.get(f).finalColor;

                            break;
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                fabs[i].setTag((Aux.isTinyColor(fabBGCodes[i]) ? tinydb.getString(fabBGCodes[i]) : backupPrimary)
                        + "~" + backupText);

                fabs[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ThemeActivity.shouldRecreateMain = true;

                        int o;

                        final AlertDialog.Builder builder = createAlertDialog("Select Color");

                        final View viewInflated = LayoutInflater.from(EditorActivity.this).inflate(R.layout.bg_text_dialog, (ViewGroup) findViewById(R.id.editorBG), false);

                        builder.setView(viewInflated);

                        final String[] codes = {fabBGCodes[fi], fabTextCodes[fi]};
                        final String[] titles = {"Background Color", "Text Color"};

                        final ImageButton bgCircle = viewInflated.findViewById(R.id.bg);
                        final ImageButton textIcon = viewInflated.findViewById(R.id.txtIcon);

                        final ConstraintLayout[] dialogOptions = {viewInflated.findViewById(R.id.bgLayout), viewInflated.findViewById(R.id.txtLayout)};

                        final AlertDialog alertDialog = builder.create();

                        try {
                            bgCircle.setColorFilter(fabs[fi].getBackgroundTintList().getDefaultColor());
                        } catch (Exception e) {
                            e.printStackTrace();

                            try {
                                bgCircle.setColorFilter(Aux.isTinyColor(fabBGCodes[fi]) ? Aux.getTinyColor(fabBGCodes[fi]) : Aux.isTinyColor("cPrimary") ? Aux.getTinyColor("cPrimary") : Color.parseColor("#53E2D4"));
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }

                        textIcon.setColorFilter(fabs[fi].getColorFilter());

                        for (o = 0; o < dialogOptions.length; o++) {
                            final int fo = o;

                            dialogOptions[o].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();

                                    openCPAlertDialog(R.layout.new_cpdialog, titles[fo], "Set Color", "Cancel", codes[fo], fabs[fi], fo);
                                }
                            });
                        }

                        alertDialog.show();
                    }
                });
            }

            //Initialize ArrayLists
            for (i=0; i < allCodes.length; i++) {
                for (String code : allCodes[i]) {
                    //Initialize "setZones" array
                    if (i == 0) {
                        if (Aux.isTinyColor(code))
                            setZones.add(code);
                    }
                    //Initialize "setButtons" array
                    else {
                        if (code.startsWith("-b")) {
                            if (Aux.isTinyColor(code))
                                setButtons.add(code);
                            if (Aux.isTinyColor(code + "t"))
                                setButtons.add(code + "t");
                        }
                        else if (code.startsWith("cFab")) {
                            if (Aux.isTinyColor(code)) {
                                setButtons.add(code);
                            }
                        }
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

    public AlertDialog openCPAlertDialog(int layout, final String title, String positive, String negative, final String code, final View button, final int option) {
        try {
            final TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

            int[] bigColors = {Color.WHITE, Color.WHITE};
            String manualBG = "#FFFFFF", manualText = "#FFFFFF";
            boolean isManual = false;
            boolean isFab = false;

            final AlertDialog.Builder builder = createAlertDialog(title);

            final View viewInflated = LayoutInflater.from(EditorActivity.this).inflate(layout, (ViewGroup) findViewById(R.id.editorBG), false);

            ClipboardColorAdapter adapter;
            final ArrayList<String> colors = new ArrayList<>();

            final ConstraintLayout cpLayout = viewInflated.findViewById(R.id.cpLayout);
            final ImageButton cpButton = viewInflated.findViewById(R.id.bgPrimary2);
            final ImageButton icon = viewInflated.findViewById(R.id.cpPaletteIcon);

            final RecyclerView recyclerView = viewInflated.findViewById(R.id.clipboardRv);

            final EditText hexField = viewInflated.findViewById(R.id.hexField);

            final TextView errorText = viewInflated.findViewById(R.id.errorText);

            final ImageButton copy = viewInflated.findViewById(R.id.copyHex);

            //Set color picker icon depending on option
            try {
                int[] iconPadding = {icon.getPaddingLeft(), icon.getPaddingTop(), icon.getPaddingRight(), icon.getPaddingBottom()};

                icon.setImageDrawable(option == 0 ? Aux.getDrawable(R.drawable.ic_baseline_format_color_fill_28) : Aux.getDrawable(R.drawable.ic_baseline_format_color_text_24));
                icon.setPadding(iconPadding[0], option == 0 ? iconPadding[1] + 6 : iconPadding[1], iconPadding[2], option == 0 ? iconPadding[3] : iconPadding[3] + 8);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (positive.contains("#")) {
                    String[] temp = positive.split("~");

                    positive = temp[0];
                    manualBG = temp[1];
                    manualText = temp[2];

                    isManual = true;
                }
                else if (button.getTag().toString().contains("~") && button.getTag().toString().contains("#")) {
                    String[] temp = button.getTag().toString().split("~");

                    manualBG = temp[0];
                    manualText = temp[1];

                    isManual = true;
                    isFab = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Initialize hexFields and cpButtons
            try {
                try {
                    if (isManual) {
                        if (Aux.isColor(manualBG) && Aux.isColor(manualText)) {
                            bigColors[0] = Color.parseColor(manualBG);
                            bigColors[1] = Color.parseColor(manualText);
                        }
                    }
                    else {
                        bigColors[0] = Aux.getBackgroundColor(code.equals("cMain") ? findViewById(R.id.previewLayout) : button);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();

                    try {
                        bigColors[0] = Aux.getBackgroundColor((View) button.getParent());
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }

                if (!isManual) {
                    if (code.equals("-bop")) {
                        try {
                            bigColors[1] = ((TextView) findViewById(R.id.primaryZoneButtonText)).getCurrentTextColor();
                        } catch (Exception e) {
                            bigColors[1] = Aux.isTinyColor("-bop") ? Aux.getTinyColor("-bop") : Color.WHITE;
                        }
                    }
                    else {
                        try {
                            bigColors[1] = ((Button) button).getCurrentTextColor();
                        } catch (Exception e) {
                            e.printStackTrace();

                            int defaultTextColor = button.getTag() != null && button.getTag().toString().contains("cKeypad") && Aux.getThemeInt() == 2 ? darkGray : Color.WHITE;

                            String textCode = "-b" + ((Button) button).getText().toString() + "t";

                            if (button.getClass() == androidx.appcompat.widget.AppCompatButton.class) {
                                bigColors[1] = (Aux.isTinyColor(textCode) ? Aux.getTinyColor(textCode) : defaultTextColor);
                            }
                        }
                    }
                }

                cpButton.setColorFilter(bigColors[option]);
                hexField.setText(Aux.toColorString(bigColors[option]).replace("#", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Hex Field Text Changed Listener
            hexField.addTextChangedListener(new TextValidator(hexField) {
                @Override
                public void validate(TextView textView, String before, String after) {
                    try {
                        if (Aux.isColor("#" + after.replace("#", ""))) {
                            cpButton.setColorFilter(Color.parseColor("#" + after.replace("#", "")));
                        }

                        if (after.contains("#")) {
                            textView.setText(after.replace("#", ""));
                        }
                    } catch (Exception e) {
                        Aux.saveStack(e);
                    }
                }
            });

            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData pData = clipboardManager.getPrimaryClip();

            builder.setView(viewInflated);

            final ViewGroup[] containers = {findViewById(R.id.editorKeypad), findViewById(R.id.editorOps), findViewById(R.id.editorSecondaryLinear1),
                    findViewById(R.id.editorSecondaryLinear2), findViewById(R.id.editorTertiaryButtons), findViewById(R.id.editorFakeToolbar)};

            final ConstraintLayout[] zones = {findViewById(R.id.editorKeypadZone), findViewById(R.id.editorPrimaryZone), findViewById(R.id.editorSecondaryZone), null,
                    findViewById(R.id.editorTertiaryZone), findViewById(R.id.editorZonesLayout)};

            final Button[] zoneButtons = {findViewById(R.id.keypadZoneButton), null, findViewById(R.id.secondaryZoneButton), null, findViewById(R.id.tertiaryZoneButton),
                    findViewById(R.id.mainZoneButton)};

            final String initResetColor = Aux.isColor(hexField.getText().toString()) ? hexField.getText().toString() : "#FFFFFF";

            final boolean finalIsFab = isFab;
            builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        String hexCode = hexField.getText().toString();
                        String formattedHex = "#" + hexCode.replace("#", "");
                        int color = Color.parseColor(formattedHex);

                        final FloatingActionButton customs = findViewById(R.id.editorExpandCustoms);
                        final FloatingActionButton delete = findViewById(R.id.editorDelete);
                        final FloatingActionButton apply = findViewById(R.id.editorApply);

                        if (Aux.isColor(formattedHex)) {
                            int i;

                            //Background Color
                            if (option == 0) {
                                boolean isZone = button.getTag() != null && !button.getTag().toString().equals("\0") && button.getTag().toString().contains(" ");

                                String initColor;
                                String tag = isZone ? button.getTag().toString() : "~";

                                //Zone
                                if (isZone && button.getTag().toString().contains(" ")) {
                                    int index = Integer.parseInt(Aux.lastChar(tag));

                                    if (index > 2)
                                        index++;

                                    if (Aux.isTinyColor(code))
                                        initColor = Aux.toColorString(Aux.getTinyColor(code));
                                    else {
                                        if (index == 2) {
                                            initColor = Aux.toColorString(Aux.getBackgroundColor(findViewById(R.id.editorScrollBar)));
                                        }
                                        else {
                                            initColor = Aux.toColorString(Aux.getBackgroundColor(containers[index]));
                                        }
                                    }

                                    if (index == 2) {
                                        containers[index + 1].setBackgroundColor(color);

                                        final ImageButton expBG1 = findViewById(R.id.editorExpandBG);
                                        final ImageButton expBG2 = findViewById(R.id.editorExpandBG2);

                                        expBG1.setBackgroundTintList(ColorStateList.valueOf(color));
                                        expBG2.setBackgroundColor(color);
                                    }
                                    else if (index == 5) {
                                        findViewById(R.id.previewLayout).setBackgroundColor(color);
                                    }

                                    containers[index].setBackgroundColor(color);

                                    zones[index].setBackgroundColor(color);
                                }
                                //Button
                                else {
                                    if (finalIsFab) {
                                        initColor = button.getTag().toString().split("~")[0];
                                        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(formattedHex)));

                                        if (code.equals("cFab"))
                                            apply.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(formattedHex)));

                                        button.setTag(formattedHex + "~" + button.getTag().toString().split("~")[1]);

                                        if (!setButtons.contains("-bfc") && code.equals("cFab")) {
                                            customs.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(formattedHex)));

                                            try {
                                                customs.setTag(delete.getTag().toString().split("~")[0] + "~" + customs.getTag().toString().split("~")[1]);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    else {
                                        initColor = Aux.toColorString(Aux.getBackgroundColor(button));
                                        button.setBackgroundColor(Color.parseColor(formattedHex));
                                    }

                                    if (!setButtons.contains(code))
                                        setButtons.add(code);
                                }

                                addHistoryEntry(button, button.getTag() != null && button.getTag().toString().contains("-b%") ? "-b%" : code, initColor, formattedHex);
                            }
                            //Text Color
                            else if (option == 1) {
                                try {
                                    boolean isZone = button.getTag() != null && !button.getTag().toString().equals("\0") && button.getTag().toString().contains(" ");

                                    String initColor;
                                    String tag = isZone ? button.getTag().toString() : "~";

                                    int defaultTextColor = tag.contains(" ") && Aux.getThemeInt() == 2 ? darkGray : Color.WHITE;

                                    if (code.equals("-bop") || code.equals("-btt") || code.equals("cTop"))
                                        defaultTextColor = Color.WHITE;

                                    //Zone
                                    if (isZone && button.getTag().toString().contains(" ")) {
                                        initColor = Aux.toColorString(Aux.isTinyColor(code) ? Aux.getTinyColor(code) : defaultTextColor);

                                        int index = Integer.parseInt(Aux.lastChar(tag));

                                        if (index > 2)
                                            index++;

                                        if (index == 4 && !setButtons.contains("-bINV2t"))
                                            ((Button) findViewById(R.id.eBInv2)).setTextColor(color);

                                        for (i = 0; i < containers[index].getChildCount(); i++) {
                                            try {
                                                View current = containers[index].getChildAt(i);

                                                if (current.getClass() == androidx.appcompat.widget.AppCompatButton.class) {
                                                    Button btn = (Button) current;

                                                    if (index == 4 && btn.getText().toString().equals("INV"))
                                                        btn.setText("INV2");

                                                    if (!setButtons.contains("-b" + btn.getText().toString() + "t"))
                                                        btn.setTextColor(color);

                                                    if (index == 4 && btn.getText().toString().equals("INV2"))
                                                        btn.setText("INV");
                                                }
                                                else if (current.getClass() == androidx.appcompat.widget.AppCompatImageButton.class) {
                                                    ((ImageButton) current).setColorFilter(color);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            if (index == 2 && i == containers[index].getChildCount() - 1) {
                                                index = 3;
                                                i = -1;
                                            }
                                        }

                                        try {
                                            if (code.equals("-bop")) {
                                                if (!isSet("cFabText")) {
                                                    delete.setColorFilter(color);
                                                    apply.setColorFilter(color);

                                                    if (!isSet("-bfct"))
                                                        customs.setColorFilter(color);
                                                }
                                            }
                                            else if (code.equals("cTop")) {
                                                ((ImageButton) findViewById(R.id.editorExpand)).setColorFilter(color);
                                            }
                                        }
                                        catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        try {
                                            zoneButtons[index].setTextColor(color);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            (index == 1 ? (TextView) findViewById(R.id.primaryZoneButtonText) : zoneButtons[2]).setTextColor(color);
                                        }
                                    }
                                    //Button
                                    else {
                                        if (finalIsFab) {
                                            initColor = button.getTag().toString().split("~")[1];
                                            ((ImageButton) button).setColorFilter(Color.parseColor(formattedHex));

                                            if (code.equals("cFabText"))
                                                apply.setColorFilter(Color.parseColor(formattedHex));

                                            button.setTag(button.getTag().toString().split("~")[0] + "~" + formattedHex);

                                            if (!setButtons.contains("-bfct") && code.equals("cFabText")) {
                                                customs.setColorFilter(Color.parseColor(formattedHex));

                                                try {
                                                    customs.setTag(customs.getTag().toString().split("~")[0] + "~" + delete.getTag().toString().split("~")[1]);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else {
                                            initColor = Aux.toColorString(((Button) button).getCurrentTextColor());
                                            ((Button) button).setTextColor(color);
                                        }

                                        if (!setButtons.contains(code))
                                            setButtons.add(code);
                                    }

                                    addHistoryEntry(button, button.getTag() != null && button.getTag().toString().contains("-b%") ? "-b%t" : code, initColor, formattedHex);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            //Handle primary-dependent defaults
                            if (code.equals("cPrimary")) {
                                if (!isSet("cNum")) {
                                    if (!setButtons.contains("-b=t")) {
                                        ((Button) findViewById(R.id.eBEquals)).setTextColor(color);
                                    }
                                }
                                if (!isSet("cFab")) {
                                    delete.setBackgroundTintList(ColorStateList.valueOf(color));
                                    apply.setBackgroundTintList(ColorStateList.valueOf(color));

                                    if (!isSet("-bfc"))
                                        customs.setBackgroundTintList(ColorStateList.valueOf(color));
                                }
                            }

                            dialog.dismiss();
                        }
                        else {
                            Aux.makeLongToast("Error: Invalid hex code");
                        }

                        try {
                            if (!code.startsWith("cFab") && !code.startsWith("-bcf")) {
                                button.setStateListAnimator(null);
                                button.setElevation(0f);
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        Aux.saveStack(e);
                        finish();
                    }
                }
            });

            builder.setNeutralButton(negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            if (positive.equals("Set Color") && negative.equals("Cancel")) {
                builder.setNegativeButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final AlertDialog.Builder confirmation = createAlertDialog("Reset " + title.toLowerCase() + " to default?");

                        confirmation.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        confirmation.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    reset(button, code, initResetColor, option);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();

                                    if (code.startsWith("-"))
                                        Aux.makeToast("Error: Failed to reset color.\nTry running \"reset " + code + "\" in the terminal.", 1);
                                    else
                                        Aux.makeToast("Error: Failed to reset color.\nTry running \"reset [button code]\" in the terminal.\n\n(Run \"help set\" for a list of button codes)", 1);
                                }
                            }
                        });

                        confirmation.show();
                    }
                });
            }

            final AlertDialog alertDialog = builder.create();

            if (recyclerView.getVisibility() == View.VISIBLE) {
                errorText.setVisibility(View.INVISIBLE);
            }

            //Populate Clipboard RecyclerView
            try {
                int numItems = pData.getItemCount();
                int c;

                for (c = 0; c < numItems; c++) {
                    try {
                        ClipData.Item item = pData.getItemAt(c);
                        String itemText = item.getText().toString();

                        if (Aux.isColor("#" + itemText.replace("#", "")))
                            colors.add("#" + itemText.replace("#", ""));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (colors.size() > 0) {
                    adapter = new ClipboardColorAdapter(colors);
                    adapter.setOnItemClickListener(new ClipboardColorAdapter.OnItemClickListener() {
                        @Override
                        public void onColorClick(int position) {
                            String hexCode = colors.get(position);

                            hexField.setText(hexCode);
                            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();

                            alertDialog.dismiss();
                        }
                    });

                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(EditorActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    recyclerView.setAdapter(adapter);
                }
                else {
                    recyclerView.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                recyclerView.setVisibility(View.GONE);
                errorText.setVisibility(View.VISIBLE);

                e.printStackTrace();
            }

            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String color = "#" + hexField.getText().toString();

                    if (Aux.isColor(color)) {
                        ClipboardManager clipboard = (ClipboardManager) EditorActivity.editorActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("color", color);
                        clipboard.setPrimaryClip(clip);

                        Aux.makeToast("\"" + color + "\" copied to clipboard", EditorActivity.editorActivity, 0);
                    }
                }
            });

            //Color Picker
            final int finalBigColor = bigColors[option];

            cpLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ColorPickerDialog colorPickerDialog = ColorPickerDialog.createColorPickerDialog(EditorActivity.editorActivity, ColorPickerDialog.DARK_THEME);

                    colorPickerDialog.show();
                    colorPickerDialog.hideOpacityBar();

                    if (Aux.isColor(Aux.toColorString(finalBigColor))) {
                        colorPickerDialog.setInitialColor(finalBigColor);
                        colorPickerDialog.setLastColor(finalBigColor);
                    }

                    colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                        @Override
                        public void onColorPicked(int color, String hexVal) {
                            if (hexVal != null && !hexVal.equals("\0")) {
                                if (hexVal.length() > 3) {
                                    hexVal = "#" + hexVal.substring(3);
                                }

                                if (hexVal.length() > 7)
                                    hexVal = "#" + Aux.getLast(hexVal, 6);
                                else if (hexVal.length() < 7)
                                    hexVal = "#FFFFFF";

                                hexVal = Aux.colorToUpper(hexVal);

                                ((EditText) alertDialog.findViewById(R.id.hexField)).setText(hexVal.replace("#", ""));
                                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                            }
                        }
                    });
                }
            });

            alertDialog.show();

            return alertDialog;
        }
        catch (Exception e) {
            Aux.saveStack(e);
            finish();
        }

        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        return true;
    }

    public void openDrawer() {
        ConstraintLayout editorBG = findViewById(R.id.editorBG);
        ConstraintLayout drawerBG = findViewById(R.id.editorDrawerBG);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(editorBG);
        constraintSet.connect(R.id.editorDrawer,ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END,0);
        constraintSet.clear(R.id.editorDrawer,ConstraintSet.START);
        constraintSet.applyTo(editorBG);

        drawerBG.setVisibility(View.VISIBLE);

        editorBG.getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);

        drawerBG.getLayoutTransition()
                .enableTransitionType(LayoutTransition.APPEARING);
    }

    public void closeDrawer() {
        ConstraintLayout editorBG = findViewById(R.id.editorBG);
        ConstraintLayout drawerBG = findViewById(R.id.editorDrawerBG);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(editorBG);
        constraintSet.clear(R.id.editorDrawer,ConstraintSet.END);
        constraintSet.connect(R.id.editorDrawer,ConstraintSet.START, ConstraintSet.PARENT_ID,ConstraintSet.END,0);
        constraintSet.applyTo(editorBG);

        drawerBG.setVisibility(View.INVISIBLE);

        editorBG.getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);

        drawerBG.getLayoutTransition()
                .enableTransitionType(LayoutTransition.DISAPPEARING);
    }

    public void toggleDrawer() {
        ConstraintLayout drawerBG = findViewById(R.id.editorDrawerBG);

        if (drawerBG.getVisibility() == View.VISIBLE)
            closeDrawer();
        else
            openDrawer();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        try {
           if (item.getItemId() == android.R.id.home) {
                onBackPressed();
                return true;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        try {
            ThemeActivity.themeActivity.recreate();
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }

        if ((entries.size() > 0 && currentEntry > -1) || themeChanged) {
            AlertDialog.Builder builder = createAlertDialog("Unsaved Changes");
            builder.setMessage(getString(R.string.unsaved_confirmation));

            builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    onApplyClick(null);

                    EditorActivity.super.onBackPressed();
                }
            });

            builder.setNegativeButton("Don't Apply", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    EditorActivity.super.onBackPressed();
                }
            });

            builder.setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
        else {
            super.onBackPressed();
        }
    }

    @SuppressLint("SetTextI18n")
    public String backup(String themeName) {
        try {
            TinyDB tinydb = new TinyDB(this);

            int i, a, b, c;

            String[] colors = {tinydb.getString("cPrimary"), tinydb.getString("cSecondary"), tinydb.getString("cTertiary"), tinydb.getString("cbEquals"), tinydb.getString("cFab"), tinydb.getString("cPlus"), tinydb.getString("cMinus"), tinydb.getString("cMulti"), tinydb.getString("cDiv"), tinydb.getString("cMain"), tinydb.getString("cKeypad"), tinydb.getString("cTop"), tinydb.getString("cNum"), tinydb.getString("cFabText")};

            if (themeName == null)
                themeName = " ";

            int testLength = 0;

            for (a = 0; a < allButtons.length; a++) {
                for (b = 0; b < allButtons[a].length; b++) {
                    testLength++;
                }
            }

            testLength += 1;

            String[] extraTexts = new String[testLength + 1];

            c = 0;

            for (a = 0; a < allButtons.length; a++) {
                for (b = 0; b < allButtons[a].length; b++) {
                    if (allButtons[a][b] != null)
                        extraTexts[c] = allButtons[a][b].getText().toString();

                    c++;
                }
            }

            extraTexts[extraTexts.length - 1] = "ⁿ√";

            String[] extraColors = new String[extraTexts.length];
            String[] extraTextColors = new String[extraTexts.length];

            for (b = 0; b < extraTexts.length; b++) {
                extraColors[b] = tinydb.getString("-b" + extraTexts[b]);
                extraTextColors[b] = tinydb.getString("-b" + extraTexts[b] + "t");
            }

            String fileText, filename;

            int numColors = colors.length;

            cmd = "backup " + themeName;

            if (cmd.length() > 7 && !cmd.substring(7).equals("\0")) {
                if (cmd.substring(7).endsWith(".txt"))
                    filename = themeName;
                else
                    filename = themeName + ".txt";

                if (colors[0] == null || colors[0].equals("\0") || colors[0].equals("0"))
                    colors[0] = "#reset0";

                fileText = colors[0] + "\n";

                for (i = 1; i < numColors; i++) {
                    if (colors[i] == null || colors[i].equals("\0") || colors[i].equals("") || colors[i].equals("0") || !Aux.isColor(colors[i]))
                        colors[i] = "#reset0";

                    fileText += colors[i] + "\n";
                }

                boolean hasAddedButton = false;

                for (a = 0; a < extraColors.length; a++) {
                    if (extraTexts[a] != null) {
                        if (extraTexts[a].equals("%"))
                            extraTexts[a] = "ⁿ√";

                        if (extraColors[a] != null) {
                            if (Aux.isColor(extraColors[a])) {
                                if (!hasAddedButton) {
                                    fileText += "\n";
                                    hasAddedButton = true;
                                }

                                fileText += extraColors[a] + "-b" + extraTexts[a] + "\n";
                            }
                        }

                        if (extraTextColors[a] != null) {
                            if (Aux.isColor(extraTextColors[a])) {
                                if (!hasAddedButton) {
                                    fileText += "\n";
                                    hasAddedButton = true;
                                }

                                fileText += extraTextColors[a] + "-b" + extraTexts[a] + "t" + "\n";
                            }
                        }
                    }
                }

                String[] extraCodes = {"-bop", "-btt", "-bINV2", "-bINV2t", "-mt"};

                for (a=0; a < extraCodes.length; a++) {
                    if (Aux.isTinyColor(extraCodes[a]))
                        fileText += tinydb.getString(extraCodes[a]) + extraCodes[a] + "\n";
                }

                if (Aux.isDigit(bigTheme))
                    fileText += bigTheme;
                else
                    fileText += tinydb.getString("theme");

                fileText += "\nname:" + themeName + "\n";

                writeTheme(this, fileText, filename);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }

        return null;
    }

    public final void writeTheme(Context mcoContext, String body, String filename) {
        File dir = new File(mcoContext.getFilesDir(), "themes");

        if (!dir.exists()) {
            dir.mkdir();
        }

        try {
            File theme = new File(dir, filename);
            FileWriter writer = new FileWriter(theme);
            writer.append(body);
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(View v) {
        try {
            final AlertDialog.Builder builder = createAlertDialog("Backup Current Theme\n");

            View viewInflated = LayoutInflater.from(this).inflate(R.layout.content, (ViewGroup) findViewById(R.id.editorBG), false);

            final EditText input = (EditText) viewInflated.findViewById(R.id.input);

            bigTheme = (newTheme == 2 || newTheme == 5) ? THEME_LIGHT : THEME_DARK;

            builder.setView(viewInflated);

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    final String themeName = input.getText().toString();

                    if (!themeName.equals("\0") && !themeName.equals("") && themeName.length() > 0) {
                        if (themeExists(themeName)) {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(EditorActivity.editorActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

                            builder2.setTitle("A theme with that name already exists. Do you want to overwrite it?\n");

                            builder2.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    onApplyClick(null);

                                    backup(themeName);

                                    bigTheme = "N/A";

                                    Toast.makeText(EditorActivity.this, "Successfully backed up \"" + input.getText().toString() + "\"", Toast.LENGTH_SHORT).show();

                                    recreate();
                                }
                            });
                            builder2.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder2.show();
                        }
                        else {
                            onApplyClick(null);

                            backup(themeName);

                            bigTheme = "N/A";

                            Toast.makeText(EditorActivity.this, "Successfully backed up \"" + input.getText().toString() + "\"", Toast.LENGTH_SHORT).show();

                            recreate();
                        }
                    }
                    else
                        Toast.makeText(EditorActivity.this, "Error: Theme name cannot be empty", Toast.LENGTH_SHORT).show();
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
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }
    }

    public void onApplyClick(View v) {
        int i;
        TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

        //Loop through history entries and apply colors in theme
        for (i=0; i <= currentEntry; i++) {
            tinydb.putString(entries.get(i).code, entries.get(i).finalColor);

            if (entries.get(i).code.equals("-mt"))
                tinydb.putBoolean("mtIsSet", true);
        }

        //Apply main theme
        tinydb.putString("theme", newTheme == 2 ? THEME_LIGHT : THEME_DARK);
        tinydb.putString("customTheme", newTheme == 2 ? THEME_LIGHT : THEME_DARK);

        try {
            ThemeActivity.themeActivity.recreate();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            MainActivity.mainActivity.recreate();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (v != null)
            super.onBackPressed();
    }

    public void applyTheme() {
        int i, j;

        TinyDB tinydb = new TinyDB(EditorActivity.editorActivity);

        final String darkerSecondaryStr = Aux.hexAdd("#53E2D4", -6);

        final int darkerSecondary = Color.parseColor(darkerSecondaryStr);

        //Primary, Secondary, Tertiary, Main, Keypad
        String[][] defaultByTheme = {{}, {"#03DAC5", "#00B5A3", "#00C5B1", "#272C33", "#202227"}, {"#03DAC5", "#53E2D4", "#3CDECE", "#FFFFFF", "#FFFFFF"},
                {"#03DAC5", "#53E2D4", "#3CDECE", "#000000", "#000000"}, {"#000000", "#000000", "#000000", "#000000", "#000000"},
                {darkerSecondaryStr, darkerSecondaryStr, darkerSecondaryStr, "#53E2D4", darkerSecondaryStr}};

        String[] defaultZones = defaultByTheme[newTheme];

        // ------ ~Zones~ ------
        final ConstraintLayout eMain = findViewById(R.id.previewLayout);
        final ConstraintLayout eKeypad = findViewById(R.id.editorKeypad);

        final ConstraintLayout keypadZone = findViewById(R.id.editorKeypadZone);

        final ConstraintLayout primaryButtons = findViewById(R.id.editorOps);
        final ConstraintLayout primaryZone = findViewById(R.id.editorPrimaryZone);
        final ConstraintLayout secondaryButtons = findViewById(R.id.editorScrollBar);
        final ConstraintLayout secondaryZone = findViewById(R.id.editorSecondaryZone);
        final ConstraintLayout tertiaryButtons = findViewById(R.id.editorTertiaryButtons);
        final ConstraintLayout tertiaryZone = findViewById(R.id.editorTertiaryZone);

        final ImageButton expBG1 = findViewById(R.id.editorExpandBG);
        final ImageButton expBG2 = findViewById(R.id.editorExpandBG2);
        final ImageButton expand = findViewById(R.id.editorExpand);

        // ------ ~Editor Buttons~ ------
        final Button bEquals = findViewById(R.id.eBEquals);
        final FloatingActionButton delete = findViewById(R.id.editorDelete);
        final FloatingActionButton customs = findViewById(R.id.editorExpandCustoms);

        // ------ ~Toolbar Elements~ ------
        final ImageButton hmb = findViewById(R.id.editorHMB);
        final Button toolbarTitle = findViewById(R.id.editorPreviewTitle);
        final Button degRad = findViewById(R.id.editorDegRad);
        final ImageButton bOverflow = findViewById(R.id.editorOverflow);

        // ------ ~Miscellaneous~ ------
        final FloatingActionButton apply = findViewById(R.id.editorApply);

        //Set colors in current theme
        eMain.setBackgroundColor(Aux.isTinyColor("cMain") ? Aux.getTinyColor("cMain") : Color.parseColor(defaultByTheme[newTheme][3]));
        eKeypad.setBackgroundColor(Aux.isTinyColor("cKeypad") ? Aux.getTinyColor("cKeypad") : Color.parseColor(defaultByTheme[newTheme][4]));
        keypadZone.setBackgroundColor(Aux.isTinyColor("cKeypad") ? Aux.getTinyColor("cKeypad") : Color.parseColor(defaultByTheme[newTheme][4]));

        primaryButtons.setBackgroundColor(Aux.isTinyColor("cPrimary") ? Aux.getTinyColor("cPrimary") : Color.parseColor(defaultByTheme[newTheme][0]));
        primaryZone.setBackgroundColor(Aux.isTinyColor("cPrimary") ? Aux.getTinyColor("cPrimary") : Color.parseColor(defaultByTheme[newTheme][0]));
        secondaryButtons.setBackgroundColor(Aux.isTinyColor("cSecondary") ? Aux.getTinyColor("cSecondary") : Color.parseColor(defaultByTheme[newTheme][1]));
        secondaryZone.setBackgroundColor(Aux.isTinyColor("cSecondary") ? Aux.getTinyColor("cSecondary") : Color.parseColor(defaultByTheme[newTheme][1]));
        tertiaryButtons.setBackgroundColor(Aux.isTinyColor("cTertiary") ? Aux.getTinyColor("cTertiary") : Color.parseColor(defaultByTheme[newTheme][2]));
        tertiaryZone.setBackgroundColor(Aux.isTinyColor("cTertiary") ? Aux.getTinyColor("cTertiary") : Color.parseColor(defaultByTheme[newTheme][2]));

        String tabColor = Aux.isTinyColor("cSecondary") ? tinydb.getString("cSecondary") : defaultByTheme[newTheme][1];

        if (tinydb.getBoolean("isDarkTab"))
            tabColor = Aux.hexAdd(tabColor, -6);

        expBG1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(tabColor)));
        expBG2.setBackgroundColor(Color.parseColor(tabColor));

        bEquals.setTextColor(Aux.isTinyColor("cPrimary") && !Aux.isTinyColor("cNum") ? Aux.getTinyColor("cPrimary") :
                (Aux.isTinyColor("cNum") ? Aux.getTinyColor("cNum") : Color.parseColor(defaultByTheme[newTheme][0])));

        if (Aux.isTinyColor("cFab")) {
            delete.setBackgroundTintList(ColorStateList.valueOf(Aux.getTinyColor("cFab")));
            customs.setBackgroundTintList(ColorStateList.valueOf(Aux.getTinyColor("cFab")));
            apply.setBackgroundTintList(ColorStateList.valueOf(Aux.getTinyColor("cFab")));
        }
        else if (Aux.isTinyColor("cPrimary")) {
            delete.setBackgroundTintList(ColorStateList.valueOf(Aux.getTinyColor("cPrimary")));
            customs.setBackgroundTintList(ColorStateList.valueOf(Aux.getTinyColor("cPrimary")));
            apply.setBackgroundTintList(ColorStateList.valueOf(Aux.getTinyColor("cPrimary")));
        }
        else {
            delete.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(defaultByTheme[newTheme][0])));
            customs.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(defaultByTheme[newTheme][0])));
            apply.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(defaultByTheme[newTheme][0])));
        }

        if (Aux.isTinyColor("cFabText")) {
            delete.setColorFilter(Aux.getTinyColor("cFabText"));
            customs.setColorFilter(Aux.getTinyColor("cFabText"));
            apply.setColorFilter(Aux.getTinyColor("cFabText"));
        }
        else if (Aux.isTinyColor("-bop")) {
            delete.setColorFilter(Aux.getTinyColor("-bop"));
            customs.setColorFilter(Aux.getTinyColor("-bop"));
            apply.setColorFilter(Aux.getTinyColor("-bop"));
        }
        else {
            delete.setColorFilter(Color.WHITE);
            customs.setColorFilter(Color.WHITE);
            apply.setColorFilter(Color.WHITE);
        }

        if (Aux.isTinyColor("-bfc"))
            customs.setBackgroundTintList(ColorStateList.valueOf(Aux.getTinyColor("-bfc")));
        if (Aux.isTinyColor("-bfct"))
            customs.setColorFilter(Aux.getTinyColor("-bfct"));

        int mtColor = Aux.isTinyColor("-mt") ? Aux.getTinyColor("-mt") : (newTheme == 2 ? darkGray : Color.WHITE);

        hmb.setColorFilter(mtColor);
        toolbarTitle.setTextColor(mtColor);
        degRad.setTextColor(mtColor);
        bOverflow.setColorFilter(mtColor);

        final Button[] zoneButtons = {findViewById(R.id.keypadZoneButton), findViewById(R.id.secondaryZoneButton),
                findViewById(R.id.tertiaryZoneButton), findViewById(R.id.mainZoneButton)};
        final TextView primaryZoneButtonText = findViewById(R.id.primaryZoneButtonText);

        final ViewGroup[] containers = {eKeypad, primaryButtons, findViewById(R.id.editorSecondaryLinear1), findViewById(R.id.editorSecondaryLinear2), tertiaryButtons};
        final String[] zoneTextCodes = {"cNum", "-bop", "cTop", "cTop", "-btt"};

        expand.setColorFilter(Aux.isTinyColor("cTop") ? Aux.getTinyColor("cTop") : Color.WHITE);

        for (i = 0; i < containers.length; i++) {
            int defaultTextColor = i == 0 && newTheme == 2 ? darkGray : Color.WHITE;

            for (j = 0; j < containers[i].getChildCount(); j++) {
                if (containers[i].getChildAt(j).getClass() == androidx.appcompat.widget.AppCompatButton.class) {
                    final Button button = (Button) containers[i].getChildAt(j);

                    if (button != null) {
                        String text = button.getText().toString();
                        final String bgCode = "-b" + text;
                        String textCode = "-b" + text + "t";

                        if (Aux.isTinyColor(bgCode))
                            button.setBackgroundColor(Aux.getTinyColor(bgCode));

                        if (!textCode.equals("-b=t"))
                            button.setTextColor(Aux.isTinyColor(textCode) ? Aux.getTinyColor(textCode) : Aux.isTinyColor(zoneTextCodes[i]) ? Aux.getTinyColor(zoneTextCodes[i]) : defaultTextColor);

                        try {
                            button.setElevation(0f);
                            button.setStateListAnimator(null);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (Aux.isTinyColor("-b=t"))
            bEquals.setTextColor(Aux.getTinyColor("-b=t"));

        for (i=0; i < zoneButtons.length; i++) {
            int defaultTextColor = i == 0 && newTheme == 2 ? darkGray : Color.WHITE;
            String zoneTextCode = zoneButtons[i].getTag().toString().split(" ")[1];

            zoneButtons[i].setTextColor(Aux.isTinyColor(zoneTextCode) ? Aux.getTinyColor(zoneTextCode) : defaultTextColor);
        }

        primaryZoneButtonText.setTextColor(Aux.isTinyColor("-bop") ? Aux.getTinyColor("-bop") : Color.WHITE);

        Button inv2 = findViewById(R.id.eBInv2);

        if (Aux.isTinyColor("-bINV2"))
            inv2.setBackgroundColor(Aux.getTinyColor("-bINV2"));

        if (Aux.isTinyColor("-bINV2t"))
            inv2.setTextColor(Aux.getTinyColor("-bINV2t"));
    }

    public void addHistoryEntry (View view, String code, String initColor, String finalColor) {
        int i;

        if (currentEntry + 1 < entries.size()) {
            for (i = currentEntry; i < entries.size(); i++) {
                try {
                    entries.remove(currentEntry + 1);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        entries.add(new ThemeHistoryStep(view, code, initColor, finalColor));

        currentEntry++;

        undoCheck();
    }

    public void undo() {
        ThemeHistoryStep entry = entries.get(currentEntry);

        int initColor = Color.parseColor(entry.initColor);
        String code = entry.code;

        Button button = null;
        FloatingActionButton fab = null;

        FloatingActionButton delete = findViewById(R.id.editorDelete);
        FloatingActionButton customs = findViewById(R.id.editorExpandCustoms);
        FloatingActionButton apply = findViewById(R.id.editorApply);

        try {
            button = (Button) entry.view;
        }
        catch (Exception e) {
            e.printStackTrace();

            fab = (FloatingActionButton) entry.view;
        }

        final ViewGroup[] containers = {findViewById(R.id.editorKeypad), findViewById(R.id.editorOps), findViewById(R.id.editorSecondaryLinear1),
                findViewById(R.id.editorSecondaryLinear2), findViewById(R.id.editorTertiaryButtons), findViewById(R.id.editorFakeToolbar)};

        //Simple Button
        if (code.startsWith("-b") && code.endsWith("t") && !code.endsWith("tt") && !code.startsWith("-bfc")) {
            button.setTextColor(initColor);

            if (Aux.getFirstHistoryEntry(entries, code) == currentEntry)
                setButtons.remove(code);
        }
        //Fabs (Delete or Custom)
        else if (fab != null) {
            if (code.endsWith("t")) {
                fab.setColorFilter(initColor);

                if (code.equals("cFabText"))
                    apply.setColorFilter(initColor);

                if (!setButtons.contains("-bfct") && code.equals("cFabText"))
                    customs.setColorFilter(initColor);
            }
            else {
                fab.setBackgroundTintList(ColorStateList.valueOf(initColor));

                if (code.equals("cFab"))
                    apply.setBackgroundTintList(ColorStateList.valueOf(initColor));

                if (!setButtons.contains("-bfc") && code.equals("cFab"))
                    customs.setBackgroundTintList(ColorStateList.valueOf(initColor));
            }

            if (!setButtons.contains("-bfc") && code.equals("cFab"))
                customs.setBackgroundTintList(ColorStateList.valueOf(initColor));

            if (Aux.getFirstHistoryEntry(entries, code) == currentEntry)
                setButtons.remove(code);
        }
        //Text Zone
        else if (code.equals("cNum") || code.equals("cTop") || code.equals("-bop") || code.equals("-btt") || code.equals("-mt")) {
            String tag = button.getTag() != null ? button.getTag().toString() : "~";

            int i;
            int index = Integer.parseInt(Aux.lastChar(tag));

            if (index > 2)
                index++;

            for (i=0; i < containers[index].getChildCount(); i++) {
                View current = containers[index].getChildAt(i);

                if (current.getClass() == androidx.appcompat.widget.AppCompatButton.class) {
                    Button btn = (Button) current;

                    if (index == 4 && btn.getText().toString().equals("INV"))
                        btn.setText("INV2");

                    if (!setButtons.contains("-b" + btn.getText().toString() + "t"))
                        btn.setTextColor(initColor);

                    if (index == 4 && btn.getText().toString().equals("INV2"))
                        btn.setText("INV");
                }
                else if (current.getClass() == androidx.appcompat.widget.AppCompatImageButton.class) {
                    ((ImageButton) current).setColorFilter(initColor);
                }

                if (index == 2 && i == containers[index].getChildCount() - 1) {
                    index = 3;
                    i = -1;
                }
            }

            try {
                if (code.equals("-bop")) {
                    ((TextView) findViewById(R.id.primaryZoneButtonText)).setTextColor(initColor);

                    if (!isSet("cFabText")) {
                        delete.setColorFilter(initColor);
                        apply.setColorFilter(initColor);

                        if (!isSet("-bfct"))
                            customs.setColorFilter(initColor);
                    }
                }
                else if (code.equals("cTop")) {
                    ((ImageButton) findViewById(R.id.editorExpand)).setColorFilter(initColor);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (index > 2)
                index--;

            try {
                zoneButtons[index].setTextColor(initColor);
            }
            catch (Exception e) {
                e.printStackTrace();
                (index == 1 ? (TextView) findViewById(R.id.primaryZoneButtonText) : zoneButtons[2]).setTextColor(initColor);
            }
        }
        //Zones & ~Other~
        else {
            if (zoneCodes.contains(code)) {
                int index = zoneCodes.indexOf(code);

                bigContainers[index].setBackgroundColor(initColor);
                bigZones[index].setBackgroundColor(initColor);

                //Handle primary-dependent defaults
                if (code.equals("cPrimary")) {
                    if (!isSet("cNum")) {
                        if (!setButtons.contains("-b=t")) {
                            ((Button) findViewById(R.id.eBEquals)).setTextColor(initColor);
                        }
                    }
                    if (!isSet("cFab")) {
                        delete.setBackgroundTintList(ColorStateList.valueOf(initColor));
                        apply.setBackgroundTintList(ColorStateList.valueOf(initColor));

                        if (!isSet("-bfc"))
                            customs.setBackgroundTintList(ColorStateList.valueOf(initColor));
                    }
                }

                if (index == 2) {
                    try {
                        ViewGroup[] secondaryLinearLayouts = {findViewById(R.id.editorSecondaryLinear1), findViewById(R.id.editorSecondaryLinear2)};

                        findViewById(R.id.editorExpandBG).setBackgroundTintList(ColorStateList.valueOf(initColor));
                        findViewById(R.id.editorExpandBG2).setBackgroundColor(initColor);

                        for (ViewGroup layout : secondaryLinearLayouts)
                            layout.setBackgroundColor(initColor);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (index == zoneCodes.indexOf("cMain")) {
                    ((ConstraintLayout) findViewById(R.id.editorFakeToolbar)).setBackgroundColor(initColor);
                }
            }
            else
                entry.view.setBackgroundColor(initColor);

            try {
                if (Aux.getFirstHistoryEntry(entries, code) == currentEntry)
                    setButtons.remove(code);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (currentEntry > -1) {
            currentEntry--;
        }

        undoCheck();

        try {
            if (!code.startsWith("cFab") && !code.startsWith("-bcf")) {
                button.setElevation(0f);
                button.setStateListAnimator(null);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void redo() {
        ThemeHistoryStep entry = entries.get(currentEntry + 1);

        int finalColor = Color.parseColor(entry.finalColor);
        String code = entry.code;

        Button button = null;
        FloatingActionButton fab = null;

        FloatingActionButton delete = findViewById(R.id.editorDelete);
        FloatingActionButton customs = findViewById(R.id.editorExpandCustoms);
        FloatingActionButton apply = findViewById(R.id.editorApply);

        try {
            button = (Button) entry.view;
        }
        catch (Exception e) {
            e.printStackTrace();

            fab = (FloatingActionButton) entry.view;
        }

        final ViewGroup[] containers = {findViewById(R.id.editorKeypad), findViewById(R.id.editorOps), findViewById(R.id.editorSecondaryLinear1),
                findViewById(R.id.editorSecondaryLinear2), findViewById(R.id.editorTertiaryButtons), findViewById(R.id.editorFakeToolbar)};

        if (code.startsWith("-b") && code.endsWith("t") && !code.endsWith("ot") && !code.equals("-btt")) {
            button.setTextColor(finalColor);

            if (Aux.getFirstHistoryEntry(entries, code) == currentEntry + 1)
                setButtons.add(code);
        }
        else if (fab != null) {
            if (code.endsWith("t")) {
                fab.setColorFilter(finalColor);

                if (code.equals("cFabText"))
                    apply.setColorFilter(finalColor);

                if (!setButtons.contains("-bfct") && code.equals("cFabText"))
                    customs.setColorFilter(finalColor);
            }
            else {
                fab.setBackgroundTintList(ColorStateList.valueOf(finalColor));

                if (code.equals("cFab"))
                    apply.setBackgroundTintList(ColorStateList.valueOf(finalColor));

                if (!setButtons.contains("-bfc") && code.equals("cFab"))
                    customs.setBackgroundTintList(ColorStateList.valueOf(finalColor));
            }

            if (!setButtons.contains(code))
                setButtons.add(code);
        }
        else if (code.equals("cNum") || code.equals("cTop") || code.equals("-bop") || code.equals("-btt") || code.equals("-mt")) {
            String tag = button.getTag() != null ? button.getTag().toString() : "~";

            int i;
            int index = Integer.parseInt(Aux.lastChar(tag));

            if (index > 2)
                index++;

            for (i=0; i < containers[index].getChildCount(); i++) {
                View current = containers[index].getChildAt(i);

                if (current.getClass() == androidx.appcompat.widget.AppCompatButton.class) {
                    Button btn = (Button) current;

                    if (index == 4 && btn.getText().toString().equals("INV"))
                        btn.setText("INV2");

                    if (!setButtons.contains("-b" + btn.getText().toString() + "t"))
                        btn.setTextColor(finalColor);

                    if (index == 4 && btn.getText().toString().equals("INV2"))
                        btn.setText("INV");
                }
                else if (current.getClass() == androidx.appcompat.widget.AppCompatImageButton.class) {
                    ((ImageButton) current).setColorFilter(finalColor);
                }

                if (index == 2 && i == containers[index].getChildCount() - 1) {
                    index = 3;
                    i = -1;
                }
            }

            try {
                if (code.equals("-bop")) {
                    ((TextView) findViewById(R.id.primaryZoneButtonText)).setTextColor(finalColor);

                    if (!isSet("cFabText")) {
                        delete.setColorFilter(finalColor);
                        apply.setColorFilter(finalColor);

                        if (!isSet("-bfct"))
                            customs.setColorFilter(finalColor);
                    }
                }
                else if (code.equals("cTop")) {
                    ((ImageButton) findViewById(R.id.editorExpand)).setColorFilter(finalColor);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (index > 2)
                index--;

            try {
                zoneButtons[index].setTextColor(finalColor);
            }
            catch (Exception e) {
                e.printStackTrace();
                (index == 1 ? (TextView) findViewById(R.id.primaryZoneButtonText) : zoneButtons[2]).setTextColor(finalColor);
            }
        }
        else {
            if (zoneCodes.contains(code)) {
                int index = zoneCodes.indexOf(code);

                bigContainers[index].setBackgroundColor(finalColor);
                bigZones[index].setBackgroundColor(finalColor);

                //Handle primary-dependent defaults
                if (code.equals("cPrimary")) {
                    if (!isSet("cNum")) {
                        if (!setButtons.contains("-b=t")) {
                            ((Button) findViewById(R.id.eBEquals)).setTextColor(finalColor);
                        }
                    }
                    if (!isSet("cFab")) {
                        delete.setBackgroundTintList(ColorStateList.valueOf(finalColor));
                        apply.setBackgroundTintList(ColorStateList.valueOf(finalColor));

                        if (!isSet("-bfc"))
                            customs.setBackgroundTintList(ColorStateList.valueOf(finalColor));
                    }
                }

                if (index == 2) {
                    try {
                        ViewGroup[] secondaryLinearLayouts = {findViewById(R.id.editorSecondaryLinear1), findViewById(R.id.editorSecondaryLinear2)};

                        findViewById(R.id.editorExpandBG).setBackgroundTintList(ColorStateList.valueOf(finalColor));
                        findViewById(R.id.editorExpandBG2).setBackgroundColor(finalColor);

                        for (ViewGroup layout : secondaryLinearLayouts)
                            layout.setBackgroundColor(finalColor);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (index == zoneCodes.indexOf("cMain")) {
                    ((ConstraintLayout) findViewById(R.id.editorFakeToolbar)).setBackgroundColor(finalColor);
                }
            }
            else
                entry.view.setBackgroundColor(finalColor);

            if (Aux.getFirstHistoryEntry(entries, code) == currentEntry + 1)
                setButtons.add(code);
        }

        currentEntry++;
        undoCheck();

        try {
            if (!code.startsWith("cFab") && !code.startsWith("-bcf")) {
                button.setElevation(0f);
                button.setStateListAnimator(null);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void onSaveClick(View v) {
        save(null);
    }

    public void undoCheck() {
        final ImageButton undo = findViewById(R.id.undo);
        final ImageButton redo = findViewById(R.id.redo);

        boolean canUndo = entries.size() > 0 && currentEntry > -1;
        boolean canRedo = (entries.size() > 0 && currentEntry < entries.size()-1);

        undo.setEnabled(canUndo);
        undo.setAlpha(canUndo ? 1.0f : 0.5f);

        redo.setEnabled(canRedo);
        redo.setAlpha(canRedo ? 1.0f : 0.5f);
    }

    public AlertDialog.Builder createAlertDialog(String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.editorActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

        builder.setTitle(title);

        return builder;
    }

    public void reset(View view, String buttonCode, String initColor, int option) {
        try {
            View zone;
            String[] codes;
            int zoneIndex = 1;
            int finalColor, defaultZoneColor = Color.WHITE;
            int themeInt = Aux.getThemeInt();
            boolean isZone = false;

            //Check button's tag to determine whether or not the button is a zoneButton
            try {
                isZone = Aux.isDigit(Aux.lastChar(view.getTag().toString()));
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            //Determine zone codes
            if (isZone) {
                zone = view;
                codes = zone.getTag().toString().split(" ");
            }
            else {
                zone = (View) view.getParent();
                codes = zone.getTag().toString().split("~");
            }

            //Determine default zone color
            try {
                zoneIndex = Integer.parseInt(codes[2]);

                if (isZone && zoneIndex > 2)
                    zoneIndex++;
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            //Aux.makeToast("" + zoneIndex, 0);

            try {
                defaultZoneColor = Color.parseColor(option == 0 ? defaultBGByTheme[themeInt][zoneIndex] : defaultTextByTheme[themeInt][zoneIndex]);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            //Zone
            if (isZone) {
                int initEntry = currentEntry;

                AlertDialog alertDialog = openCPAlertDialog(R.layout.new_cpdialog, " ", "Set color", "Cancel", buttonCode, view, option);

                ((EditText) alertDialog.findViewById(R.id.hexField)).setText(Aux.toColorString(defaultZoneColor));

                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();

                if (currentEntry > initEntry && currentEntry >= 0) {
                    entries.remove(currentEntry);
                    currentEntry--;
                }
            }
            //Button
            else {
                String code = codes[option];

                try {
                    if (isSet(code))
                        finalColor = Color.parseColor(entries.get(Aux.getLastHistoryEntry(entries, code)).finalColor);
                    else
                        finalColor = defaultZoneColor;
                }
                catch (Exception e) {
                    e.printStackTrace();

                    finalColor = defaultZoneColor;
                }

                //Buttons
                try {
                    Button button = (Button) view;

                    try {
                        if (code.equals("-b=t") && isSet("cPrimary") && !isSet("cNum"))
                            finalColor = Aux.getLastHistoryEntry(entries, "cPrimary") != -1 ? Color.parseColor(entries.get(Aux.getLastHistoryEntry(entries, "cPrimary")).finalColor) : (Aux.isTinyColor("cPrimary") ? Aux.getTinyColor("cPrimary") : defaultZoneColor);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (option == 0) {
                        button.setBackgroundColor(finalColor);
                    }
                    else if (option == 1) {
                        button.setTextColor(finalColor);
                    }
                }
                //FABs
                catch (Exception e) {
                    try {
                        if (codes[0].equals("cPrimary") && codes[1].equals("-bop")) {
                            FloatingActionButton fab = (FloatingActionButton) view;

                            if (option == 0) {
                                fab.setBackgroundTintList(ColorStateList.valueOf(finalColor));
                            }
                            else if (option == 1) {
                                fab.setColorFilter(finalColor);
                            }
                        }
                    }
                    catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }

            addHistoryEntry(view, buttonCode, initColor, "#reset0");
        }
        catch (Exception e) {
            Aux.saveStack(e);
            Aux.makeToast("Error: Failed to reset color.\nTry resetting this color using the terminal.", 1);
        }
    }

    public boolean isSet(String code) {
        if (code == null || code.equals("\0"))
            return false;

        try {
            if (setButtons.contains(code))
                return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (setZones.contains(code))
                return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return !((Aux.getFirstHistoryEntry(entries, code) == -1 && !Aux.isTinyColor(code)) || (Aux.getLastHistoryEntry(entries, code) != -1 && entries.get(Aux.getLastHistoryEntry(entries, code)).finalColor.equalsIgnoreCase("#reset0")));
    }



    public void addItem(String title) {
        cards.add(new BackupCard(title));
        adapter.notifyItemInserted(cards.size() - 1);
    }

    public void replaceItem(String title, int position) {
        cards.remove(position);
        cards.add(position, new BackupCard(title));
        adapter.notifyItemChanged(position);
    }

    public void replaceItem(String title, int position, String[] colors) {
        cards.remove(position);
        cards.add(position, new BackupCard(title, colors));
        adapter.notifyItemChanged(position);
    }

    public void replaceItem(String title, int position, String[] colors, String cEquals) {
        cards.remove(position);
        cards.add(position, new BackupCard(title, colors, cEquals));
        adapter.notifyItemChanged(position);
    }

    public void removeItem(int position) {
        cards.remove(position);
        adapter.notifyItemRemoved(position);
    }

    public void newRun(String cmd) {
        try {
            run(cmd);
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }
    }

    public void rename(final String old) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

            builder.setTitle("Rename Theme\n");

            View viewInflated = LayoutInflater.from(this).inflate(R.layout.rename, (ViewGroup) findViewById(R.id.mainBackup), false);

            final EditText input = (EditText) viewInflated.findViewById(R.id.input);
            input.setText(old);

            builder.setView(viewInflated);

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    String themeName = input.getText().toString();

                    if (themeExists(themeName)) {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(EditorActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

                        builder2.setTitle("A theme with that name already exists. Do you want to overwrite it?\n");

                        builder2.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                String themeName = input.getText().toString();

                                File directory = new File(MainActivity.mainActivity.getFilesDir(), "themes");
                                File theme = new File(directory, old + ".txt");

                                removeItem(getCardPosition(old));

                                File newTheme = new File(directory, themeName + ".txt");

                                if (theme.renameTo(newTheme)) {
                                    int i;

                                    Toast.makeText(EditorActivity.this, "Successfully renamed \"" + old + "\" to \"" + themeName + "\"", Toast.LENGTH_SHORT).show();

                                    replaceItem(themeName, getCardPosition(themeName));
                                    backupsRv.scrollToPosition(getCardPosition(themeName));
                                }
                            }
                        });
                        builder2.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder2.show();
                    }
                    else {
                        File directory = new File(MainActivity.mainActivity.getFilesDir(), "themes");
                        File theme = new File(directory, old + ".txt");
                        File newTheme = new File(directory, themeName + ".txt");

                        if (theme.renameTo(newTheme)) {
                            int i;

                            Toast.makeText(EditorActivity.this, "Successfully renamed \"" + old + "\" to \"" + themeName + "\"", Toast.LENGTH_SHORT).show();

                            String[] colors = new String[0];

                            try {
                                colors = getColorsFromFile(newTheme);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            String[] newColors = new String[6];

                            if (colors.length > 6) {
                                for (i = 0; i < 6; i++)
                                    newColors[i] = colors[i];
                            }

                            if (colors.length > 6)
                                replaceItem(themeName, bigPosition, newColors, colors[6]);
                            else if (colors.length == 6)
                                replaceItem(themeName, bigPosition, colors);
                            else
                                replaceItem(themeName, bigPosition);
                        }
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
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }
    }

    public int getCardPosition(String name) {
        int i;

        for (i = 0; i < cards.size(); i++) {
            if (cards.get(i).getThemeName().equals(name))
                return i;
        }

        return cards.size() - 1;
    }

    public boolean themeExists(String name) {
        if (name == null || cards.size() < 1)
            return false;

        for (BackupCard card : cards) {
            if (card.getThemeName().equals(name))
                return true;
        }

        return false;
    }

    public String[] getColorsFromFile(File file) throws IOException {
        TinyDB tinydb = new TinyDB(MainActivity.mainActivity);
        String theme = tinydb.getString("theme");

        int i;

        int[] colorIndexes = {0, 6, 7, 8, 10, 12};
        ArrayList<String> currentColors = new ArrayList<>();
        int index = 0;
        String line;

        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);

        for (i = 0; (line = bufferedReader.readLine()) != null; i++) {
            if (i > 12 && Aux.isDigit(line))
                theme = line;
        }

        fis = new FileInputStream(file);
        isr = new InputStreamReader(fis);
        bufferedReader = new BufferedReader(isr);

        String tempEqualsColor = null;
        boolean hasSetMinus = false, hasSetMulti = false, hasSetDiv = false;

        for (i = 0; (line = bufferedReader.readLine()) != null; i++) {
            if ((i == 3 || i > 13) && (Aux.isColor(line) || (line.endsWith("-b=t") && Aux.isColor(Aux.newTrim(line, 4))))) {
                if (i == 3)
                    tempEqualsColor = line;
                else
                    tempEqualsColor = Aux.newTrim(line, 4);
            }

            if (index < colorIndexes.length && i == colorIndexes[index]) {
                if (Aux.isColor(line))
                    currentColors.add(line);
                else if (i == 0){
                    currentColors.add("#03DAC5");
                }
                else if (i == 10) {
                    if (theme.equals("2"))
                        currentColors.add("#FFFFFF");
                    else if (theme.equals("1"))
                        currentColors.add("#202227");
                    else if (theme.equals("5"))
                        currentColors.add("#03DAC5");
                    else
                        currentColors.add("#000000");
                }
                else if (i == 12) {
                    if (theme.equals("2"))
                        currentColors.add("#222222");
                    else if (theme.equals("5"))
                        currentColors.add("#303030");
                    else
                        currentColors.add("#FFFFFF");
                }
                //Operation button colors
                else {
                    if (theme.equals("5"))
                        currentColors.add("#303030");
                    else
                        currentColors.add("#FFFFFF");
                }

                index++;
            }
            else if (i > 13) {
                if (line.endsWith("-b-t")) {
                    currentColors.set(1, Aux.newTrim(line, 4));
                    hasSetMinus = true;
                }
                else if (line.endsWith("-b" + Aux.multi + "t")) {
                    currentColors.set(2, Aux.newTrim(line, 4));

                    hasSetMulti = true;
                }
                else if (line.endsWith("-b" + Aux.divi + "t")) {
                    currentColors.set(3, Aux.newTrim(line, 4));

                    hasSetDiv = true;
                }
                else if (line.endsWith("-bop")) {
                    if (!hasSetMinus)
                        currentColors.set(1, Aux.newTrim(line, 4));
                    if (!hasSetMulti)
                        currentColors.set(2, Aux.newTrim(line, 4));
                    if (!hasSetDiv)
                        currentColors.set(3, Aux.newTrim(line, 4));
                }
            }
        }

        if (tempEqualsColor != null)
            currentColors.add(tempEqualsColor);

        return currentColors.toArray(new String[0]);
    }

    public void onImportClick(View v) {
        openStorageAccess();
    }

    //Import Theme
    int openDirectoryAccessCode;

    private void openStorageAccess() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        startActivityForResult(intent, openDirectoryAccessCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == openDirectoryAccessCode && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();

            try {
                process(uri);
            } catch (Exception e) {
                e.printStackTrace();
                Aux.makeLongToast("Error: Either the specified file could not be found, or access was denied.");
            }
        }
    }

    String themeTitle = "\0";
    boolean noName = false;

    public void process(Uri inputTheme) throws IOException {
        if (inputTheme != null) {
            int i;
            int numSlashes = 0;

            FloatingActionButton newTheme = findViewById(R.id.newBackup);

            themeTitle = inputTheme.getLastPathSegment();

            if (themeTitle != null) {
                if (themeTitle.startsWith("0000-0000:"))
                    themeTitle = themeTitle.substring(10);
                else if ((themeTitle.length() > 5 && themeTitle.startsWith("msf:") && Aux.isDigit(Aux.chat(themeTitle, 5))) || themeTitle.startsWith("document")) {
                    themeTitle = "\0";
                    noName = true;
                }

                try {
                    for (i = 0; i < themeTitle.length(); i++) {
                        if (Aux.chat(themeTitle, i).equals("/"))
                            numSlashes++;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();

                    numSlashes = 0;
                }

                for (i=0; i < numSlashes; i++) {
                    if (themeTitle.contains("/")) {
                        themeTitle = themeTitle.substring(Aux.searchFor(themeTitle, "/") + 1);
                    }
                }

                if (!noName && themeTitle != null && themeTitle.endsWith(".txt"))
                    themeTitle = Aux.newTrim(themeTitle, 4);

                if (themeTitle != null && themeTitle.contains("/"))
                    themeTitle = themeTitle.replace("/", "");

                if (themeTitle == null || themeTitle.equals("\0") || themeTitle.length() < 1) {
                    try {
                        themeTitle = "" + Calendar.getInstance().getTimeInMillis();
                    }
                    catch (Exception e) {
                        try {
                            e.printStackTrace();
                            themeTitle = ("" + Math.random()).replace(".", "");
                        }
                        catch (Exception e2){
                            e2.printStackTrace();
                            themeTitle = "name_not_found";
                        }
                    }
                }
            }
            else {
                themeTitle = "\0";
                noName = true;
            }

            String line, filename;
            String themeName = "temp-" + System.currentTimeMillis();

            InputStreamReader isr = new InputStreamReader(getContentResolver().openInputStream(inputTheme));
            BufferedReader br = new BufferedReader(isr);

            File dir = new File(this.getFilesDir(), "themes");

            if (!dir.exists()) {
                dir.mkdir();
            }

            for (i=0; i < 200; i++) {
                line = br.readLine();

                if (line != null) {
                    if (line.startsWith("name:")) {
                        themeName = line.substring(5);
                        themeTitle = themeName;
                        break;
                    }
                }
                else
                    break;
            }

            filename = themeName + ".txt";

            isr = new InputStreamReader(getContentResolver().openInputStream(inputTheme));
            br = new BufferedReader(isr);

            try {
                File theme = new File(dir, filename);
                FileWriter writer = new FileWriter(theme);

                for (i = 0; i < 100; i++) {
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

                run("restore " + themeName);
                save(newTheme);

                if ((themeName.startsWith("temp-") || themeTitle.startsWith("temp-")) && Aux.isDigit(Aux.lastChar(themeName)))
                    theme.delete();

                applyTheme();
                closeDrawer();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public String run(String cmd) throws IOException {
        final TinyDB tinydb = new TinyDB(this);

        String setError;
        String hex = "none";
        String output = "";

        String cPrimary, cSecondary, cTertiary, cbEquals, cPlus, cMinus, cMulti, cDiv, cKeypad, cMain, cTop, cNum, cFab, cFabText = null;

        int i;

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
                if (cmd.length() > 12 && (cmd.substring(4).startsWith("buttons") || cmd.substring(4).startsWith("-buttons")) && cmd.endsWith("#reset0")) {
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

                    String[] extraColors = new String[extraTexts.length];
                    String[] extraTextColors = new String[extraTexts.length];

                    for (b = 0; b < extraTexts.length; b++) {
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

                    Handler mainHandler = new Handler(this.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                        }
                    };
                    mainHandler.post(myRunnable);
                }
                //Mode
                else if ((cmd.length() == 6 && Aux.newTrim(cmd, 1).equals("mode ")) || (cmd.length() == 7 && Aux.newTrim(cmd, 1).equals("theme "))) {
                    if (Aux.isDigit(Aux.lastChar(cmd)) && (Aux.getLast(Aux.newTrim(cmd, 1), 5).equals("mode ") || Aux.getLast(Aux.newTrim(cmd, 1), 6).equals("theme "))) {
                        if (Integer.parseInt(Aux.lastChar(cmd)) < 6 && Integer.parseInt(Aux.lastChar(cmd)) > 0) {
                            String newTheme = Aux.lastChar(cmd);
                            String[] themeNames = {"Dark", "Light", "AMOLED Black (Colored Buttons)", "AMOLED Black (Black Buttons)", "Monochrome"};
                            int themeInt = Integer.parseInt(newTheme);

                            tinydb.putString("theme", newTheme);
                            Log.d("printf", "Theme set to " + themeNames[themeInt - 1]);

                            Handler mainHandler = new Handler(this.getMainLooper());

                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.mainActivity.recreate();
                                }
                            };
                            mainHandler.post(myRunnable);
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
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (hex.equals("#reset0")) {
                            Log.d("printf", "Plus button text color reset");
                            cPlus = "\0";
                            tinydb.putString("-b+t", cPlus);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();
                                    }
                                };
                                mainHandler.post(myRunnable);
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
                else if (cmd.length() > 9 && cmd.startsWith("-bs", 4) && !cmd.startsWith("-bsin", 4) && !cmd.startsWith("-bsec", 4)) {
                    if (isHex) {
                        Log.d("printf", "Minus button text color set to " + hex);
                        cMinus = hex;
                        tinydb.putString("-b-t", cMinus);

                        if (!isAll) {
                            Handler mainHandler = new Handler(this.getMainLooper());

                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.mainActivity.recreate();
                                }
                            };
                            mainHandler.post(myRunnable);
                        }
                    }
                    else if (hex.equals("#reset0")) {
                        Log.d("printf", "Minus button text color reset");
                        cMinus = "\0";
                        tinydb.putString("-b-t", cMinus);

                        if (!isAll) {
                            Handler mainHandler = new Handler(this.getMainLooper());

                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.mainActivity.recreate();
                                }
                            };
                            mainHandler.post(myRunnable);
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
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")) {
                            Log.d("printf", "Multiply button text color reset");
                            cMulti = "\0";
                            tinydb.putString("-b×t", cMulti);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
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
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")) {
                            Log.d("printf", "Divide button color reset");
                            cDiv = "\0";
                            tinydb.putString("-b÷t", cDiv);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
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

                    for (c = 0; c < cmdEnd.length(); c++) {
                        if (Aux.chat(cmdEnd, c).equals(" "))
                            break;

                        codeLength++;
                    }

                    String buttonCode = cmdEnd.substring(0, codeLength);
                    String buttonText;

                    if (buttonCode.endsWith("t") && !buttonCode.endsWith("ot")) {
                        buttonText = Aux.newTrim(buttonCode.substring(2), 1);

                        if (Aux.buttonExists(buttonText) || Aux.isExtraButtonCode(buttonCode)) {
                            if (isHex) {
                                Log.d("printf", "Button " + buttonText + " text color set to " + hex);
                                tinydb.putString(buttonCode, hex);
                            }
                            else if (isReset) {
                                Log.d("printf", "Button " + buttonText + " text color reset");
                                tinydb.putString(buttonCode, "\0");
                            }
                        }
                        else {
                            Log.d("printf", "Error: Unknown button code.\nTry running the \"help set\" or \"help get\" commands.");
                        }
                    }
                    else {
                        buttonText = buttonCode.substring(2);

                        if (Aux.buttonExists(buttonText) || Aux.isExtraButtonCode(buttonCode)) {
                            if (isHex) {
                                Log.d("printf", "Button " + buttonText + " color set to " + hex);
                                tinydb.putString(buttonCode, hex);
                            }
                            else if (isReset) {
                                Log.d("printf", "Button " + buttonText + " color reset");
                                tinydb.putString(buttonCode, "\0");
                            }
                        }
                        else {
                            Log.d("printf", "Error: Unknown button code.\nTry running the \"help set\" or \"help get\" commands.");
                        }
                    }

                    if (!isAll && (isHex || isReset)) {
                        Handler mainHandler = new Handler(this.getMainLooper());

                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                            }
                        };
                        mainHandler.post(myRunnable);
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
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")) {
                            Log.d("printf", "Top bar text color reset");
                            cTop = "\0";
                            tinydb.putString("cTop", cTop);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
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
                //Toolbar/UI Text Color
                else if (cmd.length() > 6 && cmd.startsWith("-mt", 4)) {
                    if (cmd.length() > 9) {
                        if (isHex) {
                            Log.d("printf", "Toolbar/UI text color set to " + hex);
                            tinydb.putString("-mt", hex);
                            tinydb.putBoolean("mtIsSet", true);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")) {
                            Log.d("printf", "Toolbar/UI text color reset");
                            tinydb.putString("-mt", "\0");
                            tinydb.putBoolean("mtIsSet", false);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();
                                    }
                                };
                                mainHandler.post(myRunnable);
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
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")) {
                            Log.d("printf", "Keypad text color reset");
                            cNum = "\0";
                            tinydb.putString("cNum", cNum);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
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
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(8).equals("#reset0")) {
                            Log.d("printf", "Delete button icon color reset");
                            cFabText = "\0";
                            tinydb.putString("cFabText", "\0");

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
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
                //All
                else if (cmd.length() > 5 && cmd.substring(4, 6).equals("-a")) {
                    String[] cmdCodes = {"-p", "-s", "-t", "-k", "-m", "-tt", "-kt", "-ft", "-bp", "-bs", "-bm", "-bd", "-f", "-e"};

                    if (cmd.length() > 8) {
                        int k;

                        if (isHex) {
                            isAll = true;

                            for (k = 0; k < 14; k++) {
                                run("set " + cmdCodes[k] + " " + hex);
                            }

                            isAll = false;

                            Handler mainHandler = new Handler(this.getMainLooper());

                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                }
                            };
                            mainHandler.post(myRunnable);
                        }
                        else if (hex.equals("#reset0")) {
                            FloatingActionButton enter = findViewById(R.id.enterCmd);

                            String initCmd = cmd;

                            isAll = true;

                            for (k = 0; k < 14; k++) {
                                run("set " + cmdCodes[k] + " " + hex);
                            }

                            isAll = false;

                            Handler mainHandler = new Handler(this.getMainLooper());

                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                }
                            };
                            mainHandler.post(myRunnable);
                        }
                        else {
                            Log.d("smolPrintf", setError);
                        }

                    }
                    else {
                        Log.d("smolPrintf", setError);
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
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")) {
                            Log.d("printf", "Delete button color reset");
                            cFab = "\0";
                            tinydb.putString("cFab", cFab);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
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
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")) {
                            Log.d("printf", "Primary color reset");
                            cPrimary = "\0";
                            tinydb.putString("cPrimary", cPrimary);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
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
                            Aux.tinydb().putBoolean("isSetSecondary", true);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")) {
                            Log.d("printf", "Secondary color reset");
                            cSecondary = "\0";
                            tinydb.putString("cSecondary", cSecondary);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
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
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")) {
                            Log.d("printf", "Tertiary color reset");
                            cTertiary = "\0";
                            tinydb.putString("cTertiary", cTertiary);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
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
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")) {
                            Log.d("printf", "Keypad color reset");
                            cKeypad = "\0";
                            tinydb.putString("cKeypad", cKeypad);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
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
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")) {
                            Log.d("printf", "Main background color reset");
                            cMain = "\0";
                            tinydb.putString("cMain", cMain);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
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
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else if (cmd.substring(7).equals("#reset0")) {
                            Log.d("printf", "Equals button text color reset");
                            cbEquals = "\0";
                            tinydb.putString("-b=t", cbEquals);

                            if (!isAll) {
                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.mainActivity.recreate();

                                        /*
                                        if (!theme.equals(tinydb.getString("theme")))
                                            EditorActivity.editor.recreate();
                                        */
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                        else {
                            Log.d("printf", setError);
                        }

                    }
                }
                else if (cmd.startsWith("set") || cmd.startsWith("Set") || cmd.startsWith("SET")) {
                    Log.d("printf", setError);
                }

                if (!Aux.isNull(cFabText)) {
                    if (cFabText.equals("#reset0")) {
                        cFabText = "\0";
                        tinydb.putString("cFabText", "\0");
                    }
                }
            }


            //Delete
            else if (cmd.startsWith("delete ") && cmd.length() > 7) {
                String filename = cmd.substring(7);

                if (filename.endsWith(".txt"))
                    filename = Aux.newTrim(filename, 4);

                File path = new File(this.getFilesDir(), "themes");
                File file = new File(path, filename + ".txt");
                boolean deleted = file.delete();

                if (deleted)
                    Log.d("printf", "\"" + filename + "\" successfully deleted.");
                else
                    Log.d("printf", "Error: \"" + filename + "\" could not be deleted.\nPlease check that you have typed the name correctly, and try again.");
            }


            //Share
            else if (cmd.startsWith("share ") && cmd.length() > 6) {
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
            else if (cmd.startsWith("backup") && cmd.length() > 6 && Character.toString(cmd.charAt(6)).equals(" ")) {
                int a, b, c;

                String[] colors = {tinydb.getString("cPrimary"), tinydb.getString("cSecondary"), tinydb.getString("cTertiary"), tinydb.getString("cbEquals"), tinydb.getString("cFab"), tinydb.getString("cPlus"), tinydb.getString("cMinus"), tinydb.getString("cMulti"), tinydb.getString("cDiv"), tinydb.getString("cMain"), tinydb.getString("cKeypad"), tinydb.getString("cTop"), tinydb.getString("cNum"), tinydb.getString("cFabText")};

                int testLength = 0;

                for (a = 0; a < allButtons.length; a++) {
                    for (b = 0; b < allButtons[a].length; b++) {
                        testLength++;
                    }
                }

                testLength += 1;

                String[] extraTexts = new String[testLength + 1];

                c = 0;

                for (a = 0; a < allButtons.length; a++) {
                    for (b = 0; b < allButtons[a].length; b++) {
                        if (allButtons[a][b] != null)
                            extraTexts[c] = allButtons[a][b].getText().toString();

                        c++;
                    }
                }

                extraTexts[extraTexts.length - 1] = "ⁿ√";

                String[] extraColors = new String[extraTexts.length];
                String[] extraTextColors = new String[extraTexts.length];

                for (b = 0; b < extraTexts.length; b++) {
                    extraColors[b] = tinydb.getString("-b" + extraTexts[b]);
                    extraTextColors[b] = tinydb.getString("-b" + extraTexts[b] + "t");
                }

                String fileText, filename, themeName;

                int numColors = colors.length;

                if (cmd.length() > 7 && !cmd.substring(7).equals("\0")) {
                    themeName = cmd.substring(7);

                    if (cmd.substring(7).endsWith(".txt"))
                        filename = themeName;
                    else
                        filename = themeName + ".txt";

                    if (colors[0] == null || colors[0].equals("\0") || colors[0].equals("0"))
                        colors[0] = "#reset0";

                    fileText = colors[0] + "\n";

                    for (i = 1; i < numColors; i++) {
                        if (colors[i] == null || colors[i].equals("\0") || colors[i].equals("") || colors[i].equals("0") || !Aux.isColor(colors[i]))
                            colors[i] = "#reset0";

                        fileText += colors[i] + "\n";
                    }

                    boolean hasAddedButton = false;

                    for (a = 0; a < extraColors.length; a++) {
                        if (extraTexts[a] != null) {
                            if (extraTexts[a].equals("%"))
                                extraTexts[a] = "ⁿ√";

                            if (extraColors[a] != null) {
                                if (Aux.isColor(extraColors[a])) {
                                    if (!hasAddedButton) {
                                        fileText += "\n";
                                        hasAddedButton = true;
                                    }

                                    fileText += extraColors[a] + "-b" + extraTexts[a] + "\n";
                                }
                            }

                            if (extraTextColors[a] != null) {
                                if (Aux.isColor(extraTextColors[a])) {
                                    if (!hasAddedButton) {
                                        fileText += "\n";
                                        hasAddedButton = true;
                                    }

                                    fileText += extraTextColors[a] + "-b" + extraTexts[a] + "t" + "\n";
                                }
                            }
                        }
                    }

                    String[] extraCodes = {"-bop", "-btt", "-bINV2", "-bINV2t", "-mt"};

                    for (a=0; a < extraCodes.length; a++) {
                        if (Aux.isTinyColor(extraCodes[a]))
                            fileText += tinydb.getString(extraCodes[a]) + extraCodes[a] + "\n";
                    }

                    if (Aux.isDigit(bigTheme))
                        fileText += bigTheme;
                    else
                        fileText += tinydb.getString("theme");

                    if (filename.endsWith(".txt"))
                        fileText += "\nname:" + themeName + "\n";

                    writeTheme(this, fileText, filename);
                }
            }





            //Reset
            else if ((cmd.length() > 6 && cmd.startsWith("reset ") && !cmd.equals("reset buttons"))) {
                String end = cmd.substring(6);
                String[] editorCodes = {"-p", "-s", "-t", "-m", "-k", "-kt", "-tt"};

                int e;

                if (end.equals("-s"))
                    Aux.tinydb().putBoolean("isSetSecondary", false);

                if (end.equalsIgnoreCase("all") || end.equals("-a")){
                    run("set -a #reset0");
                    run("reset buttons");

                    Aux.tinydb().putBoolean("isSetSecondary", false);

                    for (e=0; e < editorCodes.length; e++)
                        tinydb.putString(editorCodes[e], "\0");
                }
                else if (end.equalsIgnoreCase("button") || end.equalsIgnoreCase("-buttons")){
                    run("reset buttons");
                }
                else if (Aux.isButtonCode(end)){
                    run("set " + end + " #reset0");
                }
            }




            //Reset Buttons
            else if (cmd.equalsIgnoreCase("reset buttons")){
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

                for (b=0; b < extraTexts.length; b++){
                    extraColors[b] = tinydb.getString("-b" + extraTexts[b]);
                }

                for (a = 0; a < extraColors.length; a++) {
                    tinydb.putString("-b" + extraTexts[a], "\0");
                    tinydb.putString("-b" + extraTexts[a] + "t", "\0");
                }

                tinydb.putString("-bⁿ√", "\0");
                tinydb.putString("-bⁿ√t", "\0");
            }


            //Restore
            else if (cmd.startsWith("restore") && cmd.length() > 7 && Character.toString(cmd.charAt(7)).equals(" ")) {
                int f = 0;
                boolean exists = false;
                String[] colorKeys = {"cPrimary", "cSecondary", "cTertiary", "cbEquals", "cFab", "cPlus", "cMinus", "cMulti", "cDiv", "cMain", "cKeypad", "cTop", "cNum", "cFabText"};

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

                            for (i = 0; (line = bufferedReader.readLine()) != null; i++) {
                                if (!((i > 12 && Aux.isDigit(line)) || Aux.isColor(line) || line.equals("#reset0") || (line.length() >= 6 && (Aux.isColor(line.substring(0, 6)) || Aux.isColor(line.substring(0, 7))) && line.contains("-b")))) {
                                    if (i < 14)
                                        isValid = false;

                                    Log.d("printf", "Error restoring theme:\n" + i + " color hex code (" + line + ") is invalid.\n\nPlease check the file and try again.");
                                }
                            }

                            if (isValid) {
                                fis = new FileInputStream(files[f]);
                                isr = new InputStreamReader(fis);
                                bufferedReader = new BufferedReader(isr);

                                run("reset all");
                                run("reset -bⁿ√");
                                run("reset -bⁿ√t");
                                run("set -mt #reset0");

                                tinydb.putString("-bINV2", "");
                                tinydb.putString("-bINV2t", "");

                                tinydb.putString("-btt", "");
                                tinydb.putString("-bop", "");

                                tinydb.putString("-bfc", "");
                                tinydb.putString("-bfct", "");

                                for (i = 0; (line = bufferedReader.readLine()) != null; i++) {
                                    if (Aux.isDigit(Aux.chat(line, 0)) && line.contains("name:"))
                                        line = Aux.chat(line, 0);

                                    if (Aux.isColor(line)) {
                                        if (i == 3)
                                            tinydb.putString("-b=t", line);
                                        else if (i >= 5 && i <= 8) {
                                            String[] codes = {"-b+t", "-b-t", "-b×t", "-b÷t"};

                                            tinydb.putString(codes[i - 5], line);
                                        }
                                        else {
                                            tinydb.putString(colorKeys[i], line);

                                            if (colorKeys[i].equals("cSecondary"))
                                                Aux.tinydb().putBoolean("isSetSecondary", true);
                                        }
                                    }
                                    else if (line.startsWith("name:")) {
                                        continue;
                                    }
                                    else if (line.equals("#reset0")) {
                                        tinydb.putString(colorKeys[i], "\0");

                                        if (colorKeys[i].equals("cSecondary"))
                                            Aux.tinydb().putBoolean("isSetSecondary", false);
                                    }
                                    else if (line.contains("-b")) {
                                        String buttonHex, buttonCode;

                                        buttonHex = line.substring(0, 7);
                                        buttonCode = Aux.getLast(line, line.length() - buttonHex.length());

                                        if (Aux.isColor(buttonHex) && buttonCode != null) {
                                            run("set " + buttonCode + " " + buttonHex);
                                        }
                                    }
                                    else if (line.endsWith("-mt")){
                                        String uiHex = line.substring(0, 7);

                                        if (Aux.isColor(uiHex)){
                                            run("set -mt " + uiHex);
                                        }
                                    }
                                    else if (Aux.isDigit(line) && Integer.parseInt(line) > 0 && Integer.parseInt(line) <= 5) {
                                        if (line.equals("2"))
                                            tinydb.putString("customTheme", line);
                                        else if (line.equals("5"))
                                            tinydb.putString("customTheme", "2");
                                        else {
                                            if (tinydb.getBoolean("darkStatusBar"))
                                                tinydb.putString("customTheme", "1");
                                            else
                                                tinydb.putString("customTheme", "3");
                                        }

                                        tinydb.putString("theme", tinydb.getString("customTheme"));
                                    }
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
}