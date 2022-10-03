package com.mirambeau.termcalc;

import androidx.core.app.ActivityCompat;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        WebView webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);

        TinyDB tinydb = new TinyDB(this);
        String site = tinydb.getString("site");

        if (site != null && !site.equals("\0")) {
            if (site.equals("play")) {
                webView.loadUrl("https://play.app.goo.gl/?link=https://play.google.com/store/apps/details?id=com.mirambeau.termcalc&ddl=1&pcampaignid=web_ddl_1");
                super.onBackPressed();
            }
            else if (site.equals("spotify")) {
                webView.loadUrl("https://open.spotify.com/artist/59ZjjvjMWzXTsv1KVv9znW?si=gZtQNdORSm-TBo2tcM_KxQ");
                super.onBackPressed();
            }
            else if (site.equals("twitter")) {
                webView.loadUrl("https://twitter.com/nickmirambeau?s=09");
                super.onBackPressed();
            }
            else if (site.equals("discord")) {
                webView.loadUrl("https://discord.gg/6JT5gMGqEC");
                super.onBackPressed();
            }
            else {
                webView.loadUrl(site);
                if (site.startsWith("https://dev"))
                    super.onBackPressed();
            }
        }
    }
}