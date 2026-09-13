package com.sujayarvind.dax90

import android.content.Context
import android.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import androidx.glance.unit.sp

class DaXWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val raw = context.getSharedPreferences("dax", 0).getString("state", null) ?: ""
        val done = Regex("\\\"doneCount\\\"\\s*:\\s*(\\d+)").find(raw)?.groupValues?.getOrNull(1) ?: "0"
        val total = Regex("\\\"total\\\"\\s*:\\s*(\\d+)").find(raw)?.groupValues?.getOrNull(1) ?: "13"
        val current = Regex("\\\"current\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"").find(raw)?.groupValues?.getOrNull(1) ?: "Open 90DaX"
        provideContent {
            Column(modifier = GlanceModifier.fillMaxSize().background(ColorProvider(Color.rgb(9,11,10))).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("90DaX", style = TextStyle(color = ColorProvider(Color.WHITE), fontSize = 18.sp))
                    Text("  •  TODAY", style = TextStyle(color = ColorProvider(Color.LTGRAY), fontSize = 10.sp), modifier = GlanceModifier.padding(start = 5.dp))
                }
                Text("$done / $total activities", style = TextStyle(color = ColorProvider(Color.WHITE), fontSize = 15.sp), modifier = GlanceModifier.padding(top = 6.dp))
                Text(current, style = TextStyle(color = ColorProvider(Color.LTGRAY), fontSize = 12.sp), modifier = GlanceModifier.padding(top = 4.dp))
                Text("Tap to open", style = TextStyle(color = ColorProvider(Color.WHITE), fontSize = 11.sp), modifier = GlanceModifier.padding(top = 8.dp).clickable(actionStartActivity<MainActivity>()))
            }
        }
    }
}

class DaXWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DaXWidget()
}
