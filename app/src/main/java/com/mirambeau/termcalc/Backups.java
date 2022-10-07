package com.mirambeau.termcalc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

public class Backups extends AppCompatActivity {
    final Activity main = MainActivity.mainActivity;

    public static String THEME_DARK = Aux.tinydb().getBoolean("darkStatusBar") ? "1" : "3";
    public static String THEME_LIGHT = "2";

    final private ArrayList<BackupCard> cards = new ArrayList<>();

    private BackupAdapter adapter;
    RecyclerView recyclerView;

    Button[] nums, compBar, trigBar, mainOps;
    Button bDec, bParenthesisOpen, bParenthesisClose, bEquals, bMod;
    Button[][] allButtons;

    int bigPosition;
    String cmd, bigTheme = "N/A";

    boolean isRestore = false, isAll = false, isHex = false, ftIsSecondary = false, shouldRecreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_backups);

            final TinyDB tinydb = new TinyDB(this);
            tinydb.putString("tempTheme", tinydb.getString("theme"));

            int i;

            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(Color.parseColor("#16181B"));
                getWindow().setNavigationBarColor(Color.parseColor("#16181B"));
            }

            Toolbar toolbar = findViewById(R.id.backupsToolbar);

            toolbar.setTitle("Backups");
            toolbar.showOverflowMenu();
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_light);

            setTheme(R.style.ThemeOverlay_AppCompat_Dark);

            bDec = main.findViewById(R.id.bDec);
            bParenthesisOpen = main.findViewById(R.id.bParenthesisOpen);
            bParenthesisClose = main.findViewById(R.id.bParenthesisClose);
            bEquals = main.findViewById(R.id.bEquals);
            bMod = main.findViewById(R.id.bMod);

            nums = new Button[]{main.findViewById(R.id.b0), main.findViewById(R.id.b1), main.findViewById(R.id.b2), main.findViewById(R.id.b3), main.findViewById(R.id.b4), main.findViewById(R.id.b5), main.findViewById(R.id.b6), main.findViewById(R.id.b7), main.findViewById(R.id.b8), main.findViewById(R.id.b9)};
            compBar = new Button[]{main.findViewById(R.id.bSqrt), main.findViewById(R.id.bExp), main.findViewById(R.id.bFact), main.findViewById(R.id.bPi), main.findViewById(R.id.bE), main.findViewById(R.id.bLog), main.findViewById(R.id.bLn), bMod};
            trigBar = new Button[]{main.findViewById(R.id.bSin), main.findViewById(R.id.bCos), main.findViewById(R.id.bTan), main.findViewById(R.id.bCsc), main.findViewById(R.id.bSec), main.findViewById(R.id.bCot), main.findViewById(R.id.bInv)};
            mainOps = new Button[]{main.findViewById(R.id.sPlus), main.findViewById(R.id.sMinus), main.findViewById(R.id.sMulti), main.findViewById(R.id.sDiv)};

            allButtons = new Button[][]{nums, compBar, trigBar, mainOps, {bDec, bParenthesisOpen, bParenthesisClose, bEquals, bMod}};

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

            recyclerView = findViewById(R.id.backupView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            final FloatingActionButton newBackup = findViewById(R.id.newBackup);

            adapter.setOnItemClickListener(new BackupAdapter.OnItemClickListener() {
                @Override
                public void onDeleteClick(final int position) {
                    final String title = cards.get(position).getThemeName();

                    AlertDialog.Builder builder = new AlertDialog.Builder(Backups.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                    builder.setTitle("Are you sure you want to delete \"" + title + "\"?\n");

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            try {
                                run("delete " + title);
                                removeItem(position);
                                Toast.makeText(Backups.this, "Successfully deleted \"" + title + "\"", Toast.LENGTH_SHORT).show();
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

                    shouldRecreate = true;
                    onBackPressed();

                    Toast.makeText(Backups.this, "Successfully restored \"" + title + "\"", Toast.LENGTH_SHORT).show();

                    HandlerThread thread = new HandlerThread("MyHandlerThread");
                    thread.start();

                    new Handler(thread.getLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            newRun("restore " + title);
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

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                }

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (recyclerView.canScrollVertically(1))
                        newBackup.show();
                    else if (!recyclerView.canScrollVertically(1) && recyclerView.canScrollVertically(-1))
                        newBackup.hide();

                    super.onScrollStateChanged(recyclerView, newState);
                }
            });

            if (Aux.isColor(tinydb.getString("cPrimary")))
                newBackup.setRippleColor(Color.parseColor(tinydb.getString("cPrimary")));
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            finish();
        }
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
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(Backups.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

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

                                    Toast.makeText(Backups.this, "Successfully renamed \"" + old + "\" to \"" + themeName + "\"", Toast.LENGTH_SHORT).show();

                                    replaceItem(themeName, getCardPosition(themeName));
                                    recyclerView.scrollToPosition(getCardPosition(themeName));
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

                            Toast.makeText(Backups.this, "Successfully renamed \"" + old + "\" to \"" + themeName + "\"", Toast.LENGTH_SHORT).show();

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

    public void save(View v) {
        try {
            if (themeTitle.equals("\0")) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

                final TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

                builder.setTitle("Backup Current Theme\n");

                View viewInflated = LayoutInflater.from(this).inflate(R.layout.content, (ViewGroup) findViewById(R.id.mainBackup), false);

                final EditText input = (EditText) viewInflated.findViewById(R.id.input);

                int theme;

                try {
                    theme = Aux.getThemeInt();
                }
                catch (Exception e) {
                    e.printStackTrace();

                    try {
                        theme = Integer.parseInt(tinydb.getString("theme"));
                    }
                    catch (Exception e2) {
                        theme = 1;
                    }
                }

                bigTheme = (theme == 2 || theme == 5) ? THEME_LIGHT : THEME_DARK;

                builder.setView(viewInflated);

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        final String themeName = input.getText().toString();

                        if (themeExists(themeName)) {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(Backups.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                            builder2.setTitle("Do you want to overwrite \"" + themeName + "\"?\n");

                            builder2.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    int position = getCardPosition(themeName);

                                    if (!themeName.equals("\0") && !themeName.equals("") && themeName.length() > 0) {
                                        try {
                                            run("backup " + themeName);

                                            ArrayList<BackupCard> cards = new ArrayList<>();

                                            File directory = new File(MainActivity.mainActivity.getFilesDir(), "themes");
                                            File[] files = directory.listFiles();

                                            if (files != null) {
                                                if (files.length > 0) {
                                                    for (File file : files) {
                                                        if (file.getName().endsWith(".txt"))
                                                            cards.add(new BackupCard(file.getName().replace(".txt", "")));
                                                    }
                                                }
                                                else {
                                                    Log.d("printf", "\nError: No theme backups currently exist.");
                                                }
                                            }
                                            else {
                                                Log.d("printf", "\nError: No theme backups currently exist.");
                                            }

                                            replaceItem(themeName, position);
                                            recyclerView.scrollToPosition(position);

                                            Toast.makeText(Backups.this, "Successfully backed up \"" + input.getText().toString() + "\"", Toast.LENGTH_SHORT).show();

                                            bigTheme = "N/A";
                                        }
                                        catch (IOException except) {
                                            except.printStackTrace();
                                        }
                                    }
                                    else
                                        Toast.makeText(Backups.this, "Error: Theme name cannot be empty", Toast.LENGTH_SHORT).show();
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
                            if (!themeName.equals("\0") && !themeName.equals("") && themeName.length() > 0) {
                                try {
                                    run("backup " + themeName);

                                    ArrayList<BackupCard> cards = new ArrayList<>();

                                    File directory = new File(MainActivity.mainActivity.getFilesDir(), "themes");
                                    File[] files = directory.listFiles();

                                    if (files != null) {
                                        if (files.length > 0) {
                                            for (File file : files) {
                                                if (file.getName().endsWith(".txt"))
                                                    cards.add(new BackupCard(file.getName().replace(".txt", "")));
                                            }
                                        }
                                        else {
                                            Log.d("printf", "\nError: No theme backups currently exist.");
                                        }
                                    }
                                    else {
                                        Log.d("printf", "\nError: No theme backups currently exist.");
                                    }

                                    addItem(themeName);
                                    recyclerView.scrollToPosition(cards.size() - 1);

                                    Toast.makeText(Backups.this, "Successfully backed up \"" + input.getText().toString() + "\"", Toast.LENGTH_SHORT).show();

                                    bigTheme = "N/A";
                                } catch (IOException except) {
                                    except.printStackTrace();
                                }
                            }
                            else
                                Toast.makeText(Backups.this, "Error: Theme name cannot be empty", Toast.LENGTH_SHORT).show();
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
            else {
                try {
                    run("backup " + themeTitle);

                    ArrayList<BackupCard> cards = new ArrayList<>();

                    File directory = new File(MainActivity.mainActivity.getFilesDir(), "themes");
                    File[] files = directory.listFiles();

                    if (files != null) {
                        if (files.length > 0) {
                            for (File file : files) {
                                if (file.getName().endsWith(".txt"))
                                    cards.add(new BackupCard(file.getName().replace(".txt", "")));
                            }
                        }
                        else {
                            Log.d("printf", "\nError: No theme backups currently exist.");
                        }
                    }
                    else {
                        Log.d("printf", "\nError: No theme backups currently exist.");
                    }

                    addItem(themeTitle);
                    recyclerView.scrollToPosition(cards.size() - 1);

                    Toast.makeText(Backups.this, "Successfully backed up \"" + themeTitle + "\"", Toast.LENGTH_SHORT).show();
                } catch (IOException except) {
                    except.printStackTrace();
                }
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.backups_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.help) {
            final AlertDialog.Builder builder = Aux.createAlertDialog("Help", Backups.this);

            builder.setMessage("Any themes you save can be found here.\n\nUsing the buttons on each theme's card, you can either favorite, share, delete, rename, or restore that theme.\n\nThe button on the bottom right of the screen can be used to import new themes from your device's file manager.");

            builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();
        }
        else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (shouldRecreate) {
            Aux.restored = true;

            try {
                EditorActivity.editorActivity.recreate();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            ThemeActivity.themeActivity.recreate();
        }

        super.onBackPressed();
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

                shouldRecreate = true;
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
}