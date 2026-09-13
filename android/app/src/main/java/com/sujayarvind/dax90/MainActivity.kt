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
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.userAgentString += " 90DaX-Android"
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(DaXBridge(this), "Android90DaX")
        webView.loadUrl("https://sujayarvind132.github.io/90DaX/")
        setContentView(webView)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("90dax-widget", ExistingPeriodicWorkPolicy.UPDATE, PeriodicWorkRequestBuilder<WidgetWorker>(30, TimeUnit.MINUTES).build())
    }

    override fun onBackPressed() {
        val w = window.decorView.findViewById<WebView>(android.R.id.content)
        if (w?.canGoBack() == true) w.goBack() else super.onBackPressed()
    }
}

class DaXBridge(private val activity: Activity) {
    @JavascriptInterface
    fun syncState(json: String) {
        activity.getSharedPreferences("dax", 0).edit().putString("state", json).apply()
        DaXWidget().updateAll(activity)
    }
}
