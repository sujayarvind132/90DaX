package com.sujayarvind.dax90

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.userAgentString += " 90DaX-Android"
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                view.evaluateJavascript("""
                    (function(){
                      function sync(){
                        try {
                          const a=JSON.parse(localStorage.getItem('90dax-activities')||'[]');
                          const c=JSON.parse(localStorage.getItem('90dax-completed')||'{}');
                          const now=new Date(); const m=now.getHours()*60+now.getMinutes();
                          let cur=a.find(x=>{let [h,mm]=x.time.split(':').map(Number);let [eh,em]=x.end.split(':').map(Number);let s=h*60+mm,e=eh*60+em;return x.title==='Sleep'?(m>=1410||m<450):(m>=s&&m<e);});
                          let up=cur||a.find(x=>{let [h,mm]=x.time.split(':').map(Number);return h*60+mm>m;});
                          Android90DaX.syncState(JSON.stringify({doneCount:Object.keys(c).length,total:a.length,current:(up&&up.title)||'Open 90DaX'}));
                        } catch(e){}
                      }
                      sync(); setInterval(sync,5000);
                    })();
                """.trimIndent(), null)
            }
        }
        webView.addJavascriptInterface(DaXBridge(this), "Android90DaX")
        webView.loadUrl("https://sujayarvind132.github.io/90DaX/")
        setContentView(webView)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("90dax-widget", ExistingPeriodicWorkPolicy.UPDATE, PeriodicWorkRequestBuilder<WidgetWorker>(30, TimeUnit.MINUTES).build())
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}

class DaXBridge(private val activity: Activity) {
    @JavascriptInterface
    fun syncState(json: String) {
        activity.getSharedPreferences("dax", 0).edit().putString("state", json).apply()
        DaXWidget().updateAll(activity)
    }
}
