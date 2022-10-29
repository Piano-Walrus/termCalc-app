package com.mirambeau.termcalc;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {
    public static final String KEY_PREF_COLOR = "color";
    public static final String KEY_PREF_THEME = "theme";

    boolean liveEvaluate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_options);

        TinyDB tinydb = new TinyDB(this);
        tinydb.putBoolean("closeDrawer", true);

        liveEvaluate = tinydb.getBoolean("showPreviousExpression");

        Toolbar toolbar = findViewById(R.id.themeToolbar);

        setTheme(Ax.switchColors[Ax.isFullNum(tinydb.getString("color")) ? (Integer.parseInt(tinydb.getString("color")) - 1) : 0]);

        toolbar.setTitle(R.string.theme_settings);
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

        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
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

    @Override
    public void onBackPressed() {
        TinyDB tinydb = new TinyDB(this);
        tinydb.putBoolean("closeDrawer", true);

        super.onBackPressed();

        if ((tinydb.getBoolean("isDynamic") != tinydb.getBoolean("tempDynamic")) || (tinydb.getInt("precision") != tinydb.getInt("tempPrecision")) ||
                (tinydb.getBoolean("exInput") != tinydb.getBoolean("tempExInput")) ||
                !tinydb.getString("whereCustom").equals(tinydb.getString("tempWhereCustom")) ||
                (tinydb.getBoolean("isFocus") != tinydb.getBoolean("tempIsFocus")) ||
                tinydb.getBoolean("isGradMain") || liveEvaluate != tinydb.getBoolean("showPreviousExpression")) {
            MainActivity.mainActivity.recreate();
        }
    }
}