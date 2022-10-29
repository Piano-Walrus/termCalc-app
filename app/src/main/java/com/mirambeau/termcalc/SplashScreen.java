package com.mirambeau.termcalc;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TinyDB tinydb = new TinyDB(this);
        String theme = tinydb.getString("theme");

        if (Ax.isNull(theme))
            theme = "1";

        if (theme.equals("1"))
            setContentView(R.layout.activity_splash_screen);
        else if (theme.equals("2"))
            setContentView(R.layout.activity_splash_screen_light);
        else
            setContentView(R.layout.activity_splash_screen_black);

        tinydb.putString("fragTag", "Splash");

        int stackCount = tinydb.getInt("stackCount");

        if (stackCount < 2 && stackCount >= 0) {
            tinydb.putInt("stackCount", stackCount + 1);
        }
        else if (stackCount >= 2){
            tinydb.putString("stackTrace", "No crash has been recorded.");
            tinydb.putString("reason", "No crash has been recorded.");

            tinydb.putInt("stackCount", 0);
        }
        else {
            tinydb.putInt("stackCount", 0);
        }

        try {
            int systemBars;

            if (theme.equals("2")) {
                systemBars = Color.parseColor("#1A1A1B");

                if (Build.VERSION.SDK_INT >= 23)
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            else if (theme.equals("1")) {
                systemBars = getResources().getColor(R.color.darkColorPrimaryDark);

                getWindow().setStatusBarColor(systemBars);
            }
            else {
                systemBars = Color.BLACK;

                getWindow().setStatusBarColor(Color.BLACK);
            }

            getWindow().setNavigationBarColor(systemBars);
        }
        catch (NullPointerException ignored) {
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 88);
    }
}