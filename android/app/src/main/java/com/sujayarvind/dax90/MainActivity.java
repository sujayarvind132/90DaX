package com.sujayarvind.dax90;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;
    private final Handler handler = new Handler();
    private final Runnable syncRunnable = new Runnable() {
        @Override public void run() {
            syncFromWebApp();
            handler.postDelayed(this, 15000);
        }
    };

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
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                syncFromWebApp();
                handler.removeCallbacks(syncRunnable);
                handler.postDelayed(syncRunnable, 15000);
                handleWidgetIntent(getIntent());
            }
        });
        webView.addJavascriptInterface(new DaXBridge(), "Android90DaX");
        webView.loadUrl("https://sujayarvind132.github.io/90DaX/");
        setContentView(webView);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleWidgetIntent(intent);
    }

    private void handleWidgetIntent(Intent intent) {
        if (webView == null || intent == null) return;
        String action = intent.getStringExtra("widget_action");
        if (action == null) return;
        if (action.startsWith("complete:")) {
            String id = action.substring("complete:".length());
            webView.evaluateJavascript("(function(){try{const c=JSON.parse(localStorage.getItem('90dax-completed')||'{}');c[" + id + "]=true;localStorage.setItem('90dax-completed',JSON.stringify(c));location.reload();}catch(e){}})()", null);
        } else if (action.startsWith("page:")) {
            String page = action.substring("page:".length());
            String safe = page.replace("'", "");
            webView.evaluateJavascript("(function(){try{const els=[...document.querySelectorAll('button')];const b=els.find(x=>x.innerText.trim()==='" + safe + "');if(b)b.click();}catch(e){}})()", null);
        }
    }

    private void syncFromWebApp() {
        if (webView == null) return;
        String js = "(function(){try{" +
                "const a=JSON.parse(localStorage.getItem('90dax-activities')||'null')||[];" +
                "const c=JSON.parse(localStorage.getItem('90dax-completed')||'{}');" +
                "const n=new Date().getHours()*60+new Date().getMinutes();" +
                "function m(x){const p=x.split(':').map(Number);return p[0]*60+p[1];}" +
                "function st(x){if(c[x.id])return 'done';const s=m(x.time),e=m(x.end);if(x.title==='Sleep')return n>=1410||n<450?'current':n>=450?'missed':'upcoming';return n>=s&&n<e?'current':n>=e?'missed':'upcoming';}" +
                "let current=a.find(x=>st(x)==='current')||a.find(x=>st(x)==='upcoming');" +
                "Android90DaX.syncState(JSON.stringify({doneCount:Object.keys(c).filter(k=>c[k]).length,total:a.length||13,current:current?current.title:'Open 90DaX',activities:a.map(x=>({id:x.id,title:x.title,time:x.time,end:x.end,category:x.category,status:st(x)}))}));" +
                "}catch(e){}})()";
        webView.evaluateJavascript(js, null);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (webView != null) webView.destroy();
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    private class DaXBridge {
        @JavascriptInterface public void syncState(String json) {
            getSharedPreferences(DaXWidget.PREFS, MODE_PRIVATE).edit().putString(DaXWidget.STATE_KEY, json).apply();
            DaXWidget.updateAll(MainActivity.this);
        }
    }
}
