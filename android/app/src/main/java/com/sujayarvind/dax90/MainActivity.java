package com.sujayarvind.dax90;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends Activity {
    private static final String APP_URL = "https://sujayarvind132.github.io/90DaX/";
    private WebView webView;
    private boolean pageFailed = false;
    private final Handler handler = new Handler();
    private final Runnable syncRunnable = new Runnable() {
        @Override public void run() { syncFromWebApp(); handler.postDelayed(this, 15000); }
    };

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(9, 11, 10));
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setUserAgentString(settings.getUserAgentString() + " 90DaX-Android");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pageFailed = false;
                syncFromWebApp();
                handler.removeCallbacks(syncRunnable);
                handler.postDelayed(syncRunnable, 15000);
                handleWidgetIntent(getIntent());
            }

            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) showConnectionFallback();
            }
        });
        webView.addJavascriptInterface(new DaXBridge(), "Android90DaX");
        setContentView(webView);
        loadApp();
    }

    private void loadApp() {
        pageFailed = false;
        // Cache-busting prevents an old WebView asset cache from mixing with a new GitHub Pages build.
        webView.loadUrl(APP_URL + "?android=1&v=" + System.currentTimeMillis());
    }

    private void showConnectionFallback() {
        if (pageFailed || webView == null) return;
        pageFailed = true;
        webView.loadDataWithBaseURL(APP_URL,
            "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'><style>html,body{margin:0;background:#090b0a;color:#fff;font-family:Arial,sans-serif;height:100%}body{display:flex;align-items:center;justify-content:center}.box{text-align:center;padding:28px}.x{font-size:48px;color:#20e878;font-weight:800}.title{font-size:22px;font-weight:700;margin:12px 0 8px}.text{color:#9aa29d;font-size:14px;line-height:1.5}.btn{margin-top:20px;padding:12px 18px;border:0;border-radius:10px;background:#20e878;color:#061009;font-weight:700;font-size:14px}</style></head><body><div class='box'><div class='x'>90DaX</div><div class='title'>Unable to load 90DaX</div><div class='text'>Please check your internet connection and try again.</div><button class='btn' onclick="location.href='https://sujayarvind132.github.io/90DaX/?android=1&retry='+Date.now()">RETRY</button></div></body></html>",
            "text/html", "UTF-8", null);
    }

    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); handleWidgetIntent(intent); }

    private void handleWidgetIntent(Intent intent) {
        if (webView == null || intent == null) return;
        String action=intent.getStringExtra("widget_action"); if(action==null)return;
        if(action.startsWith("complete:")) {
            String id=action.substring("complete:".length());
            webView.evaluateJavascript("(function(){try{const d=new Date(),k=d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0');const all=JSON.parse(localStorage.getItem('90dax-completed-by-date')||'{}');const c=all[k]||{};c['"+id+"']=true;all[k]=c;localStorage.setItem('90dax-completed-by-date',JSON.stringify(all));localStorage.setItem('90dax-completed',JSON.stringify(c));location.reload();}catch(e){}})()",null);
        } else if(action.startsWith("page:")) {
            String page=action.substring("page:".length()).replace("'","");
            webView.evaluateJavascript("(function(){try{const els=[...document.querySelectorAll('button')];const b=els.find(x=>x.innerText.trim()==='"+page+"');if(b)b.click();}catch(e){}})()",null);
        }
    }

    private void syncFromWebApp() {
        if(webView==null || pageFailed)return;
        String js="(function(){try{"+
          "const a=JSON.parse(localStorage.getItem('90dax-activities')||'null')||[];"+
          "const d=new Date(),k=d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0');"+
          "const all=JSON.parse(localStorage.getItem('90dax-completed-by-date')||'{}');const c=all[k]||JSON.parse(localStorage.getItem('90dax-completed')||'{}');"+
          "const n=d.getHours()*60+d.getMinutes()+d.getSeconds()/60;function m(x){const p=x.split(':').map(Number);return p[0]*60+p[1];}"+
          "function st(x){if(c[x.id])return 'done';const s=m(x.time),e=m(x.end);if(x.title==='Sleep')return n>=1410||n<450?'current':'missed';return n>=s&&n<e?'current':n>=e?'missed':'upcoming';}"+
          "const mapped=a.map(x=>({id:x.id,title:x.title,time:x.time,end:x.end,category:x.category,status:st(x)}));const current=mapped.find(x=>x.status==='current')||mapped.find(x=>x.status==='upcoming');"+
          "Android90DaX.syncState(JSON.stringify({date:k,doneCount:mapped.filter(x=>x.status==='done').length,total:mapped.length||13,current:current?current.title:'Open 90DaX',activities:mapped}));"+
          "}catch(e){}})()";
        webView.evaluateJavascript(js,null);
    }

    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);if(webView!=null)webView.destroy();super.onDestroy();}
    @Override public void onBackPressed(){if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed();}
    private class DaXBridge { @JavascriptInterface public void syncState(String json){getSharedPreferences(DaXWidget.PREFS,MODE_PRIVATE).edit().putString(DaXWidget.STATE_KEY,json).apply();DaXWidget.updateAll(MainActivity.this);} }
}
