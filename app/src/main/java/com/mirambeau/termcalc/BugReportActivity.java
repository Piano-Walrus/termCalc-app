package com.mirambeau.termcalc;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.core.app.ActivityCompat;

public class BugReportActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.loadUrl(
                "https://docs.google.com/forms/d/e/1FAIpQLScEMnaH6fuxM_6VGipbRbuh1JqYfE--_TNqsM4CDHoBQTZtBA/viewform?usp=sf_link");
    }
}