package com.sujayarvind.dax90;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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
            }
        });
        webView.addJavascriptInterface(new DaXBridge(), "Android90DaX");
        webView.loadUrl("https://sujayarvind132.github.io/90DaX/");
        setContentView(webView);
    }

    private void syncFromWebApp() {
        if (webView == null) return;
        String js = "(function(){" +
                "try{" +
                "const a=JSON.parse(localStorage.getItem('90dax-activities')||'null')||[];" +
                "const c=JSON.parse(localStorage.getItem('90dax-completed')||'{}');" +
                "const now=new Date(); const n=now.getHours()*60+now.getMinutes();" +
                "function m(x){const p=x.split(':').map(Number);return p[0]*60+p[1];}" +
                "let current='Open 90DaX';" +
                "for(const x of a){if(c[x.id])continue; const s=m(x.time),e=m(x.end);" +
                "if((x.title==='Sleep'&&(n>=1410||n<450))||(x.title!=='Sleep'&&n>=s&&n<e)){current=x.title;break;}" +
                "}" +
                "Android90DaX.syncState(JSON.stringify({doneCount:Object.keys(c).filter(k=>c[k]).length,total:a.length||13,current:current}));" +
                "}catch(e){}" +
                "})()";
        webView.evaluateJavascript(js, null);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (webView != null) webView.destroy();
        super.onDestroy();
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
