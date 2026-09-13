package com.sujayarvind.dax90;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUserAgentString(settings.getUserAgentString() + " 90DaX-Android");
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new DaXBridge(), "Android90DaX");
        webView.loadUrl("https://sujayarvind132.github.io/90DaX/");
        setContentView(webView);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    private class DaXBridge {
        @JavascriptInterface
        public void syncState(String json) {
            getSharedPreferences(DaXWidget.PREFS, MODE_PRIVATE)
                    .edit().putString(DaXWidget.STATE_KEY, json).apply();
            DaXWidget.updateAll(MainActivity.this);
        }
    }
}
