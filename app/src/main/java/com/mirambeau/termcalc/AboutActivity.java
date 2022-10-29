package com.mirambeau.termcalc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

public class AboutActivity extends AppCompatActivity {
    int darkGray = Color.parseColor("#222222");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

            if (theme == null) {
                theme = "1";
            }

            if (theme.equals("1")) {
                setContentView(R.layout.activity_about);

                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setStatusBarColor(Color.parseColor("#16181B"));
                    getWindow().setNavigationBarColor(Color.parseColor("#16181B"));
                }
            }
            else if (theme.equals("2")) {
                setContentView(R.layout.activity_about_light);

                ConstraintLayout parent = findViewById(R.id.parentAbout);
                parent.setFitsSystemWindows(true);

                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setNavigationBarColor(Color.parseColor("#1A1A1B"));

                    if (Build.VERSION.SDK_INT >= 23) {
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    }
                }
            }
            else if (theme.equals("3") || theme.equals("4")) {
                setContentView(R.layout.activity_about_black);

                ConstraintLayout parent = findViewById(R.id.parentAbout);
                parent.setFitsSystemWindows(true);

                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setNavigationBarColor(Color.BLACK);
                    getWindow().setStatusBarColor(Color.BLACK);
                }
            }
            else {
                setContentView(R.layout.activity_about);

                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setStatusBarColor(Color.parseColor("#16181B"));
                    getWindow().setNavigationBarColor(Color.parseColor("#16181B"));
                }
            }

            Toolbar toolbar = findViewById(R.id.aboutToolbar);

            toolbar.setTitle("About");
            toolbar.showOverflowMenu();
            setSupportActionBar(toolbar);

            if (theme.equals("2")) {
                toolbar.setTitleTextColor(darkGray);

                if (getSupportActionBar() != null)
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_dark);
            }
            else {
                toolbar.setTitleTextColor(Color.WHITE);

                if (getSupportActionBar() != null)
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_light);
            }

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            setTheme(R.style.DarkTheme);

            if (theme.equals("2"))
                setTheme(R.style.ThemeOverlay_AppCompat_Light);
            else
                setTheme(R.style.ThemeOverlay_AppCompat_Dark);

            TextView version = findViewById(R.id.textView2);

            version.setText(getResources().getString(R.string.version) + " " + version.getText().toString());
        }
        catch (Exception e){
            e.printStackTrace();
            Ax.saveStack(e);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.main_about, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.contributors) {
            Intent contributorsIntent = new Intent(this, Contributors.class);
            startActivity(contributorsIntent);
        }
        else if (item.getItemId() == R.id.attributions) {
            Intent licensesIntent = new Intent(this, Licenses.class);
            startActivity(licensesIntent);
        }
        else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openWebView(View v){
        TinyDB tinydb = new TinyDB(this);
        tinydb.putString("site", "play");

        Intent webIntent = new Intent(this, WebViewActivity.class);
        startActivity(webIntent);
    }

    public void openChangelog(View v){
        Intent changelogIntent = new Intent(this, ChangelogActivity.class);
        startActivity(changelogIntent);
    }

    public void openLink(String name){
        TinyDB tinydb = new TinyDB(this);
        tinydb.putString("site", name);

        Intent intent = new Intent(this, WebViewActivity.class);
        startActivity(intent);
    }

    public void linkHelper(View v){
        if (v != null && v.getTag() != null && v.getTag().toString().length() > 1)
            openLink(v.getTag().toString().toLowerCase());
    }

    public void openBugReport(View v){
        Intent bugIntent = new Intent(this, BugReportActivity.class);
        startActivity(bugIntent);
    }

    public void back(View v){
        super.onBackPressed();
    }
}