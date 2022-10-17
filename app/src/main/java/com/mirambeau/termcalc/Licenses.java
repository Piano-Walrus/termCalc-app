package com.mirambeau.termcalc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

public class Licenses extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.licensesToolbar);
        ConstraintLayout main = findViewById(R.id.licensesMainBG);

        int i;

        TextView[] headers = {findViewById(R.id.apacheHeader), findViewById(R.id.imgAttributionsHeader), findViewById(R.id.artisticHeader), findViewById(R.id.mitHeader)};
        TextView[] iconLinks = {findViewById(R.id.twitterLinkTitle)};

        TextView[] titles = {findViewById(R.id.appcompatTitle), findViewById(R.id.constraintLayoutTitle), findViewById(R.id.materialTitle), findViewById(R.id.material2Title),
                findViewById(R.id.twitterTitle), findViewById(R.id.drawerTitle), findViewById(R.id.navFragTitle), findViewById(R.id.navuiTitle),
                findViewById(R.id.prefTitle), findViewById(R.id.cardTitle), findViewById(R.id.junitTitle), findViewById(R.id.testTitle), findViewById(R.id.colorTitle), findViewById(R.id.introTitle),
                findViewById(R.id.mathTitle), findViewById(R.id.currencyTitle), findViewById(R.id.bigDecimalMathTitle), findViewById(R.id.blurViewTitle)};

        TextView[] licenses = {findViewById(R.id.appcompatCopyright), findViewById(R.id.constraintLayoutCopyright), findViewById(R.id.materialCopyright), findViewById(R.id.material2Copyright),
                findViewById(R.id.twitterLicenseTitle), findViewById(R.id.drawerCopyright), findViewById(R.id.navFragCopyright),
                findViewById(R.id.navuiCopyright), findViewById(R.id.prefCopyright), findViewById(R.id.cardCopyright), findViewById(R.id.junitCopyright), findViewById(R.id.testCopyright),
                findViewById(R.id.colorCopyright), findViewById(R.id.introCopyright), findViewById(R.id.mathCopyright),
                findViewById(R.id.currencyCopyright), findViewById(R.id.bigDecimalMathCopyright), findViewById(R.id.blurViewCopyright)};

        ConstraintLayout[] cards = {findViewById(R.id.appCompatLayout), findViewById(R.id.constraintLayoutLayout), findViewById(R.id.materialLayout),
                findViewById(R.id.material2Layout), findViewById(R.id.twitterLayout), findViewById(R.id.drawerLayout), findViewById(R.id.navFragLayout),
                findViewById(R.id.navuiLayout), findViewById(R.id.prefLayout), findViewById(R.id.cardLayout), findViewById(R.id.junitLayout),
                findViewById(R.id.testLayout), findViewById(R.id.colorLayout), findViewById(R.id.introLayout), findViewById(R.id.mathLayout),
                findViewById(R.id.currencyConverterLayout), findViewById(R.id.bigDecimalMathLayout), findViewById(R.id.blurViewLayout)};

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

            for (i=0; i < headers.length; i++){
                headers[i].setTextColor(Color.parseColor("#222222"));

                if (i < iconLinks.length)
                    iconLinks[i].setTextColor(Color.parseColor("#222222"));
            }

            for (i=0; i < cards.length; i++){
                cards[i].setBackground(getResources().getDrawable(R.drawable.rounded_bg_light));
                titles[i].setTextColor(Color.parseColor("#222222"));
                licenses[i].setTextColor(Color.parseColor("#222222"));
            }
        }
        else {
            toolbar.setBackgroundColor(Color.parseColor("#000000"));
            main.setBackgroundColor(Color.parseColor("#000000"));

            getWindow().setNavigationBarColor(Color.parseColor("#000000"));
            getWindow().setStatusBarColor(Color.parseColor("#000000"));

            for (i=0; i < cards.length; i++){
                try {
                    cards[i].setBackground(getResources().getDrawable(R.drawable.rounded_bg_black));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            main.setFitsSystemWindows(true);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.licenses_title));

        if (getSupportActionBar() != null) {
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

    public void openLink(View v){
        new TinyDB(this).putString("site", ((TextView) v).getText().toString());

        Intent webIntent = new Intent(this, WebViewActivity.class);
        startActivity(webIntent);
    }
}