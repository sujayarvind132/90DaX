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
        @Override public void run() { syncFromWebApp(); handler.postDelayed(this, 15000); }
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override protected void onCreate(Bundle savedInstanceState) {
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
        if(webView==null)return;
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
