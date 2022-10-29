package com.mirambeau.termcalc;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

public class AdvancedThemeOptionsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_options);

        TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

        Toolbar toolbar = findViewById(R.id.themeToolbar);

        setTheme(Ax.switchColors[Ax.isFullNum(tinydb.getString("color")) ? (Integer.parseInt(tinydb.getString("color")) - 1) : 0]);

        toolbar.setTitle("Advanced Options");
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_light);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().setStatusBarColor(Color.parseColor("#16171B"));
        getWindow().setNavigationBarColor(Color.parseColor("#16171B"));

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.theme, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.advanced_theme_preferences, rootKey);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        else if (itemId == R.id.terminal){
            startActivity(new Intent(this, TerminalActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public void fullBackup(View v) {

    }

    public void fullRestore(View v) {

    }

    @Override
    public void onBackPressed() {
        TinyDB tinydb = new TinyDB(this);
        tinydb.putBoolean("closeDrawer", true);

        super.onBackPressed();

        try {
            ThemeActivity.themeActivity.recreate();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        ThemeActivity.shouldRecreateMain = true;

        try {
            EditorActivity.editorActivity.recreate();
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
    }
}