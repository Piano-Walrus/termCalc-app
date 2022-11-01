package com.mirambeau.termcalc;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class Contributors extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contributors);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

        Toolbar toolbar = findViewById(R.id.contributorsToolbar);
        ConstraintLayout main = findViewById(R.id.contributorsMainBG);

        TextView[] names = {findViewById(R.id.firstCont), findViewById(R.id.secondCont)};
        TextView[] descriptions = {findViewById(R.id.contDesc1), findViewById(R.id.contDesc2)};

        ImageView imgTerry = findViewById(R.id.img_terry);
        CardView icTerry = findViewById(R.id.ic_terry);
        ImageView imgQuetz = findViewById(R.id.img_quetz);
        CardView icQuetz = findViewById(R.id.ic_quetz);

        if (Build.VERSION.SDK_INT > 23) {
            icTerry.setVisibility(View.VISIBLE);
            icQuetz.setVisibility(View.VISIBLE);

            imgTerry.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_terry));
            imgQuetz.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_quetz));
        }

        int i;

        if (theme == null || theme.equals("\0"))
            theme = "1";

        if (theme.equals("1")){
            getWindow().setStatusBarColor(Color.parseColor("#16181B"));
            getWindow().setNavigationBarColor(Color.parseColor("#16181B"));
        }
        else if (theme.equals("2")){
            setTheme(R.style.LightTheme);
            toolbar.setBackgroundColor(Color.parseColor("#FFFFFF"));
            main.setBackgroundColor(Color.parseColor("#FFFFFF"));


            getWindow().setNavigationBarColor(Color.parseColor("#1A1A1B"));

            if (Build.VERSION.SDK_INT >= 23) {
                main.setFitsSystemWindows(true);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }

            for (i=0; i < names.length; i++){
                names[i].setTextColor(Color.parseColor("#222222"));
                descriptions[i].setTextColor(Color.parseColor("#8D8D8D"));
            }
        }
        else {
            toolbar.setBackgroundColor(Color.parseColor("#000000"));
            main.setBackgroundColor(Color.parseColor("#000000"));

            getWindow().setNavigationBarColor(Color.parseColor("#000000"));
            getWindow().setStatusBarColor(Color.parseColor("#000000"));

            main.setFitsSystemWindows(true);
        }

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Contributors");

            if (theme.equals("2")) {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_dark);
                toolbar.setTitleTextColor(Color.parseColor("#222222"));
            }
            else {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_toolbar_back_light);
                toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
            }
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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