package com.mirambeau.termcalc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

public class ChangelogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

        TinyDB tinydb = new TinyDB(this);

        if (theme == null){
            theme = "1";
        }

        if (theme.equals("1")) {
            setContentView(R.layout.activity_changelog);

            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(Color.parseColor("#16181B"));
                getWindow().setNavigationBarColor(Color.parseColor("#16181B"));
            }
        }
        else if (theme.equals("2")) {
            setContentView(R.layout.activity_changelog_light);

            ConstraintLayout parent = findViewById(R.id.changeMain);
            parent.setFitsSystemWindows(true);

            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setNavigationBarColor(Color.parseColor("#1A1A1B"));

                if (Build.VERSION.SDK_INT >= 23) {
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }
            }
        }
        else if (theme.equals("3") || theme.equals("4")){
            setContentView(R.layout.activity_changelog_black);

            ConstraintLayout parent = findViewById(R.id.changeMain);
            parent.setFitsSystemWindows(true);

            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setNavigationBarColor(Color.parseColor("#000000"));
                getWindow().setStatusBarColor(Color.parseColor("#000000"));
            }
        }
        else {
            setContentView(R.layout.activity_changelog);

            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(Color.parseColor("#16181B"));
                getWindow().setNavigationBarColor(Color.parseColor("#16181B"));
            }
        }

        Toolbar toolbar = findViewById(R.id.changelogToolbar);

        toolbar.setTitle(getResources().getString(R.string.changelog));

        setSupportActionBar(toolbar);

        if (theme.equals("2")){
            toolbar.setTitleTextColor(Color.parseColor("#222222"));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_dark);
        }
        else {
            toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_light);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTheme(R.style.DarkTheme);

        TextView[] versions = {findViewById(R.id.versionTitle), findViewById(R.id.previousTitle), findViewById(R.id.beforePreviousTitle), findViewById(R.id.beforePreviousTitle2),
                findViewById(R.id.beforePreviousTitle3), findViewById(R.id.beforePreviousTitle4)};

        for (TextView version : versions)
            version.setText(getResources().getString(R.string.version) + " " + version.getText().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}